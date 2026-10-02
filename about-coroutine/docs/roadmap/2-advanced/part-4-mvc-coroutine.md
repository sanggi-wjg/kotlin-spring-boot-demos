# Part 4. Spring MVC + 코루틴

> 모듈: `mvc` (+ `stub-server`)
>
> 실무와 가장 가까운 Part다. 공통 시나리오(주문 상세 조회)로 "Tomcat 위에서 코루틴을 쓰면 무엇을 얻고 무엇을 얻지 못하는가"를 확인한다.
> 처리량 실험은 모두 0-6의 **기준선**과 비교한다.

---

## 4-1. suspend 컨트롤러는 어느 스레드에서 도는가 `핵심`

**목표**: MVC가 `suspend fun` 핸들러를 어떻게 처리하는지, 그 방식이 Tomcat 스레드에 어떤 영향을 주는지 확인한다.

**핵심 개념**
> 1단계 [D-1](../1-basics/d-spring.md#d-1-spring-mvc에서-코루틴)에서 먼저 익힌다.

- Tomcat은 요청마다 스레드 하나를 배정한다. 핸들러가 반환될 때까지 그 스레드는 다른 요청을 받지 못한다(0-6).
- 서블릿 3.0부터 비동기 처리가 있다. 요청을 비동기 모드로 전환하면 요청 스레드는 컨테이너로 돌아가고, 결과가 준비되면 다른 스레드가 응답을 마저 쓴다. Spring MVC는 이것을 `DeferredResult`, `Callable` 같은 반환 타입으로 감싸서 제공한다.
- `suspend fun`은 컴파일되면 마지막 파라미터로 `Continuation`을 받는 일반 함수가 된다. 프레임워크가 이 함수를 호출하려면 `Continuation`을 넘겨주고 결과를 받아낼 방법이 있어야 한다.
- `delay`는 스레드를 놓아주는 중단이고, `Thread.sleep`은 스레드를 붙잡은 채 멈추는 블로킹이다(Part 1).

**실행 전 예측**
- `kotlinx-coroutines-reactor`가 **없는** 상태에서 `suspend fun` 컨트롤러를 만들면, 실패는 언제(애플리케이션 시작 시점인가, 첫 요청 시점인가) 어떤 형태(예외 종류, HTTP 상태 코드)로 드러날까?
- `threads.max=20`에서 `delay(1000)` 핸들러에 동시 사용자 100명을 걸면, `tomcat.threads.busy`는 대략 몇을 가리킬까? 그때 동시에 처리 중인 요청 수를 제한하는 것은 무엇일까?
- 같은 조건에서 `Thread.sleep(1000)` 버전의 TPS는 얼마일까? 0-6 기준선과 같을까, 다를까? 다르다면 그 차이는 어디서 올까?
- 0-6과 같은 조건(`threads.max=20`, 동시 사용자 100명)에서 `delay` 버전의 TPS는 기준선의 몇 배일까? p99는?

**실험 과제**
1. `suspend` 컨트롤러를 의존성 없이 먼저 실행해 실패 형태를 기록하고, 의존성을 넣어 해결한다. 그 의존성이 **왜** 필요한지 Spring 소스(`CoroutinesUtils`)에서 찾아본다.
2. 핸들러 진입 시점, 중단 이후, 응답 직전의 스레드 이름을 로그로 남긴다.
3. `delay` 버전과 `Thread.sleep` 버전을 k6로 각각 측정한다. 결과를 0-6 기준선과 비교한다.

**완료 조건**
- [ ] 스레드 전환 로그와 그 해석(서블릿 비동기 처리와의 관계)
- [ ] 기준선 대비 처리량 비교표와 그 이유

**열린 질문**
- "MVC에서 suspend 컨트롤러를 쓰면 처리량이 늘어난다"는 말은 어떤 조건에서만 참인가?

**공식 문서**
- [Spring, Coroutines: Spring MVC](https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html)
- [Spring MVC, Asynchronous Requests](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html)

---

## 4-2. 외부 API 병렬 호출: Feign vs @HttpExchange `핵심`

> 필요한 인프라: Spring Cloud **2025.1.2 이상**(Oakwood). Boot 4.1.x를 지원하는 첫 버전이고 OpenFeign 5.0.x가 들어 있다.

**목표**: 같은 외부 API 3개를 클라이언트 방식별로 병렬 호출한다. 방식마다 스레드 사용량과 처리량을 비교한다.

| 방식 | 블로킹 여부 | 요청 1건당 점유 스레드(측정) | TPS / p99 @ 동시 사용자 200(측정) |
|---|---|---|---|
| OpenFeign + `async(Dispatchers.IO)` | 블로킹 | ? | ? |
| `@HttpExchange` + RestClient 백엔드 + `async(Dispatchers.IO)` | 블로킹 | ? | ? |
| `@HttpExchange` + WebClient 백엔드(`suspend`) | 논블로킹 | ? | ? |

> 코루틴과 함께 쓰는 법(어떤 어댑터에서 `suspend`가 되는지 등)은 1단계 D-1에서 다뤘다. 이 장은 그 결론이 부하 아래에서 **숫자로** 어떻게 나타나는지 잰다.

**핵심 개념**
> 1단계 [D-1](../1-basics/d-spring.md#d-1-spring-mvc에서-코루틴)에서 먼저 익힌다.

- OpenFeign은 인터페이스 선언으로 HTTP 클라이언트를 만드는 라이브러리다. 런타임에 프록시가 만들어지고, 호출은 블로킹 HTTP 클라이언트로 실행된다.
- `@HttpExchange`는 Spring Framework의 HTTP Service Client다. 인터페이스 선언을 `HttpServiceProxyFactory`가 프록시로 만들고, 실제 호출은 어댑터(RestClient, WebClient, RestTemplate)가 맡는다. 같은 인터페이스라도 어댑터에 따라 블로킹인지 논블로킹인지가 달라진다.
- 블로킹 호출을 `async`로 감싸도, 그 호출은 실행되는 동안 스레드 하나를 붙잡는다. 어느 스레드를 붙잡을지는 디스패처가 정한다.
- 논블로킹 클라이언트(WebClient)는 소켓 이벤트를 소수의 이벤트 루프 스레드(Reactor Netty)가 처리한다. 응답을 기다리는 동안 호출자의 스레드를 쓰지 않는다.

**실행 전 예측**
- Feign 호출 3개를 `async(Dispatchers.IO)`로 병렬화하고 동시 사용자를 100 → 200 → 400으로 늘린다. `Dispatchers.IO`의 한도(64)와 Tomcat `threads.max` 중 어느 쪽이 먼저 병목이 될까? 병목이 생기는 동시 사용자 수와 그때의 p99를 숫자로 예측해보자.
- WebClient 백엔드에서 동시 사용자를 1,000명으로 늘리면 JVM 라이브 스레드 수는 얼마나 늘까? 스레드 대신 어떤 자원이 먼저 한계에 닿을까? (힌트: WebClient의 커넥션 풀 설정)
- 동시 사용자 100명에서 세 방식의 TPS 순위는 어떻게 될까? 동시 사용자 1,000명에서는 순위가 바뀔까?

**실험 과제**
1. 세 방식으로 공통 시나리오의 외부 호출 부분을 구현한다. 세 방식 모두 `async`로 병렬 호출한다.
2. 단일 요청의 응답 시간이 이론값(max(300, 500, 200)ms)에 가까운지 확인한다.
3. k6로 측정한다. 지표는 TPS, p99, JVM 라이브 스레드 수, `Dispatchers.IO` 사용 여부다.
4. 위 표의 `?` 칸을 채운다.

**완료 조건**
- [ ] 세 방식 비교표(코드량, 점유 스레드, TPS, p99)
- [ ] "Feign을 쓰는 기존 서비스에 코루틴을 도입하면 얻는 것과 얻지 못하는 것" 정리

**열린 질문**
- Spring Cloud OpenFeign은 공식적으로 "feature-complete" 상태다. 버그 수정만 하고 Spring HTTP Service Clients로 옮기라고 권한다. 새 프로젝트라면 무엇을 고르겠는가? 기존 Feign 코드는 어떻게 하겠는가?

**공식 문서**
- [Spring, HTTP Interface Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Spring Cloud OpenFeign](https://docs.spring.io/spring-cloud-openfeign/reference/)

---

## 4-3. 타임아웃, 실패, 취소 전파 `핵심`

**목표**: 1-7과 1-8에서 배운 취소와 예외 규칙이 **실제 HTTP 호출**에서도 그대로 통하는지 확인한다.

**핵심 개념**
- 이 장의 예측은 1-7(취소는 언제 실제로 일어나는가)과 1-8(자식의 실패는 어디까지 전파되는가)의 결론을 HTTP 호출에 적용하는 문제다. 두 장의 노트를 먼저 다시 본다.
- HTTP 호출의 시간 제한은 여러 층에 있다. connect timeout(연결 수립)과 read/response timeout(응답 대기)은 HTTP 클라이언트 라이브러리가 걸고, `withTimeout`은 코루틴 런타임이 건다.
- 클라이언트가 요청을 "중단"한다는 것은 결국 커넥션을 닫거나 스트림을 리셋한다는 뜻이다. 서버(stub)는 커넥션이 닫힌 것을 보고서야 요청이 중단된 것을 안다.
- 서버 쪽에서 클라이언트의 연결 종료는 서블릿 컨테이너가 감지한다(응답을 쓰다가 실패하거나, 비동기 요청의 오류 이벤트로). 이 신호가 핸들러의 코루틴까지 이어지는지가 확인할 대상이다.

**실행 전 예측**
- 결제 API가 실패해 배송, 쿠폰 코루틴이 취소될 때, **stub 쪽 로그에서** 배송, 쿠폰 요청이 끊긴 것으로 보일까? Feign, RestClient(+`Dispatchers.IO`), WebClient마다 다를까?
- `withTimeout(1000)` 안에서 `withContext(Dispatchers.IO)`로 Feign을 불러 지연 5초짜리 API를 호출한다. 호출한 쪽은 몇 초 시점에 예외를 받을까? 그 IO 스레드는 언제 풀려날까? `runInterruptible`로 바꾸면 Feign의 기본 HTTP 클라이언트는 인터럽트에 반응할까?
- 클라이언트가 응답을 기다리다 연결을 끊으면 서버의 코루틴은 취소될까?

**실험 과제**
1. stub-server에 "요청을 받았다"와 "요청이 취소되었다(커넥션 종료)" 두 가지 로그를 남긴다.
2. 방식마다 실패와 타임아웃을 일으킨다. stub 쪽 로그로 실제 호출이 중단되었는지 확인한다.
3. `withTimeout`과 HTTP 클라이언트의 read timeout을 함께 설정한다. 어느 쪽이 먼저 동작하는지 확인한다.
4. "쿠폰은 실패해도 되고 결제는 필수"인 정책을 구현한다(1-8 응용).

**완료 조건**
- [ ] 방식별 취소 전파 매트릭스(코루틴 취소 → HTTP 호출 중단 여부)
- [ ] 부분 실패 정책 구현과 테스트

**열린 질문**
- HTTP 클라이언트의 connect/read timeout, 호출 단위 `withTimeout`, 요청 전체 제한을 함께 쓸 때, 각 값은 서로 어떤 관계로 정해야 할까? 하나를 잘못 잡으면 어떤 증상이 나타날까?

---

## 4-4. 안티패턴 실험실 `핵심`

**목표**: 운영에서 자주 터지는 코루틴 안티패턴을 일부러 재현한다. 각 안티패턴이 Grafana와 스레드 덤프에서 어떻게 보이는지 기록한다.

**핵심 개념**
- `runBlocking`은 현재 스레드를 막고, 블록 안의 코루틴이 모두 끝날 때까지 기다린다. 블로킹 코드와 코루틴 코드를 잇는 다리다.
- 디스패처마다 스레드 수가 다르다. `Dispatchers.Default`는 CPU 코어 수(최소 2)만큼, `Dispatchers.IO`는 64와 코어 수 중 큰 값만큼 스레드를 쓴다. 둘은 스레드 풀을 공유하지만 한도는 따로 계산한다.
- `GlobalScope`는 어떤 `Job`에도 묶이지 않은 최상위 스코프다. 구조화된 동시성의 부모-자식 관계 밖에 있다.
- 스레드 덤프에는 코루틴이 아니라 코루틴을 실행 중인 스레드만 보인다. 코루틴 단위로 보려면 `kotlinx-coroutines-debug`의 `DebugProbes`로 코루틴 덤프를 뜬다.

**실행 전 예측** (항목마다 답한다)
- 요청 스레드에서 `runBlocking`으로 외부 API 3개를 병렬 호출한다. `threads.max=20`, 동시 사용자 100명에서 TPS는 대략 얼마일까? 0-6 기준선(순차 호출)의 몇 배일까?
- 블로킹 호출(지연 1초)을 `Dispatchers.IO`에서 실행하고 동시 요청을 200개 넣으면 p99는 얼마일까?
- 이 노트북(코어 N개)에서 `Dispatchers.Default`로 1초짜리 블로킹 호출을 동시에 몇 개 넣으면, 같은 서버의 CPU 작업 엔드포인트 p99가 튀기 시작할까? 그 순간 스레드 덤프의 `DefaultDispatcher-worker`들은 어떤 상태로 보일까?
- 요청마다 `GlobalScope.launch`로 외부 API(지연 2초)를 부르는 후처리를 띄운다. 초당 200건이 들어오면 무엇이 쌓이고, 어떤 지표(힙, 스레드, 외부 API 동시 요청 수)로 드러날까?

**실험 과제**
1. 위 4가지 안티패턴을 각각 별도 엔드포인트로 만든다.
2. 엔드포인트마다 k6로 부하를 건다. Grafana 지표와 스레드 덤프를 수집한다.
3. 안티패턴마다 "증상 → 원인 → 올바른 대안"을 정리한다.

**완료 조건**
- [ ] 안티패턴별 증상 기록(지표 스크린샷, 덤프 일부)
- [ ] 각각의 대안 구현과 개선 수치

**판단 가이드에 남길 것**: "`runBlocking`을 써도 되는 곳", "블로킹 코드를 코루틴에서 호출하는 규칙"

---

## 4-5. JPA와 코루틴 `핵심`

> 필요한 인프라: MySQL(docker-compose), Testcontainers

**목표**: 블로킹 JDBC를 코루틴과 함께 쓰면 자원 크기가 서로 맞지 않는다. 디스패처 스레드 수와 커넥션 풀 크기의 불일치가 어떤 문제를 만드는지 확인한다.

**핵심 개념**
- JDBC는 블로킹 API다. 쿼리를 보낸 스레드는 결과가 올 때까지 소켓 읽기에서 멈춘다.
- 커넥션 풀(HikariCP)은 커넥션을 미리 만들어두고 빌려준다. 모두 빌려가면 다음 요청자는 `connectionTimeout`(기본 30초)까지 기다리다가 실패한다.
- 요청 하나가 DB까지 가려면 코루틴 → 디스패처 스레드 → 커넥션 → DB 세션 순서로 자원을 하나씩 얻어야 한다. 각 자원의 상한(디스패처 크기, 풀 크기)을 숫자로 적어두고 예측한다.
- `limitedParallelism(n)`은 원본 디스패처 위에서 동시에 실행되는 작업 수를 n개로 제한하는 뷰를 만든다. 새 스레드 풀을 만드는 것이 아니다.
- MySQL에서 실행 중인 쿼리를 멈추려면 서버에 `KILL QUERY`를 보내야 한다. JDBC에서는 `Statement.cancel()`이 그 역할을 한다.

**실행 전 예측**
- Hikari 풀이 10개다. `withContext(Dispatchers.IO)`에서 동시에 쿼리 200개를 날리면 IO 스레드는 몇 개 쓰일까? 그중 몇 개가 커넥션을 기다릴까?
- `SELECT SLEEP(5)` 쿼리를 실행 중인 코루틴을 1초 시점에 취소하면, MySQL `SHOW PROCESSLIST`에서 그 쿼리는 몇 초 시점까지 보일까? JDBC 쿼리 타임아웃(예: `jakarta.persistence.query.timeout`)을 2초로 걸면 달라질까?
- `Dispatchers.IO.limitedParallelism(10)` 전용 디스패처를 쓰면, 동시 요청 200개에서 Hikari pending과 사용 중인 IO 스레드 수는 각각 어떻게 바뀔까? 대기는 사라질까, 다른 곳으로 옮겨갈까? 옮겨간다면 어디서 보일까?

**실험 과제**
1. 공통 시나리오의 주문 조회를 JPA로 바꾼다.
2. 느린 쿼리로 커넥션 풀 고갈을 재현한다. Hikari 지표(active, pending)와 IO 스레드 상태를 관찰한다.
3. 전용 디스패처 패턴을 적용하고 전후를 비교한다.
4. 쿼리 실행 중에 취소한다. MySQL `SHOW PROCESSLIST`로 실제 쿼리 상태를 확인한다.

**완료 조건**
- [ ] 풀 고갈 재현과 전용 디스패처 적용 전후 비교
- [ ] 블로킹 쿼리 취소 결과(1-7 열린 질문에 대한 답)

**공식 문서**: [HikariCP, About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)

---

## 4-6. @Transactional과 코루틴 `핵심`

**목표**: Spring 트랜잭션은 ThreadLocal에 기반한다. 이 트랜잭션이 코루틴 경계에서 어떻게 깨지는지 확인하고(0-5에서 깔아둔 복선) 안전한 트랜잭션 경계를 설계한다.

**핵심 개념**
- `@Transactional`은 AOP 프록시가 메서드 앞뒤에서 트랜잭션을 시작하고 커밋하는 방식으로 동작한다. JPA의 `PlatformTransactionManager`는 현재 트랜잭션과 커넥션(EntityManager)을 `TransactionSynchronizationManager`의 ThreadLocal에 묶어둔다.
- 같은 트랜잭션에 참여한다는 것은 같은 커넥션을 쓴다는 뜻이다. 참여 여부는 `TransactionSynchronizationManager.isActualTransactionActive()`로 확인한다.
- 기본 롤백 규칙: unchecked 예외(`RuntimeException`, `Error`)에서는 롤백하고 checked 예외에서는 커밋한다. Kotlin에는 checked 예외 구분이 없지만, Spring은 Java 예외 계층으로 판단한다.
- 지연 로딩은 연관 객체를 프록시로 두었다가 처음 접근할 때 쿼리한다. 이 쿼리는 영속성 컨텍스트가 열려 있어야 실행된다. Boot는 기본적으로 OSIV(`spring.jpa.open-in-view=true`)로 요청 동안 영속성 컨텍스트를 열어두므로 실험할 때 이 설정을 의식한다.
- 트랜잭션이 시작되면 커밋할 때까지 커넥션 하나를 붙잡는다. 트랜잭션 범위가 곧 커넥션 점유 범위다.

**실행 전 예측**
- JPA(`PlatformTransactionManager`) 환경에서 `@Transactional suspend fun` 본문의 첫 `delay` 앞과 뒤에서 `isActualTransactionActive()`를 찍으면 각각 무엇이 나올까? 본문 끝에서 예외를 던지면 그 앞의 save는 롤백될까?
- 일반 `@Transactional fun` 안에서 `runBlocking { withContext(Dispatchers.IO) { repository.save(...) } }`를 하면, save는 같은 트랜잭션에 참여할까? 참여하지 않는다면 save는 어느 트랜잭션에서 언제 커밋될까? 바깥 메서드가 그 뒤에 예외를 던지면 저장된 행은 남을까?
- 트랜잭션 안에서 `async`로 쿼리 2개를 병렬 실행하면 어떻게 될까?
- 트랜잭션 밖의 다른 스레드에서 지연 로딩(lazy loading) 연관관계에 접근하면?

**실험 과제**
1. 위 상황을 각각 테스트로 재현한다. 트랜잭션 참여 여부는 `TransactionSynchronizationManager`로 확인한다.
2. 롤백이 기대대로 되는지 DB 상태로 assert한다.
3. 안전한 패턴을 설계한다. 예를 들어 "트랜잭션은 블로킹 영역 안에 가두고 외부 API 호출은 트랜잭션 밖에서 병렬로" 같은 패턴이다. `TransactionTemplate` 사용도 검토한다.
4. 4-5의 시나리오(JPA 포함)에서 외부 API 호출을 트랜잭션 안에 넣었을 때와 밖으로 뺐을 때를 비교한다. 커넥션 점유 시간과 Hikari pending이 어떻게 달라지는지 측정한다.

**완료 조건**
- [ ] 트랜잭션이 깨지는 상황 매트릭스(테스트로 증명)
- [ ] 권장 트랜잭션 경계 패턴과 그 근거

**열린 질문**
- Part 6에서 R2DBC를 쓰면 이 문제는 어떻게 해결될까? 미리 예측해두자.

**공식 문서**
- [Spring, Coroutines](https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html): 트랜잭션 섹션은 reactive 트랜잭션만 다룬다.
- [about-transaction: 트랜잭션 동기화](../../../../about-transaction/README.md)

**판단 가이드에 남길 것**: "코루틴과 JPA 트랜잭션을 함께 쓰는 규칙"

---

## 4-7. 컨텍스트 전파: MDC, Tracing, Security `핵심`

**목표**: 스레드가 바뀌면 컨텍스트가 사라진다(0-5). 코루틴에서 이 컨텍스트를 올바르게 전파하는 방법을 익힌다.

**핵심 개념**
- MDC, `SecurityContextHolder`(기본 전략 `MODE_THREADLOCAL`), `RequestContextHolder`는 모두 값을 ThreadLocal에 담는다.
- 코루틴은 `CoroutineContext`라는 자체 컨텍스트를 가진다. 이 컨텍스트는 실행 스레드가 바뀌어도 코루틴을 따라가고, 자식 코루틴은 부모의 컨텍스트를 물려받는다.
- `ThreadContextElement`는 `CoroutineContext`와 ThreadLocal을 잇는 장치다. 코루틴이 어떤 스레드에서 실행을 시작하거나 재개될 때, 그리고 중단될 때 호출되는 훅을 제공한다. `MDCContext`가 이 방식으로 구현되어 있다.
- Micrometer Context Propagation은 ThreadLocal 값을 `ThreadLocalAccessor`로 등록해 캡처하고 복원하는 라이브러리다. Reactor와 Micrometer Tracing이 이것을 쓴다.

**실행 전 예측**
- 필터에서 MDC에 `traceId`를 넣었다. suspend 컨트롤러가 WebClient 호출 뒤 `reactor-http-nio` 스레드에서 재개되어 로그를 찍으면 traceId가 나올까? Micrometer Tracing을 붙이고 Spring Boot의 context propagation 설정(`spring.reactor.context-propagation`)을 켜면 달라질까?
- `async` 블록 안에서 `SecurityContextHolder.getContext()`를 호출하면? 첫 중단 지점 **전**과 **후**에 결과가 다를까?
- 결제 호출 구간에서만 MDC에 `step=payment`를 추가하고, 구간이 끝나면 지우고 싶다. 중단과 스레드 전환이 섞여도 다른 요청의 로그로 새지 않게 하려면 어떻게 해야 할까? 동시 요청 50개에서 섞임이 없음을 어떻게 검증할까?

**실험 과제**
1. MDC 유실을 재현한다. `kotlinx-coroutines-slf4j`의 `MDCContext`로 해결한다.
2. Micrometer Tracing을 붙인다. 외부 API 호출 span이 같은 trace로 묶이는지 확인한다(context-propagation).
3. SecurityContext와 RequestContextHolder 유실을 재현하고 해결한다.
4. `ThreadContextElement`를 직접 구현해서 커스텀 ThreadLocal을 전파한다.

**완료 조건**
- [ ] 컨텍스트별 유실 재현 테스트와 해결 테스트
- [ ] 직접 구현한 `ThreadContextElement`

**공식 문서**
- [kotlinx-coroutines-slf4j](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-slf4j/)
- [Micrometer Context Propagation](https://docs.micrometer.io/context-propagation/reference/)

**판단 가이드에 남길 것**: "코루틴을 도입할 때 확인할 ThreadLocal 의존성 체크리스트"

---

## 4-8. Flow 반환과 SSE `선택`

**목표**: 컨트롤러에서 Flow를 반환해 스트리밍 응답을 만든다. 연결이 끊기면 취소가 전파되는지 확인한다.

**핵심 개념**
- SSE(Server-Sent Events)는 `text/event-stream` 형식으로 HTTP 응답 하나를 닫지 않고 `data:` 줄 단위 이벤트를 계속 보내는 표준이다. 브라우저에서는 `EventSource`로 받는다.
- MVC의 스트리밍 응답은 서블릿 비동기 처리(4-1) 위에서 동작한다. 응답이 끝날 때까지 커넥션은 열려 있다.
- Flow는 cold 스트림이다. 누군가 collect할 때 실행되고, collect하는 쪽이 취소되면 업스트림도 함께 취소된다(Part 3). `onCompletion`은 정상 종료, 예외, 취소 모두에서 호출된다.

**실행 전 예측**
- 값을 1초마다 하나씩 5개 내보내는 `Flow<T>`를 반환한다. `produces`를 지정하지 않았을 때와 `text/event-stream`일 때, 클라이언트는 각각 언제 첫 바이트를 받을까?
- 클라이언트가 SSE 연결을 끊으면 서버의 Flow 수집은 멈출까? 멈춘다면 끊은 뒤 몇 초 안에 멈출까?
- 동시 SSE 연결 1,000개를 열어두면 Tomcat 스레드는 몇 개 쓰일까? 무엇이 먼저 한계에 닿을까?

**실험 과제**
1. 3-5의 주문 상태 브로드캐스트를 SSE 엔드포인트로 노출한다.
2. curl로 연결했다가 끊는다. 서버 쪽 `onCompletion` 로그를 확인한다.
3. 동시 SSE 연결 수를 늘리면서 스레드 사용량을 관찰한다.

**완료 조건**
- [ ] SSE 동작과 연결 종료 시 취소 전파 확인
- [ ] 동시 연결 수에 따른 스레드 사용량 기록

---

## 4-9. Spring 통합 지점과 테스트 `핵심`

**목표**: Spring은 컨트롤러 밖에서도 suspend 함수를 다룬다(스케줄링, 비동기, 이벤트). 이 지점들의 동작을 확인하고 코루틴 코드를 **Spring 레벨에서** 테스트하는 방법도 익힌다. 1-4가 순수 코루틴 테스트였다면, 이 장은 실무 코드베이스의 테스트를 다룬다.

**핵심 개념**
- `@Scheduled`, `@Async`, `@EventListener`는 모두 Spring이 빈 후처리기나 프록시로 메서드를 찾아 감싸는 방식이다. suspend 지원 여부는 감싸는 쪽이 메서드 시그니처와 반환 타입을 어떻게 다루느냐에 달려 있다.
- 일반 `@Scheduled`는 `TaskScheduler`에서 실행된다(Boot 기본은 스레드 1개). `fixedDelay`는 이전 실행이 **끝난** 시점부터, `fixedRate`는 이전 실행이 **시작된** 시점부터 간격을 잰다.
- `@Async`는 호출을 `TaskExecutor`에 넘기고 즉시 반환한다. 호출자가 결과를 받으려면 반환 타입이 `CompletableFuture` 같은 `Future` 계열이어야 한다.
- `@EventListener`는 기본적으로 이벤트를 발행한 스레드에서 동기로 호출된다. `@TransactionalEventListener`는 발행한 쪽 트랜잭션의 특정 단계(기본 `AFTER_COMMIT`)에 맞춰 호출된다.
- `runTest`는 가상 시간을 쓰는 `TestDispatcher` 위에서 돈다. 코드가 디스패처를 직접 지정해버리면 그 부분에는 가상 시간이 적용되지 않는다. 그래서 디스패처를 주입받는 구조가 필요하다(1-4). MockK에서 suspend 함수를 다룰 때는 `coEvery`, `coVerify`를 쓴다.

**실행 전 예측**
- `@Scheduled`를 `suspend fun`에 붙이면 동작할까? 동작한다면 어느 스레드에서 실행될까? 이전 실행이 끝나지 않았을 때 다음 실행은 어떻게 될까?
- `@Async`를 `suspend fun`에 붙이면?
- `@EventListener`가 붙은 `suspend fun`은 이벤트를 발행한 쪽의 트랜잭션이나 스레드와 어떤 관계일까?
- MockMvc로 suspend 컨트롤러를 테스트하면, 일반 컨트롤러와 똑같이 응답 본문을 검증할 수 있을까?

**실험 과제**
1. `@Scheduled`, `@Async`, `@EventListener`를 각각 `suspend fun`에 붙인다. 동작 여부, 실행 스레드, 예외 처리 방식을 확인한다.
2. suspend 컨트롤러를 MockMvc로 테스트한다. 4-1에서 확인한 "비동기 요청 처리"가 테스트 코드에 어떤 영향을 주는지 확인한다.
3. suspend 의존성을 MockK(`coEvery`, `coVerify`)로 대체하는 서비스 테스트를 작성한다.
4. 1-4의 디스패처 주입 패턴을 Spring 빈으로 옮긴다. 테스트에서는 `TestDispatcher`로 바꿔서 가상 시간으로 검증한다.

**완료 조건**
- [ ] 통합 지점별 동작 정리(지원 여부, 실행 스레드, 예외 처리)
- [ ] suspend 컨트롤러와 서비스의 Spring 레벨 테스트
- [ ] 디스패처를 빈으로 주입하고 테스트에서 교체하는 패턴

**공식 문서**
- [Spring, Coroutines](https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html)
- [MockK, Coroutines](https://mockk.io/#coroutines)

**판단 가이드에 남길 것**: "코루틴 코드를 테스트 가능하게 설계하는 규칙"
