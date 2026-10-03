# Part 3. Flow

> 모듈: `basics`
>
> `suspend fun`은 값 하나를 돌려준다. Flow는 값 여러 개를 비동기로 흘려보낸다.
> 이 Part에서는 Flow가 언제, 어느 스레드에서 실행되고 느린 소비자와 실패를 어떻게 다루는지 확인한다. 여기서 익힌 내용은 4-8(MVC SSE)과 Part 6(WebFlux)에서 다시 쓴다.

---

## 3-1. Cold stream: 언제 실행되는가 `핵심`

**목표**: `List`, `Sequence`, `Flow`가 각각 언제 실행되는지 비교한다.

**핵심 개념**
> 1단계 [C-2](../1-basics/c-concurrency-flow.md#c-2-flow-기초)에서 먼저 익힌다.

- 즉시 평가(eager)는 연산을 호출하는 순간 결과 전체를 만든다. 지연 평가(lazy)는 결과가 실제로 필요해질 때까지 계산을 미룬다. `List` 연산은 즉시 평가, `Sequence` 연산은 지연 평가다.
- Flow 연산자는 중간 연산자(`map`, `filter` 등)와 최종 연산자(`collect`, `toList`, `first` 등)로 나뉜다. 중간 연산자는 새 Flow를 돌려주고, 최종 연산자는 값을 실제로 받아 처리한다.
- `flow { }` 빌더 안에서 `emit`으로 값을 내보낸다. `collect`는 suspend 함수라서 코루틴 안에서만 호출할 수 있다.
- `Sequence`는 값 사이에서 중단할 수 없다. Flow는 값과 값 사이에 `delay`나 외부 API 호출 같은 중단 함수를 쓸 수 있다.

**실행 전 예측**
- 외부 API를 부르는 코드를 세 방식으로 Flow로 만든다: `flow { emit(callApi()) }`, `flowOf(callApi())`, `suspend fun`이 돌려준 `List`에 `.asFlow()`. 각 방식에서 API는 언제(Flow를 만들 때, collect할 때) 몇 번 호출될까?
- 같은 파이프라인(`map` → `filter` → `take(2)`)을 `List`, `Sequence`, `Flow`로 만들고 원소 10개를 넣으면, `map`은 각각 몇 번 호출될까?

**실험 과제**
1. List, Sequence, Flow로 같은 파이프라인을 만든다. 실행 순서를 로그로 비교한다.
2. Flow를 만드는 방식별로 원본 계산(API 호출)이 일어나는 시점과 횟수를 테스트로 확인한다.

**완료 조건**
- [ ] 세 가지 방식의 실행 순서 비교 테스트
- [ ] Flow 생성 방식별 원본 계산 시점 테스트

**공식 문서**: [Flows](https://kotlinlang.org/docs/coroutines-flow.html)

---

## 3-2. 연산자와 컨텍스트 `핵심`

**목표**: Flow가 어느 스레드에서 실행되는지 정하는 규칙(context preservation)을 이해한다.

**핵심 개념**
> 1단계 [C-2](../1-basics/c-concurrency-flow.md#c-2-flow-기초)에서 먼저 익힌다.

- 연산자 체인에서 생산자(`flow { }`) 쪽을 업스트림(위쪽), 수집자(`collect`) 쪽을 다운스트림(아래쪽)이라고 부른다.
- Flow에는 "값을 어느 코루틴 컨텍스트에서 emit해도 되는가"에 대한 규칙이 있다. 공식 문서는 이 규칙을 context preservation이라고 부른다.
- `withContext`는 블록을 다른 디스패처에서 실행하고 그 결과를 돌려주는 suspend 함수다(Part 1). `flowOn`은 Flow 연산자다. 둘은 서로 다른 도구다.
- `flatMapMerge`는 값마다 내부 Flow를 만들고 그 Flow들을 동시에 수집한다. 동시에 수집하는 개수는 `concurrency` 인자로 정한다(기본 16).
- 실행 스레드를 로그로 남길 때 `-Dkotlinx.coroutines.debug` 옵션을 켜면 스레드 이름 뒤에 코루틴 이름(`@coroutine#N`)이 붙는다.

**실행 전 예측**
- `flowOn`을 두 번 쓰면(`flow { } → map → flowOn(IO) → filter → flowOn(Default) → collect`) 각 구간은 어느 디스패처에서 돌까?
- `flowOn`을 붙이기 전과 후에 생산자와 소비자는 같은 코루틴일까? 생산자가 소비자보다 빠르면, `flowOn`만 붙여도 둘의 실행이 겹칠까?
- 내부 Flow 하나가 `delay(100)` 간격으로 값 3개를 낸다. 이런 내부 Flow 10개를 `flatMapMerge(concurrency = 4)`로 합치면 총 소요 시간은 얼마일까? 결과 값의 순서는 입력 순서와 같을까?

**실험 과제**
1. `map`, `filter`, `transform`, `flatMapMerge`로 파이프라인을 만든다. 단계마다 실행 스레드와 코루틴 이름을 로그로 남긴다.
2. `flowOn`을 하나, 둘 붙인 경우의 구간별 실행 스레드와 생산·소비 타이밍을 비교한다.
3. `flatMapMerge`의 `concurrency`를 1, 4, 16으로 바꿔가며 총 소요 시간과 결과 순서를 기록한다.

**완료 조건**
- [ ] `flowOn` 구간별 실행 스레드를 로그로 증명
- [ ] `flatMapMerge` 동시성별 소요 시간과 순서 비교

---

## 3-3. Backpressure와 buffer `선택`

**목표**: 생산자가 소비자보다 빠를 때 Flow가 어떻게 동작하는지 확인한다.

**핵심 개념**
> 1단계 [C-2](../1-basics/c-concurrency-flow.md#c-2-flow-기초)에서 먼저 익힌다.

- backpressure는 소비자의 처리 속도가 생산자에게 전달되어 생산 속도가 조절되는 것이다. 이 장치가 없으면 빠른 생산자가 메모리를 무한히 채울 수 있다.
- `buffer()`는 생산자와 소비자 사이에 버퍼를 두고 둘을 서로 다른 코루틴으로 나눈다. `conflate()`는 소비자가 바쁜 동안 들어온 중간 값을 버리고 가장 최근 값만 남긴다. `collectLatest`는 새 값이 오면 이전 값을 처리하던 블록을 취소하고 새 값으로 다시 시작한다.
- `runTest` 안에서 `delay`는 실제로 기다리지 않고 가상 시계만 앞으로 돌린다. 경과 시간은 `currentTime`으로 읽는다. 그래서 수 초짜리 시나리오도 테스트는 바로 끝난다.
- 서버에서는 SSE 클라이언트 중 느린 클라이언트가 소비자에 해당한다(4-8).

**실행 전 예측**
- 생산자는 100ms마다 emit하고 소비자는 값 하나에 300ms씩 걸린다. `buffer(capacity = 2)`처럼 버퍼를 작게 제한하면, 생산자는 몇 번째 값부터 기다리기 시작할까? 값 20개를 처리하는 총 시간은?
- 소비자 처리 시간이 값마다 50~500ms로 들쭉날쭉하다. `conflate()`와 `collectLatest`는 각각 값 100개 중 몇 개쯤 끝까지 처리할까? 어느 쪽이 더 많이 버릴까?

**실험 과제**
1. 위 시나리오를 `runTest`의 가상 시간으로 측정한다. 랜덤 지연은 시드를 고정해 재현 가능하게 만든다.
2. 방식마다 처리된 값 목록과 총 시간을 assert한다.

**완료 조건**
- [ ] 방식별 시간과 처리된 값 비교 테스트

**열린 질문**
- Reactor의 backpressure(`request(n)`)와 Flow의 backpressure는 동작 방식이 어떻게 다른가?

---

## 3-4. 예외, 취소, 완료 `선택`

**목표**: Flow 파이프라인에서 실패와 종료를 다루는 방법을 익힌다.

**핵심 개념**
> 1단계 [C-2](../1-basics/c-concurrency-flow.md#c-2-flow-기초)에서 먼저 익힌다.

- Flow의 예외를 다루는 방법은 두 가지다. `collect` 호출을 일반 `try/catch`로 감싸는 방법과 `catch` 연산자를 쓰는 방법이다.
- 중간 연산자는 체인 안의 위치(업스트림/다운스트림, 3-2)에 따라 적용 범위가 달라진다. `catch`, `onCompletion`, `retry`를 어디에 두는지가 중요하다.
- `onCompletion`은 Flow가 끝날 때 호출되고, 끝난 원인(`cause`)을 인자로 받는다. `retry`와 `retryWhen`은 실패했을 때 업스트림을 다시 수집한다.
- Flow의 취소도 1-7에서 본 협력적 취소 규칙을 따른다.
- 외부 API 재시도를 Flow의 `retry`로 할지, Resilience4j 같은 라이브러리로 할지는 7-5에서 다시 다룬다.

**실행 전 예측**
- 업스트림이 값 2개를 낸 뒤 실패하고, 그 아래에 `retry(3)`이 붙어 있다. 소비자는 같은 값을 몇 번 받을까? 외부 API의 페이지 조회라면 어떤 문제가 생길까?
- `collect` 도중 바깥 코루틴이 취소됐는데 업스트림 `flow {}` 블록이 `Thread.sleep(1000)`(블로킹) 중이라면, 업스트림의 `finally`와 `onCompletion`은 언제 실행될까?
- `catch`를 `onCompletion`보다 위에 둘 때와 아래에 둘 때, `onCompletion`이 받는 `cause`는 각각 무엇일까?

**실험 과제**
1. `catch`, `onCompletion`, `retry`, `retryWhen`이 어디까지 적용되는지 테스트한다.
2. 수집 도중 취소한다. 업스트림의 정리 코드(`onCompletion`, `finally`)가 실행되는지 확인한다.

**완료 조건**
- [ ] 예외 처리 연산자의 적용 범위를 정리한 테스트

---

## 3-5. Hot flow: StateFlow와 SharedFlow `선택`

**목표**: hot stream은 구독자가 있든 없든 값을 흘려보낸다. 이런 stream이 서버에서 어디에 쓰이는지 이해한다.

**핵심 개념**
> 1단계 [C-3](../1-basics/c-concurrency-flow.md#c-3-channel과-hot-flow-선택)에서 먼저 익힌다.

- `SharedFlow`는 값 하나를 **모든** 구독자에게 보낸다(브로드캐스트). 값 하나가 소비자 하나에게만 가는 Channel(2-4)과 다른 점이다.
- `MutableSharedFlow`의 동작은 세 설정으로 정해진다: `replay`(새 구독자에게 다시 보내줄 최근 값 개수), `extraBufferCapacity`(추가 버퍼 크기), `onBufferOverflow`(버퍼가 찼을 때의 정책).
- `StateFlow`는 항상 현재 값 하나를 가진 특수한 `SharedFlow`다. 만들 때 초기값이 필요하고, `value` 프로퍼티로 현재 값을 바로 읽을 수 있다.
- `shareIn`과 `stateIn`은 cold Flow를 hot Flow로 바꾼다.
- hot flow는 애플리케이션 인스턴스 **하나**의 메모리 안에서만 공유된다. 서버가 여러 대라면 인스턴스 사이의 브로드캐스트에는 Redis Pub/Sub 같은 외부 수단이 필요하다.

**실행 전 예측**
- `MutableSharedFlow(extraBufferCapacity = 10)`의 구독자가 값 하나에 100ms씩 걸린다. 값 100개를 쉬지 않고 emit하면 생산자는 어떻게 될까? `onBufferOverflow = BufferOverflow.DROP_OLDEST`로 바꾸면 구독자는 몇 개를 받을까?
- 구독자가 두 명이고 그중 한 명만 느리다면, 빠른 구독자도 함께 느려질까?
- `MutableStateFlow`에 가변 필드를 가진 객체를 담고, 같은 인스턴스의 필드만 바꾼 뒤 다시 `value`에 대입하면 구독자는 변경을 받을까?

**실험 과제**
1. `replay`, `extraBufferCapacity`, `onBufferOverflow` 설정에 따라 생산자 대기 시간과 구독자가 받은 값이 어떻게 바뀌는지 측정한다.
2. 느린 구독자 하나가 섞였을 때 다른 구독자와 생산자에 미치는 영향을 측정한다.
3. 서버 시나리오를 구현한다. 주문 상태 변경 이벤트를 여러 구독자(SSE 클라이언트)에게 브로드캐스트한다.

**완료 조건**
- [ ] 설정별 동작 테스트
- [ ] 느린 구독자 영향 측정
- [ ] 브로드캐스트 프로토타입(4-8에서 HTTP로 노출)

**판단 가이드에 남길 것**: "Flow, Channel, `List<suspend 결과>` 중 무엇을 반환할지 고르는 기준"
