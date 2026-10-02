# C. 동시성 도구와 Flow

B에서 코루틴을 띄우고, 멈추고, 실패를 다루는 법을 익혔다. C에서는 코루틴 여러 개가 **함께 일할 때** 필요한 도구를 다룬다. 같은 값을 안전하게 바꾸는 법, 동시 실행 수를 제한하는 법, 여러 값을 흘려보내는 법이다.

[← 1단계 목차](./README.md)

---

## C-1. 공유 상태와 동시 실행 제한

### 이 장에서 답할 질문

- 코루틴 여러 개가 같은 변수를 동시에 바꾸면 어떻게 되나? 어떻게 막나?
- 코루틴 안에서 `synchronized`를 써도 되나? `Mutex`는 무엇이 다른가?
- "외부 API는 동시에 10개까지만 호출한다"는 요구사항은 무엇으로 구현하나?

### 개념

**코루틴도 race condition을 피할 수 없다**

코루틴은 가볍지만 결국 스레드 위에서 돈다. `Dispatchers.Default`는 여러 스레드를 가진 풀이므로([B-4](./b-coroutine-core.md#b-4-디스패처와-컨텍스트)), 코루틴 1,000개가 같은 변수를 건드리면 실제로 여러 스레드가 그 변수를 동시에 건드린다. 스레드에서 생기던 동시성 버그가 코루틴에서도 그대로 생긴다.

`counter++`는 한 줄이지만 실제로는 세 단계다.

```
스레드 A: 읽기(5) ──── +1 ──── 쓰기(6)
스레드 B:     읽기(5) ──── +1 ──── 쓰기(6)    ← A의 증가분이 사라진다
```

두 스레드가 같은 값을 읽고 각자 1을 더해 쓰면, 두 번 증가했는데 결과는 한 번 증가한 것과 같다. 이것이 **race condition**이다.

**해결 방법은 크게 세 가지다**

| 방법 | 원리 | 비유 |
|---|---|---|
| `AtomicInteger` 등 | CPU의 원자적 명령으로 "읽고-더하고-쓰기"를 한 번에 한다 | 숫자 세는 기계를 하나 두고 모두 그 버튼만 누른다 |
| `Mutex` | 한 번에 코루틴 하나만 임계 구역에 들어간다 | 열쇠가 하나뿐인 화장실 |
| 스레드 한정(`limitedParallelism(1)`) | 그 상태를 건드리는 코드를 한 번에 하나씩만 실행한다(실행 스레드는 바뀔 수 있지만, 앞 실행의 결과는 다음 실행에서 보인다) | 장부는 한 번에 한 명만 쓴다 |

**`Mutex`와 `synchronized`의 차이: 기다리는 방식**

둘 다 "한 번에 하나만" 들여보낸다. 차이는 기다리는 쪽이 무엇을 하느냐다.

```
synchronized: 락을 기다리는 스레드가 BLOCKED 상태로 멈춘다 → 스레드 하나를 통째로 낭비
Mutex.lock(): 락을 기다리는 코루틴이 중단(suspend)된다    → 스레드는 풀려나 다른 코루틴을 실행
```

그리고 `synchronized` 블록 안에서는 중단 함수를 호출할 수 없다. 컴파일러가 막는다. 락의 주인은 스레드인데, 코루틴은 중단했다가 **다른 스레드에서** 재개될 수 있기 때문이다([B-3](./b-coroutine-core.md#b-3-suspend는-어떻게-동작하나)). 다른 스레드가 남의 락을 풀 수는 없다. 단, 컴파일러가 모든 경우를 막아주지는 못한다. 함정 2에서 본다.

`Mutex`에서 주의할 점이 하나 있다. Java의 `synchronized`나 `ReentrantLock`과 달리 **재진입이 안 된다**. 이미 잠근 코루틴이 같은 `Mutex`를 다시 잠그려 하면 영원히 기다린다.

**동시 실행 수 제한: `Semaphore`와 `limitedParallelism`은 세는 대상이 다르다**

외부 API가 "동시 요청 10개까지"를 요구한다고 하자. 두 도구가 모두 "10"이라는 숫자를 받지만 세는 대상이 다르다.

```
Semaphore(10)              : "허가증을 가진 코루틴" 수를 센다.
                             API 응답을 기다리며 중단된 코루틴도 허가증을 쥐고 있으므로 한도에 포함된다.

limitedParallelism(10)     : "지금 스레드 위에서 실행 중인 코루틴" 수를 센다.
                             중단된 코루틴은 스레드를 내려놓으므로 한도에 포함되지 않는다.
```

그래서 **논블로킹 호출(중단 함수)의 동시 실행 수를 제한하려면 `Semaphore`**를 쓴다. `limitedParallelism`은 "이 작업에 스레드를 몇 개까지 쓸 것인가"를 정하는 도구다. 블로킹 호출을 감쌀 때 의미가 있다(B-4).

### 예제

**예제 1. race condition 재현**

```kotlin
import kotlinx.coroutines.*

var counter = 0

fun main() = runBlocking {
    withContext(Dispatchers.Default) {
        repeat(1_000) {
            launch { repeat(1_000) { counter++ } }
        }
    }
    println("counter = $counter")
}
```

```
counter = 442359   ← 실행할 때마다 다르다. 1,000,000이 나오지 않는다
```

**예제 2. 세 가지 해결 방법 비교**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.measureTime

// 코루틴 1,000개가 각각 action을 1,000번 실행한다
suspend fun massiveRun(name: String, action: suspend () -> Unit) {
    val elapsed = measureTime {
        coroutineScope {
            repeat(1_000) { launch(Dispatchers.Default) { repeat(1_000) { action() } } }
        }
    }
    println("$name: $elapsed")
}

fun main() = runBlocking {
    val atomic = AtomicInteger()
    massiveRun("AtomicInteger") { atomic.incrementAndGet() }
    println("  → ${atomic.get()}")

    val mutex = Mutex()
    var mutexCounter = 0
    massiveRun("Mutex") { mutex.withLock { mutexCounter++ } }
    println("  → $mutexCounter")

    val single = Dispatchers.Default.limitedParallelism(1)
    var confinedCounter = 0
    massiveRun("limitedParallelism(1)") { withContext(single) { confinedCounter++ } }
    println("  → $confinedCounter")
}
```

```
AtomicInteger: 83ms                ← 시간은 환경마다 다르다(대략적인 값)
  → 1000000
Mutex: 262ms
  → 1000000
limitedParallelism(1): 507ms
  → 1000000
```

셋 다 정확하다. 단순한 숫자 하나라면 `AtomicInteger`가 가장 빠르다. `Mutex`와 스레드 한정은 여러 값을 한꺼번에 일관되게 바꿔야 할 때 쓴다.

스레드 한정이 가장 느린 이유는 증가 한 번마다 `withContext`로 스레드를 오가기 때문이다(세밀한 한정). 증가 1,000번을 담은 블록 전체를 한정된 디스패처에서 실행하면(굵은 한정) 오가는 횟수가 줄어 훨씬 빨라진다. 공식 가이드의 [Shared mutable state](https://kotlinlang.org/docs/shared-mutable-state-and-concurrency.html) 절이 두 방식을 비교한다.

**예제 3. 외부 API를 동시에 10개까지만 호출하기**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.measureTime

val inFlight = AtomicInteger()    // 지금 호출 중인 수
val maxInFlight = AtomicInteger() // 동시에 호출 중이던 최대 수

suspend fun callExternalApi() {
    val now = inFlight.incrementAndGet()
    maxInFlight.updateAndGet { maxOf(it, now) }
    delay(100) // 응답을 기다리는 100ms (논블로킹)
    inFlight.decrementAndGet()
}

fun main() = runBlocking {
    val semaphore = Semaphore(10)
    val t1 = measureTime {
        coroutineScope { repeat(100) { launch { semaphore.withPermit { callExternalApi() } } } }
    }
    println("Semaphore(10)          : 최대 동시 호출 ${maxInFlight.get()}개, $t1")

    maxInFlight.set(0)
    val limited = Dispatchers.IO.limitedParallelism(10)
    val t2 = measureTime {
        coroutineScope { repeat(100) { launch(limited) { callExternalApi() } } }
    }
    println("limitedParallelism(10) : 최대 동시 호출 ${maxInFlight.get()}개, $t2")
}
```

```
Semaphore(10)          : 최대 동시 호출 10개, 1.05s      ← 100건 ÷ 10개 × 100ms ≈ 1초
limitedParallelism(10) : 최대 동시 호출 100개, 112ms    ← 한도 10이 지켜지지 않았다
```

`limitedParallelism(10)`에서 100개가 동시에 호출됐다. `delay`로 중단된 코루틴은 스레드를 내려놓기 때문에, 스레드 10개가 코루틴 100개를 번갈아 실행하며 모두 "호출 중" 상태로 만든 것이다.

### ❌/✅ 함정

**1. `@Volatile`로 race condition을 막으려 한다**

```kotlin
@Volatile var counter = 0 // ❌ 예제 1에 붙여도 결과는 여전히 1,000,000보다 작다
```

`@Volatile`은 "다른 스레드가 쓴 최신 값을 읽게" 보장할 뿐(가시성), 읽고-더하고-쓰기를 한 번에 하게 해주지는 않는다(원자성). ✅ 카운터는 `AtomicInteger`, 여러 값은 `Mutex`를 쓴다.

**2. `synchronized` 안에서 중단 함수를 호출한다**

```kotlin
synchronized(lock) {
    delay(10) // ❌ 컴파일 에러: The 'delay' suspension point is inside a critical section.
}
```

✅ 임계 구역 안에서 중단해야 한다면 `Mutex.withLock { }`을 쓴다. 중단 함수가 없는 짧은 임계 구역이라면 `synchronized`도 괜찮다. 스레드를 아주 잠깐만 붙잡기 때문이다.

⚠ Kotlin 2.3 컴파일러는 `synchronized { }`, `ReentrantLock.withLock { }`(`kotlin.concurrent`), `@Synchronized suspend fun`까지 막아준다. 하지만 `lock.lock()` / `unlock()`을 직접 호출하고 그 사이에서 중단하는 코드는 **그대로 컴파일된다.**

```kotlin
lock.lock()
try { delay(10) } finally { lock.unlock() } // ❌ 컴파일은 되지만 위험하다
```

이 코드를 `Dispatchers.Default`에서 코루틴 20개로 돌리면 프로그램이 **멈춘다.** 락을 기다리는 코루틴들이 Default의 스레드를 모두 블로킹으로 붙잡는 바람에, 락을 쥔 코루틴이 재개할 스레드를 얻지 못하기 때문이다. 다른 스레드에서 재개되면 `unlock()`에서 `IllegalMonitorStateException`이 날 수도 있다. 에러 메시지 없이 조용히 멈출 수 있어서 실무에서는 이쪽이 더 위험하다. 코루틴 코드에서 락이 필요하면 `Mutex`를 쓴다.

**3. `Mutex`를 재진입 가능하다고 가정한다**

```kotlin
fun main() = runBlocking {
    val mutex = Mutex()
    val result = withTimeoutOrNull(1_000) {
        mutex.withLock {
            println("바깥 withLock 진입")
            mutex.withLock { "안쪽 withLock 진입" } // ❌ 같은 코루틴이 다시 잠그려 한다
        }
    }
    println("결과: $result")
}
```

```
바깥 withLock 진입
결과: null           ← 안쪽 withLock이 영원히 기다리다가 타임아웃
```

✅ 락을 잡은 함수 안에서 같은 락을 잡는 다른 함수를 부르지 않는다. 락은 바깥 한 곳에서만 잡고, 안쪽 함수는 "이미 락 안에서 호출된다"는 전제로 만든다.

**4. 논블로킹 호출의 동시 수를 `limitedParallelism`으로 제한한다**

예제 3에서 본 대로 한도가 지켜지지 않는다. ✅ 외부 API 호출 수 제한은 `Semaphore`, 블로킹 작업에 쓸 스레드 수 제한은 `limitedParallelism`이다.

### 바꿔보기

실행 전에 결과를 한 줄로 예상해본다.

1. 예제 1에서 `withContext(Dispatchers.Default)`를 지우고 `runBlocking`의 스레드 하나에서만 실행하면 결과가 어떻게 될까?
2. 예제 3의 `callExternalApi()`에서 `delay(100)`을 `Thread.sleep(100)`으로 바꾸면 `Semaphore(10)`과 `limitedParallelism(10)`의 최대 동시 호출 수와 소요 시간은 각각 어떻게 될까? (두 실행 모두 결과가 바뀐다. 기다리기 지루하면 `repeat(100)`을 `repeat(20)`으로 줄인다)
3. 예제 3의 `Semaphore(10)`을 `Semaphore(50)`으로 바꾸면 소요 시간은 얼마가 될까?

### 정리

- 코루틴도 여러 스레드 위에서 돌기 때문에 공유 상태에는 동기화가 필요하다. `@Volatile`로는 부족하고 `Atomic*`, `Mutex`, 스레드 한정을 쓴다.
- `Mutex`는 기다리는 동안 스레드를 붙잡지 않고 중단한다. 대신 재진입이 안 된다. `synchronized` 안에서는 중단 함수를 부를 수 없다.
- `Semaphore`는 중단된 코루틴까지 세고, `limitedParallelism`은 실행 중인 스레드만 센다. 외부 API 동시 호출 제한에는 `Semaphore`를 쓴다.

**실무 연결**: 외부 API의 동시 요청 제한은 `Semaphore`로, 블로킹 라이브러리 호출에 쓸 스레드 수는 `limitedParallelism`으로 정한다. 둘을 함께 쓰는 경우도 많다.

→ 2단계: [2-1](../2-advanced/part-2-shared-state.md#2-1-코루틴에서도-race-condition은-생긴다-핵심), [2-2](../2-advanced/part-2-shared-state.md#2-2-mutex와-synchronized-핵심), [2-3](../2-advanced/part-2-shared-state.md#2-3-동시-실행-수-제한하기-핵심)

---

## C-2. Flow 기초

### 이 장에서 답할 질문

- 값을 여러 개 비동기로 돌려줘야 할 때 `List`가 아니라 `Flow`를 쓰는 이유는?
- Flow는 언제 실행되나? 어느 스레드에서 실행되나?
- Flow에서 예외는 어떻게 다루나? 생산자가 소비자보다 빠르면?

### 개념

**왜 Flow인가**

suspend 함수는 값을 **하나** 돌려준다. 값이 여러 개라면 선택지가 셋이다.

| | 값을 언제 주나 | 값 사이에 중단 함수(`delay`, API 호출)를 쓸 수 있나 |
|---|---|---|
| `suspend fun (): List<T>` | 전부 모은 뒤 한 번에 | 모으는 동안은 가능. 다 모일 때까지 첫 값도 못 받는다 |
| `Sequence<T>` | 하나씩, 필요할 때 | ❌ 불가능 (스레드를 막는 방식만 가능) |
| `Flow<T>` | 하나씩, 필요할 때 | ✅ 가능 |

`Flow`는 "중단 가능한 Sequence"라고 보면 된다. 값이 준비되는 대로 하나씩 흘려보내고, 값 사이에서 스레드를 막지 않고 기다릴 수 있다.

**Flow는 cold stream이다**

`flow { }`로 만든 Flow는 **레시피**일 뿐이다. 만들기만 해서는 아무 일도 일어나지 않는다. 최종 연산자(`collect`, `toList`, `first` 등)를 호출해야 비로소 블록이 실행되고, `collect`할 때마다 **처음부터 다시** 실행된다.

```
val flow = flow { ... }   ← 레시피 작성 (실행 안 됨)
flow.collect { ... }      ← 요리 시작 (블록 실행)
flow.collect { ... }      ← 처음부터 다시 요리 (블록 다시 실행)
```

**연산자: 중간 연산자와 최종 연산자**

- 중간 연산자(`map`, `filter`, `take`, `onEach` 등)는 새 Flow를 돌려줄 뿐 실행하지 않는다.
- 최종 연산자(`collect`, `toList`, `first`, `single` 등)는 suspend 함수이고, Flow를 실제로 실행한다.

값은 **하나씩 차례로** 체인 전체를 통과한다. 값 1이 `map` → `filter` → `collect`까지 내려간 뒤에 값 2가 만들어진다.

**어느 스레드에서 실행되나: `flowOn`**

기본적으로 Flow는 `collect`를 호출한 코루틴의 컨텍스트에서 실행된다. 생산 쪽만 다른 디스패처에서 돌리고 싶으면 `flowOn`을 쓴다.

```
flow { emit }  →  map  →  flowOn(IO)  →  onEach  →  collect
└──── IO 스레드에서 실행 ────┘            └── collect한 코루틴의 스레드 ──┘
        (위쪽 = upstream)                      (아래쪽 = downstream)
```

`flowOn`은 **자신보다 위쪽**에만 영향을 준다. 반대로 `flow { }` 안에서 `withContext`로 감싸서 `emit`하면 안 된다. Flow는 "값은 collect한 쪽의 컨텍스트에서 emit한다"는 규칙(context preservation)을 지켜야 하고, 이를 어기면 예외가 난다.

**예외와 완료: `catch`, `onCompletion`**

- `catch { }`는 **자신보다 위쪽**에서 난 예외를 잡는다. 잡은 뒤 대체 값을 `emit`할 수 있다.
- `onCompletion { cause -> }`는 Flow가 끝날 때 항상 호출된다. 정상 종료면 `cause`가 `null`, 예외나 취소로 끝나면 그 원인이 들어온다. `finally`와 비슷하다.

**생산자가 빠르고 소비자가 느리면: `buffer`, `conflate`, `collectLatest`**

기본 Flow는 생산과 소비가 같은 코루틴에서 번갈아 일어난다. 소비자가 느리면 생산자도 기다린다. 이것이 Flow의 기본 backpressure다. 이 동작을 바꾸는 연산자가 세 개 있다.

| 연산자 | 동작 | 언제 쓰나 |
|---|---|---|
| `buffer()` | 생산자와 소비자를 다른 코루틴으로 나누고 사이에 버퍼를 둔다 | 모든 값을 처리하되 생산과 소비를 겹치고 싶을 때 |
| `conflate()` | 소비자가 바쁜 동안 들어온 값 중 **가장 최근 값만** 남긴다 | 중간 값은 버려도 되는 상태 갱신(시세, 진행률) |
| `collectLatest { }` | 새 값이 오면 처리 중이던 블록을 **취소**하고 새 값으로 다시 시작한다 | 마지막 값만 의미 있는 경우(검색어 자동완성) |

### 예제

**예제 1. cold stream: collect할 때마다 처음부터 실행된다**

```kotlin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun numbers(): Flow<Int> = flow {
    println("  [flow] 시작")
    for (i in 1..3) {
        delay(100) // 값 사이에 중단 함수를 쓸 수 있다
        println("  [flow] emit $i")
        emit(i)
    }
}

fun main() = runBlocking {
    val flow = numbers()
    println("Flow를 만들었다")

    flow.collect { println("첫 번째 collect: $it") }

    flow.map { it * 10 }
        .filter { it > 10 }
        .collect { println("두 번째 collect: $it") }
}
```

```
Flow를 만들었다          ← 만들기만 해서는 "시작"이 찍히지 않는다
  [flow] 시작
  [flow] emit 1
첫 번째 collect: 1       ← 값 하나가 끝까지 내려간 뒤 다음 값을 만든다
  [flow] emit 2
첫 번째 collect: 2
  [flow] emit 3
첫 번째 collect: 3
  [flow] 시작            ← 두 번째 collect에서 처음부터 다시 실행된다
  [flow] emit 1          ← 10은 filter에서 걸러진다
  [flow] emit 2
두 번째 collect: 20
  [flow] emit 3
두 번째 collect: 30
```

**예제 2. `flowOn`은 위쪽에만 영향을 준다**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun log(msg: String) = println("%-8s: %s".format(msg, Thread.currentThread().name))

fun main() = runBlocking {
    flow { log("emit"); emit(1) }
        .map { log("map"); it }
        .flowOn(Dispatchers.IO) // 위쪽(emit, map)만 IO에서 실행된다
        .onEach { log("onEach") }
        .collect { log("collect") }
}
```

```
emit    : DefaultDispatcher-worker-1
map     : DefaultDispatcher-worker-1
onEach  : main
collect : main
```

`Dispatchers.IO`의 스레드 이름도 `DefaultDispatcher-worker-N`이다. IO와 Default가 스레드 풀을 공유하기 때문이다(B-4).

**예제 3. `catch`와 `onCompletion`**

```kotlin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    flow {
        emit(1)
        emit(2)
        throw IllegalStateException("업스트림 실패")
    }
        .onCompletion { cause -> println("onCompletion: cause = $cause") }
        .catch { e ->
            println("catch: ${e.message}")
            emit(-1) // 대체 값을 내보낼 수 있다
        }
        .collect { println("받음: $it") }
}
```

```
받음: 1
받음: 2
onCompletion: cause = java.lang.IllegalStateException: 업스트림 실패
catch: 업스트림 실패
받음: -1
```

**예제 4. 느린 소비자: `buffer`, `conflate`, `collectLatest`**

```kotlin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime

// 생산자: 100ms마다 값 하나 (1..5)
fun producer() = flow { for (i in 1..5) { delay(100); emit(i) } }

// 소비자: 값 하나에 300ms
suspend fun measure(name: String, collectWith: suspend (suspend (Int) -> Unit) -> Unit) {
    val done = mutableListOf<Int>()
    val elapsed = measureTime { collectWith { delay(300); done += it } }
    println("%-13s %5dms 처리한 값 %s".format(name, elapsed.inWholeMilliseconds, done))
}

fun main() = runBlocking {
    measure("기본") { c -> producer().collect { c(it) } }
    measure("buffer()") { c -> producer().buffer().collect { c(it) } }
    measure("conflate()") { c -> producer().conflate().collect { c(it) } }
    measure("collectLatest") { c -> producer().collectLatest { c(it) } }
}
```

```
기본             2039ms 처리한 값 [1, 2, 3, 4, 5]   ← (100 + 300) × 5 ≈ 2,000ms
buffer()       1640ms 처리한 값 [1, 2, 3, 4, 5]   ← 100 + 300 × 5 ≈ 1,600ms
conflate()     1014ms 처리한 값 [1, 3, 5]         ← 바쁜 동안 온 값 중 일부가 버려졌다(실행에 따라 [1, 4, 5]도 나올 수 있다)
collectLatest   824ms 처리한 값 [5]               ← 처리 중에 새 값이 오면 취소, 마지막 5만 끝까지
```

시간은 대략적인 값이다. `conflate()`가 어떤 값을 건너뛰는지는 타이밍에 따라 달라질 수 있다.

### ❌/✅ 함정

**1. Flow 안에서 `withContext`로 감싸서 emit한다**

```kotlin
flow {
    withContext(Dispatchers.IO) { emit(1) } // ❌
}.collect { println(it) }
```

```
IllegalStateException: Flow invariant is violated:
		Flow was collected in [BlockingCoroutine{Active}@..., BlockingEventLoop@...],
		but emission happened in [DispatchedCoroutine{Active}@..., Dispatchers.IO].
		Please refer to 'flow' documentation or use 'flowOn' instead
```

✅ `flow { emit(1) }.flowOn(Dispatchers.IO)`처럼 `flowOn`을 쓴다. 블록 안에서 값을 **계산**만 `withContext`로 하고 `emit`은 바깥에서 하는 것은 괜찮다.

```kotlin
flow {
    val data = withContext(Dispatchers.IO) { loadBlocking() } // ✅ 계산만 IO에서
    emit(data)                                               // emit은 원래 컨텍스트에서
}
```

**2. Flow를 만들기만 하고 실행됐다고 생각한다**

```kotlin
fun saveAll(): Flow<Unit> = flow { repository.saveAll(...); emit(Unit) }

saveAll() // ❌ 아무 일도 일어나지 않는다. collect하지 않았기 때문이다
```

✅ Flow는 최종 연산자를 호출해야 실행된다. 결과가 필요 없으면 `collect()`를 호출하거나, 애초에 suspend 함수로 만든다.

**3. 같은 Flow를 여러 번 collect하면서 한 번만 실행될 거라 기대한다**

API를 호출하는 Flow를 두 번 collect하면 API도 두 번 호출된다(예제 1). ✅ 결과를 재사용하려면 `toList()`로 한 번 모으거나, 여러 구독자가 공유해야 한다면 hot flow([C-3](#c-3-channel과-hot-flow-선택))를 쓴다.

**4. `catch`가 아래쪽 예외까지 잡을 거라 기대한다**

```kotlin
flow.catch { ... }.collect { throw RuntimeException() } // ❌ collect 안의 예외는 catch가 잡지 않는다
```

✅ `catch`는 위쪽만 잡는다. `collect` 블록의 처리까지 보호하려면 처리 로직을 `onEach`로 옮기고 그 아래에 `catch`를 둔 뒤 `collect()`를 호출한다.

### 바꿔보기

실행 전에 결과를 한 줄로 예상해본다.

1. 예제 1에서 `flow.map { ... }` 체인 앞에 `.take(1)`을 붙이면 `[flow] emit`은 몇 번 찍힐까?
2. 예제 2에서 `.flowOn(Dispatchers.IO)`를 `.onEach { }` 아래(즉 `collect` 바로 위)로 옮기면 네 줄의 스레드 이름은 각각 어떻게 될까?
3. 예제 4에서 소비자 처리 시간을 300ms에서 50ms로 줄이면 네 방식의 결과(시간, 처리한 값)는 어떻게 달라질까?

### 정리

- Flow는 값을 여러 개 비동기로 흘려보내는 cold stream이다. 최종 연산자를 호출할 때 실행되고, collect할 때마다 처음부터 다시 실행된다.
- 실행 스레드는 `flowOn`으로 바꾼다. `flowOn`과 `catch`는 모두 **자신보다 위쪽**에만 영향을 준다. Flow 안에서 `withContext`로 emit하면 안 된다.
- 느린 소비자는 `buffer`(모두 처리), `conflate`(최신 값만), `collectLatest`(이전 처리 취소)로 다룬다.

**실무 연결**: Spring Data의 코루틴 리포지토리(`CoroutineCrudRepository`, R2DBC 같은 리액티브 모듈 전용이고 Spring Data JPA에는 없다)는 여러 건 조회를 `Flow`로 돌려주고, Spring MVC와 WebFlux는 `Flow`를 반환하는 핸들러를 SSE나 스트리밍 응답으로 내보낸다.

→ 2단계: [3-1](../2-advanced/part-3-flow.md#3-1-cold-stream-언제-실행되는가-핵심), [3-2](../2-advanced/part-3-flow.md#3-2-연산자와-컨텍스트-핵심), [3-3](../2-advanced/part-3-flow.md#3-3-backpressure와-buffer-선택), [3-4](../2-advanced/part-3-flow.md#3-4-예외-취소-완료-선택)

---

## C-3. Channel과 hot flow `선택`

> 백엔드 API 서버에서는 C-1, C-2보다 쓰일 일이 적다. 개념만 훑고 넘어가도 2단계 핵심 장을 진행하는 데 지장이 없다.

### 이 장에서 답할 질문

- 코루틴끼리 값을 주고받으려면 무엇을 쓰나?
- 작업 큐와 워커 N개 구조를 코루틴으로 만들려면?
- 값 하나를 여러 구독자에게 동시에 알리려면? Flow와 무엇이 다른가?

### 개념

**Channel: 코루틴 사이의 파이프**

`Channel`은 코루틴끼리 값을 주고받는 통로다. Java의 `BlockingQueue`와 비슷하지만, 가득 차거나 비었을 때 스레드를 막는 대신 **코루틴을 중단**한다.

```
생산자 코루틴 ──send()──▶ [ Channel ] ──receive()──▶ 소비자 코루틴
```

- 공유 메모리를 락으로 보호하는 대신, 상태는 코루틴 하나만 갖고 나머지는 메시지로 요청하는 방식이다. "메모리를 공유해서 통신하지 말고, 통신해서 메모리를 공유하라."
- 값 하나는 소비자 **하나**에게만 간다. 소비자가 여럿이면 값을 나눠 받는다. 이것이 워커풀의 바탕이다.
- `close()`는 "더 보낼 값이 없다"는 신호다. 소비자의 `for (x in channel)` 루프는 남은 값을 다 받은 뒤 끝난다. close하지 않으면 루프는 다음 값을 영원히 기다린다.

**버퍼 종류**

| 생성 | 버퍼 | `send`가 중단되는 때 |
|---|---|---|
| `Channel()` (RENDEZVOUS, 기본) | 없음 | 받는 쪽이 꺼내 갈 때까지 항상 |
| `Channel(capacity = n)` | n개 | 버퍼가 가득 찼을 때 |
| `Channel(Channel.BUFFERED)` | 기본 크기(64) | 버퍼가 가득 찼을 때 |
| `Channel(Channel.UNLIMITED)` | 무제한 | 중단되지 않는다(대신 메모리가 무한히 늘 수 있다) |
| `Channel(Channel.CONFLATED)` | 최신 값 1개 | 중단되지 않는다(이전 값은 덮어쓴다) |

**hot flow: 구독자와 상관없이 살아 있는 Flow**

C-2의 Flow는 cold다. collect할 때마다 새로 실행되고, 구독자마다 자기만의 실행을 가진다. **hot flow**는 반대다. 구독자가 있든 없든 하나의 흐름이 존재하고, 모든 구독자가 같은 값을 **함께** 받는다(브로드캐스트).

| | Channel | `SharedFlow` | `StateFlow` |
|---|---|---|---|
| 값 하나를 받는 수 | 소비자 하나 | 모든 구독자 | 모든 구독자 |
| 구독 전 값 | 버퍼에 남아 기다린다 | `replay` 개수만큼만 다시 받는다(기본 0개, 나머지는 사라짐) | 현재 값 하나를 바로 받는다 |
| 같은 값 연속 | 모두 전달 | 모두 전달 | 이전 값과 같으면 전달하지 않는다 |
| 값이 빠르게 바뀔 때 | 버퍼 설정을 따른다 | 버퍼 설정을 따른다 | 최신 값 하나만 유지한다. 느린 구독자는 중간 값을 건너뛸 수 있다 |
| 비유 | 작업 대기열 | 방송, 이벤트 버스 | 현재 상태 표시판 |

- `MutableStateFlow(초기값)`는 항상 값 하나를 갖고, `value`로 바로 읽고 쓴다.
- `MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow)`는 이벤트 스트림이다.
- `stateIn`, `shareIn`은 cold Flow를 hot flow로 바꾼다. 예를 들어 DB 변경을 읽는 Flow 하나를 여러 구독자가 공유하게 만들 때 쓴다. 이 장에서는 이름만 소개하고, 2단계 [3-5](../2-advanced/part-3-flow.md#3-5-hot-flow-stateflow와-sharedflow-선택)에서 다룬다.

### 예제

**예제 1. RENDEZVOUS Channel: send는 받는 쪽을 기다린다**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

val start = System.currentTimeMillis()
fun log(msg: String) = println("%4dms  %s".format(System.currentTimeMillis() - start, msg))

fun main() = runBlocking {
    val channel = Channel<Int>() // 기본값 RENDEZVOUS: 버퍼 없음

    launch { // 생산자
        for (i in 1..3) {
            log("send $i 시도")
            channel.send(i) // 받는 쪽이 꺼내 갈 때까지 중단된다
        }
        channel.close() // 더 보낼 값이 없다는 신호
    }

    for (value in channel) { // 소비자: close될 때까지 반복한다
        log("    receive $value")
        delay(300) // 느린 소비자
    }
    log("채널이 닫혀서 for 루프가 끝났다")
}
```

```
  54ms  send 1 시도
  63ms  send 2 시도         ← 1은 바로 전달됐고, 2는 소비자가 꺼내 갈 때까지 기다린다
  63ms      receive 1
 369ms      receive 2       ← 소비자가 300ms 뒤 꺼내 가자
 370ms  send 3 시도         ← 그제야 생산자가 다음으로 진행한다
 670ms      receive 3
 976ms  채널이 닫혀서 for 루프가 끝났다
```

시간은 대략적인 값이다(첫 값의 수십 ms는 JVM 시작 시간이 섞인 것이다). 생산자가 소비자 속도에 맞춰 끌려가는 것이 보인다.

**예제 2. 워커풀: 작업 9개를 워커 3개가 나눠 처리한다**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.time.measureTime

fun main() = runBlocking {
    val elapsed = measureTime {
        val jobs = Channel<Int>(capacity = 100)
        val processed = IntArray(3) // 워커별 처리 건수

        val workers = List(3) { id ->
            launch {
                for (job in jobs) { // 값 하나는 워커 하나에게만 간다
                    delay(100) // 작업 처리
                    processed[id]++
                }
            }
        }
        repeat(9) { jobs.send(it) }
        jobs.close()
        workers.joinAll()
        println("워커별 처리 건수: ${processed.toList()}")
    }
    println("소요 시간: $elapsed")
}
```

```
워커별 처리 건수: [3, 3, 3]
소요 시간: 337ms              ← 9건 ÷ 워커 3개 × 100ms ≈ 300ms
```

**예제 3. StateFlow와 SharedFlow**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() = runBlocking {
    val state = MutableStateFlow("READY")       // 현재 값 하나를 항상 가진다
    val events = MutableSharedFlow<String>()   // replay = 0

    val a = launch { state.collect { println("[state 구독자] $it") } }
    val b = launch { events.collect { println("[event 구독자 1] $it") } }
    val c = launch { events.collect { println("[event 구독자 2] $it") } }
    delay(100) // 구독자들이 구독을 시작할 시간을 준다

    state.value = "RUNNING"
    delay(50)
    state.value = "RUNNING" // 같은 값
    delay(50)
    state.value = "DONE"

    events.emit("order-created")
    delay(100)
    listOf(a, b, c).forEach { it.cancel() } // hot flow의 collect는 스스로 끝나지 않는다
}
```

```
[state 구독자] READY        ← 구독하자마자 현재 값을 받는다
[state 구독자] RUNNING
[state 구독자] DONE         ← 같은 값 RUNNING은 두 번 전달되지 않았다
[event 구독자 1] order-created
[event 구독자 2] order-created   ← 이벤트 하나를 두 구독자가 모두 받았다
```

### ❌/✅ 함정

**1. Channel을 close하지 않는다**

생산자가 `close()`를 빼먹으면 소비자의 `for` 루프가 끝나지 않고, 그 코루틴을 기다리는 부모 스코프도 끝나지 않는다. ✅ 생산자는 작업이 끝나면 반드시 `close()`한다. 생산자 코루틴과 채널을 함께 만들어주는 `produce { }` 빌더를 쓰면 블록이 끝날 때 자동으로 닫힌다.

**2. `UNLIMITED` 버퍼로 문제를 덮는다**

✅ 소비자가 느린데 버퍼를 무제한으로 두면 값이 메모리에 계속 쌓인다. 버퍼 크기를 정하고, 가득 찼을 때 생산자가 기다리게(backpressure) 두는 것이 기본이다.

**3. hot flow의 collect가 끝나길 기다린다**

```kotlin
state.collect { ... }
println("여기는 실행되지 않는다") // ❌ StateFlow/SharedFlow의 collect는 스스로 끝나지 않는다
```

✅ 구독은 별도 코루틴(`launch`)에서 하고, 필요 없어지면 그 Job을 취소한다. 값 하나만 필요하면 `first()`를 쓴다.

**4. 서버가 여러 대인데 hot flow로 이벤트를 공유한다**

✅ `SharedFlow`와 `Channel`은 **프로세스 메모리 안**에서만 공유된다. 서버 인스턴스가 여러 대면 다른 인스턴스의 구독자는 이벤트를 받지 못하고, 재시작하면 남은 값은 사라진다. 인스턴스 사이의 이벤트는 Kafka나 Redis Pub/Sub 같은 외부 수단으로 보낸다.

### 바꿔보기

실행 전에 결과를 한 줄로 예상해본다.

1. 예제 1의 `Channel<Int>()`를 `Channel<Int>(capacity = 3)`으로 바꾸면 `send 3 시도`는 몇 ms쯤 찍힐까?
2. 예제 2에서 `jobs.close()` 대신 `jobs.cancel()`을 부르면 워커별 처리 건수는 어떻게 될까? 아직 처리되지 않은 작업은 어떻게 될까?
3. 예제 3에서 `delay(100) // 구독자들이 구독을 시작할 시간을 준다`를 지우면 각 구독자는 무엇을 받을까?
4. 예제 3에서 `state.value` 사이의 `delay(50)` 두 줄을 지우면 state 구독자는 무엇을 받을까?

### 정리

- `Channel`은 코루틴 사이의 큐다. 값 하나는 소비자 하나에게만 가고, 버퍼가 차면 `send`가 중단된다. 생산자는 끝나면 `close()`한다.
- `SharedFlow`는 모든 구독자에게 방송하고, `StateFlow`는 현재 값 하나를 유지하며 같은 값은 다시 보내지 않는다. `StateFlow`는 "모든 변화"가 아니라 "현재 상태"를 전하는 도구라서, 느린 구독자는 중간 값을 놓칠 수 있다.
- 둘 다 한 프로세스 메모리 안에서만 동작한다. 유실되면 안 되거나 서버 여러 대가 공유해야 하는 이벤트에는 외부 메시징을 쓴다.

**실무 연결**: 백엔드에서는 요청 처리 중 비동기 후처리 큐, 배치의 워커풀(Channel), 설정 값이나 캐시 상태 공유(StateFlow), 인스턴스 내부 이벤트 전파(SharedFlow) 정도에 쓰인다.

→ 2단계: [2-4](../2-advanced/part-2-shared-state.md#2-4-channel-코루틴-간-통신-선택), [3-5](../2-advanced/part-3-flow.md#3-5-hot-flow-stateflow와-sharedflow-선택)
