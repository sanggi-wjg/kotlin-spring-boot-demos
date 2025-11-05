# Database Layer 장애 시나리오 테스트 계획

## 📌 프로젝트 개요

### HikariCP 연결 풀 구성

**Consumer Application**:

- **Maximum Pool Size**: 20
- **Connection Timeout**: 2초
- **Validation Timeout**: 1초
- **Max Lifetime**: 10분
- **Keep-alive Time**: 30초

**Producer Application**:

- **Maximum Pool Size**: 10
- **Connection Timeout**: 2초

### 현재 Database 사용 패턴

**Consumer의 메시지 처리당 DB 작업**:

1. **SELECT**: `findByEventId(eventId)` - 중복 메시지 체크
2. **INSERT**: `save(EventEntity)` - 이벤트 저장
3. **의도적 실패 (10% 확률)**: `randomValue % 10 == 0` 시 3초 sleep 후 RuntimeException 발생

**리소스 경쟁 구조**:

- Consumer 스레드: 10개 (동시 처리)
- Connection Pool: 20개
- 이상적 상황: 2:1 비율 (여유 있음)
- 부하 상황: 모든 스레드가 동시에 트랜잭션 보유 시 최대 10개 연결 사용

### MySQL 구성

- **Host**: localhost:3400
- **Database**: ak_demo
- **Max Connections**: 151 (MySQL 기본값)
- **다중 인스턴스 시나리오**: Consumer 3개 실행 시 최대 60개 연결 필요 (20 × 3)

---

## 🛠️ 테스트 환경 준비

### 사전 요구사항

```bash
# 모든 서비스 시작
cd docker
docker-compose up -d

# Kafka UI 접속: http://localhost:8100
# Grafana 접속: http://localhost:3000
# Prometheus 접속: http://localhost:9090

# Producer 실행
./gradlew :applications:producer:bootRun

# Consumer 실행 (단일 인스턴스로 시작)
./gradlew :applications:consumer:bootRun
```

### 모니터링 메트릭 확인

Prometheus에서 다음 메트릭을 사용할 수 있어야 합니다:

```promql
# HikariCP 메트릭
hikaricp_connections_active{pool="abut-kafka-pool"}
hikaricp_connections_idle{pool="abut-kafka-pool"}
hikaricp_connections_pending{pool="abut-kafka-pool"}
hikaricp_connections_timeout_total{pool="abut-kafka-pool"}
hikaricp_connections_acquire_seconds{pool="abut-kafka-pool"}
hikaricp_connections_creation_seconds{pool="abut-kafka-pool"}

# Kafka Consumer 메트릭
kafka_consumer_records_lag_max
kafka_consumer_records_consumed_total
```

Grafana에서 위 메트릭을 차트로 시각화하여 실시간 모니터링을 권장합니다.

---

## 🔴 Tier 2: Database Layer 장애 시나리오

### 2.1 Connection Pool Exhaustion - Burst Traffic

#### 📖 시나리오 설명

**상황**: 갑작스러운 대량의 트래픽으로 인해 Consumer가 처리하는 메시지 수가 급증하면서 HikariCP 연결 풀의 모든 연결이 사용 중인 상태가 됨

**중요성**:

- 실제 프로덕션에서 가장 흔한 장애 중 하나
- 마케팅 이벤트, 장애 복구 후 메시지 폭주, 배치 작업 등에서 발생
- 연결 고갈 시 Consumer 스레드가 대기 상태에 빠져 전체 처리 속도 저하

**테스트 목표**:

1. 연결 풀 포화 시 시스템의 동작 확인
2. Connection timeout(2초) 발생 시 재시도 메커니즘 동작 검증
3. 메시지 손실 없이 처리되는지 확인
4. 부하 종료 후 시스템 정상 복구 확인

**취약점**:

- 메시지당 2개의 DB 작업 (SELECT + INSERT)으로 연결 사용 시간이 길어질 수 있음
- 10개 스레드가 동시에 트랜잭션을 보유하면 pool size 20의 절반을 즉시 소모
- **의도적 실패 메시지 (10%)는 3초간 connection 점유** 후 실패 → connection pool 압박 극대화
- Retry 로직 실행 시 재시도 토픽에서 추가 메시지를 읽어오면서 부하 증가
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
```

**Step 3: 버스트 트래픽 생성**

```bash
cd loader

# Poetry를 통해 부하 테스트 스크립트 실행
poetry run python scripts/second_scenario_loader.py

# 기본 설정:
# - 총 10,000번 API 호출
# - API 엔드포인트: POST /events/second-scenario
# - 메시지 발행: 1 API 호출 = 3개 메시지
# - 총 메시지 수: 30,000개 (10,000 × 3)
```

**스크립트 동작**:

1. 10,000번의 HTTP 요청을 Producer에 전송 (`POST /events/second-scenario`)
2. 각 요청마다 Producer는 **3개의 메시지**를 Kafka로 발행 (`second-scenario.v1` 토픽)
3. Consumer의 10개 스레드가 메시지를 소비하며 DB 작업 수행
4. **10% 메시지 (약 3,000개)는 의도적으로 실패**:
   - `randomValue % 10 == 0` 조건 충족 시
   - 3초간 DB connection 점유
   - `RuntimeException` 발생 → retry 토픽으로 이동
5. 실시간 통계 출력: 진행률, 성공/실패율
6. 완료 후 생성된 eventId 목록을 JSON 파일로 저장

**Step 4: 실시간 모니터링**

테스트 실행 중 다음을 동시에 관찰합니다:

**터미널 1 - 부하 테스트 스크립트 출력**:

```
Progress: 5000/10000 (50.0%)
Success: 4998, Failed: 2, TPS: 250.5
Avg Response Time: 45ms, P95: 120ms, P99: 350ms
```

**터미널 2 - Consumer 로그**:

```bash
# Consumer 애플리케이션 로그 확인
tail -f applications/consumer/logs/application.log
```

주목할 로그:

- `😢 앗앗앗, [randomValue]` - 의도적 실패 발생
- `SecondScenarioEvent 생성 완료` - 정상 처리
- `Connection is not available` - 연결 풀 고갈 (발생 가능)
- `Retry attempt X of 3` - 재시도 메커니즘 작동
- `Sending to DLT` - Dead Letter Topic으로 이동 (3회 재시도 실패 후)

**터미널 3 - Prometheus 메트릭 실시간 조회**:

```bash
# 5초마다 HikariCP 메트릭 확인
watch -n 5 'curl -s http://localhost:8081/actuator/prometheus | grep hikaricp_connections'
```

**Grafana 대시보드**:

- http://localhost:3000 접속
- HikariCP 메트릭을 시각화한 대시보드에서 실시간 관찰
- 특히 `hikaricp_connections_pending` 메트릭이 0 이상으로 증가하는지 확인

**Step 5: 부하 종료 후 복구 확인**

부하 테스트가 완료된 후:

```bash
# 연결 풀이 정상 상태로 돌아왔는지 확인
curl http://localhost:8081/actuator/prometheus | grep hikaricp_connections_active

# Consumer lag 확인 (Kafka UI 또는 메트릭)
# http://localhost:8100 -> Consumer Groups -> ak-demo-consumer-group
```

예상 결과:

- `hikaricp_connections_active`: 0~2로 감소 (트랜잭션 완료)
- Consumer lag: 0 (모든 메시지 처리 완료)

#### ✅ 검증 체크리스트

**1. 메시지 손실 확인**

```sql
-- 부하 테스트 스크립트가 생성한 eventId 개수와 비교
SELECT COUNT(*) as total_saved
FROM ak_demo.event;
-- 예상: 약 27,000개
-- (총 30,000개 중 10% 실패 → 3,000개가 DLT로 이동 가능)
-- 만약 재시도가 성공하면 30,000개 전체가 저장될 수도 있음
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

**4. 재시도 토픽 확인**

Kafka UI(http://localhost:8100)에서:

- Topic: `second-scenario.v1-retry` - 메시지가 있는가?
- Topic: `second-scenario.v1-dlt` - 최종 실패한 메시지가 있는가?

**예상 동작**:
- **Retry 토픽**: 의도적 실패 메시지 (10%, 약 3,000개)가 일시적으로 쌓임
  - 1초, 2초, 10초 backoff 후 재시도
  - 재시도 시에도 `randomValue % 10 == 0` 조건이 계속 true이므로 **계속 실패**
  - 3회 재시도 모두 실패 → DLT로 이동
- **DLT 토픽**: 약 3,000개의 메시지가 최종적으로 쌓임
  - 이는 `randomValue`가 고정되어 있어 재시도해도 계속 같은 조건으로 실패하기 때문

**5. Connection Pool 메트릭 분석**

Prometheus에서 테스트 기간 동안의 메트릭 조회:

```promql
# 최대 active connection 수
max_over_time(hikaricp_connections_active{pool="abut-kafka-pool"}[10m])

# Timeout 발생 횟수
increase(hikaricp_connections_timeout_total{pool="abut-kafka-pool"}[10m])

# Connection 획득 대기 시간 (P95)
histogram_quantile(0.95, rate(hikaricp_connections_acquire_seconds_bucket[5m]))
```

**체크리스트**:

- [ ] 총 발행 메시지 수: 30,000개 (10,000 API 호출 × 3개)
- [ ] DB 저장 성공 메시지: 약 27,000개 (90%)
- [ ] DLT 메시지: 약 3,000개 (10% 의도적 실패)
- [ ] 중복 메시지가 DB에 저장되지 않음
- [ ] `hikaricp_connections_active`가 최대값(20)에 근접했는가?
- [ ] `hikaricp_connections_pending` > 0 (대기가 발생했는가?)
- [ ] `hikaricp_connections_timeout_total` 증가 (타임아웃이 발생했는가?)
- [ ] Consumer 로그에 "😢 앗앗앗" 에러 로그가 있는가? (의도적 실패)
- [ ] Consumer 로그에 "SecondScenarioEvent 생성 완료" 로그가 있는가? (정상 처리)
- [ ] 부하 종료 후 Consumer lag이 0이 되는가?
- [ ] 부하 종료 후 `hikaricp_connections_active`가 정상 수준(0~2)으로 돌아오는가?
- [ ] Connection leak이 없는가? (idle 연결이 정상적으로 반환되는가?)

#### 📊 예상 결과

**정상 동작 시나리오**:

1. **부하 초기 (0~10초)**:
    - Producer API가 10,000번의 요청을 받아 30,000개 메시지 발행
    - `second-scenario.v1` 토픽에 메시지가 빠르게 쌓임
    - Consumer의 10개 스레드가 메시지를 소비하기 시작
    - 약 10% 메시지에서 `randomValue % 10 == 0` 조건 충족
    - 이들 메시지는 3초간 connection 점유 후 RuntimeException 발생
    - `hikaricp_connections_active`가 15~18로 증가

2. **연결 풀 포화 단계 (10~90초)**:
    - 의도적 실패 메시지들이 3초씩 connection을 점유하면서 pool 압박
    - `hikaricp_connections_active`가 18~20에 도달
    - `hikaricp_connections_pending`이 1 이상으로 증가 (스레드가 연결을 기다림)
    - 약 3,000개의 실패 메시지가 `second-scenario.v1-retry` 토픽으로 이동
    - Consumer lag이 증가하기 시작 (특히 retry 토픽)

3. **재시도 메커니즘 작동 (90~180초)**:
    - Retry 토픽의 메시지가 백오프 시간(1초 → 2초 → 10초) 후 재처리
    - **재시도 시에도 `randomValue`가 동일하므로 계속 실패**
    - 1차 재시도 실패 (1초 후) → 2차 retry
    - 2차 재시도 실패 (2초 후) → 3차 retry
    - 3차 재시도 실패 (10초 후) → DLT로 이동
    - 약 3,000개의 메시지가 `second-scenario.v1-dlt` 토픽으로 최종 이동

4. **부하 종료 및 복구 (180~240초)**:
    - Producer가 모든 메시지 전송 완료
    - Consumer가 정상 메시지(90%) 처리 완료
    - 실패 메시지(10%)는 DLT에 적재됨
    - `hikaricp_connections_active`가 감소
    - Consumer lag이 0으로 수렴
    - Connection pool이 정상 상태로 복구

**메트릭 예상치**:

- `hikaricp_connections_active` 최대값: 18~20 (pool size에 근접)
- `hikaricp_connections_timeout_total`: 10~100 증가 (3초 sleep으로 인한 대기)
- `hikaricp_connections_acquire_seconds` P95: 500ms~3초
- Consumer lag 최대값: 10,000~15,000 메시지 (메시지 수가 3배로 증가)
- 최종 DB 저장 메시지: 약 27,000개 (90% 성공)
- DLT 메시지: 약 3,000개 (10% 의도적 실패)
- 중복 메시지: 0개

#### 🎯 성공 기준

**✅ Pass 조건**:

1. **의도적 실패 재현**: 약 10% (3,000개) 메시지가 RuntimeException으로 실패
2. **재시도 메커니즘 작동**: 실패 메시지가 retry 토픽으로 이동하고, 3회 재시도 후 DLT로 이동
3. **정상 메시지 처리**: 약 90% (27,000개) 메시지가 DB에 정상 저장됨
4. **중복 방지**: 중복 체크 로직이 작동하여 DB에 중복이 없음
5. **연결 풀 포화 재현**: `hikaricp_connections_active`가 최대값에 근접하고, `pending` 또는 `timeout` 발생
6. **시스템 복구**: 부하 종료 후 Consumer lag이 0이 되고, connection pool이 정상 상태로 복구

**❌ Fail 조건**:

1. **의도적 실패가 발생하지 않음**: DLT에 메시지가 없거나 매우 적음 (randomValue 로직 문제)
2. **중복 발생**: 같은 eventId가 DB에 2회 이상 저장됨
3. **Connection leak**: 부하 종료 후에도 `hikaricp_connections_active`가 높게 유지됨
4. **Consumer 중단**: Connection 획득 실패로 인해 Consumer 스레드가 중단되거나 에러로 종료
5. **재시도 메커니즘 미작동**: Retry 토픽을 거치지 않고 바로 DLT로 이동하거나, retry가 아예 발생하지 않음

#### 🔍 트러블슈팅

**문제 1: 의도적 실패가 발생하지 않음 (DLT에 메시지가 없음)**

원인: `randomValue` 생성 로직 문제 또는 조건 체크 오류

확인 방법:

```bash
# Consumer 로그에서 "😢 앗앗앗" 검색
grep "😢 앗앗앗" applications/consumer/logs/application.log

# randomValue 분포 확인 (DB에서)
SELECT message, COUNT(*) as cnt
FROM ak_demo.event
GROUP BY message
ORDER BY cnt DESC;
```

해결 방법: EventProducer의 `Random.nextInt(101)` 로직 확인

**문제 2: Connection timeout이 전혀 발생하지 않음**

원인: 부하가 충분하지 않거나 3초 sleep이 pool을 압박하지 못함

해결 방법:

```bash
# API 호출 횟수 증가 (메시지 수는 3배로 증가)
# second_scenario_loader.py에서 repeat 값 변경
repeat = 20000  # 60,000개 메시지 발행
```

**문제 3: 대부분의 메시지가 DLT로 이동 (예상보다 많음)**

원인: MySQL이 응답하지 않거나 네트워크 문제

확인 방법:

```bash
# MySQL 연결 확인
docker exec -it mysql-container mysql -u general_user -p ak_demo

# Consumer 로그에서 실제 에러 메시지 확인
tail -n 100 applications/consumer/logs/application.log | grep ERROR

# DLT 메시지 수 확인 (Kafka UI)
# 예상: 약 3,000개 (10%)
# 만약 그 이상이면 다른 에러 발생 중
```

**문제 4: Connection leak 발생 (부하 종료 후에도 active 연결이 높음)**

원인: 트랜잭션이 완료되지 않고 남아있음

확인 방법:

```sql
-- MySQL에서 현재 실행 중인 트랜잭션 확인
SELECT *
FROM information_schema.innodb_trx;

-- 장기 실행 중인 프로세스 확인
SHOW PROCESSLIST;
```

해결 방법: Consumer 애플리케이션을 재시작하여 연결 초기화

**문제 5: Producer API가 502/503 에러 반환**

원인: Producer 자체가 과부하 상태

해결 방법:

```bash
# Producer의 connection pool도 확인
curl http://localhost:8200/actuator/prometheus | grep hikaricp_connections

# 필요시 Producer의 pool size 증가 (application.yaml 수정)
```

---

## 📈 테스트 결과 분석 가이드

### 성능 튜닝 인사이트

이 테스트를 통해 다음을 평가할 수 있습니다:

1. **현재 Pool Size의 적정성**:
    - `hikaricp_connections_active` 최대값이 15 이하: Pool size 축소 가능 (리소스 절약)
    - 최대값이 20에 도달하고 timeout 빈번: Pool size 증가 필요

2. **Connection Timeout 설정 평가**:
    - 2초 timeout으로 너무 많은 재시도 발생: timeout 증가 고려
    - 2초 내에 대부분 처리됨: 적절한 설정

3. **Concurrency vs Pool Size 비율**:
    - 현재: 10 스레드 / 20 연결 = 2:1
    - 각 메시지 처리 시 1개 연결 사용하므로 이론적으로 10개면 충분
    - 하지만 트랜잭션 시작/종료 타이밍의 중첩으로 여유분 필요

4. **중복 체크 쿼리 최적화 필요성**:
    - `findByEventId()` SELECT 쿼리의 응답 시간 측정
    - 인덱스 최적화 또는 캐싱 전략 고려

### 다음 단계 제안

이 테스트를 성공적으로 완료했다면, 다음 시나리오로 확장할 수 있습니다:

**2.2 Connection Pool Exhaustion - Slow Query** (다음 테스트):

- MySQL에서 의도적으로 쿼리 지연을 발생시켜 연결 보유 시간 증가
- `SET GLOBAL long_query_time = 0.5` + slow query log 분석
- 어떤 쿼리가 병목인지 파악

**2.3 MySQL Max Connections Limit**:

- MySQL의 max_connections를 30으로 제한
- 3개의 Consumer 인스턴스 동시 실행
- 인스턴스 간 연결 경쟁 상황 재현

**2.4 Database Network Partition**:

- `docker network disconnect`로 MySQL 접근 불가 상태 시뮬레이션
- Connection timeout 후 재시도/DLT 동작 검증
- 네트워크 복구 후 자동 재연결 확인

---

## 📝 테스트 실행 기록 템플릿

테스트 실행 시 다음 정보를 기록하여 재현성을 확보하세요:

```markdown
### 테스트 실행 기록

**실행 날짜**: YYYY-MM-DD HH:MM
**테스트 시나리오**: 2.1 Connection Pool Exhaustion - Burst Traffic

**설정**:

- Consumer 인스턴스 수: 1개
- HikariCP Pool Size: 20
- Consumer Concurrency: 10
- API 호출 횟수: 10,000
- 메시지 발행 수: 30,000 (1 API 호출 = 3 메시지)
- 의도적 실패 확률: 10% (randomValue % 10 == 0)

**결과**:

- API 호출 횟수: 10,000
- 발행 메시지 수: 30,000
- DB 저장 메시지 수: 27,134 (90.4%)
- DLT 메시지: 2,866 (9.6%)
- 중복 메시지: 0
- 최대 hikaricp_connections_active: 19
- Connection timeout 발생 횟수: 45
- Consumer lag 최대값: 12,543
- 테스트 소요 시간: 185초

**관찰 사항**:

- 의도적 실패 메시지가 예상대로 약 10% 발생
- 실패 메시지들이 3초간 connection을 점유하면서 pool 압박
- Connection timeout이 부하 중간 시점(30~90초)에 집중 발생
- Retry 메커니즘이 정상 작동: retry → 재시도 3회 → DLT 이동
- `randomValue`가 고정되어 재시도해도 계속 실패하여 DLT로 이동
- 부하 종료 후 약 90초 내에 시스템 정상 복구

**Pass/Fail**: ✅ Pass

**개선 제안**:

- Pool size는 현재 설정(20)이 적절함
- 의도적 실패 로직이 잘 작동하여 Connection Pool Exhaustion 재현 성공
- Retry 메커니즘이 정상 작동함을 확인
```

---

## 🎓 학습 포인트

이 테스트를 통해 배울 수 있는 것들:

1. **Connection Pool의 역할**: 제한된 리소스(DB 연결)를 효율적으로 관리하는 방법
2. **Backpressure 처리**: Consumer가 DB 병목으로 처리 속도가 느려질 때 시스템이 어떻게 대응하는가
3. **Retry 메커니즘의 중요성**: 일시적 장애(connection timeout)를 재시도로 극복
4. **모니터링의 필요성**: 메트릭이 없으면 연결 풀 고갈을 사전에 감지할 수 없음
5. **Graceful Degradation**: 시스템이 과부하 상태에서도 완전히 중단되지 않고 느리게나마 작동하는 설계
