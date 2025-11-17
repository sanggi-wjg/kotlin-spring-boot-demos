# Broker 장애 시나리오 테스트

## 📌 시나리오 개요

**상황**: 3-broker 클러스터 중 1개 브로커가 다운되는 상황

**중요성**: Kafka의 고가용성(HA) 기능 검증 - Replication Factor 3, Min ISR 2 설정 효과 확인

**테스트 목표**:

- Broker 1개 다운 시 Producer/Consumer가 정상 동작하는지 확인
- Leader election이 자동으로 발생하는지 검증 (1~3초 이내)
- ISR(In-Sync Replica) 변화 관찰: 3 → 2 → 3
- 메시지 손실 없이 처리되는지 확인 (10,000개 발행 → 10,000개 저장)

---

## 🔧 재현 방법

### Step 1: 환경 준비

```bash
# 인프라 시작
cd docker && docker-compose up -d

# 브로커 3개 확인
docker ps | grep kafka
# ak-kafka-1 (9092), ak-kafka-2 (9093), ak-kafka-3 (9094)

# Producer & Consumer 실행
./gradlew :applications:producer:bootRun
./gradlew :applications:consumer:bootRun
```

### Step 2: 데이터베이스 초기화

```sql
-- MySQL 접속: mysql -h 127.0.0.1 -P 3400 -u general_user -ppassw0rd ak_demo
TRUNCATE TABLE ak_demo.event;
SELECT COUNT(*)
FROM ak_demo.event; -- 0
```

### Step 3: 부하 생성 시작

```bash
cd loader
poetry run python scripts/third_scenario_loader.py
# 10,000개 메시지 발행 (초당 약 200개)
```

### Step 4: 브로커 1개 다운 (30초 후 실행)

```bash
# 부하 30% 진행 시 (약 3,000개 발행 후)
docker stop ak-kafka-1
```

**관찰 포인트**:

- Producer 로그: `Connection could not be established` → 메타데이터 갱신 → 다른 브로커로 전환
- Consumer 로그: `Rebalancing in progress` → 파티션 재할당
- Kafka UI (http://localhost:8100): ISR이 [1,2,3] → [2,3]으로 변경

### Step 5: 브로커 복구 (70% 진행 시)

```bash
# 약 7,000개 발행 후
docker start ak-kafka-1
```

**관찰 포인트**:

- Broker 로그: `Replica fetcher thread started` → 재동기화 시작
- Kafka UI: ISR이 [2,3] → [1,2,3]으로 복구

### Step 6: 부하 완료 대기

스크립트가 10,000개 메시지 모두 발행할 때까지 대기

---

## ✅ 검증

### 1. 메시지 손실 확인

```sql
SELECT COUNT(*)
FROM ak_demo.event;
-- 예상: 10,000개 (손실 없음)

SELECT event_id, COUNT(*) as cnt
FROM ak_demo.event
GROUP BY event_id
HAVING cnt > 1;
-- 예상: 0개 (중복 없음)
```

### 2. 시간대별 처리량 분석

```sql
SELECT DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') as second,
       COUNT(*)                                     as messages_per_second
FROM ak_demo.event
GROUP BY second
ORDER BY second;
-- 브로커 다운 시점에 일시적으로 감소 → 복구 확인
```

### 3. Consumer Lag 확인

Kafka UI (http://localhost:8100):

- Consumer Groups > ak-demo-consumer-group
- Lag: 0 (모든 파티션)

### 4. 체크리스트

- [ ] 총 10,000개 메시지 모두 DB 저장
- [ ] 중복 메시지 0개
- [ ] Broker 다운 시 메시지 발행/소비 계속됨
- [ ] Leader election 발생 (1~3초)
- [ ] ISR 변화: 3 → 2 → 3
- [ ] Broker 복구 시 자동 재동기화

---

## 🎯 성공 기준

**✅ Pass**:

- 메시지 손실 없음 (10,000개 저장)
- Broker 다운 시에도 서비스 정상 동작
- Leader election 자동 발생 (5초 이내)
- ISR이 예상대로 변화
- Broker 복구 시 자동으로 클러스터 재참여

**❌ Fail**:

- 메시지 손실 발생 (DB < 10,000개)
- Broker 다운 시 Producer/Consumer 중단
- Leader election 지연 (10초 이상)

---

## 💡 핵심 개념

**Replication Factor (RF) = 3**:

- 각 파티션이 3개 브로커에 복제됨
- 최대 2개 브로커까지 장애 허용 가능

**Min In-Sync Replicas (Min ISR) = 2**:

- 쓰기 작업 성공하려면 최소 2개 브로커가 데이터 확인 필요
- ISR < 2 시: `NOT_ENOUGH_REPLICAS` 예외 (쓰기 차단)

**Producer Acks = all**:

- 모든 ISR이 확인할 때까지 대기
- 강력한 내구성 보장

**Leader Election**:

1. Leader 브로커 다운 감지
2. ISR 중에서 새로운 Leader 선출
3. Follower 중 하나가 Leader로 승격
4. 메타데이터 업데이트 전파
5. 소요 시간: 1~3초