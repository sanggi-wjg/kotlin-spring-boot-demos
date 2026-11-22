# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Spring Cloud Gateway(WebFlux) 학습용 데모 프로젝트. Kotlin 2.4 / Spring Boot 4.1 / Java 25 / Spring Cloud 2025.1.

## Commands

```bash
docker compose -f docker/docker-compose.yaml up -d   # Redis (localhost:6501) - RateLimiter에 필요
./gradlew build
./gradlew :gateway:bootRun                            # 모듈별 실행 (:application-order, :application-task, :application-auth)
./gradlew :gateway:test --tests "*GatewayApplicationTests"
./scripts/ratelimit.sh 30 http://order.localhost:8080/api/v1/orders/1   # 레이트 리밋 검증 (200 → 429)
```

Host 기반 라우팅이므로 `/etc/hosts`에 `127.0.0.1 order.localhost`, `127.0.0.1 task.localhost` 추가 필요.

## Architecture

| 모듈 | 포트 | 역할 |
|---|---|---|
| `gateway` | 8080 | 진입점. 라우팅/필터/레이트 리밋 |
| `application-order` | 8081 | `GET /api/v1/orders/{id}` |
| `application-task` | 8082 | `GET /api/v1/task/{id}` |
| `application-auth` | 8083 | 인증 서버 (아직 비어 있음, 인증은 TODO) |

- 라우트는 코드가 아닌 `gateway/src/main/resources/application.yaml`의 `spring.cloud.gateway.server.webflux.routes`에 정의. `Host` predicate로 `order.localhost` → 8081, `task.localhost` → 8082.
- `CustomGlobalFilter`(order -1): 모든 요청에 `X-Gateway-Finger-Print`, `X-Gateway-Requested-At` 헤더를 추가하고 요청/응답 로깅. 라우트별로는 `X-Gateway-Id` 헤더 추가. 하위 서비스 컨트롤러는 이 헤더들을 받아 로깅한다.
- Rate limit: order 라우트에만 `RequestRateLimiter`(Redis 토큰 버킷, replenish 10 / burst 20) 적용. `key-resolver`는 `RateLimiterConfig`의 `userKeyResolver` / `ipKeyResolver` 빈을 SpEL로 참조 (현재 둘 다 원격 IP 기반).
- 공통 빌드 설정(Kotlin, Java 툴체인, 테스트 의존성)은 루트 `build.gradle.kts`의 `subprojects`에 있고, 모듈 파일에는 모듈별 의존성만 둔다.
