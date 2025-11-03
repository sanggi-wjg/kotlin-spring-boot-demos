# Kafka 장애 시나리오 테스트 계획

## 📌 프로젝트 개요

### 현재 Kafka 구성

- **Kafka Cluster**: 3 brokers (localhost:9092, 9093, 9094)
- **Replication Factor**: 3
- **Min In-Sync Replicas**: 2
- **Partitions**: 30
- **Topic**:
    - `FIRST_SCENARIO` (+ retry/DLT topics)

### Producer 설정

- **Acks**: `all`
- **Idempotence**: `true`
- **Retries**: 3
- **Delivery Timeout**: 120초
- **Request Timeout**: 30초

### Consumer 설정

- **Group ID**: `ak-demo-consumer-group`
- **Ack Mode**: `RECORD`
- **Concurrency**: 10
- **Auto Commit**: `false`
- **Max Poll Records**: 500
- **Max Poll Interval**: 5분
- **Retry 정책**: 3회 재시도 (1s → 2s → 10s backoff)
- **DLT**: Retry 실패 시 Dead Letter Topic으로 이동

### Database

- **MySQL**: localhost:3400
- **Connection Pool**: 20
- **Transaction**: `@Transactional` 적용

---

## 🛠️ 테스트 환경 준비

### 사전 요구사항

```bash
# 모든 서비스 시작
cd docker
docker-compose up -d

# Kafka UI 접속
http://localhost:8100

# Producer 실행
./gradlew :applications:producer:bootRun

# Consumer 실행
./gradlew :applications:consumer:bootRun
./gradlew :applications:consumer:bootRun --args='--spring.profiles.active=consumer2'
./gradlew :applications:consumer:bootRun --args='--spring.profiles.active=consumer3'
```

---

## 🔴 Tier 1: 필수 테스트 시나리오

### 1.1 Rolling Deployment & Graceful Shutdown

#### 📖 시나리오 설명

**상황**: 배포 시 consumer 애플리케이션이 재시작되는 상황
**중요성**: 배포할 때마다 발생하며, 메시지 손실이나 중복 처리가 발생할 수 있음
**테스트 목표**: Graceful shutdown이 제대로 동작하여 처리 중인 메시지를 완료하고 종료하는지 검증

#### 🔧 재현 방법

**Step 1: 부하 생성** (loader 폴더 참고)

```bash
# Producer로 지속적으로 메시지 전송 (초당 100개)
import time
import httpx

if __name__ == "__main__":
    repeat = 10000

    with httpx.Client() as client:
        for i in range(repeat):
            response = client.post("http://localhost:8200/events/first-scenario")
            print(i, response.json())

            if (i % 200) == 0:
                time.sleep(1)

```

**Step 2: Consumer 정상 종료 (Graceful Shutdown)**

SIGTERM 신호 전송

**Step 3: 로그 확인하며 Consumer 재시작**

#### ✅ 검증 체크리스트

**로그 확인**

- [ ] Consumer가 SIGTERM을 받고 "Shutdown requested" 로그가 출력되는가?
- [ ] 현재 처리 중인 배치가 완료될 때까지 대기하는가?
- [ ] Rebalancing이 발생하는가? (`Revoking previously assigned partitions`)
- [ ] 다른 consumer instance가 파티션을 인수받았는가?

**데이터 정합성 확인**

```sql
SELECT COUNT(*)
FROM ak_demo.first_scenario_event;

SELECT event_id, COUNT(*) as cnt
FROM ak_demo.first_scenario_event
GROUP BY event_id
HAVING cnt > 1;
```

**메트릭 확인**

- [ ] Offset이 정확히 commit되었는가?
- [ ] 메시지 손실이 없는가? (전송 수 = DB 저장 수)
- [ ] 중복 처리된 메시지가 있는가?

#### 📊 예상 결과

- Consumer가 종료 신호를 받으면 새로운 메시지 polling을 중단
- 현재 처리 중인 배치는 완료하고 offset을 commit
- 다른 consumer instance로 파티션이 재할당됨 (약 3초 이내)
- 메시지 손실 없음, 중복도 없음 (idempotency 동작)

#### 🎯 성공 기준

- ✅ **Pass**: 전송한 메시지 수 = DB 저장된 고유 eventId 수
- ✅ **Pass**: Consumer lag이 0이 됨
- ✅ **Pass**: 중복 메시지가 없거나, 있더라도 중복 체크 로직으로 DB에 저장되지 않음
- ❌ **Fail**: 메시지 손실 발생
- ❌ **Fail**: 처리 중인 메시지가 중단되고 rebalance 후 재처리됨 (성능 저하)

