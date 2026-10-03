# 2단계: 심화 과정

[1단계 기초 과정](../1-basics/README.md)에서 익힌 개념을 실제 상황에서 **직접 실험하며** 검증한다.
다루는 주제는 Thread, Blocking/Non-blocking, Structured Concurrency, Cancellation, 외부 API 호출, JPA/Transaction, Virtual Thread, WebFlux다.

## 1. 목표

다음 세 가지를 할 수 있으면 로드맵을 마친 것이다. 위에 있을수록 중요하다.

1. **판단**: "여기에 코루틴을 써야 하나? Virtual Thread면 충분한가? 그냥 블로킹으로 두는 게 맞나?"를 결정할 수 있다.
2. **적용**: MVC + JPA 서비스에 코루틴을 도입할 때 쓸 패턴과 안티패턴 목록이 있다.
3. **설명**: "코루틴은 왜 스레드보다 가벼운가", "`runBlocking`을 왜 쓰면 안 되는가"를 실험 근거로 설명할 수 있다.

최종 결과물은 [DECISION-GUIDE.md](./DECISION-GUIDE.md)다.
장을 하나 끝낼 때마다 결론을 "실무에서는 ~할 때 ~를 쓴다" 같은 규칙으로 정리해 이 문서에 쌓는다.

## 2. 로드맵

| Part | 주제 | 모듈 | `선택` 장 | 진행 |
|---|---|---|---|---|
| [Part 0](./part-0-jvm-concurrency.md) | JVM 동시성 기초: 스레드, 스레드풀, CompletableFuture, ThreadLocal, Tomcat 모델 | basics, mvc | - | ☐ |
| [Part 1](./part-1-coroutine-core.md) | 코루틴 핵심: suspend, 빌더, 테스트, Dispatcher, 구조화된 동시성, 취소, 예외 | basics | - | ☐ |
| [Part 2](./part-2-shared-state.md) | 공유 상태와 동시성 제어: Mutex, Semaphore, Channel | basics | 2-4 | ☐ |
| [Part 3](./part-3-flow.md) | Flow: cold/hot stream, backpressure | basics | 3-3, 3-4, 3-5 | ☐ |
| [Part 4](./part-4-mvc-coroutine.md) | MVC + 코루틴: 외부 API(Feign, @HttpExchange), JPA, @Transactional, 컨텍스트 전파, Spring 통합과 테스트 | mvc | 4-8 | ☐ |
| [Part 5](./part-5-virtual-thread.md) | Virtual Thread: 플랫폼 스레드 vs 코루틴 vs VT, pinning | mvc | 5-4 | ☐ |
| [Part 6](./part-6-webflux-coroutine.md) | WebFlux + 코루틴: 이벤트 루프, R2DBC, Reactor 브리지 | webflux | 6-4 | ☐ |
| [Part 7](./part-7-observability.md) | 관측성과 운영: 코루틴 덤프, 누수, 메트릭, 종료, 장애 격리 | 전체 | 7-4 | ☐ |
| [Part 8](./part-8-wrap-up.md) | 종합: 판단 가이드, 안티패턴 체크리스트, 회고 | docs | 8-4 | ☐ |

장 제목 옆에는 `핵심` 또는 `선택`이 붙어 있다.

- `핵심` 장은 모두 진행한다.
- `선택` 장은 필요하거나 궁금할 때 진행한다. 건너뛰어도 뒤의 `핵심` 장을 진행하는 데 지장이 없다.

> **1단계와 겹치는 장**: Part 0의 0-1 ~ 0-5와 Part 1 ~ 3은 1단계에서 개념을 먼저 다룬다. 1단계를 마쳤다면 이 장들은 [빠른 통과](#빠른-통과) 후보다. 예측을 적고, 측정이 필요한 실험만 확인한다. 0-6은 1단계 A-1과 주제가 이어지지만 이후 비교의 기준선이므로 빠른 통과 없이 진행한다. Part 4 ~ 8도 그대로 진행한다.

## 3. 한 장을 진행하는 방법

### 진행 순서

1. **핵심 개념을 읽는다.** 실험을 설계하고 예측하는 데 필요한 배경지식이다. 예측 질문의 답은 적혀 있지 않다. 개념은 실험으로 확인할 대상이다. 더 자세한 설명은 각 장에 연결된 1단계 장을 본다.
2. **예측을 먼저 적는다.** 실험 전에 "실행 전 예측" 질문에 답을 적는다. 예측이 틀린 곳이 가장 중요한 학습 지점이다.
3. **실험 코드는 직접 작성한다.** Claude는 과제를 내고, 질문을 던지고, 리뷰만 한다.
4. **가설 → 실험 → 관찰 → 결론** 순서로 기록한다. 결론에는 관찰 근거(로그, assert, 수치)가 있어야 한다.
5. **리뷰를 요청한다.** "Part X-Y 리뷰해줘"라고 하면 Claude가 다음을 확인한다.
   - 완료 조건을 채웠는가
   - 예측과 결과가 왜 달랐는지 설명했는가
   - 결론에 관찰 근거가 있는가
   - 실험 설계에 허점(측정 오류, 교란 변수)은 없는가
6. **결론을 [DECISION-GUIDE.md](./DECISION-GUIDE.md)에 옮긴다.**

### 무엇으로 증명하나

- 개념 장(취소, 예외, 구조화된 동시성 등)은 테스트의 **assert**로 증명한다.
- 처리량 장(스레드 고갈, VT 비교, 벤치마크 등)은 엔드포인트에 부하를 걸어 **k6 + Prometheus/Grafana**로 증명한다.

### 빠른 통과

예측을 **근거까지** 모두 맞혔다면 측정으로 확인만 하고 다음 장으로 넘어간다. 이미 아는 내용에 시간을 쓰지 않기 위해서다.

하나라도 틀렸거나, 근거가 "그렇다고 들었다" 수준이면 장 전체를 진행한다.

### 막혔을 때

Claude에게 힌트를 요청한다. 정답 코드는 주지 않고 아래 순서로 조금씩 돕는다.

1. 방향과 키워드
2. 공식 문서나 소스 코드 위치
3. 의사코드 수준의 구조

### 실험 기록 템플릿

기록은 `docs/study/`에 남긴다. 파일은 장 단위(`docs/study/1-7-cancellation.md`)로 나눠도 되고 Part 단위(`docs/study/part-0.md`)로 모아도 된다.

```markdown
# 1-7. 취소

## 실행 전 예측
- Q1: ...
- Q2: ...

## 실험
- 코드: basics/src/test/kotlin/.../ch07cancellation/...
- 조건: (디스패처, 지연, 동시 요청 수 등)

## 관찰
- (로그, assert 결과, k6 수치, Grafana 스크린샷)

## 예측과 다른 점
- (가장 중요한 섹션이다)

## 결론 → DECISION-GUIDE 반영
- 실무 규칙: "~할 때는 ~한다. 왜냐하면 ~이기 때문이다(근거: 관찰 N)"
```

## 4. 환경

### 실행 환경

모든 실험은 **Java 25**(LTS) 하나로 진행한다. Kotlin 2.3.21, Spring Boot 4.1.1을 쓴다.

### 모듈 구조

| 모듈 | 스택 | 용도 | Part |
|---|---|---|---|
| `basics` | 순수 Kotlin + kotlinx-coroutines | Spring 없이 개념만 실험 | 0, 1, 2, 3 |
| `mvc` | Spring MVC (Tomcat), 포트 8080 | 공통 시나리오, JPA, Virtual Thread | 0-6, 4, 5 |
| `webflux` | Spring WebFlux (Netty), 포트 8081 | 논블로킹 비교, R2DBC | 6 |
| `stub-server` | Spring WebFlux, 포트 8090 | 느린 외부 API(결제, 배송, 쿠폰) 역할 | 0-6부터 |

- 모든 모듈에 `kotlinx-coroutines-core`와 `kotlinx-coroutines-test`가 들어 있다. 버전은 Spring Boot BOM이 관리한다.
- 그 밖의 의존성(JPA, Feign, Actuator 등)은 필요한 장에서 직접 추가한다.
- 장별 패키지 이름은 `...basics.part1.ch07cancellation` 형식을 권장한다.

### 인프라

인프라는 **Docker(Compose) 하나로** 구성하고 필요해질 때 직접 준비한다.

- 측정 대상 앱(mvc, webflux)은 평소에 **호스트 JVM**에서 실행한다. JFR, jstack, IntelliJ 디버거를 쓰기 쉽기 때문이다.
- MySQL, Prometheus, Grafana 같은 의존 서비스는 Compose로 띄운다.
- 앱을 컨테이너로 띄우는 것은 7-5(종료, CPU 제한)뿐이다.

> ⚠ **Docker Desktop VM의 CPU 할당량부터 확인한다.** `docker info`의 `NCPU` 값이다.
> 할당량이 1이면 MySQL, Prometheus, Grafana가 CPU 1개를 나눠 쓴다. 그러면 4-5 이후 벤치마크에서 측정 대상보다 DB가 먼저 병목이 될 수 있다([측정 원칙](#6-참고-벤치마크-측정-원칙) 6번).

인프라는 다음 시점에 필요해진다.

| 필요 시점 | 인프라 |
|---|---|
| Part 0-6 | `stub-server` 구현, k6, Prometheus + Grafana, mvc 모듈의 Actuator |
| Part 4-2 | Spring Cloud 2025.1.2 이상(OpenFeign). Boot 4.1.x를 지원하는 첫 버전이다 |
| Part 4-5 | MySQL(docker-compose), 테스트용 Testcontainers |
| Part 6-3 | R2DBC MySQL 드라이버 |
| Part 7-5 | 앱의 Docker 이미지(종료 실험, CPU 제한 실험용) |

## 5. 참고: 공통 시나리오 "주문 상세 조회"

> 0-6과 Part 4~6에서 쓴다.

Part 4~6은 모두 같은 시나리오로 비교한다. 그래야 MVC, VT, WebFlux를 같은 조건에서 비교할 수 있다.

```
GET /orders/{id}/detail
  1. 주문 조회            ← DB (MySQL, Part 4-5부터. 그 전에는 인메모리로 대체해도 된다)
  2. 결제 정보 조회        ← stub-server  /payments/{orderId}   (기본 지연 300ms)
  3. 배송 정보 조회        ← stub-server  /deliveries/{orderId} (기본 지연 500ms)
  4. 쿠폰 정보 조회        ← stub-server  /coupons/{orderId}    (기본 지연 200ms)
  → 하나로 합쳐 응답
```

stub-server 엔드포인트는 모두 `?delay=<ms>&failRate=<0.0~1.0>` 파라미터를 받는다. 이 값으로 지연과 실패를 바꿔가며 실험한다.

생각해볼 것: 순차로 호출하면 이론상 응답 시간은 얼마일까? 병렬로 호출하면?

## 6. 참고: 벤치마크 측정 원칙

> 0-6부터 처리량을 잴 때 쓴다.

처리량 실험의 결론은 측정이 공정할 때만 의미가 있다. 아래 원칙을 지키지 못했다면 그 사실을 기록에 함께 적는다.

1. **환경을 기록한다.** CPU 코어 수, JDK 버전, JVM 옵션(힙, `-Xss`, debug 플래그), 앱 설정(`threads.max`, Hikari 풀 크기)을 적는다.
   - 코어 수는 `Dispatchers.Default` 크기와 carrier thread 수를 직접 바꾼다.
2. **워밍업 후 측정한다.** JIT 컴파일이 끝나기 전 수치는 버린다. 예를 들어 k6의 첫 30초는 워밍업으로 둔다.
3. **3회 이상 반복하고 중앙값을 쓴다.** 회차마다 결과가 크게 다르면 측정 조건부터 의심한다.
4. **자원 경쟁을 의식한다.** k6, stub-server, 앱, MySQL, Grafana를 한 대에서 돌리면 서로 CPU를 빼앗는다.
   - 측정 대상 앱 말고 다른 프로세스가 CPU를 얼마나 쓰는지 함께 기록한다.
   - 앱은 호스트 JVM에서 돌기 때문에 Docker `cpus`로 제한할 수 없다. JVM이 인식하는 코어 수를 고정하려면 `-XX:ActiveProcessorCount=N`을 쓴다. 이 옵션은 스레드 풀 크기만 바꾸고 실제 CPU 사용량은 제한하지 않는다.
5. **한 번에 변수 하나만 바꾼다.** 클라이언트, 디스패처, 스레드 수, 지연을 한꺼번에 바꾸면 무엇이 결과를 바꿨는지 알 수 없다.
6. **병목이 측정 대상에 있는지 확인한다.** stub-server나 k6가 먼저 포화되면, 앱이 아니라 측정 도구의 한계를 잰 셈이다.
7. **k6 스크립트와 결과를 버전 관리한다.** 같은 스크립트로 0-6부터 6-5까지 비교한다.

## 7. 공식 문서

- Kotlin Coroutines 가이드: https://kotlinlang.org/docs/coroutines-guide.html
- kotlinx.coroutines API 레퍼런스: https://kotlinlang.org/api/kotlinx.coroutines/
- Spring Framework, Kotlin Coroutines 지원: https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html
- Roman Elizarov(코루틴 설계자)의 글 모음: https://elizarov.medium.com/
