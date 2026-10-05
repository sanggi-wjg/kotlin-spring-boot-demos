# boiler

커머스 주문 API 예제. 요구사항 원본은 `docs/requirements.md`이며, 구현 판단은 이 문서를 기준으로 한다.

## 명령어

```bash
docker compose -f docker/docker-compose.yaml up -d   # MySQL(6500), Redis(6501)
./gradlew bootRun                                     # 앱 실행 (8080)
./gradlew test                                        # 테스트 (Testcontainers 사용, Docker 필요)
./gradlew ktlintCheck                                 # 린트 (ktlintFormat 으로 자동 수정)
k6 run k6/create-order.js                             # 부하 테스트
```

## 주의사항

- 스택: Kotlin 2.3, Spring Boot 4.1, Java 21. `requirements.md`의 버전 표기(Spring Boot 3.4)와 다르다.
- docker compose 에 볼륨이 없어 컨테이너를 다시 만들면 DB 데이터가 사라진다. 실행 중 리소스 변경은 `docker update` 를 쓴다.
- 스키마 변경은 `src/main/resources/db/migration` 에 Flyway 마이그레이션으로 추가한다 (`ddl-auto: none`).
- `R__seed_data.sql`은 파일이 바뀔 때마다 다시 실행되므로 id 를 고정하고 `ON DUPLICATE KEY UPDATE`(row alias 문법)로 작성한다.
- 인증 대신 `X-User-Id` 헤더로 사용자를 식별한다. 주문 생성·취소는 `Idempotency-Key` 헤더가 필요하다 (`@Idempotent`). 쿠폰 발급은 1인 1매 규칙으로 중복이 막혀 의도적으로 적용하지 않는다 (requirements.md C4 와 다름).
- 주문 생성은 상품별 Redisson 분산 락(`DistributedLockExecutor`) 안에서 재고를 차감한다.
