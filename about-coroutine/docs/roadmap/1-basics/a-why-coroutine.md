# A. 왜 코루틴인가

코루틴을 배우기 전에, 코루틴이 해결하려는 문제부터 확인한다. 스레드는 비싸고, 블로킹은 그 비싼 스레드를 놀린다. 그 문제를 풀려던 기존 방법에도 한계가 있다.

[← 1단계 목차](./README.md)

---

## A-1. 스레드 모델과 블로킹의 비용

### 이 장에서 답할 질문

- Java 스레드는 왜 마음껏 만들 수 없을까?
- 블로킹 호출은 정확히 무엇을 낭비할까?
- Tomcat 스레드 200개짜리 서버는 초당 몇 건까지 처리할 수 있을까?

### 개념

#### 1. Java 스레드 = OS 스레드

Java의 플랫폼 스레드(`Thread`)는 OS 스레드와 **1:1**로 대응한다. `thread.start()`를 호출하면 JVM이 OS에 네이티브 스레드를 하나 만들어 달라고 요청한다(macOS, Linux에서는 `pthread_create`).

```
 JVM                         OS
┌──────────────┐           ┌──────────────┐
│ Thread-0     │ ────────▶ │ OS 스레드 #1 │
│ Thread-1     │ ────────▶ │ OS 스레드 #2 │
│ Thread-2     │ ────────▶ │ OS 스레드 #3 │
└──────────────┘           └──────────────┘
```

그래서 스레드 하나를 만들고 유지하는 데 OS 수준의 비용이 든다.

| 비용 | 내용 |
|---|---|
| **메모리** | 스레드마다 자기 호출 스택을 가진다. 스택은 힙 바깥(네이티브 메모리)에 잡히므로 `-Xmx`에 포함되지 않는다 |
| **생성 시간** | 시스템 콜과 커널 자료구조 생성이 필요하다 |
| **개수 한도** | OS가 프로세스당 스레드 수를 제한한다 |
| **컨텍스트 스위칭** | **실행 가능한(RUNNABLE) 스레드**가 CPU 코어보다 많으면 OS가 스레드를 번갈아 실행한다. 바꿀 때마다 레지스터 저장과 복원, 캐시 손실이 생긴다. sleep이나 IO 대기 중인 스레드는 스케줄링 대상이 아니므로, 비용은 스레드 총수보다 **깨어나는 횟수**(IO 완료 등)와 동시에 RUNNABLE인 스레드 수에 비례한다 |

#### 2. 0-1에서 직접 잰 숫자

2단계 0-1에서 이 맥(Java 25, macOS ARM)으로 직접 잰 값이다. 표에 나오는 용어는 이렇다.

- **reserved(예약)**: 주소 공간만 잡아둔 양. 물리 메모리는 아직 쓰지 않는다.
- **committed(확정)**: JVM이 실제로 쓰겠다고 OS에 확정한 양. NMT(Native Memory Tracking)로 본다. `-XX:NativeMemoryTracking=summary`로 실행하고 `jcmd <pid> VM.native_memory summary`로 조회한다.
- **RSS**: 프로세스가 지금 물리 메모리에 올려둔 양. OS가 센다(`ps -o rss= -p <pid>`).

| 항목 | 측정값 |
|---|---|
| 기본 스택 크기(`ThreadStackSize`) | 2,048KB (2MB). 플랫폼마다 다르다. Linux x64는 보통 1MB |
| 스레드 1,000개의 스택 예약(reserved) | +2,016MB |
| 스택 실제 사용(NMT committed) | +40MB, 스레드당 약 40KB |
| 실제 물리 메모리(RSS) | +89MB, **스레드당 약 90KB** |
| 개수 한도 | `kern.num_taskthreads` = 8,192 (macOS 프로세스당). `Thread-8159`에서 실패 |

스택 2MB는 **주소 공간을 예약**만 할 뿐이다. 물리 메모리는 스택을 실제로 건드린 페이지만큼만 들어간다. 그래서 스레드 하나의 실제 비용은 "2MB"가 아니라 **"건드린 스택 페이지 + 스레드별 JVM·네이티브 자료구조"**다. 이 맥에서는 RSS 기준 약 90KB였고, 그중 스택 committed가 약 40KB, 나머지 약 50KB는 스택이 아닌 스레드별 메모리(JVM 스레드 구조, malloc, 처음 건드린 힙 페이지 등)다. 스택을 깊게 쓰면 최대 2MB까지 늘어난다.

`Thread-0`부터 `Thread-8159`까지는 8,160개다. 여기에 JVM이 원래 띄워둔 스레드(main, Reference Handler, GC, JIT 컴파일러 등)를 더하면 프로세스 한도 8,192에 닿는다.

한도에 걸리면 이런 에러가 난다. 메모리가 모자라서가 아니라 OS 제한 때문이다(`EAGAIN`).

```
[warning][os,thread] Failed to start thread "Unknown thread" - pthread_create failed (EAGAIN) ...
Exception in thread "main" java.lang.OutOfMemoryError: unable to create native thread:
    possibly out of memory or process/resource limits reached
```

스레드 수 한도는 OS마다 다르다.

| OS | 주요 한도 |
|---|---|
| macOS | 프로세스당 `kern.num_taskthreads`(이 맥에서 8,192) |
| Linux | `ulimit -u`(사용자당 프로세스·스레드 수), `kernel.threads-max`, `vm.max_map_count`(기본 65,530, 스레드 약 3만 개 근처에서 걸린다), 컨테이너의 `pids.max` |

Linux에서는 설정에 따라 수만 개까지도 만들 수 있다. 하지만 **수십만~수백만 개**는 어떤 OS에서도 현실적이지 않다. 그리고 그만큼 만들 수 있더라도 메모리, 생성 비용, 한꺼번에 깨어날 때의 스케줄링 비용이 따라온다.

#### 3. 블로킹 = 스레드를 붙잡고 아무것도 안 하기

외부 API를 호출하고 응답을 기다리는 동안, 그 스레드는 **아무 일도 하지 않으면서 다른 일에 쓰일 수도 없다.** 이것이 블로킹이다.

```
스레드 1개가 외부 API를 300ms 기다리는 동안:

시간 ─────────────────────────────────────────▶
     [요청 받음][───── 응답 대기 300ms ─────][응답 조립]
       CPU 사용      CPU 안 씀, 스레드는 점유됨     CPU 사용
```

식당에 비유하면 이렇다. 웨이터(스레드)가 주문을 주방에 넣고, 요리가 나올 때까지 **주방 앞에 서서 기다린다.** 그동안 다른 손님 주문은 받지 못한다. 손님이 늘면 웨이터를 더 뽑아야 하는데, 웨이터는 비싸다.

블로킹 중인 스레드는 스레드 덤프에서 이렇게 보인다.

| 상황 | 스레드 상태 |
|---|---|
| `Thread.sleep()` | `TIMED_WAITING` |
| 다른 스레드가 가진 `synchronized` 락 대기 | `BLOCKED` |
| `LockSupport.park`, `Future.get()`, `CompletableFuture.join()`, 일감을 기다리는 풀 워커 | `WAITING` (시간 제한이 있으면 `TIMED_WAITING`) |
| 전통적인 소켓 `read()`로 응답 대기 | `RUNNABLE` (JVM은 네이티브 IO 대기를 구분하지 못한다) |
| CPU 계산 중 | `RUNNABLE` |

같은 "HTTP 응답 대기"라도 클라이언트 구현에 따라 상태가 다르다. 소켓에서 직접 읽는 클라이언트는 `RUNNABLE`, 내부적으로 다른 스레드의 결과를 기다리는 클라이언트(JDK `HttpClient.send` 등)는 `WAITING`으로 보인다.

#### 4. thread-per-request와 처리량의 한계

Spring MVC(Tomcat)는 **요청 하나에 스레드 하나**를 배정한다. 응답을 보낼 때까지 그 스레드는 그 요청 전용이다. Tomcat의 기본 최대 스레드 수는 200이다(`server.tomcat.threads.max`).

그러면 처리량의 상한은 간단한 식으로 나온다(Little's Law).

```
최대 처리량(TPS) ≈ 스레드 수 / 요청 하나가 스레드를 점유하는 시간

스레드 200개, 요청당 1초(대부분 외부 API 대기)
  → 200 / 1초 = 초당 200건

201번째 요청부터는 스레드가 빌 때까지 줄을 선다.
CPU는 거의 놀고 있는데도 그렇다.
```

처리량을 늘리는 방법은 두 가지뿐이다. 스레드를 늘리거나, 요청 하나가 스레드를 점유하는 시간을 줄이는 것이다. 스레드는 앞에서 본 대로 비싸다. 그러니 **기다리는 동안에는 스레드를 놓아주는 방법**이 필요하다.

### 예제

#### 예제 1. 스레드 1,000개 만들기

```kotlin
import java.lang.management.ManagementFactory
import kotlin.concurrent.thread
import kotlin.time.measureTime

fun main() {
    val threadMXBean = ManagementFactory.getThreadMXBean()
    println("시작 전 스레드 수: ${threadMXBean.threadCount}")

    val threads = mutableListOf<Thread>()
    val elapsed = measureTime {
        repeat(1_000) { i ->
            threads += thread(name = "worker-$i") { Thread.sleep(3_000) }
        }
    }

    println("스레드 1,000개 생성: $elapsed")
    println("생성 후 스레드 수: ${threadMXBean.threadCount}")
    threads.forEach { it.join() }
    println("모두 종료 후 스레드 수: ${threadMXBean.threadCount}")
}
```

출력 (시간과 시작 스레드 수는 환경마다 다르다):

```
시작 전 스레드 수: 6
스레드 1,000개 생성: 48.8ms
생성 후 스레드 수: 1006
모두 종료 후 스레드 수: 6
```

시작 전에도 스레드가 6개 있다. `main`, `Reference Handler`, `Finalizer`, `Signal Dispatcher`, `Common-Cleaner`, `Notification Thread`다. GC와 JIT 컴파일러 스레드도 있지만 `ThreadMXBean`에는 잡히지 않는다(`jcmd <pid> Thread.print`나 NMT로 보인다).

생성 시간은 1,000개에 약 49ms, 스레드 하나에 약 50µs다. 요청 하나 처리하는 데 비하면 작지만, 요청마다 스레드를 새로 만들면 쌓인다. 스레드풀이 스레드를 재사용하는 이유가 이것이다.

#### 예제 2. 블로킹 중인 스레드의 상태

```kotlin
import kotlin.concurrent.thread

fun main() {
    val sleeper = thread(name = "sleeper") { Thread.sleep(5_000) }
    val lock = Any()
    val holder = thread(name = "holder") { synchronized(lock) { Thread.sleep(5_000) } }
    Thread.sleep(100)
    val waiter = thread(name = "waiter") { synchronized(lock) { } }
    val worker = thread(name = "worker") {
        var x = 0L
        while (!Thread.currentThread().isInterrupted) x++
    }
    Thread.sleep(100)

    listOf(sleeper, holder, waiter, worker).forEach { println("${it.name}: ${it.state}") }
    worker.interrupt()
}
```

출력:

```
sleeper: TIMED_WAITING
holder: TIMED_WAITING
waiter: BLOCKED
worker: RUNNABLE
```

`sleeper`, `holder`, `waiter`는 CPU를 전혀 쓰지 않는다. 그래도 스레드 하나씩을 붙잡고 있다.

#### 예제 3. 스레드 2개짜리 서버에 요청 6개

```kotlin
import java.util.concurrent.Executors
import kotlin.time.measureTime

fun main() {
    // 요청 처리 스레드가 2개뿐인 서버라고 생각하자
    val pool = Executors.newFixedThreadPool(2)

    val elapsed = measureTime {
        val futures = (1..6).map { i ->
            pool.submit {
                Thread.sleep(1_000) // 외부 API 응답을 1초 기다린다 (블로킹)
                println("요청 $i 완료 (${Thread.currentThread().name})")
            }
        }
        futures.forEach { it.get() }
    }

    println("요청 6개 처리: $elapsed")
    pool.shutdown()
}
```

출력 (완료 순서와 스레드 배정은 실행마다 다를 수 있다):

```
요청 1 완료 (pool-1-thread-1)
요청 2 완료 (pool-1-thread-2)
요청 3 완료 (pool-1-thread-2)
요청 4 완료 (pool-1-thread-1)
요청 5 완료 (pool-1-thread-2)
요청 6 완료 (pool-1-thread-1)
요청 6개 처리: 3.03s
```

요청 6개가 1초짜리 대기를 하는데 3초가 걸렸다. Little's Law 그대로 `2 / 1초 = 초당 2건`이다. CPU는 3초 내내 거의 놀았다.

### ❌/✅ 함정

**❌ "스레드를 늘리면 해결된다"**

```properties
# 처리량 문제를 스레드 수로 해결하려는 설정
server.tomcat.threads.max=5000
```

✅ 스레드 수에는 OS 한도가 있고, 스레드가 많을수록 메모리가 늘고 한꺼번에 깨어날 때 스케줄링 비용이 커진다. 게다가 스레드를 늘리면 병목이 사라지는 게 아니라 **다음 자원으로 옮겨간다.** DB 커넥션 풀(HikariCP 기본 10개)이나 하위 서비스가 먼저 버티지 못한다. 적당히 늘리는 것이 맞는 해법일 때도 있지만, 대기 시간이 길다면 **대기 중에 스레드를 붙잡지 않는 방식**을 고려한다. 이 방식이 코루틴, Virtual Thread, WebFlux다.

**❌ "`-Xmx`를 넉넉히 잡았으니 메모리는 안전하다"**

✅ 스레드 스택은 힙 바깥에 있어서 `-Xmx`에 포함되지 않는다. 컨테이너 메모리 한도를 정할 때는 `힙 + 스레드 수 × 실제 스택 사용량 + 기타 네이티브 메모리`를 함께 계산한다.

**❌ "CPU 사용률이 낮으니 서버에 여유가 있다"**

✅ 블로킹 대기가 많은 서버는 CPU가 놀면서도 스레드가 바닥날 수 있다. 처리량이 막혔는지는 CPU가 아니라 **바쁜 스레드 수와 대기 중인 요청 수**로 판단한다.

### 바꿔보기

실행하기 전에 결과를 한 줄로 예상해본다.

1. 예제 3에서 `newFixedThreadPool(2)`를 `newFixedThreadPool(6)`으로 바꾸면 총 소요 시간은?
2. 예제 3에서 스레드는 2개 그대로 두고 요청을 6개에서 7개로 늘리면 총 소요 시간은?
3. 예제 2에서 `lock`을 `Object()`로 만들고, `holder`의 `Thread.sleep(5_000)`을 `lock.wait(5_000)`으로 바꾸면 `holder`와 `waiter`의 상태는 어떻게 바뀔까? (Kotlin이 `Object` 사용에 경고를 내지만 실험용이라 괜찮다)

### 정리

- Java 스레드는 OS 스레드와 1:1이다. 메모리, 생성 시간, OS 한도, 스케줄링 비용이 있어서 수십만 개 이상은 현실적이지 않다.
- 블로킹 호출은 CPU를 쓰지 않으면서 스레드를 붙잡는다.
- thread-per-request 서버의 처리량 상한은 `스레드 수 / 요청당 점유 시간`이다. 그래서 **기다리는 동안 스레드를 놓아주는 방법**이 필요하다.

**실무 연결**: 외부 API 지연이 늘었을 때 CPU는 한가한데 응답이 밀린다면, Tomcat 스레드 고갈을 먼저 의심한다. `tomcat.threads.busy`가 `tomcat.threads.config.max`에 붙어 있는지 본다(Actuator 메트릭, `server.tomcat.mbeanregistry.enabled=true` 필요).

→ 2단계: [0-1 스레드 비용](../2-advanced/part-0-jvm-concurrency.md#0-1-스레드는-얼마나-비싼가-핵심), [0-2 스레드풀](../2-advanced/part-0-jvm-concurrency.md#0-2-스레드풀과-executor-핵심), [0-3 블로킹과 스레드 덤프](../2-advanced/part-0-jvm-concurrency.md#0-3-블로킹-io는-스레드를-붙잡는다-핵심), [0-6 Tomcat 스레드 고갈](../2-advanced/part-0-jvm-concurrency.md#0-6-tomcat-요청-스레드-모델과-스레드-고갈-핵심)

→ 다음: [A-2. 비동기 코드의 진화와 한계](#a-2-비동기-코드의-진화와-한계)

---

## A-2. 비동기 코드의 진화와 한계

### 이 장에서 답할 질문

- 코루틴 이전에는 "기다리는 동안 스레드를 놓아주는" 문제를 어떻게 풀었을까?
- `CompletableFuture`로 충분하지 않은 이유는 무엇일까?
- 스레드를 넘나들면 왜 로그의 요청 ID(MDC)가 사라질까?

### 개념

#### 1. 콜백: 기다리지 말고 "끝나면 불러줘"

블로킹의 반대 방향은 **결과를 기다리지 않고, 끝나면 호출할 함수를 넘기는 것**이다. 식당 비유로는 웨이터가 주방에 주문을 넣고 진동벨을 맡긴 뒤 다음 손님에게 가는 방식이다. 단, 웨이터가 정말 다음 손님에게 갈 수 있는지는 그 아래 IO가 논블로킹인지에 달려 있다(아래 3번).

문제는 호출이 이어질 때다. 결제를 조회하고, 그 결과로 배송을 조회하고, 그 결과로 쿠폰을 조회하면 코드가 안으로 계속 파고든다(아래는 개념 설명용 의사 코드다).

```kotlin
getPayment(orderId) { payment ->
    getDelivery(payment) { delivery ->
        getCoupon(delivery) { coupon ->
            respond(payment, delivery, coupon)
            // 예외 처리는? 타임아웃은? 중간에 취소하려면?
        }
    }
}
```

이것을 흔히 **콜백 지옥**이라고 부른다. 읽기 어렵고, 예외와 취소를 단계마다 따로 처리해야 한다.

#### 2. CompletableFuture: 콜백을 조합 가능한 값으로

Java 8의 `CompletableFuture`는 "나중에 완료될 결과"를 값으로 다룬다. 콜백을 중첩하지 않고 연산자로 이어 붙인다.

| 하고 싶은 것 | 연산자 |
|---|---|
| 비동기로 실행 | `supplyAsync { }`, `runAsync { }` (Executor를 안 주면 `ForkJoinPool.commonPool()`에서 실행. 단 commonPool 병렬도가 1 이하인 환경, 즉 vCPU 1~2개짜리 컨테이너에서는 작업마다 새 스레드를 만든다) |
| 결과를 변환 | `thenApply` |
| 이어서 비동기 호출 | `thenCompose` |
| 두 결과 합치기 | `thenCombine` |
| 모두 기다리기 | `allOf` |
| 예외 처리 | `exceptionally`, `handle` |

콜백 지옥보다는 훨씬 낫다. 하지만 백엔드 코드에 쓰면 곧 한계에 부딪힌다.

#### 3. 비동기 ≠ 논블로킹

여기서 가장 헷갈리기 쉬운 점이 있다. **비동기로 실행한다고 스레드가 풀려나는 것은 아니다.**

```
supplyAsync { restClient.get()... }   ← 블로킹 호출을 다른 스레드로 옮겼을 뿐

[main]          ─ 다른 일을 할 수 있다(또는 join으로 기다린다)
[commonPool-1]  ─ [────── 응답 대기, 스레드 점유 ──────]   ← 블로킹은 그대로 있다
```

기다리는 동안 정말 스레드를 놓아주려면 **IO 자체가 논블로킹**이어야 한다. OS가 "응답이 오면 알려줄게"를 지원하고(Linux의 epoll, macOS의 kqueue), 소수의 스레드가 수많은 연결의 완료 알림을 받아 처리하는 방식이다. Java NIO, Netty, Spring WebClient, JDK `HttpClient.sendAsync`가 이 위에서 동작한다. 코루틴 이전에 Spring이 내놓은 답은 이 논블로킹 IO 위에 Reactor(`Mono`/`Flux`)로 콜백을 조합하는 WebFlux였다.

| 방식 | 기다리는 동안 스레드는? |
|---|---|
| 블로킹 호출을 그냥 실행 | 호출한 스레드가 붙잡힌다 |
| 블로킹 호출을 `supplyAsync`로 감쌈 | **다른 스레드**가 붙잡힌다 (응답 시간은 병렬화로 줄 수 있다) |
| 논블로킹 IO + 콜백 | 아무 스레드도 붙잡히지 않는다 |

코루틴도 마찬가지다. 코루틴 안에서 블로킹 JDBC나 RestClient를 부르면 스레드는 그대로 붙잡힌다. 코루틴은 논블로킹 IO를 **순차 코드처럼 쓰게 해주는 도구**이지, 블로킹 IO를 논블로킹으로 바꿔주지는 않는다(B-4, D-1).

#### 4. CompletableFuture의 한계

| 한계 | 무슨 일이 생기나 | 코루틴의 해법 |
|---|---|---|
| **코드 모양이 다르다** | 순차 로직도 `thenCompose` 체인으로 써야 한다. `if`, `for`, `try`를 평소처럼 쓰기 어렵다 | suspend 함수는 평범한 순차 코드처럼 쓴다 → [B-1](./b-coroutine-core.md#b-1-첫-코루틴-suspend와-delay), [B-3](./b-coroutine-core.md#b-3-suspend는-어떻게-동작하나) |
| **취소가 실행 중인 작업을 못 멈춘다** | `cancel(true)`는 future 상태만 "취소됨"으로 바꾼다. 이미 돌고 있는 작업은 끝까지 실행된다 | 협력적 취소 → [B-5](./b-coroutine-core.md#b-5-취소와-타임아웃) |
| **예외가 감싸져서 나온다** | `join()`은 `CompletionException`, `get()`은 `ExecutionException`으로 원래 예외를 감싼다 | 원래 예외가 그대로 던져진다 → [B-6](./b-coroutine-core.md#b-6-예외-처리) |
| **부모-자식 관계가 없다** | 하나가 실패해도 나머지는 계속 돈다. 요청이 끝난 뒤에도 남은 작업이 자원을 쓴다(누수) | 구조화된 동시성 → [B-2](./b-coroutine-core.md#b-2-스코프와-빌더-구조화된-동시성) |
| **스레드 경계에서 ThreadLocal이 사라진다** | 작업이 다른 스레드로 넘어가면 MDC, 트랜잭션, 보안 컨텍스트가 비어 있다 | 코루틴에서도 똑같이 사라진다. 대신 재개될 때마다 값을 복원하는 컨텍스트 요소가 있다(MDC 등) → [B-4](./b-coroutine-core.md#b-4-디스패처와-컨텍스트). JPA 트랜잭션은 이 방법으로 해결되지 않는다 → [D-1](./d-spring.md#d-1-spring-mvc에서-코루틴) |

#### 5. ThreadLocal과 스레드 경계

`ThreadLocal`은 **스레드마다 따로 보관하는 변수**다. Spring은 요청 처리에 필요한 정보를 여기에 많이 담는다.

| Spring 기능 | ThreadLocal에 담는 것 |
|---|---|
| 로깅 MDC(Logback) | 요청 ID, 사용자 ID |
| `@Transactional` | 현재 트랜잭션, DB 커넥션 |
| Spring Security | `SecurityContext`(인증 정보) |
| `RequestContextHolder` | 현재 HTTP 요청 |

thread-per-request 모델에서는 요청 하나가 끝까지 한 스레드에서 처리되므로 문제가 없다. 그런데 작업을 다른 스레드로 넘기는 순간, 그 스레드의 ThreadLocal은 **비어 있다.**

```
[http-nio-exec-1]  MDC: requestId=req-123
       │
       │ supplyAsync { log.info("결제 조회") }
       ▼
[commonPool-worker-1]  MDC: (비어 있음)  → 로그에 requestId가 안 찍힌다
```

비동기로 갈수록 스레드 경계를 자주 넘는다. 그래서 컨텍스트를 **명시적으로 옮겨주는 방법**이 필요하다.

### 예제

#### 예제 1. 순차 호출 vs CompletableFuture 병렬 호출

2단계 공통 시나리오(결제 300ms, 배송 500ms, 쿠폰 200ms)의 축소판이다.

```kotlin
import java.util.concurrent.CompletableFuture
import kotlin.time.measureTime

fun fetch(name: String, delayMs: Long): String {
    Thread.sleep(delayMs) // 외부 API 호출이라고 생각하자
    return name
}

fun main() {
    val sequential = measureTime {
        val result = listOf(fetch("결제", 300), fetch("배송", 500), fetch("쿠폰", 200))
        println("순차: $result")
    }
    println("순차 소요: $sequential")

    val parallel = measureTime {
        val payment = CompletableFuture.supplyAsync { fetch("결제", 300) }
        val delivery = CompletableFuture.supplyAsync { fetch("배송", 500) }
        val coupon = CompletableFuture.supplyAsync { fetch("쿠폰", 200) }

        val result = payment
            .thenCombine(delivery) { p, d -> listOf(p, d) }
            .thenCombine(coupon) { pd, c -> pd + c }
            .join()
        println("병렬: $result")
    }
    println("병렬 소요: $parallel")
}
```

출력 (시간은 대략. 코어 4개 이상 기준이다. 코어가 3개 이하면 commonPool 스레드가 2개 이하라서 병렬 소요가 더 길어진다):

```
순차: [결제, 배송, 쿠폰]
순차 소요: 1.03s
병렬: [결제, 배송, 쿠폰]
병렬 소요: 514ms
```

병렬로 부르니 가장 느린 호출(500ms) 정도로 줄었다. 하지만 **스레드는 하나도 풀려나지 않았다.** `main`은 `join()`에서 기다리며 붙잡혀 있고, `commonPool` 스레드 3개도 `Thread.sleep`으로 붙잡혀 있다. 블로킹된 스레드가 1개에서 4개로 오히려 늘었다. 응답 시간은 줄었지만 이것은 병렬화의 효과이지 논블로킹이 아니다(개념 3).

#### 예제 2. cancel(true)는 실행 중인 작업을 멈추지 못한다

```kotlin
import java.util.concurrent.CompletableFuture

fun main() {
    val future = CompletableFuture.runAsync {
        repeat(5) { i ->
            Thread.sleep(200)
            println("작업 진행 중... $i")
        }
        println("작업 끝까지 실행됨")
    }

    Thread.sleep(300)
    println("cancel 결과: ${future.cancel(true)}, isCancelled: ${future.isCancelled}")

    Thread.sleep(1_000) // 취소한 작업이 어떻게 되는지 지켜본다
}
```

출력:

```
작업 진행 중... 0
cancel 결과: true, isCancelled: true
작업 진행 중... 1
작업 진행 중... 2
작업 진행 중... 3
작업 진행 중... 4
작업 끝까지 실행됨
```

future는 "취소됨"인데 작업은 끝까지 돌았다. `CompletableFuture`는 실행 중인 스레드를 interrupt하지 않는다. javadoc에도 `mayInterruptIfRunning` 값은 이 구현에서 효과가 없다고 적혀 있다.

#### 예제 3. 하나가 실패해도 나머지는 계속 돈다

```kotlin
import java.util.concurrent.CompletableFuture

fun main() {
    val payment = CompletableFuture.supplyAsync<String> {
        Thread.sleep(100)
        throw IllegalStateException("결제 서버 오류")
    }
    val delivery = CompletableFuture.supplyAsync {
        Thread.sleep(500)
        println("배송 조회는 계속 실행되어 끝났다")
        "배송"
    }

    try {
        val result = payment.join() + delivery.join()
        println(result)
    } catch (e: Exception) {
        println("잡힌 예외: ${e::class.simpleName}")
        println("원래 예외: ${e.cause}")
    }

    Thread.sleep(1_000) // 실패 이후 delivery가 어떻게 되는지 지켜본다
}
```

출력:

```
잡힌 예외: CompletionException
원래 예외: java.lang.IllegalStateException: 결제 서버 오류
배송 조회는 계속 실행되어 끝났다
```

두 가지를 볼 수 있다.

- 예외가 `CompletionException`으로 감싸져서 나왔다. 원래 예외를 보려면 `cause`를 꺼내야 한다.
- 결제가 실패해서 이미 응답을 포기했는데도, 배송 조회는 계속 실행되어 끝났다. 둘 사이에 부모-자식 관계가 없어서 서로의 실패를 모른다. 실제 서버라면 이 작업이 쓸모없이 외부 API와 스레드를 쓴다.

#### 예제 4. 스레드가 바뀌면 ThreadLocal이 사라진다

```kotlin
import java.util.concurrent.CompletableFuture

val requestId = ThreadLocal<String>()

fun main() {
    requestId.set("req-123")
    println("[${Thread.currentThread().name}] requestId = ${requestId.get()}")

    CompletableFuture.runAsync {
        println("[${Thread.currentThread().name}] requestId = ${requestId.get()}")
    }.join()
}
```

출력:

```
[main] requestId = req-123
[ForkJoinPool.commonPool-worker-1] requestId = null
```

MDC도 내부적으로 ThreadLocal이라서, 실제 서비스에서는 이 지점부터 로그에 요청 ID가 빠진다.

### ❌/✅ 함정

**❌ 원래 예외 타입으로 catch한다**

```kotlin
try {
    future.join()
} catch (e: IllegalStateException) { // 잡히지 않는다. CompletionException으로 감싸져 있다
}
```

✅ `CompletionException`(또는 `get()`이면 `ExecutionException`)을 잡고 `cause`를 꺼내거나, `exceptionally`/`handle`로 처리한다.

**❌ `cancel(true)`로 작업이 멈췄다고 믿는다**

✅ `CompletableFuture`의 취소는 결과를 기다리는 쪽만 정리한다. 실행 중인 작업을 멈추려면 작업 안에서 직접 취소 여부를 확인하는 코드를 따로 짜야 한다.

**❌ Executor 없이 `supplyAsync`에서 블로킹 호출을 한다**

```kotlin
CompletableFuture.supplyAsync { restClient.get()... } // commonPool 스레드를 블로킹한다
```

✅ `commonPool`은 JVM 전체가 함께 쓰는 CPU 작업용 풀이고 크기가 작다(보통 코어 수 - 1). vCPU 1~2개짜리 컨테이너에서는 아예 작업마다 스레드를 새로 만든다. 블로킹 IO는 크기를 정한 전용 Executor를 넘겨서 실행한다.

**❌ 다른 스레드에서도 MDC가 있을 거라고 가정한다**

✅ 스레드를 넘길 때 컨텍스트를 복사하는 장치가 필요하다(예: `TaskDecorator`, Micrometer Context Propagation). 코루틴에서는 재개될 때마다 값을 복원하는 컨텍스트 요소를 쓴다(B-4).

### 바꿔보기

실행하기 전에 결과를 한 줄로 예상해본다.

1. 예제 1에서 세 `supplyAsync`에 모두 `Executors.newFixedThreadPool(1)`로 만든 Executor 하나를 넘기면 병렬 소요 시간은? (끝에 `shutdown()`을 호출해야 프로그램이 종료된다)
2. 예제 2를 `CompletableFuture` 대신 `Executors.newSingleThreadExecutor().submit { ... }`과 `Future.cancel(true)`로 바꾸면 작업은 어떻게 될까? (마찬가지로 `shutdown()`을 잊지 않는다. 작업 안에서 예외가 나도 `Future`에 담겨서 콘솔에 안 찍힐 수 있으니, 작업 블록을 `try/catch`로 감싸 출력해본다)
3. 예제 4의 `ThreadLocal`을 `InheritableThreadLocal`로 바꾸고, `runAsync` 대신 `Executors.newFixedThreadPool(1)`에 작업을 두 번 제출한다. 첫 제출 전에 값을 `"req-1"`로, 두 번째 제출 전에 `"req-2"`로 바꾸면 각 작업은 무엇을 출력할까? (`shutdown()` 포함)

### 정리

- 콜백은 논블로킹 IO와 함께 쓸 때 스레드를 놓아준다. 블로킹 호출을 다른 스레드로 옮기는 것만으로는 스레드가 풀리지 않는다.
- `CompletableFuture`는 콜백을 조합 가능한 값으로 바꿨다.
- 그래도 순차 코드처럼 쓰기 어렵고, 취소가 실행 중 작업을 멈추지 못하며, 예외가 감싸지고, 부모-자식 관계가 없어 누수가 생긴다.
- 스레드를 넘나들면 ThreadLocal(MDC, 트랜잭션, 보안)이 사라진다.

**실무 연결**: 비동기 코드를 도입했더니 로그에서 요청 ID가 빠지거나, 실패한 요청의 하위 호출이 계속 돈다면 이 장의 한계를 그대로 겪고 있는 것이다.

→ 2단계: [0-4 CompletableFuture](../2-advanced/part-0-jvm-concurrency.md#0-4-completablefuture로-병렬-조합하기-핵심), [0-5 ThreadLocal](../2-advanced/part-0-jvm-concurrency.md#0-5-threadlocal과-스레드-경계-핵심)

→ 다음: [B. 코루틴 핵심](./b-coroutine-core.md)에서 이 한계를 하나씩 해결한다.
