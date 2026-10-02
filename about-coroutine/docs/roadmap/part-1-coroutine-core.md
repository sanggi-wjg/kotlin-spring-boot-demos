# Part 1. 코루틴 핵심

> 모듈: `basics`
>
> Spring 없이 순수 Kotlin으로 코루틴의 동작 원리를 확인한다. Part 0에서 겪은 문제(스레드 비용, CompletableFuture의 예외와 취소)를 코루틴은 어떻게 다루는지 계속 비교한다.

---

## 1-1. 첫 코루틴: 중단(suspend)은 블로킹이 아니다 `핵심`

**목표**: 코루틴 1만 개와 스레드 1만 개의 차이를 잰다. 측정 방식은 0-1과 같다.

**실행 전 예측**
- `launch`로 코루틴 1만 개를 만들어 각각 `delay(10_000)` 시키면, 스레드는 몇 개 생길까?
- `delay` 대신 `Thread.sleep(10_000)`을 쓰면 전체 실행 시간은 어떻게 달라질까?

**실험 과제**
1. 0-1과 같은 항목(시간, 메모리, 스레드 수)을 코루틴으로 다시 잰다.
2. `delay` 버전과 `Thread.sleep` 버전을 비교한다. 실행 중인 스레드 이름을 로그로 남긴다.
   - ⚠ `Thread.sleep` 버전은 **N=100, 100ms**처럼 작게 시작한다. 예측이 맞는지 확인한 뒤에 키운다. 처음부터 1만 개 × 10초로 돌리면 테스트가 끝나지 않을 수 있다.

**완료 조건**
- [ ] 0-1과의 비교표
- [ ] "`delay` 중인 코루틴은 스레드를 점유하는가"에 대한 관찰 근거

**열린 질문**
- 코루틴이 "가볍다"는 말은 정확히 무엇이 가볍다는 뜻인가?

**공식 문서**: [Coroutines basics](https://kotlinlang.org/docs/coroutines-basics.html)

---

## 1-2. suspend와 Continuation `핵심`

**목표**: `suspend fun`이 컴파일되면 어떤 모습이 되는지 직접 본다. "중단"의 실체를 이해한다.

**실행 전 예측**
- 함수에 `suspend` 키워드만 붙이고 안에서 아무것도 중단하지 않으면, 이 함수는 비동기로 실행될까?
- 컴파일된 `suspend fun`의 시그니처에는 어떤 파라미터가 추가될까?

**실험 과제**
1. 중단 지점이 2개인 `suspend fun`을 작성한다. IntelliJ의 *Show Kotlin Bytecode → Decompile*로 컴파일 결과를 확인한다.
2. 상태 머신(`label`)과 `Continuation`이 어떻게 생겼는지 주석을 달며 해석한다.
3. 콜백 기반 API(예: CompletableFuture)를 `suspendCoroutine`으로 직접 감싸서 `suspend fun`으로 만든다.

**완료 조건**
- [ ] 디컴파일 결과를 해석한 노트(CPS 변환, 상태 머신)
- [ ] 콜백을 suspend 함수로 바꾼 코드와 테스트

**열린 질문**
- "`suspend` 함수는 중단될 *수도* 있는 함수다"라는 말의 의미를 설명할 수 있는가?

**공식 문서**: [KEEP: Kotlin Coroutines 설계 문서](https://github.com/Kotlin/KEEP/blob/master/proposals/coroutines.md)

---

## 1-3. 코루틴 빌더와 Job `핵심`

**목표**: `runBlocking`, `launch`, `async`가 어떻게 다른지 이해한다. Job의 생명주기를 따라가 본다.

**실행 전 예측**
- `async { a() }.await(); async { b() }.await()`는 병렬로 실행될까?
- `runBlocking`은 자신을 호출한 스레드를 어떻게 할까?
- `CoroutineStart.LAZY`로 만든 `async`를 `await`하지 않으면 어떻게 될까?

**실험 과제**
1. 공통 시나리오(300/500/200ms)를 두 가지 버전으로 만든다. `suspend` 함수를 순차로 호출하는 버전과 `async`로 병렬 호출하는 버전이다. 소요 시간을 비교한다.
2. Job의 상태 변화를 로그로 추적한다. 상태 흐름은 New → Active → Completing → Completed, 또는 Cancelling → Cancelled다.

**완료 조건**
- [ ] 0-4의 CompletableFuture 코드와 나란히 비교(코드 길이, 가독성)
- [ ] 흔한 실수를 재현하는 테스트: `async`를 만들자마자 `await`해서 결국 순차로 실행되는 경우

**공식 문서**: [Composing suspending functions](https://kotlinlang.org/docs/composing-suspending-functions.html)

---

## 1-4. 코루틴 테스트 도구 `핵심`

**목표**: 이후 모든 장에서 쓸 테스트 도구를 익힌다. `delay(10초)`가 들어간 테스트를 1ms 만에 끝내는 방법을 배운다.

**실행 전 예측**
- `runTest` 안에서 `delay(10_000)`을 하면 테스트가 실제로 10초 걸릴까?
- `runTest` 안에서 `withContext(Dispatchers.IO) { delay(10_000) }`을 하면 어떻게 될까?

**실험 과제**
1. `runTest`, `advanceTimeBy`, `advanceUntilIdle`, `currentTime`을 쓴다. 1-3의 병렬 실행 시간을 가상 시간으로 assert한다.
2. `StandardTestDispatcher`와 `UnconfinedTestDispatcher`의 실행 순서가 어떻게 다른지 테스트로 보여준다.
3. 디스패처를 주입받도록 설계해야 하는 이유를 정리한다. 위의 예측 질문과 연결해서 생각한다.

**완료 조건**
- [ ] 1-3의 병렬 실행이 "가상 시간 500ms"로 끝나는 것을 assert하는 테스트
- [ ] 디스패처 주입 패턴 정리

**공식 문서**: [kotlinx-coroutines-test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)

---

## 1-5. CoroutineContext와 Dispatcher `핵심`

**목표**: 코루틴이 **어느 스레드에서** 실행되는지 정하는 규칙을 이해한다.

**실행 전 예측**
- `Dispatchers.Default`와 `Dispatchers.IO`의 최대 스레드 수는 각각 얼마일까? 둘은 스레드를 공유할까?
- `Dispatchers.IO`에서 `Thread.sleep(1000)`하는 코루틴 1,000개를 동시에 실행하면, 전체가 몇 초 걸릴까?
- `withContext(Dispatchers.IO)` 전후로 스레드 이름은 어떻게 바뀔까? 작업이 끝나면 원래 스레드로 돌아올까?
- `Dispatchers.IO.limitedParallelism(200)`은 IO 디스패처의 한도를 넘을 수 있을까?

**실험 과제**
1. JVM 옵션 `-Dkotlinx.coroutines.debug`를 켜고 디스패처별 스레드 이름을 관찰한다. 이 옵션을 켜면 로그에 코루틴 이름도 함께 찍힌다.
2. IO 디스패처가 동시에 몇 개까지 실행하는지 실험으로 찾는다.
3. `limitedParallelism`과 `Executor.asCoroutineDispatcher()`로 전용 디스패처를 만든다.
4. `Dispatchers.Unconfined`에서 중단 이후 실행 스레드가 어떻게 바뀌는지 관찰한다.

**완료 조건**
- [ ] 디스패처별 스레드 수와 한도를 측정값으로 정리
- [ ] "블로킹 코드를 Default 디스패처에서 돌리면 안 되는 이유"를 실험으로 증명

**열린 질문**
- CoroutineContext는 왜 `+` 연산으로 합칠 수 있을까? Job, Dispatcher, CoroutineName은 각각 어떤 역할을 할까?

**공식 문서**: [Coroutine context and dispatchers](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html)

---

## 1-6. 구조화된 동시성 (Structured Concurrency) `핵심`

**목표**: 부모-자식 관계가 생명주기, 취소, 예외를 어떻게 하나로 묶는지 이해한다. 0-4의 CompletableFuture와 가장 크게 다른 점이다.

**실행 전 예측**
- `coroutineScope { launch { delay(1000) } }`는 언제 반환될까?
- `GlobalScope.launch`로 만든 코루틴은, 호출한 쪽이 취소되면 함께 취소될까?
- 함수 안에서 `CoroutineScope(Dispatchers.IO).launch { ... }`를 매번 만들면 어떤 문제가 생길까?

**실험 과제**
1. 두 가지를 테스트로 증명한다. 부모는 자식이 끝날 때까지 기다린다. 부모를 취소하면 자식도 취소된다.
2. `GlobalScope`나 임의로 만든 `CoroutineScope`가 구조를 깨뜨리는 상황을 재현한다. 누수와 예외 유실을 확인한다.
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

**실행 전 예측**
- `while (true) { i++ }` 루프를 도는 코루틴을 `cancel()`하면 멈출까?
- `Thread.sleep(5000)` 중인 코루틴을 취소하면 바로 멈출까?
- 취소된 코루틴의 `finally` 블록에서 `delay`를 호출하면 어떻게 될까?
- `runCatching { suspendCall() }`로 감싸면 취소는 어떻게 될까?

**실험 과제**
1. CPU 루프, `Thread.sleep`, `delay`가 각각 취소에 반응하는지 확인한다.
2. `isActive`, `ensureActive()`, `yield()`, `runInterruptible`로 코드가 취소에 반응하도록 고친다.
3. `finally` + `withContext(NonCancellable)`로 정리(cleanup) 작업을 수행한다.
4. `withTimeout`과 `withTimeoutOrNull`을 비교한다.
5. `CancellationException`을 삼키는 안티패턴을 재현한다. `catch (e: Exception)`과 `runCatching`이 대표적이다.

**완료 조건**
- [ ] "어떤 코드가 취소에 반응하고 어떤 코드가 반응하지 않는가" 매트릭스
- [ ] CancellationException을 삼키는 버그를 재현하는 테스트와 올바른 처리

**열린 질문**
- 블로킹 JDBC 쿼리나 블로킹 HTTP 호출 중인 코루틴을 취소하면, 실제 쿼리나 커넥션은 어떻게 될까? (Part 4에서 검증한다)

**공식 문서**: [Cancellation and timeouts](https://kotlinlang.org/docs/coroutines-cancellation.html)

---

## 1-8. 예외 전파 `핵심`

**목표**: 예외가 코루틴 계층을 따라 어떻게 전파되는지 이해한다. 예외를 어디서 잡아야 하는지 정리한다.

**실행 전 예측**
- `launch` 안에서 던진 예외를 바깥의 `try/catch`로 잡을 수 있을까?
- `async`에서 예외가 났는데 아무도 `await`하지 않으면 어떻게 될까?
- 자식 하나가 실패하면 형제 코루틴은 어떻게 될까? `supervisorScope`에서는?
- `CoroutineExceptionHandler`를 자식 코루틴에 달면 동작할까?

**실험 과제**
1. `launch`와 `async`의 예외 전파가 어떻게 다른지 테스트로 확인한다.
2. `coroutineScope`와 `supervisorScope`에서 형제 코루틴이 취소되는지 비교한다.
3. `CoroutineExceptionHandler`가 동작하는 위치와 동작하지 않는 위치를 찾는다.
4. 공통 시나리오에서 "쿠폰 조회가 실패해도 나머지 정보로 응답한다"를 구현한다.

**완료 조건**
- [ ] 예외 전파 규칙 정리표(launch, async, 스코프 종류별)
- [ ] 부분 실패를 허용하는 패턴 구현과 테스트

**공식 문서**: [Coroutine exceptions handling](https://kotlinlang.org/docs/exception-handling.html)

**판단 가이드에 남길 것**: "부분 실패를 허용해야 하는 병렬 호출은 어떤 구조로 짜는가", "취소가 안전하려면 어떤 조건이 필요한가"
