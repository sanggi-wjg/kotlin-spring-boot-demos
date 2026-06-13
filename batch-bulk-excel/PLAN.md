# 엑셀 대용량 Import/Export 배치 시스템 — 구현 계획

> 이 문서는 Claude Code가 단계적으로 구현을 진행하기 위한 작업 지시서입니다.
> 50만~100만 건 규모의 엑셀 임포트/익스포트를 메모리 안전하게(OOM 없이) 처리하는 것이 목표입니다.

---

## 1. 핵심 설계 결정 (확정 사항)

| 항목             | 결정                                                                      | 비고                                                                                           |
|----------------|-------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| 엑셀 라이브러리       | **FastExcel**                                                           | streaming read/write, xlsx 단일 포맷                                                             |
| 파일 포맷          | `.xlsx`, 시트 1개, 컬럼 ~10개                                                 | 일반 케이스                                                                                       |
| 처리 엔진          | **Spring Batch** chunk-oriented processing                              | 청크 단위 트랜잭션                                                                                   |
| 잡 트리거          | **Argo Workflow 가정**, batch의 동기 트리거 엔드포인트 호출                            | Argo 자체는 본 프로젝트 범위 밖, 수동 호출로 검증                                                              |
| 모듈 분리          | shared / producer / storage:rds / storage:file / api / batch / consumer | batch가 별도 배포 단위 → OOM 격리                                                                     |
| 검증 실패 정책       | **부분 커밋**                                                               | 성공 행 적재, 실패 행은 에러 리포트                                                                        |
| staging / undo | **사용 안 함**                                                              | 복잡도 제거                                                                                       |
| 익스포트 페이징       | **id 기반 cursor pagination**                                             | `WHERE id > ? ORDER BY id LIMIT N`                                                           |
| Kafka 역할       | **완료 알림 전용** (`excel.job.completed`)                                    | batch 발행 → consumer 구독 → 메일/Slack                                                            |
| 결과 조회          | 어드민 페이지 + `GET /excel/jobs/{jobId}`                                     | 상태/결과 조회 API                                                                                 |
| JVM 힙          | 2~4GB                                                                   | k8s 환경                                                                                       |
| 데이터베이스         | **MySQL**                                                               | JDBC URL에 `rewriteBatchedStatements=true` 필수                                                 |
| 스키마 관리         | **Flyway 단일 소스** (`storage:rds`의 `db/migration`)                        | `V1`=`BATCH_*` 메타, `V2`=도메인. 적용은 `batch`만(`api`는 OFF). Hibernate `ddl-auto`=`validate` (3.4) |
| 파일 저장소         | **로컬 파일 (S3 인터페이스로 추상화)**                                               | 실제 S3/AWS SDK 미사용. S3처럼 보이는 인터페이스 뒤를 로컬 파일로 구현(9절). 추후 실 S3로 교체 가능                           |
| 재시작 / 멱등성      | **MVP 제외, 후속 작업**                                                       | 13절 참조. 초기엔 재시작 없이 단방향 처리                                                                    |

### 메모리 안전의 핵심 원리

어떤 아키텍처든 OOM의 진짜 원인은 "엑셀을 통째로 메모리에 올리는 것". 두 축으로 해결한다.

- **(A) 처리 자체의 메모리 상한 고정**: streaming reader/writer + 청크 단위 처리 + DB 룩업은 청크 단위 배치 조회.
- **(B) 다른 잡과의 격리**: batch를 별도 배포 단위로 분리 + Argo가 잡 단위 리소스/동시성 관리.

---

## 2. 시스템 아키텍처

```
[Admin Client]
   │ 업로드 / 상태 조회
   ▼
[api]  ── 잡 접수 + 상태 조회만. 무거운 작업 없음
   │  ├─ 파일 → S3
   │  └─ ExcelRequest 저장 (PENDING) → jobId 반환
   │
   │ (외부 오케스트레이터 = Argo Workflow 가정. 본 프로젝트에서는 구성하지 않고
   │  트리거 엔드포인트를 수동/가정 호출. Argo YAML·재시도 전략 등은 범위 밖.)
   ▼
[batch]  ── 동기 트리거 엔드포인트: POST /internal/jobs/{jobId}/run (내부 전용)
   ├─ ExcelRequest RUNNING 전환
   ├─ Spring Batch Job 실행 (FastExcel + 청크) — 완료까지 동기 처리
   ├─ 결과 정리 → ExcelRequest 업데이트 (SUCCESS/PARTIAL/FAILED)
   └─ 완료 시 producer로 "excel.job.completed" 발행
   ▼
[Kafka: excel.job.completed]   ── Kafka는 "알림 전용". 가볍고 짧은 메시지.
   ▼
[consumer]  ── job.completed 구독 → 메일/Slack 알림

[공유 DB(MySQL)] : 비즈니스 테이블 + Spring Batch 메타 + ExcelRequest
[S3(추상화)] : 입력 파일 / 익스포트 결과 / 에러 리포트. 본 프로젝트는 실제 S3가 아니라 로컬 파일로 구현
              (S3 형태 인터페이스 뒤를 로컬 파일이 구현). prefix/키 레이아웃·lifecycle은 S3 가정으로 설계만 유지 → 9절
```

### 트리거 설계 (Argo 가정)

- 잡 실행 트리거는 외부 오케스트레이터(Argo Workflow)가 batch의 내부 엔드포인트를 호출하는 것으로 **가정**한다. 본 프로젝트에서는 Argo 자체를 구성하지 않고, 엔드포인트만 구현하여 수동 호출(curl/Postman)로 검증한다.
- 엔드포인트는 **동기**: 잡을 완료까지 실행하고 결과를 반환. Argo step의 완료 대기/재시도/상태추적 모델과 일치. 동기라 Kafka의 `max.poll.interval` 같은 처리시간 제약이 없다.
- 엔드포인트는 **내부 전용**: 외부 노출 금지(네트워크 정책 또는 내부 인증). 범위상 인증은 단순 토큰/헤더 수준으로 가정.
- 트리거가 Kafka가 아니므로 **Outbox·이중 쓰기 문제가 발생하지 않는다**(api는 ExcelRequest만 저장). 동시성/재시도/타임아웃은 Argo가 선언적으로 담당한다고 가정.

### 모듈 구성 (멀티 모듈)

의존은 단방향(순환 없음).

| 모듈             | 책임                                                    | 의존                                          |
|----------------|-------------------------------------------------------|---------------------------------------------|
| `shared`       | `message/event`(이벤트 DTO) 공통 계약                        | —                                           |
| `producer`     | Kafka 발행 래퍼(KafkaTemplate)                            | shared                                      |
| `storage:rds`  | JPA(MySQL): ExcelRequest·도메인 엔티티 + repository         | shared                                      |
| `storage:file` | 파일 저장소 추상화(`FileStorage`) + 로컬 구현(`LocalFileStorage`) | —                                           |
| `api`          | REST 잡 접수/상태 조회 (ExcelRequest 저장)                     | shared, storage:rds, storage:file           |
| `batch`        | Spring Batch 잡 + 동기 트리거 엔드포인트 + 완료 시 producer로 발행     | shared, storage:rds, storage:file, producer |
| `consumer`     | job.completed 구독 + 알림(메일/Slack)                       | shared, producer*                           |

\* consumer는 수신만 하므로 이벤트 DTO가 `shared`에 있어 `shared`만 의존해도 됨. 재발행이 없으면 producer 의존 불필요.

이벤트 DTO 위치: **`shared/message/event`** — 발행자(batch)와 구독자(consumer)의 공유 계약이므로 중립 위치인 shared에 둔다.

---

## 3. 데이터 모델

### 3.1 ExcelRequest (사용자 요청 추적 — 어드민 표시용)

Spring Batch 메타 테이블과 별개. 사용자 친화적 잡 단위 추적.

```sql
CREATE TABLE excel_request
(
    id                     VARCHAR(36) PRIMARY KEY, -- UUID
    excel_request_type     VARCHAR(50) NOT NULL,    -- IMPORT_USER_MILEAGE, EXPORT_ORDER ...
    status                 VARCHAR(20) NOT NULL,    -- PENDING, RUNNING, SUCCESS, PARTIAL, FAILED
    params                 JSON,                    -- 익스포트 필터 조건 등
    input_file_url         VARCHAR(512),            -- 임포트 입력 S3 키 (임포트만)
    result_file_url        VARCHAR(512),            -- 익스포트 결과 xlsx S3 키 (익스포트만)
    error_report_url       VARCHAR(512),            -- 임포트 검증 실패 리포트 S3 키 (임포트 PARTIAL/FAILED 시)
    result_summary         JSON,                    -- {total, success, failed}
    batch_job_execution_id BIGINT,                  -- Spring Batch 메타 FK (디버깅)
    started_at             TIMESTAMP   NULL,        -- RUNNING 전환 시각 (stale 판정 기준)
    finished_at            TIMESTAMP   NULL,        -- 종료 시각 (SUCCESS/PARTIAL/FAILED 시점)
    created_at             TIMESTAMP   NOT NULL,
    updated_at             TIMESTAMP   NOT NULL,
    INDEX idx_status_created (status, created_at)
);
```

> 트리거가 Argo이고 batch가 동기 실행하므로 워커 클레임 개념이 없다. `worker_id`/`claimed_at`/`last_heartbeat_at` 컬럼은 두지 않는다. stale 판정은 `status=RUNNING` + `started_at`이 임계시간보다 오래된 것으로 한다(5-4).

상태 전이: `PENDING → RUNNING → SUCCESS | PARTIAL | FAILED`

### 3.2 임포트 예제 도메인 테이블 (적립금)

```sql
CREATE TABLE user -- MySQL 예약어성 식별자 → JPA 엔티티는 @Table(name = "user") 로 매핑
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE user_mileage -- (구) user_point
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT    NOT NULL, -- FK -> user.id (1:1)
    balance    BIGINT    NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE KEY uk_user (user_id),
    CONSTRAINT fk_mileage_user FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE user_mileage_history -- (구) point_history
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_mileage_id BIGINT       NOT NULL, -- FK -> user_mileage.id (user_id 대신 사용)
    amount          BIGINT       NOT NULL, -- +적립 / -차감
    reason          VARCHAR(255) NULL,     -- 사유 (옵션)
    job_id          VARCHAR(36)  NULL,     -- 임포트 잡 ID (수동 적립 시 NULL)
    created_at      TIMESTAMP    NOT NULL,
    INDEX idx_mileage (user_mileage_id),
    CONSTRAINT fk_hist_mileage FOREIGN KEY (user_mileage_id) REFERENCES user_mileage (id)
);
```

### 3.3 익스포트 예제 도메인 테이블 (주문 2개 테이블 조인)

```sql
CREATE TABLE orders
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    total_price BIGINT      NOT NULL,
    status      VARCHAR(20) NOT NULL,
    ordered_at  TIMESTAMP   NOT NULL
);

CREATE TABLE order_item
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT       NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity     INT          NOT NULL,
    price        BIGINT       NOT NULL,
    INDEX idx_order (order_id)
);
```

### 3.4 Spring Batch 메타 테이블

- **스키마는 Flyway 단일 소스**로 관리한다. 마이그레이션은 엔티티 옆 `storage:rds`의 `src/main/resources/db/migration`에 둔다:
    - `V1__batch_metadata.sql`: `spring-batch-core`의 `schema-mysql.sql`(Spring Batch 6.0.3) 원본(`BATCH_*` 메타).
    - `V2__domain_tables.sql`: 도메인 테이블(`users`·`excel_request`·`user_mileage`·`user_mileage_history`·`orders`·`order_item`). DDL은 엔티티 매핑과 정확히 일치(`ddl-auto=validate`가 검증).
- **마이그레이션 적용 주체는 `batch` 모듈만**(`JobRepository`/`JobLauncher`·시드 데이터의 자연스러운 소유자). Flyway는 한 곳에서만 실행해야 충돌이 없으므로 **`api`는 `spring.flyway.enabled=false`** 로 끈다(둘 다 같은 DB를 보지만 적용은 batch 단일화). `storage:rds`는 라이브러리라 스스로 실행되지 않고, 이를 의존하는 앱 기동 시 Flyway 자동설정이 마이그레이션을 적용한다.
- `BATCH_*` 테이블은 **공유 DB(MySQL)에 함께** 둔다. Spring Batch 자동 스키마 초기화는 `spring.batch.jdbc.initialize-schema=never`(Flyway가 소유).
- JPA 엔티티 테이블은 Hibernate `ddl-auto=validate`(생성 안 함, 일치 검증만). **스키마 변경은 새 `V*` 마이그레이션 추가로 처리** — `docker compose down -v` 불필요.

---

## 4. API 명세

| Method | Path                         | 설명                                               | 응답               |
|--------|------------------------------|--------------------------------------------------|------------------|
| POST   | `/excel/import`              | multipart 업로드, 잡 접수                              | `{ jobId }`      |
| POST   | `/excel/export`              | 익스포트 조건 전달, 잡 접수                                 | `{ jobId }`      |
| GET    | `/excel/jobs/{jobId}`        | 단건 상태/결과 조회                                      | 상태, 요약, 다운로드 URL |
| GET    | `/excel/jobs`                | 어드민 목록 (필터: type, status, user, 기간)              | 페이지 목록           |
| POST   | `/internal/jobs/{jobId}/run` | **트리거(동기)**. Argo/오케스트레이터가 호출, 잡 완료까지 실행 후 결과 반환 | 최종 상태/요약         |

- 접수용 엔드포인트(`/excel/*`)는 `api` 모듈, 트리거 엔드포인트(`/internal/*`)는 `batch` 모듈.
- 트리거 엔드포인트는 **내부 전용**(외부 노출 금지). 본 프로젝트에서는 수동 호출(curl/Postman)로 검증하며 Argo 자체는 구성하지 않는다.
- 다운로드 URL은 **조회 시점에 presigned URL 발급(15분 유효)**. ExcelRequest에 영구 URL 저장 금지.
    - 본 프로젝트는 실제 S3를 쓰지 않으므로 presigned URL을 **모사**한다: 로컬 파일을 가리키는 단기 다운로드 URL(또는 파일 경로)을 발급. 인터페이스(`FileStorage`)는 동일하게 두어 실 S3 구현체로 교체 가능(9절).
- 업로드 시 **가벼운 사전 검증만**: 확장자, 크기 상한, 시트 수(zip 엔트리 개수). 셀 데이터 파싱·행 검증은 batch에서(2-1 참조).

---

## 5. 임포트 처리 흐름

```
1. 업로드        클라이언트가 xlsx 전송 (api)
2. 잡 접수        S3 저장 → ExcelRequest(PENDING) → jobId 반환 (api)
3. 트리거         오케스트레이터(Argo 가정)가 POST /internal/jobs/{jobId}/run 호출 (batch)
4. 실행 시작      batch가 ExcelRequest RUNNING 전환 → Spring Batch Job 동기 실행
5. 청크 읽기      FastExcel로 N행씩 스트리밍 read
6. 행 검증        Processor: 형식/범위/blank 등 단건 검증 (DB 불필요)
7. 청크 검증+적재  사전 검증(트랜잭션 밖): IN 조회로 존재/잔액 → 통과 행만 Writer 진입.
                 적재(트랜잭션 안): bulk insert + (적립금이면 FOR UPDATE 차감).
                 트랜잭션 안 예외는 Skip 정책으로 흡수
8. 결과 정리      에러 리포트 xlsx 생성 → S3 업로드 → ExcelRequest SUCCESS/PARTIAL
9. 완료 통지      producer로 "excel.job.completed" 발행 → consumer가 메일/Slack
                 (엔드포인트는 최종 상태를 응답으로 반환 — 호출자가 결과 인지)
```

5~7은 **청크 단위 루프**. 메모리는 항상 한 청크 분량만 점유 → 파일 크기와 무관하게 일정.

### 청크 트랜잭션 정책 (부분 커밋 구현)

"행 단위 부분 커밋"은 두 갈래로 달성한다.

1. **트랜잭션 진입 전 사전 필터링 (대부분의 실패는 여기서)**
    - 잔액 부족, 존재하지 않는 user_id, 중복, 형식 위반 같은 **비즈니스 검증은 트랜잭션 밖**에서 IN 조회로 한 번에 처리(3-3).
    - 통과한 행만 Writer 트랜잭션에 진입시킨다. → 트랜잭션 안 실패율을 거의 0에 가깝게 유지하여 IN 조회 배치 효과를 보존.

2. **트랜잭션 안 실패는 Skip 정책 (예외 케이스만)**
    - FOR UPDATE 락 충돌·타임아웃, DB 제약 위반, 일시적 인프라 오류 등은 트랜잭션 안에서 발생. 이때만 Spring Batch **Skip 정책**으로 처리.
    - Skip 발동 시 동작: 청크 전체 롤백 → 행을 1건씩 재처리하면서 문제 행만 skip. 해당 청크에 한해 IN 조회 배치 효과가 사라지지만, Skip이 예외 케이스에만 발동되므로 전체 영향 미미.

> 비즈니스 검증을 Skip으로 흘려보내지 말 것. 매 청크마다 Skip이 발동되면 1건씩 재처리가 누적되어 비싸진다. 비즈니스 검증은 반드시 사전 필터링으로.

### 검증 책임 분리

- **`ItemProcessor` (행 단위, 트랜잭션 밖)**: 데이터만 보면 아는 검증 — 양수/blank/형식/범위.
- **사전 필터링 단계 (청크 단위, 트랜잭션 밖)**: DB가 필요한 비즈니스 검증 — `WHERE user_id IN (...)`으로 존재·잔액 등을 한 번에 조회. 통과 행만 다음 단계로(3-3).
- **`ItemWriter` (트랜잭션 안)**: 실제 적재 + (적립금이면 FOR UPDATE 차감). 여기서의 검증은 race 대비 재확인 정도로 최소화(3-4·3-5).

### 적립금 차감 — 음수 잔액 방지

- 차감 임포트에서 **잔액 부족 행은 검증 단계에서 차단**한다(음수 잔액을 절대 허용하지 않음).
- 3-3(청크 검증, 트랜잭션 밖): 청크의 user_id들을 `IN` 조회해 현재 잔액을 가져오고, 차감액 > 잔액이면 그 행을 에러로 분류(적재 대상에서 제외).
- 3-5(적재 시점, 트랜잭션 안): 검증과 차감 사이의 잔액 변동(race) 대비, `SELECT ... FOR UPDATE`로 잠그고 **차감 직전 한 번 더 잔액 확인**. 부족하면 음수가 되지 않도록 그 행을 실패 처리(Skip 정책으로 흡수). 0까지만 차감하는 식의 부분 차감은 하지 않는다.
- **데드락 회피**: 한 청크 안에서 여러 user_id를 FOR UPDATE할 때, **user_id 오름차순 정렬 후 락**을 잡는다. 청크들이 동시에 같은 user를 다른 순서로 잡으면 데드락이 나므로 락 획득 순서를 전역으로 통일.

### bulk insert 필수 설정

- `JdbcBatchItemWriter` 사용 (또는 JPA면 `hibernate.jdbc.batch_size`, `order_inserts=true`).
- MySQL JDBC URL에 `rewriteBatchedStatements=true` 추가.
- 행 단위 로깅 금지(병목). 에러는 청크 단위로 모아 로깅.

---

## 6. 익스포트 처리 흐름

```
1. 잡 접수        조건 검증 → ExcelRequest(PENDING) → jobId 반환 (api)
2. 트리거         오케스트레이터가 POST /internal/jobs/{jobId}/run 호출 (batch)
3. 실행 시작      RUNNING 전환 → Spring Batch Job 동기 실행
4. 커서 읽기      JdbcPagingItemReader, id 기준 정렬 (WHERE id > ? ORDER BY id LIMIT N)
5. 엑셀 쓰기      FastExcel writer로 청크 단위 append
6. 결과 정리      완성 xlsx S3 업로드 → ExcelRequest SUCCESS
7. 완료 통지      producer로 "excel.job.completed" 발행 → consumer 알림
```

- `JdbcCursorItemReader`(서버 커서, 커넥션 장기 점유) 대신 **`JdbcPagingItemReader`** 사용.
- `OFFSET` 페이징 금지(뒤로 갈수록 느려짐). 단조 증가 PK 기준 keyset 페이징.
- **정렬은 id desc 고정**(MVP). 사용자 지정 정렬은 인덱스 설계가 필요해 범위 밖. 필터(status, 기간 등)는 `params`로 동적 지원.

---

## 7. 트리거 상세 (Argo 가정)

### 트리거 방식

- 외부 오케스트레이터(Argo Workflow)가 batch의 `POST /internal/jobs/{jobId}/run`을 **동기 호출**하는 것으로 가정.
- 본 프로젝트에서는 Argo를 구성하지 않고 엔드포인트만 구현, 수동 호출로 검증. Argo YAML·재시도·동시성 정책은 범위 밖(오케스트레이터가 담당한다고 가정).
- 엔드포인트는 잡을 완료까지 실행하고 최종 상태(SUCCESS/PARTIAL/FAILED)와 요약을 반환. 동기 모델이라 Kafka의 `max.poll.interval` 같은 처리시간 제약이 없음.

### Kafka는 알림 전용

- 트리거 경로에 Kafka가 없으므로 **Outbox·이중 쓰기·폴링 백업 모두 불필요**.
- Kafka는 `excel.job.completed`(알림) 한 가지 용도. 가볍고 짧은 메시지라 컨슈머 처리시간 제약 무관.

### 중복 트리거 방어 (2단 방어)

같은 jobId로 트리거가 두 번 와도 안전하도록 두 겹으로 막는다.

1. **트리거 엔드포인트 상태 체크 (1차)** — 진입부에서 ExcelRequest 상태 확인:
    - 이미 RUNNING → 중복 실행 거부(409) 또는 진행 상태 반환.
    - 이미 SUCCESS/PARTIAL → 기존 결과 반환(재실행 안 함).
    - PENDING → 실행 진행.
2. **Spring Batch JobParameters (2차)** — `jobId`를 JobParameter에 포함. Spring Batch는 `잡 이름 + JobParameters`가 같으면 동일 인스턴스로 보아, 이미 COMPLETED된 잡의 동일 파라미터 재실행을 막는다(`JobInstanceAlreadyCompleteException`).

> **MVP 주의**: MVP는 재시작을 지원하지 않는다. RUNNING 중 죽은 잡(또는 FAILED)을 같은 jobId로 재호출하면, Spring Batch는 미완료 인스턴스의 *재시작*을 시도하는데 — MVP에는 reader 위치 복원(ItemStream)·멱등성이 없어 **중복 적재 위험**이 있다. 따라서 MVP에서는 1차 방어가 죽은 잡의 재호출도 거부하고(수동 대응), JobParameters 재시작 동작은 **13절(멱등성 확보) 이후**에 활성화한다.

- (동시 실행 자체의 상한은 Argo가 담당. 본 프로젝트 수동 검증 시에는 트리거를 동시에 난사하지 않는 것을 전제로 한다.)

### 장애 처리

- 트리거 엔드포인트 실행 중 batch Pod가 죽으면 ExcelRequest가 RUNNING에 잔류.
- 오케스트레이터(Argo) 재시도가 같은 엔드포인트를 다시 호출하는 것으로 가정하되, MVP에서는 자동 재처리를 하지 않으므로 **운영자가 상태 확인 후 수동 재요청**(파일 재업로드 = 새 jobId)으로 대응.

---

## 8. 완료 통지 (Kafka)

- batch가 잡 종료 시 producer를 통해 `excel.job.completed` 발행 (페이로드: jobId, status, requesterId, 요약 통계 — PII/대용량 금지).
- `consumer` 모듈이 수신 → 메일/Slack 발송.
- **본 프로젝트 범위**: 보일러플레이트/스켈레톤이므로 **실제 발송은 하지 않는다**. consumer는 이벤트 수신 → 로그 출력 또는 모킹된 sender 호출까지만. 메일/Slack 클라이언트 통합은 후속. 검증 데이터는 소수면 충분.
- 실제 발송을 구현하게 되면 수신자 이메일 등은 consumer가 storage:rds에서 requesterId로 조회. (PII를 Kafka 페이로드에 싣지 않기 위함.)
- Kafka는 이 알림 흐름에만 사용. 메시지가 짧아 컨슈머 처리시간 제약(max.poll.interval) 무관.

---

## 9. 파일 저장소 (S3 가정 — 실제로는 로컬 파일)

> **본 프로젝트는 실제 S3/AWS SDK를 쓰지 않는다.** S3처럼 보이는 인터페이스 뒤를 **로컬 파일 시스템**으로
> 구현해 "S3를 쓰는 것처럼" 동작시킨다. 키 레이아웃·presigned URL·lifecycle 등은 S3 가정으로 설계만
> 유지하고, 구현체만 로컬 파일로 둔다. 추후 실 S3가 필요하면 동일 인터페이스의 구현체만 교체하면 된다.

### 추상화

- `storage:file`에 S3 형태의 인터페이스 `FileStorage`를 둔다(1-7).
    - `store(key, content)` : 키(=경로)에 바이트/스트림 저장.
    - `presignedUrl(key, ttl)` : 다운로드용 단기 URL **발급(모사)**. 로컬 구현은 파일을 가리키는
      단기 다운로드 URL(예: api의 임시 다운로드 엔드포인트) 또는 파일 경로를 반환.
- MVP 구현체: `LocalFileStorage` — 아래 키를 **로컬 베이스 디렉터리 하위 경로**로 매핑.
    - 베이스 디렉터리는 설정값(예: `app.storage.local.base-dir`)으로 주입. 로컬/테스트는 임시 디렉터리 사용.

### 키 레이아웃 (S3 가정 — 로컬에선 베이스 디렉터리 하위 경로)

```
excel/import/input/{yyyy-MM-dd}/{jobId}.xlsx     입력 원본
excel/import/report/{yyyy-MM-dd}/{jobId}.xlsx    검증 실패 리포트
excel/export/output/{yyyy-MM-dd}/{jobId}.xlsx    익스포트 결과
```

- Lifecycle 정책(30일 후 자동 삭제): **S3 가정의 설계 항목**. 로컬 구현에서는 강제하지 않음(필요 시 정리 스크립트로 대체).
- 다운로드는 presigned URL(15분) **모사**. 영구 URL 저장 금지.
- 실제 S3로 교체 시: `software.amazon.awssdk:s3` + BOM 추가 후 `FileStorage`의 S3 구현체만 새로 작성.

---

## 10. 구현 순서 (Claude Code 작업 단계)

> 원칙: 각 스텝은 **반나절~하루 단위**로 작게 쪼갬. 스텝마다 "완료 기준(DoD)"을 두고, 빌드·테스트 통과 후 다음으로. 앞 스텝의 산출물에만 의존하도록 순서 배치.
> MVP 범위: 임포트/익스포트 단방향 처리 + 동기 트리거 엔드포인트 + 완료 알림. **Argo 자체 구성·재시작·멱등성·자동 재처리는 제외**(13절 후속). 트리거는 수동 호출로 검증.
> 모듈: `shared` / `producer` / `storage:rds` / `storage:file` / `api` / `batch` / `consumer`.

### Phase 0 — 프로젝트 골격 & 로컬 환경

- [x] **0-1** 멀티 모듈 스캐폴딩: `shared`, `producer`, `storage:rds`, `storage:file`, `api`, `batch`, `consumer` 빈 모듈 + 루트 빌드 설정. 모듈 간 의존 방향 설정(순환 금지) *(storage 는 이후 rds/file 로 분리)*
    - DoD: `./gradlew build` 전 모듈 통과 (빈 모듈)
- [x] **0-2** 공통 의존성 정리: Spring Boot, Spring Batch, FastExcel, Spring Kafka, JDBC, MySQL 드라이버, AWS S3 SDK 버전 핀 (FastExcel은 Phase 3/4 보류, AWS S3 SDK는 제외 — 로컬 파일)
    - DoD: 의존성 충돌 없이 컴파일
- [x] **0-3** 로컬 docker-compose: MySQL 8, Kafka(KRaft)
    - DoD: `docker-compose up` 후 두 컨테이너 헬스체크 통과
    - 파일 저장소는 로컬 파일로 구현하므로 S3/LocalStack 컨테이너는 두지 않는다(9절).
- [x] **0-4** Flyway 셋업: `storage:rds`에 `spring-boot-starter-flyway` + `flyway-mysql`(Spring Boot 4.0.6 BOM 버전) 추가, `db/migration`에 `V1`(BATCH_*)·`V2`(도메인) 작성. 적용은 `batch`만(`api` OFF), `ddl-auto=validate`. *(데모 간소화로 한 번 제거했다가 재도입 — 스키마 단일 소스화 + 클린 부팅 버그 수정)*
    - DoD: `docker compose down -v && up` 후 `:batch:bootRun` 시 Flyway 가 V1·V2 적용, `flyway_schema_history`·전 테이블 생성, `validate` 통과

### Phase 1 — 데이터 모델 & 공통 (shared, storage:rds, storage:file)

- [x] **1-1** Spring Batch 메타 스키마: `schema-mysql.sql`(6.0.3) → Flyway `V1__batch_metadata.sql` 로 `BATCH_*` 생성 (~~docker-compose init.sql~~ 0-4 에서 Flyway 로 이관). (검증: 볼륨 재생성 후 `SHOW TABLES`)
- [x] **1-2** `excel_request` 테이블: JPA 엔티티 + Flyway `V2` 로 생성, `ddl-auto=validate` 로 검증 (인덱스 `idx_status_created` 포함)
    - DoD: 테이블/인덱스 생성 확인 ✅ (V2 생성 + validate 통과)
- [x] **1-3** 임포트 예제 도메인 테이블(`users`, `user_mileage`, `user_mileage_history`): JPA 엔티티 + Flyway `V2`(validate)
- [x] **1-4** 익스포트 예제 도메인 테이블(`orders`, `order_item`) + 시드: batch `ApplicationReadyEvent` 리스너(`ApplicationEventListener`)가 `users`/`user_mileage`/`orders`/`order_item` 을 각 10만으로 top-up. 독립 테이블(`users`·`orders`)은 카운트 후 부족분 `JdbcTemplate.batchUpdate`, 참조 테이블(`user_mileage`·`order_item`)은 `INSERT ... SELECT`(실제 PK 기반, `WHERE NOT EXISTS` 멱등)
    - DoD: 각 10만 건 생성 확인 ✅ (재실행 시 멱등 — 중복 없음)
- [x] **1-5** (storage:rds) `ExcelRequest` 엔티티 + repository + 상태 enum(`ExcelRequestStatus`) + `ExcelRequestType` enum
- [x] **1-6** (shared) `message/event` 이벤트 DTO: `JobCompletedEvent` data class (jobId/status/requesterId/`Summary`(total·success·failed)). status 는 storage:rds enum 의존을 피해 String
    - DoD: 직렬화/역직렬화 단위 테스트 통과 ✅ (`JobCompletedEventTest`, Jackson 3 `jacksonObjectMapper()`)
- [x] **1-7** (storage:file) 파일 저장소 추상화: S3 형태의 인터페이스(`FileStorage`: `store`, `presignedUrl`) + **로컬 파일 구현체**(`LocalFileStorage`, base-dir 주입). 실제 S3/AWS SDK는 쓰지 않는다(9절)
    - DoD: 테스트에서 로컬 파일로 업로드 후 발급된 (모사) `file://` URL 로 다운로드 성공 ✅ (`LocalFileStorageTest`)

### Phase 2 — 잡 접수 API (api)

- [x] **2-1** `POST /excel/import`: 파일 사전 검증(확장자, 크기 상한, 시트 수) → S3 업로드 → ExcelRequest(PENDING) → jobId 반환
    - 사전 검증은 **가벼운 수준만**: 확장자/크기는 메타로 확인, 시트 수는 zip 엔트리(`xl/worksheets/`) 개수만 확인하고 셀 데이터는 파싱하지 않음(전체 로딩 금지 원칙 유지). 행 단위 검증은 batch에서.
    - DoD: 통합 테스트로 jobId 반환 + DB/S3 상태 확인 ✅ (`ExcelImportControllerIntegrationTest` — Testcontainers MySQL, PENDING 행+로컬 파일 확인. 단위/서비스: `ExcelFileValidatorTest`·`ExcelImportServiceTest`(Kotest/MockK))
    - 부수: `storage:file` `FileStorage` 빈 등록(`FileStorageConfig`/`app.storage.local.base-dir`), `ExcelRequestEntity`에 `id` 생성자 파라미터 추가, `storage:rds`를 `java-library`로 전환해 repository(JpaRepository) 전이 노출, 루트에 Kotest/MockK 공통 테스트 의존성 추가
- [x] **2-2** `POST /excel/export`: 파라미터 검증 → ExcelRequest(PENDING) → jobId 반환
    - 필터는 MVP로 **status 단일**(`ExportRequestDto`). 조건을 JSON 직렬화해 `params`에 저장, `EXPORT_ORDER`/`PENDING` 적재 후 201+Location+jobId. 잘못된 status/바디는 `HttpMessageNotReadableException` 핸들러로 400(`ErrorResponseDto`).
    - DoD: 통합 테스트로 jobId 반환 + DB 상태 확인 ✅ (`ExcelExportControllerTest` — `IntegrationTestSpec` 상속, EXPORT_ORDER/PENDING/params 확인. 단위: `ExcelExportServiceTest`(Kotest/MockK, params 직렬화))
- [x] **2-3** `GET /excel/jobs/{jobId}`: 상태/요약 + 다운로드 presigned URL(조회 시점 발급). 익스포트면 `result_file_url`, 임포트 PARTIAL/FAILED면 `error_report_url`을 presigned로 변환해 응답
    - `ExcelJobQueryService`가 조회→`result_summary` JSON 파싱(`SummaryDto`)→다운로드 키를 조회 시점 presigned 변환. 없는 jobId는 `JobNotFoundException`→404. 현재 데이터는 전부 PENDING이라 summary·downloadUrl은 null이지만 Phase 3/4 대비 구조 완성.
    - DoD: 단건 상태/요약/다운로드URL 응답 ✅ (`ExcelJobControllerTest` 200/404, `ExcelJobQueryServiceTest`(MockK+실제 `LocalFileStorage`) — export/import/PENDING/404 4케이스)
- [x] **2-4** `GET /excel/jobs`: 어드민 목록(필터: type/status/기간, 페이징)
    - `ExcelRequestRepository.search`(JPQL `(:param IS NULL OR ...)` 동적 필터) + `Pageable`. 정렬은 **createdAt desc 고정**(MVP). 기간은 `from`/`to` **Instant**(ISO-8601, `InstantFormatter` 기본 바인딩, `createdAt` 양끝 포함). 응답은 `JobListResponseDto`(content/page/size/totalElements/totalPages) — 목록 행(`JobSummaryDto`)은 단건보다 가볍게, 다운로드 URL은 행마다 발급하지 않음. OFFSET 페이징은 잡당 1행짜리 작은 테이블이라 무방(6절 OFFSET 금지는 익스포트 배치 리더 전용).
    - **user 필터 보류**: `excel_request`에 사용자 컬럼이 없고 데모에 인증/식별 소스가 없음(YAGNI). 인증 도입 시 `requested_by` 컬럼(V3) + 접수 시 캡처로 확장.
    - DoD: 통합 테스트로 전체/type/status/기간 필터 + 페이징 확인 ✅ (`ExcelJobControllerTest` 7케이스, 단위 `ExcelJobQueryServiceTest`(MockK, 정렬·매핑) +2)

### Phase 3 — 임포트 잡 (batch, 핵심)

- [ ] **3-1** FastExcel streaming `ItemReader`: S3에서 스트림으로 받아 행 단위 read (전체 로딩 금지)
    - DoD: 50만 건 테스트 파일을 일정 메모리로 읽는 것 확인
- [ ] **3-2** 행 DTO 매핑 + 행 단위 검증 `ItemProcessor`: 양수/blank/형식/범위
    - DoD: 정상/이상 행에 대한 단위 테스트
- [ ] **3-3** 청크 단위 사전 검증(트랜잭션 밖): 청크 키 모아 `IN` 조회로 존재/잔액 검증. 차감 임포트는 **차감액 > 잔액이면 에러로 분류**(적재 제외). 통과 행만 Writer 트랜잭션 입력으로 전달
    - DoD: 쿼리 수 = 청크 수 + 잔액 부족 행이 에러로 빠지는 테스트
- [ ] **3-4** `ItemWriter`(적재, 트랜잭션 안): 통과 행 `JdbcBatchItemWriter` bulk insert (`rewriteBatchedStatements=true` 적용). 트랜잭션 안 실패(락 충돌·제약 위반)는 **Spring Batch Skip 정책**으로 처리
    - DoD: bulk insert가 실제 배치로 나가는지 쿼리 로그 확인 + Skip 발동 시 청크 롤백·1건씩 재처리·문제 행만 skip 확인
- [ ] **3-5** 적립금 차감 처리: 청크 안에서 **user_id 오름차순 정렬 후** `SELECT ... FOR UPDATE`로 잠그고 잔액 재확인 후 차감(데드락 회피). 부족하면 **음수가 되지 않도록 행 실패 처리**(Skip으로 흡수), 부분 차감 안 함
    - DoD: 동시 차감 테스트에서 음수 잔액 미발생 + 데드락 미발생
- [ ] **3-6** 에러 수집기 + 에러 리포트 xlsx 생성(행번호+사유) → S3 업로드
- [ ] **3-7** 잡 완료 후 ExcelRequest 결과 업데이트: SUCCESS/PARTIAL/FAILED + result_summary(total/success/failed) + 에러 리포트가 있으면 `error_report_url` 저장
    - DoD: 부분 실패 케이스가 PARTIAL로 기록 + error_report_url 저장

### Phase 4 — 익스포트 잡 (batch)

- [ ] **4-1** `JdbcPagingItemReader`: id keyset 페이징, `orders`+`order_item` 조인 쿼리 (OFFSET 금지)
    - DoD: 100만 건을 일정 메모리로 읽는 것 확인
- [ ] **4-2** FastExcel streaming `ItemWriter`: 청크 단위 append, 헤더 1회 작성
- [ ] **4-3** 완성 xlsx S3 업로드 → `result_file_url` 저장 + ExcelRequest SUCCESS 업데이트
    - DoD: 결과 파일 다운로드해 행 수/내용 검증

### Phase 5 — 트리거 엔드포인트 (batch)

- [ ] **5-1** `POST /internal/jobs/{jobId}/run` 동기 엔드포인트: ExcelRequest 조회 → RUNNING 전환 → excel_request_type → Spring Batch Job 빈 라우팅 → `JobLauncher`로 동기 실행 → 최종 상태 반환
    - excel_request_type → Job 빈 매핑은 `Map<ExcelRequestType, Job>`(Spring이 주입) 또는 빈 이름 규약으로 해결.
    - DoD: curl 호출로 임포트/익스포트 잡이 완료까지 실행되고 결과 응답
- [ ] **5-2** 중복 트리거 2단 방어: (1차) 진입부 ExcelRequest 상태 체크 — RUNNING/죽은 잡 거부(409), SUCCESS/PARTIAL이면 기존 결과 반환. (2차) `jobId`를 JobParameter로 넣어 Spring Batch 레벨 중복 실행 방지
    - DoD: 같은 jobId 재호출 시 중복 실행 안 됨 (MVP: 재시작도 막음 — 7절 주의 참조)
- [ ] **5-3** 엔드포인트 내부 전용 보호: 단순 토큰/헤더 검증(범위상 최소 수준)
- [ ] **5-4** stale job 처리(선택): `status=RUNNING` + `started_at`이 임계시간 경과한 잡을 FAILED로 마킹하는 수동/스케줄 유틸 (자동 재처리는 13절 후속)

### Phase 6 — 완료 통지 (producer, consumer)

- [ ] **6-1** (producer) Kafka 발행 래퍼(KafkaTemplate) — `excel.job.completed` 발행
- [ ] **6-2** (batch) 잡 종료 시 producer로 `JobCompletedEvent` 발행 연결
- [ ] **6-3** (consumer) `excel.job.completed` 구독 → **로그 출력 또는 모킹된 sender 호출**(실발송 없음, 스켈레톤)
- [ ] **6-4** (consumer) 메일/Slack용 sender 인터페이스만 정의해두고 구현체는 stub. 후속 통합 시 교체 가능하도록
    - DoD: 이벤트 수신 로그가 찍히고, sender 인터페이스가 빈 구현으로 호출되는지 확인

### Phase 7 — 검증 / 부하 테스트

- [ ] **7-1** 50만/100만 건 임포트 메모리 프로파일링(힙 일정 유지 확인)
- [ ] **7-2** 100만 건 익스포트 메모리/시간 측정
- [ ] **7-3** OOM 격리 테스트: batch가 큰 잡 처리 중에도 api/consumer 영향 없는지(별도 프로세스 분리 확인)
- [ ] **7-4** 트리거 실행 중 batch 강제 종료 시 RUNNING 잔류 → 수동 FAILED 처리 확인
- [ ] **7-5** 청크 사이즈 튜닝(1,000~5,000) 비교

---

## 11. 주요 리스크 / 체크포인트

| 리스크                         | 대응                                                       |
|-----------------------------|----------------------------------------------------------|
| 행 단위 DB 조회 → N+1            | 사전 필터링(3-3)에서 청크 단위 IN 조회 (Writer 트랜잭션 밖)                |
| bulk insert가 실제 배치 안 됨      | `rewriteBatchedStatements=true`, batch_size 확인           |
| batch 죽으면 잡이 RUNNING에 영구 잔류 | MVP: 수동 FAILED 처리 + 재요청. 자동 재처리는 13절 후속                  |
| 재시작 시 중복 적재                 | **MVP 범위 외** — 13절 후속 작업에서 멱등성과 함께 처리                    |
| 적립금 잔액 부족/음수                | 검증(3-3)에서 차단 + 차감 시점 FOR UPDATE 재확인(3-5). 부분 차감 안 함      |
| FOR UPDATE 데드락              | 청크 내 user_id 오름차순 정렬 후 락 획득 (전역 락 순서 통일)                 |
| 익스포트 OFFSET 페이징 성능 저하       | id keyset 페이징                                            |
| 큰 잡 하나가 메모리 독점              | batch 별도 배포 단위 분리 + 청크 streaming + (Argo 잡 단위 리소스 제한 가정) |
| 중복 트리거 호출                   | 2단 방어: 진입부 ExcelRequest 상태 체크 + JobParameters(5-2)       |
| 트리거 엔드포인트 외부 노출             | 내부 전용 + 토큰/헤더 검증(5-3)                                    |

---

## 12. 미확정 / 후속 결정 필요

- 정확한 검증 룰 셋 (컬럼별 규칙 정의)
- 청크 사이즈 구체값 (부하 테스트로 확정 — 7-5)
- 동시성·재시도·타임아웃 정책 — 오케스트레이터(Argo) 영역으로 위임, 본 프로젝트 범위 밖
- 진행률 표시 — 현재 스킵, 추후 필요 시 result_summary에 진행 카운트 추가
- 실제 메일/Slack 발송 통합 — 본 프로젝트는 stub. 후속에서 sender 구현체 교체 및 실패 재전송 정책 정의
- 트리거 엔드포인트 인증 방식 구체화 (MVP는 단순 토큰)

---

## 13. 후속 작업 — 재시작 & 멱등성 (MVP 이후)

> MVP에서는 단방향 처리만 한다. 잡이 중간에 죽으면 stale 스케줄러가 FAILED로 마킹하고, 사용자가 어드민에서 **수동으로 다시 요청**한다(파일 재업로드 = 새 jobId). 자동 재시작/재처리는 중복 적재 위험이 있어 멱등성이 전제되므로 아래를 후속으로 진행한다.

### 13.1 재시작 (이어하기)

- `jobId`는 이미 MVP에서 JobParameter에 포함됨(5-2). 후속에서 추가로 필요한 것:
    - 임포트 엑셀 reader에 `ItemStream` 구현: 읽은 행 위치를 `ExecutionContext`에 저장/복원 → 재시작 시 그 지점부터.
    - 5-2의 1차 방어를 "RUNNING/FAILED 인스턴스의 재시작 허용"으로 완화 (현재 MVP는 거부).
- 익스포트 `JdbcPagingItemReader`는 위치 저장 기본 제공.

### 13.2 멱등성 (중복 적재 방지)

재시작/중복 메시지로 같은 행이 두 번 반영돼도 결과가 같도록 데이터 레벨에서 보장.

| 데이터 성격    | 전략     | 방법                                                          |
|-----------|--------|-------------------------------------------------------------|
| 마스터/덮어쓰기  | upsert | `INSERT ... ON DUPLICATE KEY UPDATE` (MySQL)                |
| 누적성 (적립금) | 행 멱등 키 | `idempotency_key = jobId + rowNumber` 컬럼 + 유니크 인덱스, 충돌 시 스킵 |

- `user_mileage_history`에 `idempotency_key VARCHAR(64)` + `UNIQUE INDEX` 추가.
- 적재 흐름: history INSERT(멱등 키) 성공한 행만 balance 갱신, 둘을 같은 트랜잭션으로.

### 13.3 자동 재처리 연결

- 멱등성 확보 후, 오케스트레이터(Argo)의 재시도가 같은 `jobId`로 트리거 엔드포인트를 다시 호출해도 안전하게 흡수됨.
- 트리거 엔드포인트의 중복 방어(이미 RUNNING/SUCCESS 거부)를 "재시작 허용"으로 완화 가능.

### 13.4 재시작 vs 재처리 선택

- 파일이 매우 크고 재실행이 잦으면 → 재시작(이어하기)으로 시간 절약.
- 그렇지 않으면 → 재처리(처음부터 + 멱등성)가 구현·운영 단순. 권장 기본값.
