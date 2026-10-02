# Part 4. Spring MVC + 코루틴

> 모듈: `mvc` (+ `stub-server`)
>
> 실무와 가장 가까운 Part다. 공통 시나리오(주문 상세 조회)로 "Tomcat 위에서 코루틴을 쓰면 무엇을 얻고 무엇을 얻지 못하는가"를 확인한다.
> 처리량 실험은 모두 0-6의 **기준선**과 비교한다.

---

## 4-1. suspend 컨트롤러는 어느 스레드에서 도는가 `핵심`

**목표**: MVC가 `suspend fun` 핸들러를 어떻게 처리하는지, 그 방식이 Tomcat 스레드에 어떤 영향을 주는지 확인한다.

**실행 전 예측**
- 지금 mvc 모듈의 의존성 그대로 `suspend fun` 컨트롤러를 만들면 동작할까? 동작하지 않는다면 무엇이 빠졌을까?
- 핸들러에서 `delay(1000)`하는 동안 Tomcat 요청 스레드는 붙잡혀 있을까, 반환될까?
- 핸들러에서 `Thread.sleep(1000)`을 하면 결과가 달라질까?
- 0-6과 같은 조건(`threads.max=20`, 동시 사용자 100명)에서 `delay` 버전의 TPS는 기준선보다 높을까?

**실험 과제**
1. `suspend` 컨트롤러를 만들고 필요한 의존성을 찾는다. 그 의존성이 **왜** 필요한지 Spring 소스에서 찾아본다.
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

| 방식 | 블로킹 여부 | 코루틴과 함께 쓰는 법 |
|---|---|---|
| OpenFeign | 블로킹 | ? |
| `@HttpExchange` + RestClient 백엔드 | 블로킹 | ? |
| `@HttpExchange` + WebClient 백엔드 | 논블로킹 | ? |

**실행 전 예측**
- Feign 호출 3개를 `async`로 감싸 병렬화하면 어떤 디스패처에서 돌려야 할까? 이유는?
- `@HttpExchange` 인터페이스에 `suspend fun`을 선언하면 어떻게 동작할까? 백엔드가 RestClient일 때와 WebClient일 때 각각 예측해보자.
- 세 방식으로 병렬 호출할 때, 요청 1건을 처리하는 동안 스레드를 몇 개 점유할까?

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

**실행 전 예측**
- 결제 API가 실패하면 진행 중이던 배송, 쿠폰 호출은 취소될까? 실제 HTTP 커넥션까지 끊길까? 방식(Feign, RestClient, WebClient)마다 다를까?
- `withTimeout(1000)` 안에서 Feign으로 지연 5초짜리 API를 호출하면 1초 만에 반환될까?
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
- 블로킹 클라이언트에서는 "취소"를 믿을 수 없다. 그렇다면 무엇으로 시간 상한을 보장해야 할까?

---

## 4-4. 안티패턴 실험실 `핵심`

**목표**: 운영에서 자주 터지는 코루틴 안티패턴을 일부러 재현한다. 각 안티패턴이 Grafana와 스레드 덤프에서 어떻게 보이는지 기록한다.

**실행 전 예측** (항목마다 답한다)
- 요청 스레드에서 `runBlocking`으로 외부 API 3개를 병렬 호출하면, 처리량은 기준선과 비교해 어떨까?
- 블로킹 호출(지연 1초)을 `Dispatchers.IO`에서 실행하고 동시 요청을 200개 넣으면 p99는 얼마일까?
- `Dispatchers.Default`에서 블로킹 호출을 하면 같은 서버의 다른 CPU 작업은 어떻게 될까?
- 요청을 처리하다가 `GlobalScope.launch`로 후처리를 띄우면, 서버를 종료할 때 그 작업은 어떻게 될까?

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

**실행 전 예측**
- Hikari 풀이 10개다. `withContext(Dispatchers.IO)`에서 동시에 쿼리 200개를 날리면 IO 스레드는 몇 개 쓰일까? 그중 몇 개가 커넥션을 기다릴까?
- `SELECT SLEEP(1)` 쿼리를 실행 중인 코루틴을 취소하면 DB에서 쿼리가 멈출까?
- `Dispatchers.IO.limitedParallelism(10)`처럼 풀 크기에 맞춘 전용 디스패처를 쓰면 무엇이 달라질까?

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

**실행 전 예측**
- JPA(`PlatformTransactionManager`) 환경에서 `suspend fun`에 `@Transactional`을 붙이면 어떻게 될까?
- 일반 `@Transactional fun` 안에서 `runBlocking { withContext(Dispatchers.IO) { repository.save(...) } }`를 하면, save는 같은 트랜잭션에 참여할까? 예외가 나면 롤백될까?
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
- [about-transaction: 트랜잭션 동기화](../../../about-transaction/README.md)

**판단 가이드에 남길 것**: "코루틴과 JPA 트랜잭션을 함께 쓰는 규칙"

---

## 4-7. 컨텍스트 전파: MDC, Tracing, Security `핵심`

**목표**: 스레드가 바뀌면 컨텍스트가 사라진다(0-5). 코루틴에서 이 컨텍스트를 올바르게 전파하는 방법을 익힌다.

**실행 전 예측**
- 필터에서 MDC에 `traceId`를 넣었다. `withContext(Dispatchers.IO)` 안에서 찍은 로그에 traceId가 나올까?
- `async` 블록 안에서 `SecurityContextHolder.getContext()`를 호출하면?
- `MDCContext()`를 쓴 뒤 코루틴 안에서 MDC 값을 바꾸면, 중단 이후에도 바뀐 값이 유지될까?

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

**실행 전 예측**
- MVC 컨트롤러가 `Flow<T>`를 반환하면 응답은 JSON 배열일까, 스트림일까? `produces`에 따라 달라질까?
- 클라이언트가 SSE 연결을 끊으면 서버의 Flow 수집은 멈출까?

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
