# Part 6. WebFlux + 코루틴

> 모듈: `webflux` (+ `stub-server`)
>
> 요청부터 DB까지 모두 논블로킹인 환경에서 코루틴을 써본다. 코루틴이 원래 의도대로 동작하는 무대다. Part 4~5의 결론을 대조군으로 놓고 무엇이 달라지는지 확인한다.

---

## 6-1. 이벤트 루프와 블로킹의 대가 `핵심`

**목표**: Netty 이벤트 루프 모델에서 블로킹 호출 하나가 서버 전체에 어떤 영향을 주는지 확인한다.

**핵심 개념**
> 1단계 [D-2](../1-basics/d-spring.md#d-2-전체-그림-스레드-코루틴-vt-webflux)에서 먼저 익힌다.

- Spring Boot의 WebFlux는 기본으로 Reactor Netty 위에서 동작한다. Tomcat처럼 "요청 하나에 스레드 하나"가 아니다.
- 이벤트 루프 스레드는 논블로킹 소켓과 OS의 이벤트 통지(epoll, kqueue)를 써서 **여러 연결을 번갈아** 처리한다. 하나의 연결은 정해진 이벤트 루프 스레드 하나에 배정된다.
- `delay`는 타이머에 재개를 예약하고 스레드를 놓는다. `Thread.sleep`은 지금 실행 중인 스레드 자체를 멈춘다(1-1 복습).
- BlockHound는 Java 에이전트다. JDK의 블로킹 메서드를 계측해서 "블로킹하면 안 되는 스레드"에서 호출되면 예외를 던진다. JDK 버전별 추가 JVM 옵션은 공식 README에서 확인한다.

**실행 전 예측**
- WebFlux 서버에서 요청을 처리하는 스레드(reactor-http-nio)는 몇 개일까?
- `suspend` 핸들러에서 `Thread.sleep(1000)`을 하면, 동시 사용자 100명일 때 TPS는 숫자로 얼마일까? 앞 질문의 스레드 수를 근거로 계산하고, 0-6 기준선(`threads.max=20`)의 수치와 나란히 적어본다.
- 그 상태에서 가벼운 헬스 체크 엔드포인트를 따로 호출한다. 헬스 체크의 p50과 p99는 각각 얼마쯤일까? 헬스 체크 연결이 블로킹 중인 루프에 배정될 때와 그렇지 않을 때를 나눠서 생각해본다.

**실험 과제**
1. `delay` 버전과 `Thread.sleep` 버전을 k6로 측정한다.
2. 블로킹하는 동안 다른 엔드포인트도 함께 느려지는 현상을 재현한다.
3. 테스트에 BlockHound를 붙여 블로킹 호출을 자동으로 잡아낸다.

**완료 조건**
- [ ] 이벤트 루프 블로킹 재현 수치
- [ ] BlockHound 탐지 테스트

**공식 문서**
- [Spring WebFlux, Concurrency Model](https://docs.spring.io/spring-framework/reference/web/webflux/new-framework.html)
- [BlockHound](https://github.com/reactor/BlockHound)

---

## 6-2. 외부 API 호출: 끝까지 논블로킹 `핵심`

**목표**: 4-2와 같은 시나리오를 WebFlux에서 구현하고 결과를 비교한다.

**핵심 개념**
- WebClient는 Reactor Netty 클라이언트 위에서 동작한다. 기본 설정에서는 서버와 같은 이벤트 루프 자원을 공유한다.
- `@HttpExchange` 인터페이스에 `suspend` 함수를 쓰려면 WebClient 어댑터(`WebClientAdapter`)가 필요하다. RestClient 어댑터는 `suspend`를 지원하지 않는다.
- `mono { }`로 만든 Mono의 구독이 취소되면 그 안의 코루틴도 취소된다. Reactor의 취소 신호와 코루틴의 취소는 이렇게 이어진다.
- 타임아웃은 여러 층에 걸 수 있다. 코루틴의 `withTimeout`, WebClient/Reactor Netty의 응답·연결 타임아웃이 각각 다른 지점을 끊는다.

**실행 전 예측**
- WebFlux 핸들러에서 Feign 호출을 `withContext(Dispatchers.IO)`로 감싸면 6-1의 문제는 사라질까? 동시 사용자 1,000명이라면 그다음 한계는 어디서 생길까?
- `@HttpExchange` + WebClient의 `suspend` 호출 3개를 병렬로 실행한다. 핸들러 시작, 각 호출의 재개, 응답 작성이 각각 어느 스레드에서 일어날지 이름으로 적어보자. 1단계 D-1(MVC)의 로그와 무엇이 다를까?

**실험 과제**
1. `@HttpExchange` + WebClient의 suspend 클라이언트로 공통 시나리오를 구현한다.
2. 4-3의 취소, 타임아웃 실험을 다시 한다. 취소 전파가 MVC와 다른지 본다.
3. (선택) Feign 호출을 `withContext(Dispatchers.IO)`로 감싼 버전을 같은 부하로 측정해 첫 번째 예측을 확인한다.

**완료 조건**
- [ ] 4-2, 4-3과의 비교표

---

## 6-3. R2DBC와 suspend 트랜잭션 `핵심`

> 필요한 인프라: R2DBC MySQL 드라이버

**목표**: 논블로킹 DB 접근과 코루틴 트랜잭션이 어떻게 동작하는지 확인하고 4-5, 4-6과 비교한다.

**핵심 개념**
- R2DBC는 관계형 DB를 논블로킹으로 접근하는 표준 SPI다. MySQL용으로는 커뮤니티 드라이버(`io.asyncer:r2dbc-mysql`)를 쓴다.
- Spring Data R2DBC는 ORM이 아니다. 영속성 컨텍스트, 지연 로딩, 변경 감지가 없다. 4-5의 JPA 코드를 그대로 옮길 수 없다.
- 트랜잭션 매니저는 두 계열이다. JPA는 `PlatformTransactionManager`(`JpaTransactionManager`), R2DBC는 `ReactiveTransactionManager`(`R2dbcTransactionManager`)를 쓴다.
- 커넥션 풀도 다르다. HikariCP 대신 r2dbc-pool을 쓰고, 설정 키는 `spring.r2dbc.pool.*`이다.
- R2DBC 환경에서는 Hibernate의 `ddl-auto`가 없다. 스키마는 `schema.sql`(`spring.sql.init`) 등으로 따로 준비한다.

**실행 전 예측**
- R2DBC 환경에서 `suspend fun`에 `@Transactional`을 붙이면 4-6과 결과가 다를까? 트랜잭션 정보가 ThreadLocal에 담기지 않는다면 어디에 담길까?
- 트랜잭션 안에서 `withContext(Dispatchers.IO)`로 스레드를 바꿔도 트랜잭션이 유지될까?
- WebFlux에서 JPA 조회를 `withContext(Dispatchers.IO)`로 감싸 쓰면 동작은 한다. 동시 사용자 1,000명, Hikari 풀 10개일 때 이 구현과 R2DBC 구현의 p99는 각각 얼마쯤일까? 병목은 어디에 생길까?

**실험 과제**
1. `CoroutineCrudRepository`로 주문 조회를 구현한다.
2. `@Transactional` suspend 함수와 `TransactionalOperator.executeAndAwait`로 롤백 시나리오를 테스트한다.
3. 4-6에서 만든 "트랜잭션이 깨지는 상황" 매트릭스를 R2DBC 기준으로 다시 채운다.
4. (선택) JPA 조회를 `withContext(Dispatchers.IO)`로 감싼 버전을 만들어 같은 부하에서 R2DBC 구현과 p99를 비교한다.

**완료 조건**
- [ ] 4-6과 비교한 트랜잭션 매트릭스
- [ ] 4-6에서 남긴 예측 확인

**공식 문서**
- [Spring Data, Coroutines 지원](https://docs.spring.io/spring-data/relational/reference/kotlin/coroutines.html)
- [Spring, Coroutines: Transactions](https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html)

---

## 6-4. Reactor ↔ 코루틴 브리지와 컨텍스트 `선택`

**목표**: Reactor 타입과 코루틴을 서로 변환하는 방법을 익힌다. 이때 컨텍스트(MDC, Security, Tracing)가 어떻게 전파되는지도 이해한다.

**핵심 개념**
- Reactor와 코루틴은 컨텍스트를 따로 가진다. Reactor는 구독에 붙는 불변 키-값인 `Context`를, 코루틴은 `CoroutineContext`를 쓴다. 둘을 잇는 것은 `kotlinx-coroutines-reactor`의 몫이다.
- Reactor `Context`는 구독 시점에 아래(구독자)에서 위(소스) 방향으로 전달된다. `contextWrite`는 자신보다 위에 있는 연산자에만 보인다.
- WebFlux의 Spring Security는 인증 정보를 ThreadLocal이 아니라 Reactor `Context`에 둔다(`ReactiveSecurityContextHolder`).
- ThreadLocal 기반 값(MDC, 트레이싱)을 Reactor `Context`와 이어주는 Micrometer context-propagation 라이브러리도 있다. Spring Boot에서는 `spring.reactor.context-propagation` 속성으로 켠다.

**실행 전 예측**
- `mono { }` 빌더 안에서 `ReactorContext`에 담긴 값을 읽을 수 있을까?
- 4-7의 `MDCContext` 방식이 WebFlux에서도 그대로 통할까?

**실험 과제**
1. `Mono.awaitSingle()`, `mono { }`, `Flux.asFlow()`, `Flow.asFlux()` 변환을 각각 써본다.
2. WebFilter에서 넣은 값(traceId, 인증 정보)을 코루틴 핸들러에서 읽는다.
3. 4-7의 컨텍스트 전파 매트릭스를 WebFlux 기준으로 다시 채운다.

**완료 조건**
- [ ] 변환 API 정리
- [ ] 4-7과 비교한 컨텍스트 전파 매트릭스

**공식 문서**: [kotlinx-coroutines-reactor](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-reactor/)

---

## 6-5. 최종 벤치마크: MVC vs VT vs WebFlux `핵심`

**목표**: 공통 시나리오(DB + 외부 API 3개)를 방식별로 구현한 결과를 한 번에 비교한다.

**핵심 개념**
- 공정한 비교의 조건은 README의 [벤치마크 측정 원칙](./README.md#6-참고-벤치마크-측정-원칙)을 따른다. 바꾸는 변수는 구현 방식 하나여야 한다.
- HikariCP와 r2dbc-pool은 설정 키와 기본값이 다르다. 풀 크기는 기본값에 맡기지 말고 양쪽에 같은 값을 명시한다.
- JVM은 JIT 컴파일이 끝나야 성능이 안정된다. 측정 전에 워밍업 구간을 두고 그 구간은 결과에서 뺀다.
- k6, 앱, stub-server가 같은 노트북 CPU를 나눠 쓴다. CPU가 포화되면 구현 방식이 아니라 측정 환경의 한계를 재게 된다.
- TPS와 p99는 함께 본다. 처리량이 비슷해도 꼬리 지연은 크게 다를 수 있다.

**실행 전 예측**
- 동시 사용자 5,000명에서 MVC + 코루틴, MVC + VT, WebFlux + 코루틴의 순위는 어떻게 될까?
- 커넥션 풀이 10개이고 쿼리 하나가 20ms 걸린다고 하자. 각 방식의 최대 TPS를 Little's Law로 계산해 적어보자. 측정이 계산과 어긋난다면 어느 방식에서, 왜일까?

**실험 과제**
1. 같은 k6 스크립트와 같은 stub 지연으로 측정한다. 비교 대상은 **DB 조회와 외부 API 3개를 모두 포함한** 구현이다.
   - MVC + 블로킹, MVC + 코루틴: 4-5 구현(JPA 포함)
   - MVC + VT: 5-2 구현(JPA 포함)
   - WebFlux + 코루틴: 6-3 구현(R2DBC 포함)
   - 0-6과 6-2는 DB가 없다. 조건이 같지 않으므로 이 비교에서 뺀다.
2. 병목을 바꿔가며 다시 측정한다. 외부 API 지연이 클 때와 DB 풀이 작을 때를 각각 본다.

**완료 조건**
- [ ] 최종 비교표와 그래프
- [ ] "어떤 병목에서 어떤 방식이 유리한가" 정리

**판단 가이드에 남길 것**: "WebFlux로 전환할 가치가 있는 조건"
