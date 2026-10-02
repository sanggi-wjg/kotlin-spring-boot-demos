# B. 코루틴 핵심

A에서 본 문제(스레드는 비싸고, 블로킹은 스레드를 낭비하고, 콜백과 CompletableFuture는 다루기 어렵다)를 코루틴이 어떻게 푸는지 익힌다.
장 형식과 진행 방법은 [1단계 README](./README.md)를 따른다.

> 예제는 모두 `basics` 모듈에서 바로 실행된다. 별도 의존성이 필요한 예제에는 따로 적어두었다.
>
> 💡 `fun main() = runBlocking { ... }`에서 블록의 마지막 줄이 `launch { }`처럼 값을 돌려주면 `main`의 반환 타입이 `Unit`이 아니게 되어 실행되지 않는다("Main method not found"). 그럴 때는 `runBlocking<Unit> { ... }`으로 쓴다. 아래 예제에도 그렇게 쓴 곳이 있다.

---

## B-1. 첫 코루틴: suspend와 delay

### 이 장에서 답할 질문

- 코루틴은 스레드와 무엇이 다른가?
- `delay`와 `Thread.sleep`은 둘 다 "기다린다"인데, 무엇이 다른가?
- A-1에서 스레드는 수천 개에서 막혔다. 코루틴은 몇 개까지 띄울 수 있나?

### 개념

**코루틴은 "멈췄다가 다시 이어서 실행할 수 있는 작업 단위"다.** 스레드 위에서 실행되지만 스레드 자체는 아니다.

식당에 비유하면 이렇다.

```
스레드 = 직원,  코루틴 = 주문서

[스레드 방식] 직원 한 명이 주문서 하나를 끝까지 맡는다.
             주방에서 요리가 나올 때까지 직원도 그 자리에 서서 기다린다. (블로킹)

[코루틴 방식] 직원이 주문서를 주방에 넘기고, 요리가 나오는 동안 다른 주문서를 처리한다.
             요리가 나오면 아무 직원이나 그 주문서를 이어서 처리한다. (중단과 재개)
```

이 "주문서를 내려놓는 동작"이 **중단(suspend)**이다.

- `delay(1000)`은 코루틴을 1초 동안 **중단**한다. 코루틴이 내려놓은 스레드는 그동안 다른 코루틴을 실행한다.
- `Thread.sleep(1000)`은 스레드를 1초 동안 **블로킹**한다. 그 스레드는 아무것도 못 하고 서 있는다.

`delay`처럼 코루틴을 중단할 수 있는 함수를 **중단 함수(suspend function)**라고 한다. 함수 앞에 `suspend`를 붙여 만든다. 중단 함수는 코루틴 안이나 다른 중단 함수 안에서만 호출할 수 있다.

코루틴을 시작하는 함수 두 가지를 먼저 알아둔다.

| 함수 | 하는 일 |
|---|---|
| `runBlocking { }` | 일반 코드(`main`, 테스트)에서 코루틴 세계로 들어가는 입구. 블록 안의 코루틴이 모두 끝날 때까지 **현재 스레드를 블로킹**하고 기다린다 |
| `launch { }` | 새 코루틴을 만들어 실행 대기열에 넣고 바로 다음 줄로 넘어간다. 결과값은 돌려주지 않는다 |

**스레드 관점에서 무슨 일이 일어나나**

`runBlocking`은 현재 스레드(`main`) 하나로 이벤트 루프를 돌린다. `launch`로 만든 코루틴들은 이 한 스레드 위에서 번갈아 실행된다. 어떤 코루틴이 `delay`로 중단하면 그 사이에 다른 코루틴이 같은 스레드를 쓴다. 스레드 하나로 여러 작업을 겹쳐서 기다릴 수 있는 이유다.

`launch`를 호출한 순간 새 코루틴의 본문이 바로 실행되는 것은 아니다. 새 코루틴은 이벤트 루프의 **대기열에 등록**될 뿐이다. 본문은 지금 실행 중인 코루틴이 중단하거나 끝나서 스레드를 내놓을 때 비로소 실행된다.

### 예제

**예제 1. 첫 코루틴**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    launch {
        delay(1000)
        println("World! [${Thread.currentThread().name}]")
    }
    println("Hello, [${Thread.currentThread().name}]")
}
```

```
Hello, [main]
World! [main]
```

`launch`는 코루틴을 대기열에 넣기만 하고 바로 넘어간다. 바깥 코루틴이 "Hello"를 찍고 끝나서 스레드를 내놓은 뒤에야 `launch` 본문이 실행된다. 그래서 "Hello"가 먼저 찍힌다. 두 줄 모두 `main` 스레드에서 실행됐다. 새 스레드는 만들어지지 않았다.

**예제 2. delay vs Thread.sleep**

```kotlin
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() {
    val withDelay = measureTimeMillis {
        runBlocking {
            repeat(3) { launch { delay(1000) } }
        }
    }
    val withSleep = measureTimeMillis {
        runBlocking {
            repeat(3) { launch { Thread.sleep(1000) } }
        }
    }
    println("delay: ${withDelay}ms")
    println("Thread.sleep: ${withSleep}ms")
}
```

```
delay: 1052ms          (대략)
Thread.sleep: 3013ms   (대략)
```

둘 다 스레드는 `main` 하나다.

- `delay`: 세 코루틴이 각각 중단하고 스레드를 내려놓는다. 세 번의 기다림이 겹쳐서 약 1초에 끝난다.
- `Thread.sleep`: 첫 코루틴이 스레드를 1초 붙잡는다. 그동안 나머지는 실행 순서를 기다린다. 기다림이 차례로 쌓여 약 3초가 걸린다.

**예제 3. 코루틴 10만 개**

```kotlin
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() {
    println("시작 전 스레드 수: ${Thread.getAllStackTraces().size}")
    val elapsed = measureTimeMillis {
        runBlocking {
            repeat(100_000) {
                launch { delay(5000) }
            }
            println("코루틴 10만 개 시작. 현재 스레드 수: ${Thread.getAllStackTraces().size}")
        }
    }
    println("모두 끝: ${elapsed}ms")
}
```

```
시작 전 스레드 수: 6
코루틴 10만 개 시작. 현재 스레드 수: 6
모두 끝: 5210ms   (대략)
```

스레드 수의 절대값(6)은 JVM이 원래 띄워두는 스레드(GC, 시그널 처리 등)를 포함한 값이라 실행 환경마다 다르다. 볼 것은 **코루틴 10만 개를 띄운 전후로 스레드 수가 변하지 않았다**는 점이다.

A-1에서 스레드는 macOS 기준 8천 개 남짓에서 `OutOfMemoryError: unable to create native thread`로 막혔다. 코루틴은 10만 개를 띄워도 스레드가 늘지 않는다. 중단된 코루틴은 OS 스레드가 아니라 **힙에 있는 작은 객체**이기 때문이다. 어떻게 객체 하나로 "어디까지 실행했는지"를 기억하는지는 B-3에서 본다.

### ❌/✅ 함정

**❌ 코루틴 안에서 `Thread.sleep`으로 기다린다**

```kotlin
launch {
    Thread.sleep(1000) // 스레드를 붙잡는다. 같은 스레드의 다른 코루틴이 모두 멈춘다
}
```

**✅ 코루틴 안에서는 중단 함수로 기다린다**

```kotlin
launch {
    delay(1000) // 스레드를 내려놓는다
}
```

블로킹 API(JDBC, 파일 IO, 블로킹 HTTP 클라이언트)를 꼭 써야 하는 경우는 B-4에서 다룬다.

**❌ `suspend`만 붙이면 논블로킹이 된다고 생각한다**

```kotlin
suspend fun findOrder(id: Long): Order {
    return jdbcTemplate.queryForObject(...) // 여전히 스레드를 블로킹한다
}
```

`suspend`는 "이 함수는 **중단될 수도 있다**"는 표시일 뿐이다. 함수 안에서 실제로 중단 함수(`delay`, 코루틴을 지원하는 클라이언트 등)를 호출해야 중단이 일어난다. 블로킹 코드를 `suspend` 함수로 감싸도 블로킹은 그대로다.

### 바꿔보기

1. 예제 2에서 `repeat(3)`을 `repeat(10)`으로 바꾸면 두 시간은 각각 얼마가 될까? 먼저 한 줄로 예상하고 실행해보자.
2. 예제 1에서 `launch` 블록 안의 `delay(1000)`을 지우면 출력 순서는 어떻게 될까?
3. 예제 3의 `launch { delay(5000) }`을 `thread { Thread.sleep(5000) }`(`kotlin.concurrent.thread`)로 바꾸면 어떻게 될까? 개수는 10만 개 그대로 둔다.

### 정리

- 코루틴은 스레드 위에서 실행되는 "중단 가능한 작업"이다. 중단하면 스레드를 내려놓는다.
- `delay`는 코루틴을 중단하고, `Thread.sleep`은 스레드를 블로킹한다. 코루틴 안에서는 블로킹을 피한다.
- 중단된 코루틴은 힙의 작은 객체라서 수십만 개도 만들 수 있다.

**실무 연결**: 외부 API 응답을 기다리는 동안 요청 스레드를 붙잡지 않는 것이 코루틴을 서버에 쓰는 이유다.

→ 2단계: [1-1. 첫 코루틴: 중단(suspend)은 블로킹이 아니다](../2-advanced/part-1-coroutine-core.md#1-1-첫-코루틴-중단suspend은-블로킹이-아니다-핵심)

---

## B-2. 스코프와 빌더: 구조화된 동시성

### 이 장에서 답할 질문

- 코루틴은 누가 관리하나? 시작한 코루틴이 끝났는지는 어떻게 아나?
- 외부 API 세 개를 병렬로 호출하고 결과를 모으려면?
- 하나가 실패하면 나머지는 어떻게 되나?

### 개념

**모든 코루틴은 스코프(`CoroutineScope`) 안에서 시작된다.** `launch`와 `async`는 스코프의 확장 함수라서 스코프 없이는 호출할 수 없다. 스코프 안에서 시작한 코루틴은 그 스코프의 **자식**이 된다.

```
runBlocking (부모)
├── launch  (자식 1)
│   └── launch (손자)
└── async   (자식 2)
```

이 부모-자식 관계가 **구조화된 동시성(structured concurrency)**이다. 규칙은 세 가지다.

1. **부모는 자식이 모두 끝나야 끝난다.** 자식을 기다리는 코드를 따로 쓰지 않아도 된다.
2. **부모가 취소되면 자식도 모두 취소된다.** 요청이 취소되면 그 요청에서 시작한 하위 작업도 정리된다.
3. **자식이 실패하면 부모에게 알린다.** 부모는 취소되고, 그러면 다른 자식들도 취소된다(자세한 내용은 B-6).

A-2의 CompletableFuture에는 이런 관계가 없었다. 작업 하나가 실패해도 나머지는 계속 돌았고, 취소도 각각 따로 해야 했다.

**빌더와 스코프 함수**

| 함수 | 반환 | 용도 |
|---|---|---|
| `launch { }` | `Job` | 결과가 필요 없는 작업. `job.join()`으로 끝날 때까지 기다리고, `job.cancel()`로 취소한다 |
| `async { }` | `Deferred<T>` | 결과가 필요한 작업. `deferred.await()`로 결과를 받는다. `Deferred`는 `Job`이기도 하다 |
| `coroutineScope { }` | 블록의 결과 | 중단 함수 안에서 새 스코프를 연다. 안의 자식이 모두 끝나야 반환한다 |

`launch`와 `async`는 새 코루틴을 만드는 **빌더**다. `coroutineScope`는 새 코루틴을 병렬로 띄우는 대신 자식들을 담을 스코프를 여는 **스코프 함수**다. "이 함수 안에서 병렬로 여러 일을 하고, 다 끝나면 결과를 돌려준다"는 함수를 만들 때 쓴다.

```kotlin
suspend fun loadOrderDetail(orderId: Long): OrderDetail = coroutineScope {
    val payment = async { fetchPayment(orderId) }
    val delivery = async { fetchDelivery(orderId) }
    OrderDetail(payment.await(), delivery.await())
}
```

**스레드 관점에서 무슨 일이 일어나나**

`async` 세 개를 띄워도 스레드가 세 개 생기는 것은 아니다. 세 코루틴이 각자 중단하며 기다리고, 응답이 오면 스레드를 받아 이어서 실행한다. `runBlocking` 안이라면 세 코루틴 모두 `main` 스레드 하나에서 돈다.

### 예제

**예제 1. 순차 호출 vs 병렬 호출**

공통 시나리오처럼 결제(300ms), 배송(500ms), 쿠폰(200ms)을 조회한다.

```kotlin
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

suspend fun fetchPayment(orderId: Long): String { delay(300); return "결제" }
suspend fun fetchDelivery(orderId: Long): String { delay(500); return "배송" }
suspend fun fetchCoupon(orderId: Long): String { delay(200); return "쿠폰" }

fun main() = runBlocking {
    val orderId = 1L
    val sequential = measureTimeMillis {
        val payment = fetchPayment(orderId)
        val delivery = fetchDelivery(orderId)
        val coupon = fetchCoupon(orderId)
        println("순차: $payment, $delivery, $coupon")
    }
    val parallel = measureTimeMillis {
        val payment = async { fetchPayment(orderId) }
        val delivery = async { fetchDelivery(orderId) }
        val coupon = async { fetchCoupon(orderId) }
        println("병렬: ${payment.await()}, ${delivery.await()}, ${coupon.await()}")
    }
    println("순차 ${sequential}ms / 병렬 ${parallel}ms")
}
```

```
순차: 결제, 배송, 쿠폰
병렬: 결제, 배송, 쿠폰
순차 1027ms / 병렬 510ms   (대략)
```

중단 함수를 그냥 이어서 부르면 **순차**로 실행된다. 코루틴에서는 순차가 기본이다. 병렬로 하려면 `async`로 명시한다. 병렬 버전의 시간은 가장 느린 호출(배송 500ms)에 맞춰진다.

**예제 2. 부모는 자식을 기다린다**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val parent = launch {
        launch { delay(500); println("자식 1 끝") }
        launch { delay(1000); println("자식 2 끝") }
        println("부모 본문 끝")
    }
    parent.join()
    println("부모 완료: ${parent.isCompleted}")
}
```

```
부모 본문 끝
자식 1 끝
자식 2 끝
부모 완료: true
```

부모의 본문은 바로 끝났지만, `parent.join()`은 자식 둘이 끝날 때까지 기다렸다. 자식을 `join`하는 코드를 따로 쓰지 않았다.

**예제 3. 하나가 실패하면 나머지는 취소된다**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking<Unit> {
    try {
        coroutineScope {
            launch {
                try {
                    delay(1000)
                    println("배송 조회 완료")
                } catch (e: CancellationException) {
                    println("배송 조회 취소됨")
                    throw e // 취소 신호는 다시 던진다(B-5)
                }
            }
            launch {
                delay(100)
                throw IllegalStateException("결제 API 실패")
            }
        }
    } catch (e: IllegalStateException) {
        println("잡은 예외: ${e.message}")
    }
}
```

```
배송 조회 취소됨
잡은 예외: 결제 API 실패
```

결제가 실패하자 `coroutineScope`가 취소되고, 아직 기다리던 배송 조회도 취소됐다. 그리고 `coroutineScope`는 원래 예외를 바깥으로 던졌다. "하나라도 실패하면 전체 실패, 남은 작업은 정리"가 기본 동작이다.

**예제 4. GlobalScope는 구조 밖에 있다**

```kotlin
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
fun main() = runBlocking {
    val request = launch {
        launch { delay(1000); println("자식: 요청이 취소됐는데 실행됨") }
        GlobalScope.launch { delay(1000); println("GlobalScope: 요청이 취소됐는데 실행됨") }
    }
    delay(100)
    request.cancel()
    println("요청 취소")
    delay(1500)
}
```

```
요청 취소
GlobalScope: 요청이 취소됐는데 실행됨
```

`GlobalScope.launch`로 만든 코루틴은 `request`의 자식이 아니다. 그래서 요청을 취소해도 계속 실행된다. 누구도 이 코루틴을 기다리거나 취소하지 않는다. 그래서 API에 `@DelicateCoroutinesApi`가 붙어 있다.

`GlobalScope`에는 디스패처가 없어서 이 코루틴은 `Dispatchers.Default`의 스레드에서 돈다(디스패처는 B-4). 이 스레드는 데몬 스레드라서 JVM 종료를 막지 않는다. `runBlocking`은 자식이 아닌 이 코루틴을 기다려주지 않으므로, 예제 끝의 `delay(1500)`이 없으면 출력되기 전에 프로그램이 끝날 수 있다.

### ❌/✅ 함정

**❌ `async` 직후 바로 `await`한다**

```kotlin
val payment = async { fetchPayment() }.await()   // 끝날 때까지 기다린 뒤
val delivery = async { fetchDelivery() }.await() // 다음 것을 시작한다 → 결국 순차
```

**✅ 모두 시작한 다음 기다린다**

```kotlin
val payment = async { fetchPayment() }
val delivery = async { fetchDelivery() }
val result = payment.await() to delivery.await()
// 또는 awaitAll(payment, delivery)
```

**❌ "끝나든 말든 상관없는" 작업을 `GlobalScope`로 띄운다**

```kotlin
fun placeOrder(order: Order) {
    GlobalScope.launch { sendNotification(order) } // 실패해도, 서버가 종료돼도 아무도 모른다
}
```

**✅ 수명을 책임지는 스코프를 정하고 그 안에서 띄운다**

```kotlin
// 요청 안에서 끝나야 하는 작업: suspend 함수로 만들고 coroutineScope로 감싼다
suspend fun placeOrder(order: Order) = coroutineScope {
    launch { sendNotification(order) } // placeOrder는 알림이 끝나야 반환한다
}

// 요청보다 오래 살아야 하는 작업: 애플리케이션이 소유한 스코프에서 띄운다
val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
fun placeOrderAsync(order: Order) {
    appScope.launch { sendNotification(order) } // 종료할 때 appScope.cancel()
}
```

`SupervisorJob`은 자식 하나가 실패해도 다른 자식을 취소하지 않게 하는 Job이다. 자세한 내용은 B-6에서 본다.

요청 안에서 끝나야 하는 작업이면 `coroutineScope`로 감싼다. 요청보다 오래 살아야 하는 작업이면 애플리케이션이 소유한 스코프를 따로 만들고, 종료할 때 그 스코프를 취소한다. 이 스코프를 Spring에서 어떻게 관리할지는 2단계 1-6에서 다룬다.

### 바꿔보기

1. 예제 1의 병렬 버전에서 `coupon`의 `await()`를 빼면 출력과 걸린 시간은 어떻게 될까? `coupon`은 시작만 하고 결과를 쓰지 않는다.
2. 예제 3에서 결제 쪽의 `delay(100)`을 `delay(2000)`으로 바꾸면 무엇이 출력될까?
3. 예제 2에서 `parent.join()` 대신 `parent.cancel()`을 호출하면 무엇이 출력될까?

### 정리

- 코루틴은 스코프 안에서 시작되고, 스코프의 자식이 된다. 부모는 자식을 기다리고, 취소와 실패는 이 관계를 따라 전파된다.
- `launch`는 결과 없는 작업, `async`/`await`는 결과 있는 작업에 쓴다. 병렬은 "모두 시작 → 그다음 await"로 명시한다.
- `GlobalScope`는 이 구조 밖에 있다. 수명을 책임질 스코프를 정하고 그 안에서 시작한다.

**실무 연결**: "외부 API 여러 개를 병렬로 부르고, 하나라도 실패하면 나머지를 정리한다"를 `coroutineScope` + `async`로 몇 줄에 쓸 수 있다.

→ 2단계: [1-3. 코루틴 빌더와 Job](../2-advanced/part-1-coroutine-core.md#1-3-코루틴-빌더와-job-핵심), [1-6. 구조화된 동시성](../2-advanced/part-1-coroutine-core.md#1-6-구조화된-동시성-structured-concurrency-핵심)

---

## B-3. suspend는 어떻게 동작하나

### 이 장에서 답할 질문

- 함수가 중간에 멈췄다가 나중에 이어서 실행되는 원리는?
- 중단 전과 재개 후의 스레드가 다를 수 있는 이유는?
- 콜백 API나 `CompletableFuture`를 코루틴에서 쓰려면?

### 개념

**JVM에는 "함수를 멈췄다가 이어서 실행"하는 기능이 없다.** 코루틴은 이것을 **Kotlin 컴파일러가 코드를 변환**해서 구현한다. 핵심은 두 가지다.

**1. 모든 suspend 함수는 숨겨진 파라미터 `Continuation`을 받는다.**

```kotlin
// 우리가 쓰는 코드
suspend fun loadDetail(orderId: Long): Detail

// 컴파일된 모양 (개념적으로)
fun loadDetail(orderId: Long, continuation: Continuation<Detail>): Any?
```

`Continuation`은 "이 함수가 끝나면 이어서 실행할 나머지 부분"이다. 인터페이스는 단순하다.

```kotlin
public interface Continuation<in T> {
    public val context: CoroutineContext          // 어느 디스패처에서 이어갈지 등
    public fun resumeWith(result: Result<T>)      // 결과를 넣고 이어서 실행한다
}
```

이렇게 "나머지 할 일"을 콜백처럼 넘기는 방식을 **CPS(Continuation-Passing Style)**라고 한다. 반환 타입이 `Any?`인 이유는, 값을 바로 돌려주거나 "중단했음"을 뜻하는 특별한 값 `COROUTINE_SUSPENDED`를 돌려주기 때문이다.

**2. 함수 본문은 상태 머신으로 바뀐다.**

```kotlin
suspend fun loadDetail(orderId: Long): Detail {
    val order = fetchOrder(orderId)        // 중단 지점 1
    val payment = fetchPayment(order)      // 중단 지점 2
    return Detail(order, payment)
}
```

컴파일러는 중단 지점을 기준으로 함수를 잘게 나누고, "어디까지 실행했는지(label)"와 "지역 변수"를 객체에 저장한다. 단순화하면 이런 모양이다.

```kotlin
// 개념을 보여주기 위한 의사 코드다. 실제 바이트코드와 다르다
fun loadDetail(orderId: Long, cont: Continuation<Detail>): Any? {
    val sm = cont as? LoadDetailStateMachine ?: LoadDetailStateMachine(cont)
    when (sm.label) {
        0 -> {
            sm.label = 1
            val r = fetchOrder(orderId, sm)            // sm을 Continuation으로 넘긴다
            if (r == COROUTINE_SUSPENDED) return COROUTINE_SUSPENDED  // 스레드를 내려놓는다
            sm.order = r as Order
        }
        1 -> { sm.order = sm.result as Order }        // 재개되면 여기로 들어온다
    }
    // label 1 → 2도 같은 방식이다
    ...
}
```

정리하면 이렇다.

```
1. fetchOrder 호출 → 아직 응답 없음 → COROUTINE_SUSPENDED 반환
   → loadDetail도 COROUTINE_SUSPENDED 반환 → ... → 스레드가 완전히 풀려난다
2. (응답 도착) sm.resumeWith(order) 호출
   → loadDetail(…, sm)을 다시 호출 → label=1이므로 중단 지점 1 다음부터 이어서 실행
```

B-1에서 "중단된 코루틴은 힙의 작은 객체"라고 한 것이 바로 이 상태 머신 객체다. 스레드의 호출 스택 대신, 필요한 지역 변수와 label만 힙에 저장한다.

> IntelliJ에서 직접 볼 수 있다: Tools → Kotlin → Show Kotlin Bytecode → Decompile

**스레드 관점에서 무슨 일이 일어나나: 재개 시 스레드가 바뀌는 이유**

중단하면 스레드는 완전히 풀려나서 다른 일을 한다. 나중에 `resumeWith`가 호출되면 코루틴은 `Continuation`의 컨텍스트에 있는 **디스패처**에게 "이어서 실행해 달라"고 맡긴다. 디스패처가 스레드 여러 개를 가진 풀이라면, 그때 비어 있는 아무 스레드에서 이어서 실행될 수 있다. 디스패처는 B-4에서 자세히 본다.

**콜백을 suspend 함수로 바꾸기**

`suspendCancellableCoroutine`은 지금 코루틴의 `Continuation`을 꺼내준다. 콜백에서 이 `Continuation`을 `resume`하면 코루틴이 이어서 실행된다. A-2의 콜백 API를 코루틴으로 감싸는 표준 방법이다.

### 예제

**예제 1. 재개 후 스레드가 바뀔 수 있다**

`launch(Dispatchers.Default)`는 "스레드 여러 개를 가진 풀에서 실행하라"는 뜻이다. 지금은 이것만 알면 된다(디스패처는 B-4).

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    repeat(3) { i ->
        launch(Dispatchers.Default) {
            println("코루틴 $i 중단 전: ${Thread.currentThread().name}")
            delay(100)
            println("코루틴 $i 재개 후: ${Thread.currentThread().name}")
        }
    }
}
```

```
코루틴 2 중단 전: DefaultDispatcher-worker-3
코루틴 1 중단 전: DefaultDispatcher-worker-2
코루틴 0 중단 전: DefaultDispatcher-worker-1
코루틴 1 재개 후: DefaultDispatcher-worker-2
코루틴 0 재개 후: DefaultDispatcher-worker-3
코루틴 2 재개 후: DefaultDispatcher-worker-1
(실행할 때마다 다르다)
```

`Dispatchers.Default`는 스레드 풀이다(B-4). 코루틴 0은 worker-1에서 시작해 worker-3에서 재개됐다. 같은 함수 안에서도 중단 지점을 지나면 스레드가 바뀔 수 있다. ThreadLocal에 기대는 코드가 코루틴에서 위험한 이유다(B-4에서 해결한다).

**예제 2. 콜백 API를 suspend 함수로 감싸기**

```kotlin
import kotlinx.coroutines.*
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// 콜백 방식 API라고 가정한다
fun fetchPriceAsync(id: Long, onSuccess: (Int) -> Unit, onError: (Throwable) -> Unit) {
    thread(name = "callback-thread") {
        Thread.sleep(300)
        if (id > 0) onSuccess(1000) else onError(IllegalArgumentException("잘못된 id: $id"))
    }
}

suspend fun fetchPrice(id: Long): Int = suspendCancellableCoroutine { cont ->
    fetchPriceAsync(id,
        onSuccess = { price -> cont.resume(price) },
        onError = { e -> cont.resumeWithException(e) })
}

fun main() = runBlocking<Unit> {
    println("가격: ${fetchPrice(1)} [${Thread.currentThread().name}]")
    try {
        fetchPrice(-1)
    } catch (e: IllegalArgumentException) {
        println("실패: ${e.message}")
    }
}
```

```
가격: 1000 [main]
실패: 잘못된 id: -1
```

볼 점이 세 가지다.

- 콜백은 `callback-thread`에서 불렸지만, 코루틴은 원래 디스패처(`runBlocking`의 `main`)로 돌아와서 이어졌다.
- 콜백의 에러가 일반 예외처럼 `try/catch`로 잡혔다. 콜백 지옥 없이 위에서 아래로 읽힌다.
- `suspendCancellableCoroutine`이라서 코루틴이 취소되면 기다림도 함께 끝난다. 취소될 때 원래 요청도 끊고 싶다면 `cont.invokeOnCancellation { ... }`에 정리 코드를 넣는다.

**예제 3. CompletableFuture를 await하기**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture

fun legacyPaymentCall(): CompletableFuture<String> =
    CompletableFuture.supplyAsync {
        Thread.sleep(300)
        "결제 완료"
    }

fun main() = runBlocking {
    val result = legacyPaymentCall().await()
    println("$result [${Thread.currentThread().name}]")
}
```

```
결제 완료 [main]
```

`CompletableFuture.await()`는 `kotlinx-coroutines-core`에 들어 있다(예전에는 별도 모듈 `kotlinx-coroutines-jdk8`였지만 1.7부터 core에 합쳐졌다). 반대 방향이 필요하면 `Deferred.asCompletableFuture()`나 `future { }` 빌더를 쓴다. Java 코드와 섞여 있는 서비스에서 자주 쓴다.

### ❌/✅ 함정

**❌ 콜백을 감쌀 때 `resume`을 두 번 호출한다**

```kotlin
suspendCancellableCoroutine { cont ->
    api.call(
        onSuccess = { cont.resume(it) },
        onComplete = { cont.resume(DEFAULT) } // 성공 후에도 호출되는 콜백이면 IllegalStateException
    )
}
```

**✅ 정확히 한 번만 불리는 콜백에서만 재개한다**

```kotlin
suspendCancellableCoroutine { cont ->
    api.call(
        onSuccess = { cont.resume(it) },
        onError = { e -> cont.resumeWithException(e) } // 성공과 실패 중 하나만 불린다
    )
}
```

`Continuation`은 한 번만 재개할 수 있다. 콜백이 여러 번 불릴 수 있는 API(스트림, 리스너)는 suspend 함수가 아니라 Flow로 감싼다. 이때 쓰는 빌더가 `callbackFlow`다(Flow는 C-2에서 다룬다).

**❌ `suspendCoroutine`(취소 불가 버전)을 쓴다**

**✅ `suspendCancellableCoroutine`을 쓴다**

`suspendCoroutine`으로 감싸면 바깥에서 코루틴을 취소해도 콜백이 올 때까지 계속 기다린다. 특별한 이유가 없으면 취소를 지원하는 쪽을 쓴다.

### 바꿔보기

1. 예제 1의 `Dispatchers.Default`를 지우고 `launch { }`로만 실행하면 "재개 후" 스레드는 무엇일까?
2. 예제 2에서 `fetchPrice(1)` 호출을 `withTimeout(100) { fetchPrice(1) }`로 감싸면 어떻게 될까? (`withTimeout(ms) { }`는 블록이 시간 안에 끝나지 않으면 코루틴을 취소하고 예외를 던진다. 자세한 내용은 B-5)
3. IntelliJ의 Show Kotlin Bytecode → Decompile로 예제 2의 `fetchPrice`나 B-2 예제 1의 `main`을 열어보자. `label`과 `COROUTINE_SUSPENDED`를 찾을 수 있을까?

### 정리

- 컴파일러는 suspend 함수에 `Continuation` 파라미터를 추가하고, 본문을 중단 지점 기준의 상태 머신으로 바꾼다.
- 중단하면 스레드는 완전히 풀려난다. 재개는 디스패처가 맡으므로 다른 스레드에서 이어질 수 있다.
- 콜백은 `suspendCancellableCoroutine`, `CompletableFuture`는 `await()`로 suspend 함수가 된다.

**실무 연결**: 코루틴을 지원하지 않는 SDK도 콜백이나 Future 기반이라면 몇 줄로 suspend 함수로 바꿔 쓸 수 있다.

→ 2단계: [1-2. suspend와 Continuation](../2-advanced/part-1-coroutine-core.md#1-2-suspend와-continuation-핵심)

---

## B-4. 디스패처와 컨텍스트

### 이 장에서 답할 질문

- 코루틴은 어느 스레드에서 실행되나? 그건 누가 정하나?
- JDBC 같은 블로킹 코드는 코루틴에서 어떻게 호출하나?
- A-2에서 사라지던 ThreadLocal(MDC 등)은 코루틴에서 어떻게 전달하나?

### 개념

**CoroutineContext**

모든 코루틴은 **컨텍스트**를 가진다. 컨텍스트는 코루틴의 설정 묶음이고, 요소들을 `+`로 합쳐서 만든다.

```kotlin
launch(Dispatchers.IO + CoroutineName("결제 조회")) { ... }
```

| 요소 | 역할 |
|---|---|
| `Job` | 코루틴의 생명주기와 부모-자식 관계(B-2) |
| `CoroutineDispatcher` | 어느 스레드에서 실행할지 |
| `CoroutineName` | 디버깅용 이름 |
| `CoroutineExceptionHandler` | 처리되지 않은 예외의 최종 처리(B-6) |

자식 코루틴은 부모의 컨텍스트를 **물려받고**, 인자로 넘긴 요소만 덮어쓴다. `Job`은 예외로, 자식은 항상 새 `Job`을 만들고 부모 `Job`의 자식으로 연결된다.

> ⚠ `launch(Job()) { }`처럼 `Job`을 직접 넘기면 넘긴 `Job`이 새 부모가 되어 원래 부모와의 연결이 끊긴다. 부모가 기다려주지도, 취소해주지도 않는다(B-6).

**디스패처: 코루틴을 어느 스레드에 올릴지 정하는 배차 담당**

```
       코루틴들 (주문서)
            │
      ┌─────▼─────┐
      │ 디스패처   │ ← 어느 직원(스레드)에게 맡길지 정한다
      └─────┬─────┘
   ┌────────┼────────┐
 스레드1  스레드2  스레드3
```

| 디스패처 | 스레드 | 용도 |
|---|---|---|
| `Dispatchers.Default` | CPU 코어 수만큼(최소 2개) | CPU 계산 작업. 정렬, JSON 변환, 암호화 |
| `Dispatchers.IO` | 필요할 때 max(64, 코어 수)개까지 늘어난다 | **블로킹 IO**. JDBC, 파일, 블로킹 HTTP 클라이언트 |
| `Dispatchers.Unconfined` | 정해지지 않음 | 호출한 스레드에서 시작하고, 재개는 재개시킨 스레드에서. 일반 코드에서는 쓰지 않는다 |
| (지정 안 함) | 부모 것을 물려받음 | `runBlocking` 안이면 그 스레드 하나. 물려받을 디스패처가 없으면(예: `GlobalScope`) `Dispatchers.Default` |

- `Default`와 `IO`는 **같은 스레드 풀을 공유**한다. 그래서 `Default`에서 `IO`로 옮겨도 실제 스레드 전환이 일어나지 않을 때가 많다. 동시에 쓸 수 있는 스레드 수의 한도만 다르다.
- Spring MVC(Tomcat)의 요청 스레드는 위 디스패처와 별개다. 이 관계는 D-1에서 본다.

**withContext: 잠시 다른 디스패처로 옮겨 실행하고 돌아오기**

```kotlin
suspend fun findOrder(id: Long): Order = withContext(Dispatchers.IO) {
    orderRepository.findById(id) // 블로킹 JDBC 호출
}
```

`withContext`는 블록을 지정한 디스패처에서 실행하고, 끝나면 결과를 들고 원래 디스패처로 돌아온다. `launch`처럼 병렬로 새 작업을 띄우는 것이 아니다. 호출한 쪽은 블록이 끝날 때까지 중단하고 결과를 기다린다. **같은 작업이 잠시 자리를 옮겼다가 돌아오는 것**으로 보면 된다. 블로킹 코드를 코루틴에서 부르는 표준 방법이다.

**스레드 관점에서 무슨 일이 일어나나**

`withContext(Dispatchers.IO)` 블록이 IO 스레드를 붙잡고 블로킹하는 동안, 호출한 코루틴은 **중단** 상태다. 그래서 원래 스레드(`runBlocking`의 `main`, 또는 `Default`의 워커)는 풀려나서 다른 코루틴을 실행할 수 있다. 블로킹의 비용을 IO 스레드가 대신 떠안는 구조다. 블록이 끝나면 코루틴은 결과를 들고 원래 디스패처에서 재개된다.

**limitedParallelism: 동시 실행 수 제한**

```kotlin
val dbDispatcher = Dispatchers.IO.limitedParallelism(10)
```

기존 디스패처 위에 "동시에 최대 10개만 실행"하는 view를 만든다. 새 스레드 풀을 만드는 것이 아니라 IO의 스레드를 빌려 쓰되 개수만 제한한다. 예를 들어 커넥션 풀이 10개인 DB 호출 전용 디스패처를 만들 때 쓴다. `Dispatchers.IO.limitedParallelism(n)`은 IO의 64개 한도와 별개로 n개까지 쓸 수 있다(elastic).

**ThreadLocal 전파: A-2의 문제 해결**

B-3에서 봤듯 코루틴은 중단 후 다른 스레드에서 재개될 수 있다. 그러면 ThreadLocal에 넣어둔 값(MDC의 traceId, 요청 ID 등)이 사라진다. 코루틴은 이 값을 **컨텍스트 요소**로 만들어 해결한다. 코루틴이 어떤 스레드에서 실행을 시작하거나 재개할 때마다 그 값을 ThreadLocal에 다시 넣어주고, 중단할 때 원래대로 돌려놓는다.

| 대상 | 컨텍스트 요소 |
|---|---|
| 일반 ThreadLocal | `threadLocal.asContextElement(value)` |
| SLF4J MDC | `MDCContext()` (`kotlinx-coroutines-slf4j` 필요) |

**디버깅: 어느 코루틴이 어느 스레드에서 도나**

JVM 옵션 `-Dkotlinx.coroutines.debug`를 켜면 스레드 이름 뒤에 코루틴 이름과 번호가 붙는다. IntelliJ에서는 Run Configuration의 VM options에 넣는다.

### 예제

**예제 1. 디스패처별 실행 스레드**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking<Unit> {
    launch { println("상속(runBlocking): ${Thread.currentThread().name}") }
    launch(Dispatchers.Default) { println("Default: ${Thread.currentThread().name}") }
    launch(Dispatchers.IO) { println("IO: ${Thread.currentThread().name}") }
    launch(Dispatchers.Unconfined) {
        println("Unconfined 시작: ${Thread.currentThread().name}")
        delay(10)
        println("Unconfined 재개: ${Thread.currentThread().name}")
    }
}
```

```
Default: DefaultDispatcher-worker-1
IO: DefaultDispatcher-worker-1
Unconfined 시작: main
상속(runBlocking): main
Unconfined 재개: kotlinx.coroutines.DefaultExecutor
(순서와 worker 번호는 실행할 때마다 다르다)
```

- `Default`와 `IO`가 같은 이름의 스레드(`DefaultDispatcher-worker-N`)를 쓴다. 풀을 공유하기 때문이다.
- `Unconfined`는 `main`에서 시작했지만 `delay` 후에는 `delay`를 처리한 내부 스레드에서 재개됐다. 어디서 실행될지 예측하기 어려워서 일반 코드에서는 쓰지 않는다.

**예제 2. withContext로 블로킹 코드 호출하기**

```kotlin
import kotlinx.coroutines.*

suspend fun readOrderFile(): String = withContext(Dispatchers.IO) {
    println("파일 읽기: ${Thread.currentThread().name}")
    Thread.sleep(200) // 블로킹 IO라고 가정한다
    "주문 내역"
}

fun main() = runBlocking {
    println("시작: ${Thread.currentThread().name}")
    val content = readOrderFile()
    println("'$content' 받음: ${Thread.currentThread().name}")
}
```

```
시작: main
파일 읽기: DefaultDispatcher-worker-1
'주문 내역' 받음: main
```

블로킹은 IO 스레드에서 일어났고, 결과를 받은 뒤에는 원래 스레드(`main`)로 돌아왔다. 호출하는 쪽은 `readOrderFile()`이 어느 스레드에서 도는지 신경 쓰지 않아도 된다. **"블로킹하는 함수가 스스로 IO로 옮긴다"**가 관례다.

**예제 3. limitedParallelism으로 동시 실행 수 제한하기**

```kotlin
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

val dbDispatcher = Dispatchers.IO.limitedParallelism(10)

fun main() = runBlocking {
    val elapsed = measureTimeMillis {
        coroutineScope {
            repeat(30) {
                launch(dbDispatcher) { Thread.sleep(100) } // 100ms짜리 블로킹 쿼리
            }
        }
    }
    println("쿼리 30개, 동시 10개: ${elapsed}ms")
}
```

```
쿼리 30개, 동시 10개: 318ms   (대략)
```

동시에 10개씩 세 번 실행되어 약 300ms가 걸렸다.

**예제 4. ThreadLocal과 MDC 전파하기**

> 의존성 추가: `implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")`와 SLF4J 구현체(`implementation("ch.qos.logback:logback-classic")`). Spring Boot 앱에는 logback이 이미 들어 있다. 구현체가 없으면 MDC는 값을 저장하지 않는다.

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.slf4j.MDCContext
import org.slf4j.MDC

val requestId = ThreadLocal<String>()

fun main() = runBlocking {
    requestId.set("req-1")
    MDC.put("traceId", "trace-1")

    withContext(Dispatchers.IO) {
        println("그냥 이동: requestId=${requestId.get()}, traceId=${MDC.get("traceId")}")
    }
    withContext(Dispatchers.IO + requestId.asContextElement() + MDCContext()) {
        println("전파: requestId=${requestId.get()}, traceId=${MDC.get("traceId")}")
    }
}
```

```
그냥 이동: requestId=null, traceId=null
전파: requestId=req-1, traceId=trace-1
```

`asContextElement()`와 `MDCContext()`는 **만드는 시점의 값**을 복사해 컨텍스트에 담는다. 이후 코루틴이 어느 스레드에서 실행되든 그 값을 ThreadLocal에 넣어준다.

### ❌/✅ 함정

**❌ Default 디스패처에서 블로킹한다**

```kotlin
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
    val cores = Runtime.getRuntime().availableProcessors()
    val tasks = cores * 2
    val onDefault = measureTimeMillis {
        coroutineScope { repeat(tasks) { launch(Dispatchers.Default) { Thread.sleep(1000) } } }
    }
    val onIo = measureTimeMillis {
        coroutineScope { repeat(tasks) { launch(Dispatchers.IO) { Thread.sleep(1000) } } }
    }
    println("코어 ${cores}개, 블로킹 작업 ${tasks}개")
    println("Default: ${onDefault}ms / IO: ${onIo}ms")
}
```

```
코어 10개, 블로킹 작업 20개
Default: 2016ms / IO: 1010ms   (코어 수에 따라 다르다)
```

`Default`의 스레드는 코어 수만큼밖에 없다. 블로킹으로 모두 붙잡히면 나머지 작업은 줄을 서고, 같은 `Default`를 쓰는 CPU 작업까지 함께 멈춘다.

**✅ 블로킹 호출은 `withContext(Dispatchers.IO)`로 감싼다** (예제 2)

**❌ 다른 디스패처로 옮기면서 MDC를 전파하지 않는다**

```kotlin
MDC.put("traceId", traceId)
launch(Dispatchers.IO) { log.info("결제 조회") } // 다른 스레드: traceId가 비어 있다
```

**✅ 컨텍스트에 담아서 넘긴다**

```kotlin
MDC.put("traceId", traceId)
launch(Dispatchers.IO + MDCContext()) { log.info("결제 조회") }
```

반대 방향의 문제도 있다. `MDCContext` 없이 코루틴 안에서 `MDC.put`을 하면, 그 값은 그 순간의 워커 스레드에 남는다. 코루틴이 중단된 뒤 같은 스레드에서 실행되는 **다른 코루틴의 로그에 엉뚱한 traceId가 찍힐 수 있다.**

**❌ 디버깅할 때 스레드 이름만 본다**

**✅ `CoroutineName`과 debug 모드를 함께 쓴다**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking(CoroutineName("주문-조회")) {
    launch(Dispatchers.Default + CoroutineName("결제")) {
        println("[${Thread.currentThread().name}] 결제 조회")
    }
    println("[${Thread.currentThread().name}] 시작")
}
```

`-Dkotlinx.coroutines.debug`를 켜고 실행하면:

```
[DefaultDispatcher-worker-1 @결제#2] 결제 조회
[main @주문-조회#1] 시작
(두 줄의 순서와 worker 번호는 실행할 때마다 다를 수 있다)
```

### 바꿔보기

1. 예제 3의 `limitedParallelism(10)`을 `limitedParallelism(30)`으로 바꾸면 몇 ms가 걸릴까?
2. 함정의 첫 예제에서 작업 수를 `cores * 2`에서 `100`으로 바꾸면 IO 쪽은 몇 ms가 걸릴까? (IO의 한도를 떠올려보자)
3. 예제 4의 두 번째 `withContext` 블록 안에서 `MDC.put("traceId", "changed")` 후 `delay(10)`을 하고 다시 `MDC.get("traceId")`를 찍으면 무엇이 나올까?

### 정리

- 컨텍스트는 `Job`, 디스패처, 이름, 예외 핸들러의 묶음이고, 자식은 부모 것을 물려받는다.
- CPU 작업은 `Default`, 블로킹 IO는 `withContext(Dispatchers.IO)`로 감싼다. 동시 실행 수 제한은 `limitedParallelism`.
- ThreadLocal과 MDC는 `asContextElement()`, `MDCContext()`로 컨텍스트에 담아야 스레드가 바뀌어도 따라간다.

**실무 연결**: JPA, JDBC, 블로킹 HTTP 클라이언트를 코루틴 서비스에서 부를 때 가장 먼저 지켜야 할 규칙이 "IO 디스패처로 옮겨서 부른다"이다.

→ 2단계: [1-5. CoroutineContext와 Dispatcher](../2-advanced/part-1-coroutine-core.md#1-5-coroutinecontext와-dispatcher-핵심)

---

## B-5. 취소와 타임아웃

### 이 장에서 답할 질문

- 실행 중인 코루틴은 어떻게 멈추나? 바로 멈추나?
- 외부 API에 타임아웃을 걸려면?
- 취소될 때 정리 작업은 어떻게 하나?

### 개념

**코루틴 취소는 협력적(cooperative)이다.** `job.cancel()`은 코루틴을 강제로 죽이지 않는다. "취소됐다"는 표시만 하고, 코루틴이 **중단 지점에 도착하거나 스스로 확인할 때** 멈춘다.

> 정확히는 **취소를 확인하는 중단 함수**에서 멈춘다. `delay`, `withContext`, `await`처럼 kotlinx.coroutines가 제공하는 중단 함수는 대부분 취소를 확인한다. 직접 만든 suspend 함수라도 안에서 이런 함수를 부르지 않으면 취소를 확인하지 않는다.

```
job.cancel() 호출
   │
   ▼
Job 상태: 취소 중(Cancelling)으로 표시
   │
   ▼
코루틴이 delay/withContext/await 같은 중단 함수를 만나면
→ 그 함수가 CancellationException을 던진다
→ 예외가 위로 올라가며 finally 블록이 실행된다
→ Job 상태: 취소됨(Cancelled)
```

`CancellationException`은 "에러"가 아니라 **"정상적인 취소 신호"**다. 그래서 처리되지 않아도 부모를 실패시키지 않고, 에러 로그도 남지 않는다.

**중단 지점이 없는 코드는 취소되지 않는다.** CPU만 쓰는 반복문처럼 중단 함수를 한 번도 부르지 않으면, 취소 표시를 확인할 기회가 없다. 이때는 직접 확인한다.

| 방법 | 동작 |
|---|---|
| `isActive` | 취소됐으면 `false`. 반복 조건에 넣는다 |
| `ensureActive()` | 취소됐으면 `CancellationException`을 던진다. 스코프가 없는 일반 suspend 함수 안에서는 `currentCoroutineContext().ensureActive()`로 부른다 |
| `yield()` | 취소를 확인하고, 다른 코루틴에게 실행 기회도 양보한다 |

**스레드 관점에서**: 코루틴 취소는 `Thread.interrupt()`와 별개다. 블로킹 중인 `Thread.sleep`이나 JDBC 호출은 코루틴을 취소해도 바로 멈추지 않는다. 블로킹이 끝나고 다음 중단 지점에 도착해야 취소가 반영된다.

인터럽트에 반응하는 블로킹 호출(`Thread.sleep`, `BlockingQueue.take` 등)은 `runInterruptible { }`로 감싸면 코루틴이 취소될 때 그 스레드에 인터럽트를 건다. 인터럽트에 반응하지 않는 호출(JDBC 드라이버 등)은 이것으로도 멈춘다는 보장이 없다.

**타임아웃**

| 함수 | 시간 초과 시 |
|---|---|
| `withTimeout(ms) { }` | `TimeoutCancellationException`을 던진다 |
| `withTimeoutOrNull(ms) { }` | `null`을 반환한다 |

타임아웃은 내부적으로 "시간이 지나면 블록을 취소"하는 것이다. 그래서 취소와 같은 규칙(협력적)을 따른다.

`TimeoutCancellationException`은 `CancellationException`의 하위 타입이다. 그래서 `launch` 안에서 `withTimeout`의 예외를 잡지 않으면, 에러 로그도 없이 그 코루틴이 "취소"로 조용히 끝난다. 아래 `catch (e: CancellationException) { throw e }` 패턴도 타임아웃을 그대로 다시 던진다. 타임아웃을 실패로 다루고 싶으면 `withTimeout`을 직접 `try/catch`로 감싸거나 `withTimeoutOrNull`을 쓴다.

**정리 작업**

취소되면 `finally` 블록이 실행된다. 단, 이미 취소된 코루틴에서는 중단 함수를 호출하면 바로 `CancellationException`이 난다. `finally`에서 중단 함수(DB 롤백, 원격 정리 요청 등)를 꼭 불러야 하면 `withContext(NonCancellable) { }`로 감싼다.

### 예제

**예제 1. 중단 지점에서 취소된다**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val job = launch {
        repeat(10) { i ->
            println("조회 $i")
            delay(200)
        }
    }
    delay(500)
    println("취소 요청")
    job.cancelAndJoin()
    println("취소 완료")
}
```

```
조회 0
조회 1
조회 2
취소 요청
취소 완료
```

`cancelAndJoin()`은 취소를 요청하고 실제로 끝날 때까지 기다린다. `delay`가 중단 지점이라 바로 멈췄다.

**예제 2. CPU 루프는 취소되지 않는다**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val job = launch(Dispatchers.Default) {
        var i = 0
        var next = System.currentTimeMillis()
        while (i < 5) { // CPU만 쓰는 루프: 중단 지점이 없다
            if (System.currentTimeMillis() >= next) {
                println("계산 중 ${i++}")
                next += 200
            }
        }
    }
    delay(500)
    println("취소 요청")
    job.cancelAndJoin()
    println("취소 완료")
}
```

```
계산 중 0
계산 중 1
계산 중 2
취소 요청
계산 중 3
계산 중 4
취소 완료
```

취소를 요청했지만 루프가 끝까지 돌았다. `while (i < 5)`를 `while (i < 5 && isActive)`로 바꾸면 "취소 요청" 직후 멈춘다.

```
계산 중 0
계산 중 1
계산 중 2
취소 요청
취소 완료
```

**예제 3. 타임아웃**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val delivery = withTimeoutOrNull(300) {
        delay(500) // 느린 배송 API
        "배송 정보"
    }
    println("withTimeoutOrNull 결과: $delivery")

    try {
        withTimeout(300) { delay(500) }
    } catch (e: TimeoutCancellationException) {
        println("withTimeout 예외: ${e.message}")
    }
}
```

```
withTimeoutOrNull 결과: null
withTimeout 예외: Timed out waiting for 300 ms
```

"배송 정보가 없으면 빈 값으로 응답"처럼 대체 값이 있으면 `withTimeoutOrNull`, 실패로 처리해야 하면 `withTimeout`을 쓴다.

**예제 4. finally와 NonCancellable**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val job = launch {
        try {
            delay(1000)
        } finally {
            println("정리 시작")
            delay(100)
            println("정리 끝")
        }
    }
    delay(100)
    job.cancelAndJoin()
    println("---")
    val job2 = launch {
        try {
            delay(1000)
        } finally {
            withContext(NonCancellable) {
                println("정리 시작")
                delay(100)
                println("정리 끝")
            }
        }
    }
    delay(100)
    job2.cancelAndJoin()
}
```

```
정리 시작
---
정리 시작
정리 끝
```

첫 번째는 `finally` 안의 `delay`가 바로 `CancellationException`을 던져서 "정리 끝"이 찍히지 않았다. `NonCancellable`로 감싸면 정리를 끝까지 한다.

### ❌/✅ 함정

**❌ `runCatching`이나 `catch (e: Exception)`이 취소 신호를 삼킨다**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val job = launch {
        repeat(5) { i ->
            runCatching { delay(200) } // CancellationException까지 잡아버린다
            println("작업 $i")
        }
    }
    delay(300)
    println("취소 요청")
    job.cancelAndJoin()
    println("취소 완료")
}
```

```
작업 0
취소 요청
작업 1
작업 2
작업 3
작업 4
취소 완료
```

`CancellationException`도 `Exception`의 하위 타입이다. `runCatching`이 취소 신호를 잡아서 삼키는 바람에 코루틴이 멈추지 않고 나머지 작업을 다 했다(취소된 뒤의 `delay`는 즉시 예외를 던지므로 순식간에 끝난다).

**✅ 취소 신호는 다시 던진다**

```kotlin
try {
    callExternalApi()
} catch (e: CancellationException) {
    throw e                      // 취소는 그대로 올려보낸다
} catch (e: Exception) {
    log.warn("외부 API 실패", e) // 진짜 에러만 처리한다
}
```

`runCatching`을 꼭 써야 한다면 그 뒤에 `ensureActive()`(일반 suspend 함수 안이면 `currentCoroutineContext().ensureActive()`)를 호출해 취소 여부를 다시 확인한다.

**❌ 코루틴을 취소하면 블로킹 호출도 끊긴다고 생각한다**

```kotlin
withTimeout(1000) {
    jdbcTemplate.query(slowSql) // 10초짜리 쿼리: 타임아웃이 지나도 10초 동안 스레드를 붙잡는다
}
```

**✅ 블로킹 호출에는 그 라이브러리의 타임아웃을 함께 건다**

JDBC의 쿼리 타임아웃, HTTP 클라이언트의 read timeout 같은 설정을 따로 둔다. 2단계 4-3, 4-5에서 실제로 확인한다.

### 바꿔보기

1. 예제 2의 루프 안에 `yield()`를 넣으면 취소가 될까? (`isActive` 조건은 넣지 않는다)
2. 예제 3의 `withTimeoutOrNull(300)` 안의 `delay(500)`을 `Thread.sleep(500)`으로 바꾸면 결과와 걸린 시간은 어떻게 될까?
3. 함정 예제의 `runCatching { delay(200) }` 다음 줄에 `ensureActive()`를 넣으면 무엇이 출력될까?

### 정리

- 취소는 협력적이다. 중단 지점에 도착하거나 `isActive`/`ensureActive()`로 확인해야 멈춘다.
- `CancellationException`은 정상적인 취소 신호다. `catch (e: Exception)`이나 `runCatching`으로 삼키지 말고 다시 던진다.
- 타임아웃은 `withTimeout`/`withTimeoutOrNull`, 취소 후 정리는 `finally` + `withContext(NonCancellable)`.

**실무 연결**: `withTimeout`이 걸린 블록에서 시간이 초과되면, 그 블록 안의 하위 중단 호출(외부 API 호출 등)이 함께 취소된다. 블로킹 호출은 예외라서 그 라이브러리의 타임아웃이 따로 필요하다. 클라이언트가 연결을 끊었을 때 서버의 코루틴까지 취소되는지는 스택마다 다르다. 2단계 4-3에서 확인한다.

→ 2단계: [1-7. 취소](../2-advanced/part-1-coroutine-core.md#1-7-취소-cancellation-핵심)

---

## B-6. 예외 처리

### 이 장에서 답할 질문

- 자식 코루틴 하나가 예외를 던지면 형제와 부모는 어떻게 되나?
- `launch`와 `async`의 예외는 어디서 잡나?
- "하나가 실패해도 나머지는 계속"은 어떻게 만드나?

### 개념

**예외는 부모-자식 관계를 따라 위로 전파된다.**

```
        부모 (coroutineScope)
       /        \
   자식 A      자식 B
   (예외!)    (실행 중)

1. 자식 A 실패
2. 부모에게 알림 → 부모 취소
3. 부모가 다른 자식(B)을 모두 취소
4. 부모가 A의 예외를 바깥으로 던진다
```

B-2 예제 3에서 본 동작이다. "작업 하나가 실패하면 함께 시작한 작업 전체가 의미 없다"는 상황(주문 상세의 일부 조회 실패 → 응답 실패)에 맞는 기본값이다.

**`launch`와 `async`의 차이**

| | `launch` | `async` |
|---|---|---|
| 예외가 드러나는 곳 | 즉시 부모로 전파. 최상위면 처리되지 않은 예외로 출력 | `await()`를 호출할 때 던져진다 |
| 그런데 | | 일반 스코프의 자식이면 `await` 전이라도 **부모에게 즉시 전파**된다 |

마지막 줄이 중요하다. `async`의 예외를 `await()`에서 `try/catch`로 잡아도, 일반 스코프(`coroutineScope`, 일반 `Job`) 안이라면 부모와 형제는 이미 취소된다. `await`에서만 예외를 다루고 싶다면 `supervisorScope` 안에서 띄운다.

**SupervisorJob과 supervisorScope: 실패를 격리한다**

| | 자식 하나가 실패하면 |
|---|---|
| `Job` / `coroutineScope` | 부모와 다른 자식이 모두 취소된다 |
| `SupervisorJob` / `supervisorScope` | 실패한 자식만 끝난다. 부모와 다른 자식은 계속 실행된다 |

"알림 발송, 통계 집계처럼 서로 독립된 작업"이나 "일부가 실패해도 나머지 결과로 응답할 수 있는 조회"에 쓴다.

**CoroutineExceptionHandler: 마지막 안전망**

아무도 처리하지 않은 예외를 받는 핸들러다. 로그를 남기거나 알림을 보낸다. 단, 동작하는 위치가 정해져 있다.

- **최상위 코루틴**(부모 코루틴이 없는 코루틴. 예: `CoroutineScope(...)`로 직접 만든 스코프에서 `launch`한 코루틴)이나 **supervisor의 직속 자식**에 있을 때만 동작한다. `coroutineScope { }` 안의 `launch`는 부모가 있으므로 해당하지 않는다.
- 일반 자식 코루틴에 달면 무시된다. 자식의 예외는 핸들러가 아니라 부모에게 전달되기 때문이다.
- `async`에는 효과가 없다. 예외는 `await()`를 부른 쪽이 처리한다.
- 예외를 "복구"하지 못한다. 코루틴은 이미 끝났고, 핸들러는 기록만 한다.

**CancellationException은 예외 전파에서 빠진다.** 자식이 `CancellationException`으로 끝나면 그 자식만 취소된 것으로 보고, 부모는 실패하지 않는다. 핸들러도 호출되지 않는다.

### 예제

**예제 1. async는 await에서 예외를 던진다 (supervisorScope 안에서)**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    supervisorScope {
        val deferred = async {
            delay(100)
            throw IllegalStateException("쿠폰 조회 실패")
        }
        delay(300)
        println("async는 await 전까지 조용하다")
        try {
            deferred.await()
        } catch (e: IllegalStateException) {
            println("await에서 잡음: ${e.message}")
        }
    }
}
```

```
async는 await 전까지 조용하다
await에서 잡음: 쿠폰 조회 실패
```

**예제 2. coroutineScope vs supervisorScope**

```kotlin
import kotlinx.coroutines.*

suspend fun call(name: String, ms: Long, fail: Boolean = false) {
    delay(ms)
    if (fail) throw IllegalStateException("$name 실패")
    println("$name 완료")
}

fun main() = runBlocking<Unit> {
    println("== coroutineScope ==")
    try {
        coroutineScope {
            launch { call("결제", 100, fail = true) }
            launch { call("배송", 300) }
        }
    } catch (e: IllegalStateException) {
        println("잡은 예외: ${e.message}")
    }

    println("== supervisorScope ==")
    val handler = CoroutineExceptionHandler { _, e -> println("핸들러: ${e.message}") }
    supervisorScope {
        launch(handler) { call("결제", 100, fail = true) }
        launch(handler) { call("배송", 300) }
    }
}
```

```
== coroutineScope ==
잡은 예외: 결제 실패
== supervisorScope ==
핸들러: 결제 실패
배송 완료
```

`coroutineScope`에서는 결제 실패로 배송이 취소되어 "배송 완료"가 없다. `supervisorScope`에서는 결제만 실패하고 배송은 끝까지 실행됐다. supervisor의 직속 자식이라 `launch(handler)`의 핸들러가 동작했다.

**예제 3. 애플리케이션 스코프와 CoroutineExceptionHandler**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { context, e ->
        println("핸들러가 받음: ${e.message} (${context[CoroutineName]?.name})")
    }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + handler)

    scope.launch(CoroutineName("알림 발송")) {
        throw IllegalStateException("메일 서버 응답 없음")
    }.join()
    scope.launch(CoroutineName("통계 집계")) {
        println("다른 작업은 계속 실행된다")
    }.join()
    scope.cancel()
}
```

```
핸들러가 받음: 메일 서버 응답 없음 (알림 발송)
다른 작업은 계속 실행된다
```

요청과 무관하게 돌아가는 백그라운드 작업용 스코프의 기본 모양이다. `SupervisorJob`이라서 한 작업의 실패가 스코프 전체를 망가뜨리지 않고, 핸들러가 실패를 기록한다. 애플리케이션이 종료될 때 `scope.cancel()`로 정리한다.

### ❌/✅ 함정

**❌ `launch`를 `try/catch`로 감싼다**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    try {
        launch { throw IllegalStateException("결제 실패") }
    } catch (e: IllegalStateException) {
        println("여기서 잡힐까?")
    }
    delay(100)
    println("여기까지 올까?")
}
```

```
Exception in thread "main" java.lang.IllegalStateException: 결제 실패
	at ...
```

두 줄 모두 출력되지 않는다. `launch`는 코루틴을 시작만 하고 바로 반환하므로 `try` 블록은 이미 끝났다. 예외는 나중에 코루틴 안에서 나서 부모(`runBlocking`)로 전파되고, `runBlocking`이 그 예외를 던진다.

**✅ 예외는 코루틴 안에서 잡거나, 스코프 경계에서 잡는다**

```kotlin
launch {
    try { pay() } catch (e: PaymentException) { ... }   // 코루틴 안에서
}
// 또는
try {
    coroutineScope { launch { pay() } }                 // 스코프 경계에서 (B-2 예제 3)
} catch (e: PaymentException) { ... }
```

**❌ 자식 코루틴에 CoroutineExceptionHandler를 단다**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, e -> println("핸들러: ${e.message}") }
    val scope = CoroutineScope(Job() + Dispatchers.Default)
    scope.launch {
        launch(handler) { throw IllegalStateException("자식 실패") }
    }.join()
    println("끝")
}
```

```
Exception in thread "DefaultDispatcher-worker-2" java.lang.IllegalStateException: 자식 실패
	at ...
	Suppressed: kotlinx.coroutines.internal.DiagnosticCoroutineContextException: ...
끝
(worker 번호와 "끝"의 위치는 실행할 때마다 다를 수 있다. 스택 트레이스는 stderr, "끝"은 stdout으로 나간다)
```

"핸들러:"가 출력되지 않는다. 자식의 예외는 부모로 전파되고, 부모(최상위 `launch`)에는 핸들러가 없어서 스레드의 기본 처리기가 스택 트레이스를 찍었다.

**✅ 핸들러는 스코프나 최상위 코루틴에 단다** (예제 3)

### 바꿔보기

1. 예제 1의 `supervisorScope`를 `coroutineScope`로 바꾸면 "async는 await 전까지 조용하다"가 출력될까?
2. 예제 2의 supervisorScope 쪽에서 `launch(handler)`를 그냥 `launch`로 바꾸면 무엇이 출력될까? 배송은 완료될까?
3. 예제 3의 `SupervisorJob()`을 `Job()`으로 바꾸면 "다른 작업은 계속 실행된다"가 출력될까?

### 정리

- 기본은 "자식 하나가 실패하면 부모와 형제가 모두 취소"다. 실패를 격리하려면 `SupervisorJob`/`supervisorScope`를 쓴다.
- `launch`의 예외는 부모로 전파되고, `async`의 예외는 `await()`에서 던져진다. 단 일반 스코프에서는 `async`도 즉시 부모를 취소시킨다.
- `CoroutineExceptionHandler`는 최상위 코루틴이나 supervisor의 직속 자식에서만 동작하는 마지막 안전망이다.

**실무 연결**: "주문 상세 조회는 하나라도 실패하면 실패"(`coroutineScope`)와 "부가 정보는 실패해도 나머지로 응답"(`supervisorScope` + 실패한 항목은 빈 쿠폰 목록 같은 대체 값으로 채운다)을 구분해서 설계한다.

→ 2단계: [1-8. 예외 전파](../2-advanced/part-1-coroutine-core.md#1-8-예외-전파-핵심)

---

## B-7. 코루틴 테스트

### 이 장에서 답할 질문

- `delay(5000)`이 들어간 코드를 테스트할 때 정말 5초를 기다려야 하나?
- "병렬로 호출했으니 500ms에 끝난다"를 테스트로 어떻게 증명하나?
- 서비스 코드 안의 `Dispatchers.IO`는 테스트에서 어떻게 다루나?

### 개념

**`runTest`: 코루틴 테스트의 입구**

`runBlocking` 대신 `kotlinx-coroutines-test`의 `runTest`를 쓴다. `basics` 모듈에는 이미 들어 있다.

- 이 장의 예제는 테스트 코드다. `basics/src/test/kotlin/.../guide/b7`에 둔다. `kotlin.test.Test`는 이미 들어 있는 `kotlin-test-junit5` 덕분에 JUnit 5로 실행된다.
- `runTest`는 블록이 끝난 뒤에도 **남은 작업(아직 실행되지 않은 `launch` 등)을 모두 실행하고** 끝난다.
- 가상 시간과 별개로 **실제 시간 기준 타임아웃**이 있다. 기본값은 60초이고 `runTest(timeout = 10.seconds)`처럼 바꿀 수 있다. 테스트가 이 시간 안에 끝나지 않으면 실패한다.

**가상 시간**

`runTest` 안에서 `delay`는 실제로 기다리지 않는다. 테스트 전용 스케줄러(`TestCoroutineScheduler`)가 **가상 시계**를 앞으로 돌린다. 그래서 `delay(10_000)`도 즉시 끝나고, 몇 ms가 흘렀는지는 `currentTime`으로 정확히 읽을 수 있다.

| API | 하는 일 |
|---|---|
| `currentTime` | 지금까지 흐른 가상 시간(ms) |
| `advanceTimeBy(ms)` | 가상 시간을 ms만큼 앞으로 돌리고, 그 사이에 예정된 작업을 실행한다 |
| `advanceUntilIdle()` | 예정된 작업이 없을 때까지 모두 실행한다 |
| `runCurrent()` | 지금 시각에 예정된 작업만 실행한다 |

**테스트 디스패처 두 가지**

| 디스패처 | `launch`한 코루틴은 |
|---|---|
| `StandardTestDispatcher` (`runTest`의 기본) | 바로 실행되지 않고 대기열에 들어간다. 테스트 코드가 중단하거나 `advance…`를 호출해야 실행된다 |
| `UnconfinedTestDispatcher` | 첫 중단 지점까지 바로 실행된다 |

기본값인 Standard는 실행 순서를 테스트가 통제할 수 있다. Unconfined는 "launch 직후 상태"를 간단히 확인할 때 편하다.

**디스패처 주입**

가상 시간은 **테스트 스케줄러 위에서 도는 코루틴에만** 적용된다. 서비스 코드가 `withContext(Dispatchers.IO)`처럼 실제 디스패처를 직접 쓰면, 그 블록은 테스트 스케줄러 밖에서 실제 시간으로 돈다. 그래서 디스패처를 생성자로 주입받고, 테스트에서는 테스트 디스패처를 넘긴다.

```kotlin
class OrderService(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO)
```

Spring에서는 디스패처를 빈으로 등록해 주입한다(2단계 4-9).

### 예제

**예제 1. 가상 시간으로 병렬 실행 증명하기**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.Test
import kotlin.test.assertEquals

class VirtualTimeTest {

    @Test
    fun `10초 delay도 즉시 끝난다`() = runTest {
        delay(10_000)
        assertEquals(10_000L, currentTime)
    }

    @Test
    fun `병렬 호출은 가장 긴 호출만큼 걸린다`() = runTest {
        val payment = async { delay(300); "결제" }
        val delivery = async { delay(500); "배송" }
        val coupon = async { delay(200); "쿠폰" }
        assertEquals(listOf("결제", "배송", "쿠폰"), awaitAll(payment, delivery, coupon))
        assertEquals(500L, currentTime)
    }
}
```

두 테스트 모두 수 ms 안에 끝난다. 두 번째 테스트는 "병렬이면 500ms"를 실제 시간의 오차 없이 정확한 값으로 검증한다. 실제 시간으로 재면 `506ms` 같은 값이 나와 `assertEquals`를 쓸 수 없다.

**예제 2. Standard와 Unconfined의 차이**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.Test
import kotlin.test.assertEquals

class SchedulerTest {

    @Test
    fun `StandardTestDispatcher - launch는 바로 실행되지 않는다`() = runTest {
        var state = "시작 전"
        launch {
            state = "실행 중"
            delay(1000)
            state = "완료"
        }
        assertEquals("시작 전", state)

        advanceTimeBy(500)
        assertEquals("실행 중", state)

        advanceUntilIdle()
        assertEquals("완료", state)
        assertEquals(1000L, currentTime)
    }

    @Test
    fun `UnconfinedTestDispatcher - launch가 첫 중단 지점까지 바로 실행된다`() = runTest(UnconfinedTestDispatcher()) {
        var state = "시작 전"
        launch {
            state = "실행 중"
            delay(1000)
            state = "완료"
        }
        assertEquals("실행 중", state)
    }
}
```

**예제 3. 디스패처 주입**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderService(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {
    suspend fun loadOrder(): String = withContext(ioDispatcher) {
        delay(5_000)
        "주문"
    }
}

class DispatcherInjectionTest {

    @Test
    fun `Dispatchers IO를 그대로 쓰면 실제로 5초 기다린다`() = runTest {
        val start = System.currentTimeMillis()
        assertEquals("주문", OrderService().loadOrder())
        println("가상 시간: ${currentTime}ms, 실제 시간: ${System.currentTimeMillis() - start}ms")
    }

    @Test
    fun `테스트 디스패처를 주입하면 가상 시간을 따른다`() = runTest {
        val start = System.currentTimeMillis()
        val service = OrderService(StandardTestDispatcher(testScheduler))
        assertEquals("주문", service.loadOrder())
        println("가상 시간: ${currentTime}ms, 실제 시간: ${System.currentTimeMillis() - start}ms")
        assertEquals(5_000L, currentTime)
    }
}
```

```
가상 시간: 0ms, 실제 시간: 5014ms      (첫 번째 테스트, 대략)
가상 시간: 5000ms, 실제 시간: 5ms      (두 번째 테스트, 대략)
```

`StandardTestDispatcher(testScheduler)`처럼 **같은 스케줄러**를 공유하는 디스패처를 넘겨야 가상 시간이 이어진다.

**예제 4. MockK로 suspend 함수 스텁하기**

> 의존성 추가: `testImplementation("io.mockk:mockk:<버전>")`. 버전은 [MockK 문서](https://mockk.io/)에서 최신 버전을 확인한다. (이 예제는 1.14.5로 확인했다)

```kotlin
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

interface PaymentClient {
    suspend fun fetch(orderId: Long): String
}

class MockkTest {

    @Test
    fun `coEvery로 suspend 함수를 스텁한다`() = runTest {
        val client = mockk<PaymentClient>()
        coEvery { client.fetch(1) } coAnswers {
            delay(300)
            "결제 완료"
        }

        assertEquals("결제 완료", client.fetch(1))
        assertEquals(300L, currentTime)
        coVerify(exactly = 1) { client.fetch(1) }
    }
}
```

suspend 함수에는 `every`/`verify` 대신 `coEvery`/`coVerify`를 쓴다. `coAnswers` 안에서 `delay`를 쓰면 "300ms 걸리는 외부 API"를 가상 시간으로 흉내 낼 수 있다.

### ❌/✅ 함정

**❌ 코루틴 테스트를 `runBlocking`으로 쓴다**

```kotlin
@Test
fun test() = runBlocking {
    service.loadDetail(1) // delay가 실제로 흐른다. 느리고, 시간 검증이 불안정하다
}
```

**✅ `runTest`를 쓴다**

**❌ 서비스 안에서 `Dispatchers.IO`를 직접 쓴다**

```kotlin
class OrderService {
    suspend fun load() = withContext(Dispatchers.IO) { ... } // 테스트에서 가상 시간을 벗어난다
}
```

**✅ 디스패처를 주입받는다** (예제 3)

**❌ `launch`한 코루틴의 결과를 바로 확인한다**

```kotlin
runTest {
    launch { repository.save(order) }
    verify { repository.save(order) } // StandardTestDispatcher에서는 아직 실행 전이다
}
```

**✅ `advanceUntilIdle()`로 실행시킨 뒤 확인한다**

### 바꿔보기

1. 예제 1의 두 번째 테스트에서 `async` 세 개를 일반 순차 호출로 바꾸면 `currentTime`은 얼마가 될까?
2. 예제 2의 첫 테스트에서 `advanceTimeBy(500)`을 `advanceTimeBy(1000)`으로 바꾸면 `state`는 무엇일까? (`advanceTimeBy`가 정확히 그 시각에 예정된 작업도 실행하는지 문서에서 확인해보자)
3. 예제 3의 두 번째 테스트에서 `StandardTestDispatcher(testScheduler)` 대신 `StandardTestDispatcher()`(스케줄러 인자 없음)를 넘기면 어떻게 될까?

### 정리

- 코루틴 테스트는 `runTest`로 쓴다. `delay`는 가상 시간으로 처리되어 즉시 끝나고, `currentTime`으로 정확한 시간을 검증한다.
- 기본 디스패처(Standard)에서 `launch`는 바로 실행되지 않는다. `advanceUntilIdle()` 등으로 실행을 진행시킨다.
- 서비스 코드의 디스패처는 주입받는다. 그래야 테스트에서 가상 시간을 적용할 수 있다.

**실무 연결**: 타임아웃, 재시도, 병렬 호출처럼 시간이 핵심인 로직을 수 ms짜리 테스트로 정확하게 검증할 수 있다. 2단계의 개념 장들은 이 방식으로 assert를 작성한다.

→ 2단계: [1-4. 코루틴 테스트 도구](../2-advanced/part-1-coroutine-core.md#1-4-코루틴-테스트-도구-핵심)
