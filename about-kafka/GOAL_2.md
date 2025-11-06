# Database Layer 장애 시나리오 테스트 계획

## 📌 프로젝트 개요

### HikariCP 연결 풀 구성

**Consumer Application**:

- **Maximum Pool Size**: 20
- **Connection Timeout**: 2초
- **Validation Timeout**: 1초
- **Max Lifetime**: 10분
- **Keep-alive Time**: 30초

### 현재 Database 사용 패턴

**Consumer의 메시지 처리당 DB 작업**:

1. **3초 대기**: `Thread.sleep(3000)` - **모든 메시지**에 대해 처리 시간 증가 시뮬레이션
2. **SELECT**: `findByEventId(eventId)` - 중복 메시지 체크
3. **INSERT**: `save(EventEntity)` - 이벤트 저장
4. **Manual Acknowledgment**: 수동 커밋으로 트랜잭션 완료 시점 제어

**리소스 경쟁 구조**:

- Consumer 스레드: **30개** (기본 concurrency 10 × 3배 multiplier)
- Connection Pool: 20개
- **리소스 압박 상황**: 스레드 수(30) > Pool Size(20)로 의도적 병목 생성
- 각 메시지 처리 시 3초간 DB 연결을 점유하여 연결 풀 고갈 유도

### MySQL 구성

- **Host**: localhost:3400
- **Database**: ak_demo
- **Max Connections**: 151 (MySQL 기본값)

---

## 🛠️ 테스트 환경 준비

Grafana에서 대시보드로 실시간 모니터링.

```bash
# 모든 서비스 시작
cd docker
docker-compose up -d

# Kafka UI 접속: http://localhost:8100
# Grafana 접속: http://localhost:3000
# Prometheus 접속: http://localhost:9090

### 모니터링 메트릭 확인
```

---

## 🔴 Database Layer 장애 시나리오

### Connection Pool Exhaustion - Burst Traffic

#### 📖 시나리오 설명

**상황**: 갑작스러운 대량의 트래픽으로 인해 Consumer가 처리하는 메시지 수가 급증하고, 각 메시지 처리 시 3초의 지연이 발생하면서 HikariCP 연결 풀의 모든 연결이 사용 중인 상태가 됨

**중요성**:

- 실제 프로덕션에서 가장 흔한 장애 중 하나
- 마케팅 이벤트, 장애 복구 후 메시지 폭주, 배치 작업 등에서 발생
- 연결 고갈 시 Consumer 스레드가 대기 상태에 빠져 전체 처리 속도 저하
- 외부 API 호출, 복잡한 비즈니스 로직 등으로 인한 처리 지연 시뮬레이션

**테스트 목표**:

1. 연결 풀 포화 시 시스템의 동작 확인
2. Connection timeout(2초) 발생 시 동작 검증
3. 메시지 손실 없이 처리되는지 확인
4. 부하 종료 후 시스템 정상 복구 확인
5. Manual acknowledgment 패턴에서의 연결 풀 관리 검증

**취약점**:

- **30개 스레드 vs 20개 연결**: 스레드 수가 pool size를 초과하여 항상 대기 발생
- 메시지당 **3초 sleep + SELECT + INSERT** 작업으로 연결 사용 시간 증가
- 각 메시지 처리 시 DB 연결을 3초 이상 점유 → connection pool 압박 극대화
- Manual acknowledgment로 인해 트랜잭션이 완전히 종료되기 전까지 연결 점유
- **1 API 호출 = 3개 메시지 발행**으로 부하 3배 증폭

#### 🔧 재현 방법

**Step 1: 베이스라인 수집**

테스트 전 정상 상태의 메트릭을 확인합니다:

```bash
# Prometheus에서 현재 연결 상태 확인
curl http://localhost:8081/actuator/prometheus | grep hikaricp_connections
```

예상 결과:

- `hikaricp_connections_active`: 0~2 (유휴 상태)
- `hikaricp_connections_idle`: 10~20 (초기화된 연결)
- `hikaricp_connections_pending`: 0 (대기 없음)

**Step 2: 데이터베이스 초기화 (선택사항)**

```sql
-- 기존 데이터 삭제하여 깨끗한 상태로 시작
TRUNCATE TABLE ak_demo.event;
TRUNCATE TABLE ak_demo.dead_letter;
```

**Step 3: 버스트 트래픽 생성**

```bash
cd loader

# 먼저 스크립트의 repeat 값을 수정
# scripts/second_scenario_loader.py 파일을 열어서
# repeat = 100 → repeat = 10000 으로 변경

# Poetry를 통해 부하 테스트 스크립트 실행
poetry run python scripts/second_scenario_loader.py

# 수정된 설정:
# - 총 10,000번 API 호출
# - API 엔드포인트: POST /events/second-scenario
# - 메시지 발행: 1 API 호출 = 3개 메시지
# - 총 메시지 수: 30,000개 (10,000 × 3)

# 참고: 기본값(repeat=100)으로 테스트하면 300개 메시지만 발행됨
```

**스크립트 동작**:

1. 10,000번의 HTTP 요청을 Producer에 전송 (`POST /events/second-scenario`)
2. 각 요청마다 Producer는 **3개의 메시지**를 Kafka로 발행 (`second-scenario.v1` 토픽)
3. Consumer의 **30개 스레드**가 메시지를 소비하며 DB 작업 수행
4. **모든 메시지에 대해**:
    - 3초 sleep으로 처리 지연 시뮬레이션
    - DB에서 중복 체크 (`findByEventId`)
    - 새로운 이벤트 저장 (`save`)
    - Manual acknowledgment로 커밋
5. 중복 메시지 발견 시 `RuntimeException` 발생
6. 완료 후 생성된 eventId 목록 확인

**Step 4: 실시간 모니터링**

주목할 로그:

- `😎 SecondScenarioEvent 생성 완료` - 정상 처리
- `🔥 SecondScenarioEvent EventId XXX 중복 발생` - 중복 메시지 감지
- `❌ SecondScenarioEvent 생성 실패` - 처리 실패 (중복 또는 DB 에러)
- `Connection is not available` - 연결 풀 고갈 (발생 가능)
- `HikariPool-1 - Connection is not available, request timed out after 2000ms` - Connection timeout

**Grafana 대시보드**:

- http://localhost:3000 접속
- HikariCP 메트릭을 시각화한 대시보드에서 실시간 관찰
- 특히 `hikaricp_connections_pending` 메트릭이 0 이상으로 증가하는지 확인

**Step 5: 부하 종료 후 복구 확인**

부하 테스트가 완료된 후:

예상 결과:

- `hikaricp_connections_active`: 0~2로 감소 (트랜잭션 완료)
- Consumer lag: 0 (모든 메시지 처리 완료)

#### ✅ 검증 체크리스트

**1. 메시지 손실 확인**

```sql
-- 부하 테스트 스크립트가 생성한 eventId 개수와 비교
SELECT COUNT(*)
FROM ak_demo.event;
-- 예상: 30,000개 (10,000 API 호출 × 3개 메시지)
-- 중복이 없다면 모든 메시지가 정상 저장되어야 함
```

**2. 중복 메시지 확인**

```sql
-- eventId가 중복된 경우 찾기
SELECT event_id, COUNT(*) as cnt
FROM ak_demo.event
GROUP BY event_id
HAVING cnt > 1;
-- 예상: 0개 (중복 없음)
```

**3. 시간대별 처리량 분석**

```sql
-- 초당 저장된 메시지 수 (병목 지점 파악)
SELECT DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') as second,
       COUNT(*)                                     as messages_per_second
FROM ak_demo.event
GROUP BY second
ORDER BY second;
```

**4. Consumer 오프셋 확인**

Kafka UI(http://localhost:8100)에서:

- Consumer Group: `ak-demo-consumer-group`
- Topic: `second-scenario.v1`
- Lag: 0이 되어야 함 (모든 메시지 처리 완료)
- Committed Offset: 최종 메시지 오프셋까지 커밋되었는지 확인

**참고**:

- **Second scenario는 retry/DLT 설정이 없음** (First scenario만 retry/DLT 적용)
- 모든 메시지는 성공 시 정상 커밋, 실패 시 재처리 또는 에러 로그 발생
- 중복 메시지는 DB unique constraint로 차단됨

**체크리스트**:

- [ ] 총 발행 메시지 수: 30,000개 (10,000 API 호출 × 3개)
- [ ] DB 저장 성공 메시지: 30,000개 (100% - 중복 없다면)
- [ ] 중복 메시지가 DB에 저장되지 않음
- [ ] `hikaricp_connections_active`가 최대값(20)에 도달했는가?
- [ ] `hikaricp_connections_pending` > 0 (대기가 발생했는가?)
- [ ] `hikaricp_connections_timeout_total` 증가 (타임아웃이 발생했는가?)
- [ ] Consumer 로그에 "😎 SecondScenarioEvent 생성 완료" 로그가 있는가? (정상 처리)
- [ ] 부하 종료 후 Consumer lag이 0이 되는가?
- [ ] 부하 종료 후 `hikaricp_connections_active`가 정상 수준(0~2)으로 돌아오는가?
- [ ] Connection leak이 없는가? (idle 연결이 정상적으로 반환되는가?)
- [ ] 30개 스레드가 20개 연결 풀을 두고 경쟁하는 상황이 재현되었는가?

#### 📊 예상 결과

**정상 동작 시나리오**:

1. **부하 초기 (0~30초)**:
    - Producer API가 10,000번의 요청을 받아 30,000개 메시지 발행
    - `second-scenario.v1` 토픽에 메시지가 빠르게 쌓임
    - Consumer의 **30개 스레드**가 메시지를 소비하기 시작
    - 각 메시지마다 3초 sleep 실행
    - `hikaricp_connections_active`가 빠르게 20에 도달
    - `hikaricp_connections_pending`이 즉시 증가 시작 (30개 스레드 > 20개 연결)

2. **연결 풀 포화 단계 (30~1500초, 약 25분)**:
    - **모든 메시지**가 3초씩 connection을 점유
    - `hikaricp_connections_active`가 지속적으로 20 유지
    - `hikaricp_connections_pending`이 5~10 범위로 유지 (대기 중인 스레드)
    - Connection timeout(2초)이 주기적으로 발생 가능
    - Consumer lag이 계속 유지됨 (처리 속도 < 발행 속도)
    - 총 처리 시간: 30,000 메시지 × 3초 / 20 연결 = 약 4,500초 (이론상)
    - 실제로는 30개 스레드가 병렬 처리하므로 더 빠름: 30,000 × 3초 / 30 = 3,000초 (약 50분)

3. **부하 종료 및 복구 (최종 단계)**:
    - Producer가 모든 메시지 전송 완료
    - Consumer가 남은 메시지를 계속 처리
    - `hikaricp_connections_active`가 서서히 감소
    - Consumer lag이 0으로 수렴
    - Connection pool이 정상 상태로 복구
    - 모든 메시지 처리 완료

**메트릭 예상치**:

- `hikaricp_connections_active` 최대값: **20 (pool size 최대)**
- `hikaricp_connections_pending` 최대값: **5~10 (대기 스레드 수)**
- `hikaricp_connections_timeout_total`: 잦은 증가 가능 (3초 sleep > 2초 timeout)
- `hikaricp_connections_acquire_seconds` P95: **2초 이상** (timeout 설정값)
- Consumer lag 최대값: 메시지 발행 속도에 따라 변동
- 최종 DB 저장 메시지: **30,000개 (100% 성공)**
- 중복 메시지: **0개** (unique constraint 적용)
- 총 처리 시간: **약 15~25분** (실제 테스트 시 측정 필요)

#### 🎯 성공 기준

**✅ Pass 조건**:

1. **연결 풀 포화 재현**: `hikaricp_connections_active`가 지속적으로 20(최대값)에 도달
2. **대기 발생**: `hikaricp_connections_pending`이 5~10으로 증가 (스레드 > 연결 수)
3. **모든 메시지 처리**: 30,000개 메시지가 모두 DB에 정상 저장됨
4. **중복 방지**: 중복 체크 로직이 작동하여 DB에 중복이 없음
5. **Connection timeout 발생**: `hikaricp_connections_timeout_total` 증가 확인
6. **시스템 복구**: 부하 종료 후 Consumer lag이 0이 되고, connection pool이 정상 상태로 복구
7. **Manual ACK 동작**: 메시지 처리 완료 후 수동 커밋이 정상 작동

**❌ Fail 조건**:

1. **메시지 손실**: DB에 저장된 메시지가 30,000개 미만
2. **중복 발생**: 같은 eventId가 DB에 2회 이상 저장됨
3. **Connection leak**: 부하 종료 후에도 `hikaricp_connections_active`가 높게 유지됨
4. **Consumer 중단**: Connection 획득 실패로 인해 Consumer 스레드가 중단되거나 에러로 종료
5. **연결 풀 포화 미발생**: `hikaricp_connections_active`가 20에 도달하지 않음 (테스트 목적 달성 실패)
