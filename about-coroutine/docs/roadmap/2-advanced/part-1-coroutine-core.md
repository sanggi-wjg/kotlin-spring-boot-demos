# Part 1. 코루틴 핵심

> 모듈: `basics`
>
> Spring 없이 순수 Kotlin으로 코루틴의 동작 원리를 확인한다. Part 0에서 겪은 문제(스레드 비용, CompletableFuture의 예외와 취소)를 코루틴은 어떻게 다루는지 계속 비교한다.

---

## 1-1. 첫 코루틴: 중단(suspend)은 블로킹이 아니다 `핵심`

**목표**: 코루틴 1만 개와 스레드 1만 개의 차이를 잰다. 측정 방식은 0-1과 같다.

**핵심 개념**
> 1단계 [B-1](../1-basics/b-coroutine-core.md#b-1-첫-코루틴-suspend와-delay)에서 먼저 익힌다.

- 코루틴은 실행 도중 중단(suspend)했다가 나중에 이어서 실행할 수 있는 계산 단위다. 코루틴은 스레드 위에서 실행되지만 스레드 자체는 아니다.
- 중단 함수(`suspend fun`)는 코루틴 안이나 다른 중단 함수 안에서만 호출할 수 있다. `delay`는 중단 함수이고, `Thread.sleep`은 일반 블로킹 함수다.
- `launch`는 코루틴을 시작하고 `Job`을 돌려준다. `runBlocking`은 `main`처럼 일반 함수에서 코루틴을 시작하는 진입점이다. 자세한 동작은 1-3에서 본다.
- 0-1과 같은 방식으로 재므로 0-1 노트의 숫자가 이 장의 비교 기준이다.

**실행 전 예측**
- 코루틴 10만 개를 `delay`로 중단시켜 둔 상태에서 힙은 얼마나 늘어날까? 코루틴 하나당 몇 바이트일까? 0-1에서 잰 스레드당 약 90KB와 비교하면 몇 배 차이일까?
- 코루틴 1,000개가 각각 `Thread.sleep(100)`을 한다. `runBlocking` 단일 스레드, `Dispatchers.Default`, `Dispatchers.IO`에서 각각 돌리면 전체 시간은 몇 초일까? 이 맥의 코어 수로 계산해보자.

**실험 과제**
1. 0-1과 같은 항목(시간, 메모리, 스레드 수)을 코루틴으로 다시 잰다. 메모리는 힙 사용량(GC 후)과 RSS를 함께 본다.
2. `delay` 버전과 `Thread.sleep` 버전을 `runBlocking` 단일 스레드, `Dispatchers.Default`, `Dispatchers.IO`에서 비교한다. 실행 중인 스레드 이름을 로그로 남긴다.
   - ⚠ `Thread.sleep` 버전은 **N=100, 100ms**처럼 작게 시작한다. 예측이 맞는지 확인한 뒤에 키운다. 처음부터 1만 개 × 10초로 돌리면 테스트가 끝나지 않을 수 있다.

**완료 조건**
- [ ] 0-1과의 비교표(코루틴당 메모리 포함)
- [ ] "`delay` 중인 코루틴은 스레드를 점유하는가"에 대한 관찰 근거
- [ ] 디스패처별 `Thread.sleep` 소요 시간이 스레드 수로 설명되는지 확인

**열린 질문**
- 코루틴이 "가볍다"는 말이 성립하지 않는 경우는 언제인가? 코루틴당 메모리가 어떤 조건에서 커지는지 찾아보자.

**공식 문서**: [Coroutines basics](https://kotlinlang.org/docs/coroutines-basics.html)

---

## 1-2. suspend와 Continuation `핵심`

**목표**: `suspend fun`이 컴파일되면 어떤 모습이 되는지 직접 본다. "중단"의 실체를 이해한다.

**핵심 개념**
> 1단계 [B-3](../1-basics/b-coroutine-core.md#b-3-suspend는-어떻게-동작하나)에서 먼저 익힌다.

- JVM에는 코루틴 전용 명령어가 없다. 코루틴 기능의 대부분은 Kotlin 컴파일러가 코드를 변환해서 구현하고, 결과는 일반 바이트코드와 클래스다.
- 중단 지점(suspension point)은 코루틴이 실행을 멈출 수 있는 위치로, 다른 중단 함수를 호출하는 곳이다. IntelliJ는 이 위치를 거터 아이콘으로 표시한다.
- 함수가 중간에 멈췄다가 이어서 실행되려면 "어디까지 실행했는지"와 "그때의 지역 변수 값"을 어딘가에 저장해야 한다. 디컴파일 결과에서 이 정보가 어디에 저장되는지 찾는 것이 이 장의 핵심이다.
- `suspendCoroutine`과 `suspendCancellableCoroutine`은 콜백 API를 중단 함수로 바꾸는 저수준 도구다. 블록 안에서 등록한 콜백이 호출될 때 결과를 넘겨 코루틴을 재개한다. 취소까지 고려하면 `suspendCancellableCoroutine`을 쓴다(1-7).

**실행 전 예측**
- 중단 지점이 2개이고 지역 변수 3개를 쓰는 `suspend fun`을 디컴파일하면, 상태 머신 객체에 저장되는 필드는 몇 개이고 `label`은 몇 가지 값을 가질까? 중단 지점을 넘기기 전에 쓰고 버리는 변수도 필드로 저장될까?
- `suspendCoroutine`으로 감싼 함수에서 콜백이 **이미 완료된 상태라 즉시** 호출되면, 그 함수는 중단될까? 반환값은 `COROUTINE_SUSPENDED`일까?

**실험 과제**
1. 중단 지점이 2개인 `suspend fun`을 작성한다. IntelliJ의 *Show Kotlin Bytecode → Decompile*로 컴파일 결과를 확인한다.
2. 상태 머신(`label`)과 `Continuation`이 어떻게 생겼는지 주석을 달며 해석한다. 어떤 지역 변수가 필드로 저장되는지 확인한다.
3. 콜백 기반 API(예: CompletableFuture)를 `suspendCoroutine`으로 직접 감싸서 `suspend fun`으로 만든다. 이미 완료된 future를 넘겼을 때와 나중에 완료되는 future를 넘겼을 때의 실행 스레드와 순서를 비교한다.

**완료 조건**
- [ ] 디컴파일 결과를 해석한 노트(CPS 변환, 상태 머신)
- [ ] 콜백을 suspend 함수로 바꾼 코드와 테스트

**열린 질문**
- suspend 함수가 suspend 함수를 10단계 깊이로 호출한 상태에서 중단되면, 힙에는 Continuation 객체가 몇 개 생길까? 이 구조가 예외 스택 트레이스와 디버깅에 어떤 영향을 줄까(7-1)?

**공식 문서**: [KEEP: Kotlin Coroutines 설계 문서](https://github.com/Kotlin/KEEP/blob/master/proposals/coroutines.md)

---

## 1-3. 코루틴 빌더와 Job `핵심`

**목표**: `runBlocking`, `launch`, `async`가 어떻게 다른지 이해한다. Job의 생명주기를 따라가 본다.

**핵심 개념**
> 1단계 [B-2](../1-basics/b-coroutine-core.md#b-2-스코프와-빌더-구조화된-동시성)에서 먼저 익힌다.

- `launch`는 결과가 없는 작업을 시작하고 `Job`을 돌려준다. `async`는 결과가 있는 작업을 시작하고 `Deferred<T>`(`Job`의 하위 타입)를 돌려주며, 결과는 `await()`로 받는다.
- `launch`와 `async`는 `CoroutineScope`의 확장 함수다. 코루틴은 항상 어떤 스코프 안에서 시작되고, 시작된 코루틴은 그 스코프 Job의 자식이 된다.
- 빌더의 `start` 파라미터(`CoroutineStart`)로 코루틴을 언제 시작할지 바꿀 수 있다.
- Job의 상태는 직접 노출되지 않는다. `isActive`, `isCompleted`, `isCancelled` 세 속성의 조합으로 확인하며, 조합표는 `Job` KDoc에 있다.
- `Deferred`는 0-4의 `CompletableFuture`와 비슷한 자리에 있다. `asDeferred()`와 `asCompletableFuture()`로 서로 변환할 수 있다.

**실행 전 예측**
- 공통 시나리오에 "배송 조회는 결제 결과(결제 ID)가 있어야 호출할 수 있다"는 의존이 생겼다. `async`를 어떻게 배치해야 가장 빠르고, 그때 소요 시간은 몇 ms일까?
- `runBlocking` 안에서 실행 중인 코루틴이 다시 `runBlocking`을 호출하면(중첩), 바깥 `runBlocking`에 대기 중이던 다른 코루틴은 안쪽 블록이 도는 동안 실행될까?
- `CoroutineStart.LAZY`로 만든 `async`를 `await`하지 않으면 어떻게 될까?

**실험 과제**
1. 공통 시나리오(300/500/200ms)를 세 가지 버전으로 만든다. `suspend` 함수를 순차로 호출하는 버전, `async`로 병렬 호출하는 버전, 결제 → 배송 의존이 있는 버전이다. 소요 시간을 비교한다.
2. 중첩 `runBlocking`에서 바깥 코루틴이 언제 실행되는지 로그로 확인한다.
3. Job의 상태 변화를 로그로 추적한다. 상태 흐름은 New → Active → Completing → Completed, 또는 Cancelling → Cancelled다.

**완료 조건**
- [ ] 0-4의 CompletableFuture 코드와 나란히 비교(코드 길이, 가독성)
- [ ] 흔한 실수를 재현하는 테스트: `async`를 만들자마자 `await`해서 결국 순차로 실행되는 경우

**공식 문서**: [Composing suspending functions](https://kotlinlang.org/docs/composing-suspending-functions.html)

---

## 1-4. 코루틴 테스트 도구 `핵심`

**목표**: 이후 모든 장에서 쓸 테스트 도구를 익힌다. `delay(10초)`가 들어간 테스트를 1ms 만에 끝내는 방법을 배운다.

**핵심 개념**
> 1단계 [B-7](../1-basics/b-coroutine-core.md#b-7-코루틴-테스트)에서 먼저 익힌다.

- `runTest`는 코루틴 테스트용 빌더다. 블록은 `TestScope` 안에서 실행되고, 테스트 디스패처는 가상 시간(virtual time)을 관리하는 `TestCoroutineScheduler`를 가진다.
- 가상 시간은 `currentTime`으로 읽고 `advanceTimeBy`, `advanceUntilIdle`, `runCurrent`로 진행시킨다.
- `StandardTestDispatcher`는 새 코루틴을 바로 실행하지 않고 스케줄러 큐에 넣는다. `UnconfinedTestDispatcher`는 새 코루틴을 즉시 실행하기 시작한다.
- 이후 장에서 개념을 assert로 증명하는 테스트는 모두 이 도구 위에서 돈다.

**실행 전 예측**
- `runTest` 안에서 `withTimeout(1_000) { delay(5_000) }`은 실제 시간으로 몇 ms 만에, 어떤 예외로 끝날까? 그때 `currentTime`은 얼마일까?
- 디스패처를 주입받도록 고친 서비스에 테스트 디스패처를 넘겼다. 그런데 서비스 안에서 `delay`가 아니라 `Thread.sleep(2_000)`을 한다면, 테스트의 실제 소요 시간과 `currentTime`은 각각 얼마일까?

**실험 과제**
1. `runTest`, `advanceTimeBy`, `advanceUntilIdle`, `currentTime`을 쓴다. 1-3의 병렬 실행 시간을 가상 시간으로 assert한다. 타임아웃(`withTimeout`)이 걸린 코드도 가상 시간으로 검증한다.
2. `StandardTestDispatcher`와 `UnconfinedTestDispatcher`의 실행 순서가 어떻게 다른지 테스트로 보여준다.
3. 디스패처를 주입받도록 설계해야 하는 이유를 정리한다. 위의 예측 질문과 연결해서 생각한다.

**완료 조건**
- [ ] 1-3의 병렬 실행이 "가상 시간 500ms"로 끝나는 것을 assert하는 테스트
- [ ] 디스패처 주입 패턴 정리

**공식 문서**: [kotlinx-coroutines-test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)

---

## 1-5. CoroutineContext와 Dispatcher `핵심`

**목표**: 코루틴이 **어느 스레드에서** 실행되는지 정하는 규칙을 이해한다.

**핵심 개념**
> 1단계 [B-4](../1-basics/b-coroutine-core.md#b-4-디스패처와-컨텍스트)에서 먼저 익힌다.

- `CoroutineContext`는 코루틴에 붙은 요소들의 집합이다. `Job`, `CoroutineDispatcher`, `CoroutineName`, `CoroutineExceptionHandler`가 대표적인 요소다.
- 디스패처는 코루틴을 어느 스레드나 스레드풀에서 실행할지 정하는 컨텍스트 요소다. 기본 제공 디스패처는 `Default`, `IO`, `Main`, `Unconfined`이고, 서버에서는 `Main`을 쓰지 않는다.
- `Default`는 CPU 계산용으로, `IO`는 블로킹 IO를 맡기는 용도로 설계되었다. 각 디스패처의 스레드 수는 KDoc에 적혀 있으니 예측을 적은 뒤에 확인한다.
- 빌더나 `withContext`에 컨텍스트를 넘기지 않은 요소는 부모 코루틴의 것을 물려받는다.
- `withContext(context) { ... }`는 블록을 지정한 컨텍스트에서 실행하고 결과를 돌려주는 중단 함수다. 새 코루틴을 병렬로 띄우는 것이 아니고, 호출한 쪽은 블록이 끝날 때까지 중단된다.

**실행 전 예측**
- `Dispatchers.IO`에서 블로킹 작업 64개가 돌고 있는 동안, `Dispatchers.Default`에서 100ms짜리 CPU 작업을 코어 수만큼 동시에 돌리면 몇 ms 걸릴까? IO 쪽 작업이 없을 때와 다를까?
- `Dispatchers.IO.limitedParallelism(100)`으로 만든 디스패처 두 개와 `Dispatchers.IO` 자체에 각각 `Thread.sleep(1000)` 작업 1,000개를 동시에 넣으면, JVM 스레드는 최대 몇 개까지 늘어날까?
- `Dispatchers.Default`에서 블로킹 작업이 몇 개 이상 동시에 돌면, 같은 `Default`를 쓰는 다른 CPU 작업이 눈에 띄게 느려지기 시작할까? 이 맥의 코어 수로 숫자를 예측해보자.
- `Executors.newFixedThreadPool(4).asCoroutineDispatcher()`와 `Dispatchers.IO.limitedParallelism(4)`는 스레드 덤프에서 어떻게 다르게 보일까? 애플리케이션이 끝날 때 각각 무엇을 정리해야 할까?

**실험 과제**
1. JVM 옵션 `-Dkotlinx.coroutines.debug`를 켜고 디스패처별 스레드 이름을 관찰한다. 이 옵션을 켜면 로그에 코루틴 이름도 함께 찍힌다.
2. IO 디스패처와 `limitedParallelism` view가 동시에 몇 개까지 실행하는지, 그때 JVM 스레드가 몇 개인지 실험으로 찾는다.
3. `limitedParallelism`과 `Executor.asCoroutineDispatcher()`로 전용 디스패처를 만든다. 스레드 덤프에서 두 디스패처의 스레드를 비교하고, 종료 시 정리 방법을 확인한다.
4. `Dispatchers.Unconfined`에서 중단 이후 실행 스레드가 어떻게 바뀌는지 관찰한다.
5. `Default`에서 동시에 도는 블로킹 작업 수를 늘려가며, 같은 `Default`를 쓰는 CPU 작업의 소요 시간을 잰다. IO에 블로킹 부하가 있을 때도 같은 측정을 한다.

**완료 조건**
- [ ] 디스패처별 스레드 수와 한도를 측정값으로 정리
- [ ] "블로킹 코드를 Default 디스패처에서 돌리면 안 되는 이유"를 실험으로 증명

**열린 질문**
- 같은 종류의 컨텍스트 요소를 `+`로 두 번 합치면 어떻게 될까? `launch(Dispatchers.IO + Dispatchers.Default)`는 어느 디스패처에서 돌까? 같은 규칙으로 `withContext(Dispatchers.IO + Job())`을 해석하면 무슨 일이 생길까?

**공식 문서**: [Coroutine context and dispatchers](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html)

---

## 1-6. 구조화된 동시성 (Structured Concurrency) `핵심`

**목표**: 부모-자식 관계가 생명주기, 취소, 예외를 어떻게 하나로 묶는지 이해한다. 0-4의 CompletableFuture와 가장 크게 다른 점이다.

**핵심 개념**
> 1단계 [B-2](../1-basics/b-coroutine-core.md#b-2-스코프와-빌더-구조화된-동시성)에서 먼저 익힌다.

- 구조화된 동시성은 코루틴의 생명주기를 코드의 범위(스코프)에 묶는 원칙이다. 스레드나 `CompletableFuture`처럼 "시작하고 잊는" 방식과 반대되는 설계다.
- `CoroutineScope`는 `coroutineContext` 하나만 가진 인터페이스다. 그 컨텍스트의 `Job`이 이 스코프에서 시작하는 코루틴들의 부모가 된다.
- 부모 Job과 자식 Job은 트리를 이룬다. 자식 목록은 `Job.children`으로 확인할 수 있다.
- `coroutineScope`와 `supervisorScope`는 새 스코프를 열고 블록을 실행하는 중단 함수다. 반면 `CoroutineScope(context)`는 스코프 객체를 만들어 돌려주는 일반 함수다.
- Spring에서는 "스코프를 누가 소유하고 언제 닫는가"가 설계 문제가 된다. Part 4에서 다시 다룬다.

**실행 전 예측**
- 주문 조회 함수가 `coroutineScope` 안에서 결과를 만들고, `launch`로 "조회 이력 저장"(2초)도 함께 띄운다. 이 함수의 응답 시간은 몇 ms일까? 이력 저장을 응답 뒤로 미루려면 무엇을 바꿔야 하고, 그 대가는 무엇일까?
- 부모를 `cancelAndJoin()`했는데 자식 하나가 `withContext(NonCancellable)` 블록 안에서 3초짜리 정리 작업을 하는 중이다. `cancelAndJoin()`은 몇 초 뒤에 반환될까?
- 다음 세 자식은 부모가 취소될 때 각각 함께 취소될까? `launch(Job()) { }`, `CoroutineScope(coroutineContext).launch { }`, `CoroutineScope(Dispatchers.IO).launch { }`

**실험 과제**
1. 두 가지를 테스트로 증명한다. 부모는 자식이 끝날 때까지 기다린다. 부모를 취소하면 자식도 취소된다.
   - 자식이 `NonCancellable`로 정리 중일 때 부모의 `cancelAndJoin()`이 언제 반환되는지도 잰다.
2. `GlobalScope`나 임의로 만든 `CoroutineScope`가 구조를 깨뜨리는 상황을 재현한다. 누수와 예외 유실을 확인한다.
   - 예측 3의 세 가지 자식을 나란히 놓고 부모 취소 후 상태를 비교한다.
3. `coroutineScope`와 `supervisorScope`를 가볍게 써본다. 본격적인 내용은 1-8에서 다룬다.

**완료 조건**
- [ ] 구조화된 동시성의 3가지 보장(대기, 취소 전파, 예외 전파)을 각각 테스트로 증명
- [ ] 구조를 깨는 안티패턴 재현 테스트

**열린 질문**
- Spring 빈 안에서 "요청과 상관없이 백그라운드로 돌릴 작업"이 필요하다면, 스코프를 어디에 두어야 할까?

**공식 문서**: [Roman Elizarov, Structured concurrency](https://elizarov.medium.com/structured-concurrency-722d765aa952)

---

## 1-7. 취소 (Cancellation) `핵심`

**목표**: 코루틴 취소는 **협력적(cooperative)**이다. 이 말의 의미와 한계를 확인한다.

**핵심 개념**
> 1단계 [B-5](../1-basics/b-coroutine-core.md#b-5-취소와-타임아웃)에서 먼저 익힌다.

- 취소는 `Job.cancel()`로 요청한다. 취소가 요청된 Job은 Cancelling을 거쳐 Cancelled 상태가 된다(1-3).
- 취소된 코루틴 안의 중단 함수는 `CancellationException`을 던진다. 이 예외는 실패가 아니라 "정상적인 취소"를 뜻하는 신호로 취급된다.
- Java에는 이와 별도로 `Thread.interrupt()`라는 중단 신호가 있다. JDK의 블로킹 메서드 중 상당수가 이 신호에 반응한다.
- `withTimeout`은 시간이 지나면 블록을 취소하고 `TimeoutCancellationException`(`CancellationException`의 하위 타입)을 던진다. `withTimeoutOrNull`은 예외 대신 `null`을 돌려준다.
- 요청이 타임아웃되거나 클라이언트가 연결을 끊었을 때 진행 중인 작업이 어떻게 되는지가 Part 4의 질문이다. 이 장의 규칙이 그 답의 바탕이 된다.

**실행 전 예측**
- 반복 1회에 50ms가 걸리는 CPU 루프에 `isActive` 확인을 넣었다. `cancel()` 후 실제로 멈추기까지 최대 몇 ms가 걸릴까? 반복마다 `yield()`를 넣으면 루프 전체 처리 시간은 얼마나 늘어날까?
- `runInterruptible { Thread.sleep(5000) }` 중인 코루틴을 취소하면 몇 ms 만에 멈출까? `runInterruptible` 안에서 `synchronized` 락을 기다리며 BLOCKED 상태라면?
- `withTimeout(1000)` 블록이 시간 초과로 취소되고, 블록의 `finally`에서 `withContext(NonCancellable) { delay(3000) }`으로 정리한다. `withTimeout`을 호출한 쪽은 몇 ms 뒤에 예외를 받을까?
- 서비스 코드에 `try { api() } catch (e: Exception) { log.warn(...); fallback }` 패턴이 이미 있다. 이 코드를 `withTimeoutOrNull(300)`으로 감쌌는데 `api()`가 1초 걸리면, 결과는 `fallback`일까 `null`일까? 응답은 300ms 안에 올까?

**실험 과제**
1. CPU 루프, `Thread.sleep`, `delay`, `synchronized` 락 대기가 각각 취소에 반응하는지 확인한다. 취소 요청부터 실제로 멈출 때까지 걸린 시간을 잰다.
2. `isActive`, `ensureActive()`, `yield()`, `runInterruptible`로 코드가 취소에 반응하도록 고친다. `yield()`를 넣었을 때의 처리 시간 변화도 잰다.
3. `finally` + `withContext(NonCancellable)`로 정리(cleanup) 작업을 수행한다. `withTimeout`과 함께 쓸 때 호출한 쪽이 예외를 받는 시점을 잰다.
4. `withTimeout`과 `withTimeoutOrNull`을 비교한다.
5. 예측 4의 결과를 바탕으로, 취소를 망가뜨리는 예외 처리 패턴을 재현하고 올바른 처리 방법을 정리한다.

**완료 조건**
- [ ] "어떤 코드가 취소에 반응하고 어떤 코드가 반응하지 않는가" 매트릭스
- [ ] CancellationException을 삼키는 버그를 재현하는 테스트와 올바른 처리

**열린 질문**
- 블로킹 JDBC 쿼리나 블로킹 HTTP 호출 중인 코루틴을 취소하면, 실제 쿼리나 커넥션은 어떻게 될까? (Part 4에서 검증한다)

**공식 문서**: [Cancellation and timeouts](https://kotlinlang.org/docs/coroutines-cancellation.html)

---

## 1-8. 예외 전파 `핵심`

**목표**: 예외가 코루틴 계층을 따라 어떻게 전파되는지 이해한다. 예외를 어디서 잡아야 하는지 정리한다.

**핵심 개념**
> 1단계 [B-6](../1-basics/b-coroutine-core.md#b-6-예외-처리)에서 먼저 익힌다.

- 코루틴 블록 안에서 중단 함수 호출을 `try/catch`로 감싸는 것은 일반 함수와 똑같이 동작한다. 이 장의 질문은 코루틴 **경계를 넘는** 예외가 어떻게 되는가다.
- Job에는 일반 `Job`과 `SupervisorJob` 두 종류가 있고, 자식의 실패를 다루는 방식이 다르다. `coroutineScope`와 `supervisorScope`가 각각 이 둘에 대응한다.
- `CoroutineExceptionHandler`는 아무도 처리하지 않은 예외를 마지막에 받는 컨텍스트 요소다. 로그나 알림 용도이고, 예외를 복구하지는 않는다.
- `CancellationException`은 예외 전파에서 따로 취급된다(1-7).
- Spring의 `@ExceptionHandler`까지 예외가 제대로 도달하려면 핸들러의 코루틴 안에서 예외가 어떻게 흐르는지 알아야 한다. Part 4에서 다시 확인한다.

**실행 전 예측**
- `supervisorScope` 안에서 `async` 세 개 중 하나가 실패했는데 아무도 그 `await()`를 부르지 않으면, 예외는 어디에 남을까? 로그에 찍힐까?
- 쿠폰 조회가 실패하면 빈 목록으로, 배송 조회가 실패하면 전체 실패로 처리하고 싶다. `coroutineScope`와 `supervisorScope`를 어떻게 중첩해야 할까? 배송이 실패하는 순간 진행 중이던 결제 호출은 어떻게 될까?
- `CoroutineExceptionHandler`를 단 애플리케이션 스코프에서 `launch`한 작업 안에서 `coroutineScope { launch { throw ... } }`가 실패하면, 핸들러는 몇 번 호출되고 어떤 예외를 받을까?
- 자식 두 개가 거의 동시에 서로 다른 예외로 실패하면, 부모가 던지는 예외에 두 번째 예외는 어떤 형태로 남을까?

**실험 과제**
1. `launch`와 `async`의 예외 전파가 어떻게 다른지 테스트로 확인한다. 자식 두 개가 동시에 실패할 때 부모가 던지는 예외에 무엇이 담기는지도 확인한다.
2. `coroutineScope`와 `supervisorScope`에서 형제 코루틴이 취소되는지 비교한다.
3. `CoroutineExceptionHandler`가 동작하는 위치와 동작하지 않는 위치를 찾는다.
4. 공통 시나리오에서 "쿠폰 조회가 실패해도 나머지 정보로 응답한다"를 구현한다.

**완료 조건**
- [ ] 예외 전파 규칙 정리표(launch, async, 스코프 종류별)
- [ ] 부분 실패를 허용하는 패턴 구현과 테스트

**공식 문서**: [Coroutine exceptions handling](https://kotlinlang.org/docs/exception-handling.html)

**판단 가이드에 남길 것**: "부분 실패를 허용해야 하는 병렬 호출은 어떤 구조로 짜는가", "취소가 안전하려면 어떤 조건이 필요한가"
