# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 작업할 때 참고하는 가이드입니다.

## 기술 스택

- Kotlin 1.9.25
- Spring Boot 3.5.6
- Spring Kafka
- JPA/Hibernate
- MySQL Connector
- Java 21
- Gradle (Kotlin DSL)

## 프로젝트 개요

Kafka 장애 시나리오와 복원력 패턴을 시연하는 Kotlin 기반 Spring Boot 프로젝트입니다. Producer와 Consumer 애플리케이션을 분리한 멀티 모듈 Gradle 빌드 구조를 사용하며, 브로커 장애, 네트워크 파티션, Graceful Shutdown 등 다양한 Kafka 장애 시나리오를 테스트합니다.

**핵심 목적**: 3-브로커 클러스터, 재시도 메커니즘, Dead Letter Topic을 사용하여 프로덕션과 유사한 환경에서 Kafka 안정성 패턴을 테스트하고 검증합니다.

## 아키텍처

### 모듈 구조

- **shared**: 애플리케이션 간 공유하는 Kafka 상수, 토픽 이름, 그룹, 메시지 DTO
- **applications/producer**: Kafka로 이벤트를 발행하는 REST API (포트 8200)
- **applications/consumer**: Kafka 이벤트를 소비하고 MySQL에 저장하는 컨슈머 (포트 8301-8303, 다중 인스턴스 지원)

### Kafka 구성

**클러스터 설정** (고가용성 테스트를 위한 3-브로커 클러스터):

- 브로커: localhost:9092, localhost:9093, localhost:9094
- 복제 계수(Replication Factor): 3
- 최소 동기화 복제본(Min In-Sync Replicas): 2
- 기본 파티션 수: 30

**Producer 설정** (Producer 앱 application.yaml):

- acks: all (모든 복제본이 확인할 때까지 대기)
- idempotence: true (중복 방지)
- retries: 3
- delivery timeout: 120초
- compression: gzip

**Consumer 설정** (Consumer 앱 application.yaml):

- Group ID: ak-demo-consumer-group
- Ack mode: RECORD (각 레코드 처리 후 커밋)
- Concurrency: 10 스레드
- Auto-commit: false (수동 제어)
- Max poll records: 500
- Isolation level: read_committed (커밋된 트랜잭션만 읽기)

**재시도 & DLT 패턴** (KafkaConfig.kt):

- 재시도 횟수: 3회, 지수 백오프 (1초 → 2초 → 10초)
- 재시도 토픽 접미사: `-retry`
- DLT 접미사: `-dlt`
- 재시도 제외 예외: IllegalArgumentException, JsonMappingException, JsonProcessingException
- DLT 핸들러: KafkaDeadLetterTopicHandler

### 데이터베이스

- MySQL 8.0 (localhost:3400)
- 데이터베이스: ak_demo
- 계정 정보: general_user / passw0rd
- HikariCP 풀 크기: 20 (consumer), 10 (producer)
- JPA + Hibernate (validate 모드)

## 명령어

### 인프라

모든 서비스 시작 (Kafka 클러스터, Zookeeper, MySQL, Kafka UI):

```bash
cd docker
docker-compose up -d
```

Kafka UI 접속:

```
http://localhost:8100
```

모든 서비스 중지:

```bash
cd docker
docker-compose down
```

### 빌드 & 테스트

전체 프로젝트 빌드:

```bash
./gradlew build
```

특정 모듈 빌드:

```bash
./gradlew :applications:producer:build
./gradlew :applications:consumer:build
./gradlew :shared:build
```

테스트 실행:

```bash
./gradlew test
```

특정 모듈 테스트:

```bash
./gradlew :applications:producer:test
./gradlew :applications:consumer:test
```

### 애플리케이션 실행

Producer 실행 (포트 8200의 REST API):

```bash
./gradlew :applications:producer:bootRun
```

Consumer 실행 (포트 8301의 단일 인스턴스):

```bash
./gradlew :applications:consumer:bootRun
```

부하 테스트를 위한 다중 Consumer 인스턴스 실행:

```bash
# 터미널 1 - Consumer 인스턴스 1 (포트 8301)
./gradlew :applications:consumer:bootRun

# 터미널 2 - Consumer 인스턴스 2 (포트 8302)
./gradlew :applications:consumer:bootRun --args='--spring.profiles.active=consumer2'

# 터미널 3 - Consumer 인스턴스 3 (포트 8303)
./gradlew :applications:consumer:bootRun --args='--spring.profiles.active=consumer3'
```