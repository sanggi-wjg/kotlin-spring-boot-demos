# D. Spring과 전체 그림

A ~ C에서 익힌 코루틴을 Spring MVC 서비스에 넣어보고, 스레드, 코루틴, Virtual Thread, WebFlux를 한 장의 그림으로 정리한다.

[← 1단계 목차](./README.md)

---

## D-1. Spring MVC에서 코루틴

### 이 장에서 답할 질문

- Spring MVC 컨트롤러에 `suspend`를 붙이면 무엇이 달라지나?
- 외부 API 3개를 병렬로 호출하려면 어떻게 쓰나? 어떤 HTTP 클라이언트를 골라야 하나?
- JPA처럼 블로킹인 코드는 코루틴 안에서 어떻게 다루나?

### 개념

**1. suspend 컨트롤러는 "요청 스레드를 돌려주는" 컨트롤러다**

일반 MVC 컨트롤러는 응답을 만들 때까지 Tomcat 요청 스레드를 붙잡는다(A-1). 외부 API를 기다리는 동안에도 마찬가지다.

컨트롤러 메서드에 `suspend`를 붙이면 Spring은 이 메서드를 코루틴으로 실행한다. 그리고 그 결과를 리액티브 타입(`Mono`)으로 바꿔 서블릿 **비동기 처리**에 맡긴다. 그래서 코루틴이 중단된 동안 Tomcat 요청 스레드는 풀로 돌아가 다른 요청을 받는다.

```
일반 컨트롤러
  http-nio-exec-1: [요청 받음][====== 외부 API 대기 1초 (스레드 점유) ======][응답]

suspend 컨트롤러
  http-nio-exec-1: [요청 받음][중단] → 풀로 반납, 다른 요청 처리
  (외부 API 응답이 오면)        ... 코루틴 재개 [결과 완성]
  http-nio-exec-7: [응답 작성 (async dispatch)]
```

코루틴이 결과를 다 만들면 Spring MVC는 서블릿 **async dispatch**로 요청을 다시 디스패처에 넘기고, 응답은 Tomcat 스레드(`http-nio-8080-exec-N`)가 쓴다. 코루틴이 재개된 스레드와 응답을 쓰는 스레드는 다를 수 있다.

이 지원을 켜려면 클래스패스에 `kotlinx-coroutines-reactor`가 있어야 한다. Spring이 `suspend` 함수를 `Mono`로 바꿀 때 이 라이브러리를 쓰기 때문이다.

```kotlin
// mvc/build.gradle.kts
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
}
```

> ⚠ 이 프로젝트의 `mvc` 모듈에는 이 의존성을 **일부러** 넣지 않았다. 2단계 [4-1](../2-advanced/part-4-mvc-coroutine.md#4-1-suspend-컨트롤러는-어느-스레드에서-도는가-핵심)에서 "의존성이 없으면 어떻게 되나"를 실험하기 때문이다. D-1 예제를 돌릴 때는 의존성을 추가하고, 패키지를 `...mvc.guide.d1`처럼 따로 둔다. **D-1을 마치면 `guide.d1` 패키지와 추가한 의존성(아래 예제의 4줄)을 모두 지운다.** suspend 컨트롤러나 WebClient 클라이언트가 남아 있으면 4-1(의존성 없이 무엇이 깨지나)과 4-2(클라이언트 비교의 출발점) 실험이 오염된다. 임시 브랜치에서 실습하고 버리는 방법도 좋다.

**2. 병렬 호출은 B-2의 `coroutineScope` + `async` 그대로다**

공통 시나리오 "주문 상세 조회"는 결제(300ms), 배송(500ms), 쿠폰(200ms)을 호출해 합친다. 순차로 부르면 약 1,000ms, 병렬로 부르면 가장 느린 배송에 맞춰 약 500ms가 걸린다. 병렬 호출 코드는 B-2에서 본 형태와 같다. `coroutineScope` 안에서 `async` 세 개를 띄우고 `await`한다. 하나가 실패하면 나머지가 취소되는 것도 B-6에서 본 그대로다.

**3. HTTP 클라이언트는 "suspend로 기다릴 수 있는가"로 고른다**

코루틴의 장점은 **기다리는 동안 스레드를 놓아주는 것**이다. 그런데 클라이언트가 블로킹이면, 그 클라이언트를 부르는 동안 스레드가 붙잡힌다.

| 클라이언트 | 방식 | `suspend` 함수로 선언 | 코루틴에서 쓰는 법 |
|---|---|---|---|
| `@HttpExchange` + **WebClient** 어댑터 | 논블로킹 | ✅ 가능 | 그대로 `suspend` 호출 |
| `@HttpExchange` + **RestClient** 어댑터 | 블로킹 | ❌ 불가(프록시를 만들 때 `IllegalStateException`) | 일반 함수로 선언하고 `withContext(Dispatchers.IO)`로 감싼다 |
| Spring Cloud OpenFeign | 블로킹 | ❌ 공식 지원 없음(프로젝트는 feature-complete 상태) | `withContext(Dispatchers.IO)`로 감싼다 |

- RestClient 어댑터로 `suspend` 함수가 있는 인터페이스를 만들면 애플리케이션이 **시작할 때** `Kotlin Coroutines are only supported with reactive implementations` 예외가 난다.
- Feign 코어에는 코루틴용 모듈(`feign-kotlin`)이 있지만, Spring Cloud OpenFeign은 이를 공식 지원하지 않는다. 커뮤니티 스타터가 있을 뿐이다.
- WebClient를 쓰려면 `spring-boot-starter-webclient`를 추가한다. MVC 애플리케이션에 추가해도 서버는 그대로 Tomcat이다.

**4. 블로킹 코드(JPA, JDBC, 블로킹 클라이언트)는 `Dispatchers.IO`로 보낸다**

JPA는 JDBC 위에서 동작하고, JDBC는 블로킹이다. 코루틴 안에서 그대로 부르면 그 코루틴을 실행하던 스레드가 DB 응답까지 붙잡힌다. 그래서 B-4에서 본 대로 `withContext(Dispatchers.IO) { ... }`로 블로킹용 디스패처에 보낸다. `Dispatchers.IO`는 Default와 스레드 풀을 공유하지만, 블로킹을 감안해 동시 실행 한도가 크다(기본 64).

**5. 코루틴이 재개되는 스레드는 요청 스레드가 아닐 수 있다**

Spring MVC는 `suspend` 컨트롤러를 특정 스레드에 묶지 않고(`Dispatchers.Unconfined`) 실행한다. 그래서 중단 후에는 **코루틴을 깨운 쪽의 스레드**에서 이어서 실행된다. WebClient로 기다렸다면 WebClient의 이벤트 루프 스레드(`reactor-http-nio-N`), `withContext(Dispatchers.IO)`를 지났다면 IO 스레드다. 아래 예제의 로그로 직접 확인한다. 이 차이가 2단계에서 ThreadLocal(MDC, 트랜잭션) 문제로 이어진다.

같은 이유로, WebClient 호출 뒤에 이어지는 컨트롤러 코드는 **WebClient의 이벤트 루프 스레드 위에서** 돈다. 그 자리에서 무거운 CPU 작업이나 블로킹 호출을 하면 이벤트 루프가 막혀, 같은 루프를 쓰는 다른 HTTP 호출까지 함께 느려진다. 재개 후 무거운 작업이 있다면 `withContext`로 알맞은 디스패처에 보낸다.

### 예제

> 외부 API 대신 같은 애플리케이션 안에 "가짜 외부 API" 컨트롤러를 둔다. 2단계에서는 이 역할을 `stub-server`가 맡는다.

**의존성**

```kotlin
// mvc/build.gradle.kts (D-1 실습용)
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-webclient")   // WebClient
    implementation("org.springframework.boot:spring-boot-starter-restclient")  // RestClient (블로킹 비교용)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")         // suspend 컨트롤러
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
}
```

**가짜 외부 API와 클라이언트**

```kotlin
package com.raynor.demo.aboutcoroutine.mvc.guide.d1

import kotlinx.coroutines.delay
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory

data class Payment(val orderId: Long, val amount: Int)
data class Delivery(val orderId: Long, val status: String)
data class Coupon(val orderId: Long, val discount: Int)
data class OrderDetail(val orderId: Long, val payment: Payment, val delivery: Delivery, val coupon: Coupon)

// 가짜 외부 API: 지연만 흉내 낸다
@RestController
class FakeExternalApiController {
    @GetMapping("/fake/payments/{orderId}")
    suspend fun payment(@PathVariable orderId: Long, @RequestParam(name = "delay", defaultValue = "300") delayMs: Long): Payment {
        delay(delayMs)
        return Payment(orderId, 10_000)
    }

    @GetMapping("/fake/deliveries/{orderId}")
    suspend fun delivery(@PathVariable orderId: Long, @RequestParam(name = "delay", defaultValue = "500") delayMs: Long): Delivery {
        delay(delayMs)
        return Delivery(orderId, "SHIPPING")
    }

    @GetMapping("/fake/coupons/{orderId}")
    suspend fun coupon(@PathVariable orderId: Long, @RequestParam(name = "delay", defaultValue = "200") delayMs: Long): Coupon {
        delay(delayMs)
        return Coupon(orderId, 1_000)
    }
}

// 논블로킹 클라이언트: suspend 함수로 선언한다
@HttpExchange("/fake")
interface ExternalApiClient {
    @GetExchange("/payments/{orderId}")
    suspend fun payment(@PathVariable orderId: Long): Payment

    @GetExchange("/deliveries/{orderId}")
    suspend fun delivery(@PathVariable orderId: Long): Delivery

    @GetExchange("/coupons/{orderId}")
    suspend fun coupon(@PathVariable orderId: Long): Coupon
}

// 블로킹 클라이언트: 일반 함수로 선언한다
@HttpExchange("/fake")
interface BlockingExternalApiClient {
    @GetExchange("/payments/{orderId}")
    fun payment(@PathVariable orderId: Long): Payment
}

@Configuration
class ClientConfig {
    @Bean
    fun externalApiClient(builder: WebClient.Builder): ExternalApiClient {
        val webClient = builder.baseUrl("http://localhost:8080").build()
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build()
            .createClient(ExternalApiClient::class.java)
    }

    @Bean
    fun blockingExternalApiClient(builder: RestClient.Builder): BlockingExternalApiClient {
        val restClient = builder.baseUrl("http://localhost:8080").build()
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build()
            .createClient(BlockingExternalApiClient::class.java)
    }
}
```

**suspend 컨트롤러: 병렬, 순차, 블로킹 호출**

```kotlin
package com.raynor.demo.aboutcoroutine.mvc.guide.d1

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import kotlin.time.measureTimedValue

private val log = LoggerFactory.getLogger("d1")

@RestController
class OrderController(
    private val client: ExternalApiClient,
    private val blockingClient: BlockingExternalApiClient,
) {
    // 병렬: 가장 느린 호출(500ms)만큼 걸린다
    @GetMapping("/orders/{id}/detail")
    suspend fun detail(@PathVariable id: Long): OrderDetail {
        log.info("시작: {}", Thread.currentThread().name)
        val (detail, elapsed) = measureTimedValue {
            coroutineScope {
                val payment = async { client.payment(id) }
                val delivery = async { client.delivery(id) }
                val coupon = async { client.coupon(id) }
                OrderDetail(id, payment.await(), delivery.await(), coupon.await())
            }
        }
        log.info("끝: {} ({})", Thread.currentThread().name, elapsed)
        return detail
    }

    // 순차: 세 호출의 합(1,000ms)만큼 걸린다
    @GetMapping("/orders/{id}/detail-sequential")
    suspend fun detailSequential(@PathVariable id: Long): OrderDetail {
        val (detail, elapsed) = measureTimedValue {
            OrderDetail(id, client.payment(id), client.delivery(id), client.coupon(id))
        }
        log.info("순차 끝: {}", elapsed)
        return detail
    }

    // 블로킹 클라이언트는 IO 디스패처로 보낸다
    @GetMapping("/orders/{id}/payment-blocking")
    suspend fun paymentBlocking(@PathVariable id: Long): Payment {
        val payment = withContext(Dispatchers.IO) {
            log.info("블로킹 호출: {}", Thread.currentThread().name)
            blockingClient.payment(id)
        }
        log.info("블로킹 이후: {}", Thread.currentThread().name)
        return payment
    }
}
```

**실행**

```
curl localhost:8080/orders/1/detail
curl localhost:8080/orders/1/detail-sequential
curl localhost:8080/orders/1/payment-blocking
```

**출력 예** (첫 요청은 워밍업 때문에 조금 더 느리다)

```
[nio-8080-exec-3] 시작: http-nio-8080-exec-3
[ctor-http-nio-2] 끝: reactor-http-nio-2 (514.889542ms)
[ctor-http-nio-2] 순차 끝: 1.030303709s
[atcher-worker-1] 블로킹 호출: DefaultDispatcher-worker-1
[atcher-worker-1] 블로킹 이후: DefaultDispatcher-worker-1
```

읽는 법:
- 병렬은 약 500ms, 순차는 약 1,000ms다.
- `시작`은 Tomcat 요청 스레드(`http-nio-8080-exec-N`)인데, `끝`은 WebClient의 이벤트 루프 스레드(`reactor-http-nio-N`)다. 개념 5번에서 말한 "깨운 쪽 스레드에서 재개"가 이것이다.
- `withContext(Dispatchers.IO)`를 빠져나온 뒤에도 IO 스레드에 그대로 남아 있다. 이것도 같은 이유다.
- 응답을 쓰는 것은 다시 Tomcat 스레드다(개념 1번의 async dispatch). 필터에서 `request.dispatcherType`과 스레드 이름을 찍어보면 `REQUEST`는 `http-nio-8080-exec-2`, `ASYNC`는 `http-nio-8080-exec-9`처럼 서로 다른 Tomcat 스레드로 찍힌다.

### ❌/✅ 함정

**1. 컨트롤러에서 `runBlocking`**

```kotlin
// ❌ 요청 스레드를 붙잡고 코루틴이 끝날 때까지 기다린다. 코루틴을 쓰는 의미가 없다.
@GetMapping("/orders/{id}/detail")
fun detail(@PathVariable id: Long): OrderDetail = runBlocking {
    OrderDetail(id, client.payment(id), client.delivery(id), client.coupon(id))
}

// ✅ 컨트롤러 자체를 suspend로 만든다.
@GetMapping("/orders/{id}/detail")
suspend fun detail(@PathVariable id: Long): OrderDetail = coroutineScope { /* async ... */ }
```

`runBlocking`은 `main`과 테스트처럼 "코루틴 세계의 입구"에서만 쓴다(B-1).

**2. RestClient 어댑터에 `suspend` 선언**

```kotlin
// ❌ 애플리케이션 시작 시 IllegalStateException:
//    Kotlin Coroutines are only supported with reactive implementations
@HttpExchange("/fake")
interface PaymentClient {
    @GetExchange("/payments/{orderId}")
    suspend fun payment(@PathVariable orderId: Long): Payment
}
val client = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build()
    .createClient(PaymentClient::class.java)

// ✅ suspend가 필요하면 WebClientAdapter를 쓴다.
// ✅ RestClient를 유지하려면 일반 함수로 선언하고 withContext(Dispatchers.IO)로 감싼다.
```

**3. 블로킹 호출을 그냥 `async`로 감싸기**

```kotlin
// ❌ 블로킹 클라이언트를 async로 감싸도, 그 블로킹 동안 실행 스레드가 붙잡힌다.
//    Unconfined로 시작했다면 요청 스레드나 이벤트 루프 스레드를 막을 수도 있다.
coroutineScope {
    val payment = async { blockingClient.payment(id) }
    // ...
}

// ✅ 블로킹 호출은 IO 디스패처로 보낸다.
coroutineScope {
    val payment = async(Dispatchers.IO) { blockingClient.payment(id) }
    // ...
}
```

**4. JPA 리포지토리를 그대로 호출하고, `@Transactional`을 `suspend fun`에 붙이기**

```kotlin
// ❌ JDBC 블로킹이 코루틴 스레드를 막는다.
suspend fun findOrder(id: Long): Order = orderRepository.findById(id).orElseThrow()

// ✅ 블로킹 구간을 IO 디스패처로 보낸다.
suspend fun findOrder(id: Long): Order = withContext(Dispatchers.IO) {
    orderRepository.findById(id).orElseThrow()
}
```

> ⚠ `@Transactional`을 `suspend fun`에 붙이면 **기대한 대로 동작하지 않는다.** 트랜잭션 프록시는 메서드가 반환될 때 트랜잭션을 닫는데, `suspend fun`은 첫 중단 지점에서 일단 "반환"된다. JPA용 트랜잭션 매니저는 suspend 함수가 실제로 끝날 때까지 기다려주지 못해서, 트랜잭션 경계가 메서드 본문과 어긋날 수 있다. 스레드가 바뀌지 않아도 생기는 문제이고, 여기에 JPA 트랜잭션이 ThreadLocal에 묶여 있다는 문제(개념 5번)까지 겹친다. 정확히 어떻게 깨지는지는 2단계 [4-6](../2-advanced/part-4-mvc-coroutine.md#4-6-transactional과-코루틴-핵심)에서 직접 확인한다. 그때까지는 **트랜잭션 경계를 일반(non-suspend) 함수에 두고, 그 함수를 `withContext(Dispatchers.IO)` 안에서 부른다**고 기억해둔다.

### 바꿔보기

각 과제를 실행하기 전에 결과를 한 줄로 예상해 적는다.

1. 가짜 배송 API의 기본 지연을 500ms에서 2,000ms로 바꾼다. `/detail`과 `/detail-sequential`은 각각 얼마나 걸릴까?
2. `/detail`의 `async` 중 하나(쿠폰)에서 `throw IllegalStateException("쿠폰 장애")`를 던지게 바꾼다. 응답 상태 코드는 무엇일까? 배송 `async` 블록에 `try/finally` 로그를 넣어, 진행 중이던 배송 호출 코루틴이 어떻게 끝났는지(정상 완료인지 취소인지, 몇 ms 시점인지) 로그로 확인한다.
3. 새 엔드포인트를 하나 만든다. 먼저 `client.coupon(id)`(WebClient, suspend)를 호출하고, 그다음 `withContext` 없이 `blockingClient.payment(id)`(RestClient, 블로킹)를 호출한다. 블로킹 호출 직전에 찍은 스레드 이름은 무엇일까? 응답은 정상으로 올까?

### 정리

- `suspend` 컨트롤러는 기다리는 동안 Tomcat 요청 스레드를 풀에 돌려준다. `kotlinx-coroutines-reactor`가 필요하다.
- 코루틴의 이득은 클라이언트가 논블로킹일 때 나온다. `@HttpExchange`는 WebClient 어댑터에서만 `suspend`를 쓸 수 있고, 블로킹 클라이언트와 JPA는 `withContext(Dispatchers.IO)`로 보낸다.
- 재개되는 스레드는 요청 스레드가 아닐 수 있다. ThreadLocal에 기대는 기능(MDC, `@Transactional`)은 그대로 믿으면 안 된다.

**실무 연결**: 기존 MVC 서비스에서 코루틴을 도입할 때 가장 효과가 큰 곳은 "외부 API 여러 개를 병렬로 부르는 조회"이고, 가장 조심할 곳은 JPA와 트랜잭션이다.

→ 2단계: [Part 4. MVC + 코루틴](../2-advanced/part-4-mvc-coroutine.md) ([4-1](../2-advanced/part-4-mvc-coroutine.md#4-1-suspend-컨트롤러는-어느-스레드에서-도는가-핵심) suspend 컨트롤러의 정체, [4-2](../2-advanced/part-4-mvc-coroutine.md#4-2-외부-api-병렬-호출-feign-vs-httpexchange-핵심) 외부 API, [4-5](../2-advanced/part-4-mvc-coroutine.md#4-5-jpa와-코루틴-핵심) JPA, [4-6](../2-advanced/part-4-mvc-coroutine.md#4-6-transactional과-코루틴-핵심) 트랜잭션, [4-7](../2-advanced/part-4-mvc-coroutine.md#4-7-컨텍스트-전파-mdc-tracing-security-핵심) 컨텍스트 전파)

---

## D-2. 전체 그림: 스레드, 코루틴, VT, WebFlux

### 이 장에서 답할 질문

- 스레드, 코루틴, Virtual Thread, WebFlux는 각각 "기다림"을 어떻게 처리하나?
- 기존 MVC + JPA 서비스에 적용할 때 각각 무엇을 바꿔야 하나?
- 2단계에서 무엇을 측정해서 판단해야 하나?

### 개념

**한 질문으로 정리하면: "기다리는 동안 무엇이 스레드를 붙잡고 있나?"**

외부 API를 1초 기다리는 요청 하나를 네 방식으로 처리하면 다음과 같다.

```
1) 플랫폼 스레드 (thread-per-request, MVC 기본)
   http-nio-exec-1  [요청][========== 1초 대기: OS 스레드 점유 ==========][응답]
   → 기다리는 동안 OS 스레드가 붙잡힌다. 동시 요청 수 ≈ 스레드 수(threads.max)

2) MVC + 코루틴
   http-nio-exec-1  [요청][중단] ── 풀로 반납
   (대기 중)         코루틴 상태(Continuation)만 힙에 남는다
   reactor-http-nio  ................................[재개][결과 완성]
   http-nio-exec-7  ..........................................[응답 작성(async dispatch)]
   → 기다리는 동안 붙잡힌 스레드가 없다. 단, 클라이언트가 논블로킹일 때만

3) MVC + Virtual Thread
   VT#1234          [요청][========== 1초 대기 (블로킹 코드 그대로) ==========][응답]
   carrier thread   [실행][unmount ─ 다른 VT 실행 ─────────────────][mount][실행]
   → 코드는 블로킹 그대로. JVM이 기다리는 VT를 carrier thread에서 내려놓는다

4) WebFlux + 코루틴
   event loop       [요청][중단] ── 다른 연결 처리 ──────────────[재개][응답]
   → 소수의 이벤트 루프 스레드가 모든 연결을 처리한다. 블로킹 호출 하나가 루프 전체를 멈춘다
```

**코루틴과 Virtual Thread는 같은 문제를 다른 층에서 푼다**

| | 코루틴 | Virtual Thread |
|---|---|---|
| 가벼운 실행 단위를 누가 만드나 | Kotlin 컴파일러(상태 머신, B-3) + 라이브러리 | JVM (JEP 444, Java 21 정식) |
| 기다림 표시 | `suspend` 함수만 중단 가능. 블로킹 함수는 여전히 스레드를 막는다 | 일반 블로킹 코드(`Thread.sleep`, JDBC)가 그대로 "중단점"이 된다 |
| 코드 변경 | `suspend` 전파, 논블로킹 클라이언트, 디스패처 관리 | 거의 없음. Spring Boot에서는 `spring.threads.virtual.enabled=true` |
| 구조화된 동시성, 취소, 타임아웃 | 언어 차원에서 기본 제공(B-2, B-5) | `StructuredTaskScope`는 Java 25에서도 아직 preview |
| 함정 | 블로킹 호출 섞임, ThreadLocal 전파, `runBlocking` | pinning(JDK 24의 JEP 491로 `synchronized` 문제는 대부분 해소), 풀 크기를 믿던 코드의 동시성 폭증 |

**네 방식 비교**

| | 플랫폼 스레드 | MVC + 코루틴 | MVC + VT | WebFlux + 코루틴 |
|---|---|---|---|---|
| 대기 중 붙잡히는 것 | OS 스레드 | 없음(논블로킹 클라이언트일 때) | 가상 스레드만(carrier는 반납) | 없음 |
| 기존 코드 변경 비용 | 없음 | 중간: 컨트롤러, 클라이언트, 블로킹 구간 분리 | 낮음: 설정 한 줄 + pinning 점검 | 높음: 서버, 클라이언트, DB 접근(R2DBC)까지 |
| JPA/JDBC와의 궁합 | 그대로 | `Dispatchers.IO`로 격리, 트랜잭션 주의 | 그대로 | 맞지 않음. R2DBC로 바꿔야 한다 |
| 병렬 호출 표현 | `CompletableFuture`, 스레드풀 | `async`/`await` | executor 또는 preview API | `async`/`await` |
| 취소 전파 | 어렵다(A-2) | 쉽다 | 인터럽트 기반 | 쉽다 |
| 디버깅 | 스택 트레이스 그대로 | 스택이 끊긴다. 코루틴 디버거 필요 | 스택 트레이스 그대로 | 스택이 끊긴다 |
| 처리량 한계를 정하는 것 | 스레드 수 | DB 풀, 외부 API, CPU, IO 디스패처 크기(JPA를 IO로 보낼 때 기본 64) | DB 풀, 외부 API, CPU | DB 풀, 외부 API, CPU |

마지막 줄이 중요하다. 스레드라는 한계를 없애면, **그다음으로 좁은 자원**(DB 커넥션 풀, 외부 API의 처리량)이 한계가 된다. 어떤 방식이 "빠르다"는 말은 어느 자원이 병목인지와 함께 말해야 한다.

### 예제

작업 1만 개가 각각 100ms씩 기다릴 때, 세 방식의 총 소요 시간을 비교한다. `basics` 모듈의 테스트로 실행한다.

```kotlin
package com.raynor.demo.aboutcoroutine.basics.guide.d2

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.time.measureTime

class ThreeModelsTest {
    @Test
    fun `작업 1만 개가 100ms씩 기다린다`() {
        val tasks = 10_000

        val platform = measureTime {
            Executors.newFixedThreadPool(200).use { pool ->          // Tomcat 기본 threads.max와 같은 200
                repeat(tasks) { pool.submit { Thread.sleep(100) } }
            }
        }
        println("플랫폼 스레드 풀(200개): $platform")

        val platformPerTask = measureTime {
            Executors.newThreadPerTaskExecutor(Thread.ofPlatform().factory()).use { executor ->
                repeat(tasks) { executor.submit { Thread.sleep(100) } }  // 작업마다 플랫폼 스레드 1개
            }
        }
        println("플랫폼 스레드(작업마다 1개): $platformPerTask")

        val virtual = measureTime {
            Executors.newVirtualThreadPerTaskExecutor().use { executor ->
                repeat(tasks) { executor.submit { Thread.sleep(100) } }  // 블로킹 코드 그대로
            }
        }
        println("Virtual Thread: $virtual")

        val coroutine = measureTime {
            runBlocking {
                repeat(tasks) { launch { delay(100) } }               // 스레드 1개
            }
        }
        println("코루틴: $coroutine")
    }
}
```

`ExecutorService.use { }`는 블록이 끝날 때 `close()`를 호출하고, `close()`는 제출한 작업이 모두 끝날 때까지 기다린다.

**출력 예** (값은 환경마다 다르다)

```
플랫폼 스레드 풀(200개): 5.23s
플랫폼 스레드(작업마다 1개): 730ms
Virtual Thread: 147ms
코루틴: 183ms
```

- 플랫폼 스레드 풀은 한 번에 200개씩만 처리한다. 10,000 / 200 × 100ms = 약 5초다. [A-1](./a-why-coroutine.md#a-1-스레드-모델과-블로킹의-비용)의 Little's Law와 같은 계산이다. 이 5초는 "스레드가 비싸서"가 아니라 **동시 실행 상한이 200**이라서 나온 숫자다.
- 상한을 없애고 작업마다 플랫폼 스레드를 하나씩 만들면 730ms로 줄어든다. 대신 OS 스레드를 수천 개 만들고 없애는 비용(A-1의 생성 시간과 메모리, OS 스레드 수 한도)이 시간에 그대로 더해진다. 플랫폼 스레드는 "많이 만들 수는 있지만 비싸다"는 것이 이 줄이 보여주는 것이다.
- VT와 코루틴은 1만 개가 거의 동시에 기다린다. 그래서 약 100ms에 오버헤드가 조금 더해진다. 두 값의 차이는 측정 순서, JIT 워밍업, 그리고 Gradle 테스트가 `-ea`로 켜는 코루틴 디버그 모드(B-4)의 영향을 받는다. "VT가 코루틴보다 빠르다"는 결론을 내릴 근거는 아니다.
- 이 예제는 "기다리기만 하는" 작업이다. CPU를 쓰는 작업이라면 어느 방식이든 코어 수를 넘어설 수 없다.

### ❌/✅ 함정

**1. "코루틴(또는 VT)을 쓰면 빨라진다"**

```
❌ 개별 요청의 응답 시간이 줄어든다
✅ 부하가 없을 때 개별 요청의 응답 시간은 같다. 같은 하드웨어로 동시에 기다릴 수 있는 요청 수가 늘어난다.
   - 순차 호출을 병렬 호출로 바꾸면 개별 요청도 빨라진다(D-1).
   - 부하가 몰려 스레드 풀이 가득 찬 상태라면, 요청이 스레드를 기다리며 줄 서던 시간이 사라져 p99 같은 꼬리 지연이 준다(2단계 5-2에서 측정한다).
```

**2. 병목을 보지 않고 방식을 고르기**

```
❌ DB 커넥션 풀이 10개인데 동시 요청 5,000개를 코루틴으로 받는다
   → 4,990개는 커넥션을 기다린다. 스레드 대신 커넥션 풀 앞에 줄이 선다.
✅ 먼저 무엇이 병목인지(스레드, DB 풀, 외부 API, CPU) 측정하고 고른다.
```

**3. 코루틴과 VT를 섞으면 무조건 더 좋다고 생각하기**

```
❌ 둘 다 쓰면 장점만 합쳐진다
✅ 해결하는 층이 같아서 이득이 겹친다. 섞을 때는 "블로킹 구간을 VT 디스패처로 보낸다"처럼
   역할을 정해서 쓴다(2단계 [5-4](../2-advanced/part-5-virtual-thread.md#5-4-코루틴-디스패처를-vt-위에-올리기-선택)).
```

### 바꿔보기

각 과제를 실행하기 전에 결과를 한 줄로 예상해 적는다.

1. 예제의 플랫폼 스레드 풀 크기를 200에서 2,000으로 바꾼다. 소요 시간은 얼마가 될까? 그 대신 무엇을 더 쓰게 될까(A-1)?
2. 모든 방식이 대기 대신 CPU 작업(예: 10ms 동안 숫자 더하기 반복)을 하게 바꾼다. 방식별 순위는 어떻게 될까? (이 과제는 `tasks = 100`으로 줄여서 실행한다. 1만 개로 돌리면 한참 걸린다)
3. 코루틴 쪽 `delay(100)`을 `Thread.sleep(100)`으로 바꾼다. 소요 시간은 얼마가 될까? 왜 그럴까? (이 과제도 `tasks = 100`으로 줄여서 실행한다)

### 정리

- 네 방식은 "기다리는 동안 무엇이 스레드를 붙잡나"로 구분된다. 플랫폼 스레드는 OS 스레드를, VT는 가상 스레드만, 코루틴과 WebFlux는 아무것도 붙잡지 않는다(논블로킹일 때).
- VT는 코드 변경이 가장 적고, 코루틴은 구조화된 동시성과 취소가 강하며, WebFlux는 끝까지 논블로킹일 때만 의미가 있다.
- 스레드 한계를 없애면 다음 병목(DB 풀, 외부 API, CPU)이 드러난다. 선택은 병목을 측정한 뒤에 한다.

**실무 연결**: "우리 서비스에 무엇을 쓸까"는 이 표만으로 정하지 않는다. 아래 가설을 2단계에서 측정으로 검증한 뒤 [판단 가이드](../2-advanced/DECISION-GUIDE.md)에 규칙으로 남긴다.

**2단계에서 검증할 가설**

이 장의 내용은 아직 **가설**이다. 2단계에서 측정으로 맞는지 확인한다.

1. 외부 API 대기가 대부분인 조회에서, MVC + VT는 코드 변경 없이 MVC + 코루틴과 비슷한 처리량을 낸다. → [5-1](../2-advanced/part-5-virtual-thread.md#5-1-vt-켜기-코드-변경-없이-얻는-것-핵심), [5-2](../2-advanced/part-5-virtual-thread.md#5-2-플랫폼-스레드-vs-코루틴-vs-vt-핵심)
2. 병렬 호출, 부분 실패, 취소가 필요한 로직은 코루틴 쪽 코드가 더 단순하다. → [5-2](../2-advanced/part-5-virtual-thread.md#5-2-플랫폼-스레드-vs-코루틴-vs-vt-핵심)
3. Java 25에서는 `synchronized` 때문에 생기는 pinning이 더 이상 VT의 주요 문제가 아니다. → [5-3](../2-advanced/part-5-virtual-thread.md#5-3-pinning-vt의-함정은-아직-남아-있는가-핵심)
4. WebFlux에서 블로킹 호출 하나는 서버 전체의 응답 시간을 망가뜨린다. → [6-1](../2-advanced/part-6-webflux-coroutine.md#6-1-이벤트-루프와-블로킹의-대가-핵심)
5. DB 풀이 병목이면 방식 간 처리량 차이가 거의 사라진다. → [6-5](../2-advanced/part-6-webflux-coroutine.md#6-5-최종-벤치마크-mvc-vs-vt-vs-webflux-핵심)

→ 2단계: [Part 5. Virtual Thread](../2-advanced/part-5-virtual-thread.md), [Part 6. WebFlux + 코루틴](../2-advanced/part-6-webflux-coroutine.md)
