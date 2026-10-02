# Part 3. Flow

> 모듈: `basics`
>
> `suspend fun`은 값 하나를 돌려준다. Flow는 값 여러 개를 비동기로 흘려보낸다.
> 이 Part에서는 Flow가 언제, 어느 스레드에서 실행되고 느린 소비자와 실패를 어떻게 다루는지 확인한다. 여기서 익힌 내용은 4-8(MVC SSE)과 Part 6(WebFlux)에서 다시 쓴다.

---

## 3-1. Cold stream: 언제 실행되는가 `핵심`

**목표**: `List`, `Sequence`, `Flow`가 각각 언제 실행되는지 비교한다.

**실행 전 예측**
- `flow { println("start"); emit(1) }`를 만들기만 하고 `collect`하지 않으면 "start"가 출력될까?
- 같은 Flow를 두 번 `collect`하면 블록이 몇 번 실행될까?

**실험 과제**
1. List, Sequence, Flow로 같은 파이프라인을 만든다. 실행 순서를 로그로 비교한다.
2. Flow가 cold하다는 것을 테스트로 증명한다.

**완료 조건**
- [ ] 세 가지 방식의 실행 순서 비교 테스트

**공식 문서**: [Flows](https://kotlinlang.org/docs/coroutines-flow.html)

---

## 3-2. 연산자와 컨텍스트 `핵심`

**목표**: Flow가 어느 스레드에서 실행되는지 정하는 규칙(context preservation)을 이해한다.

**실행 전 예측**
- `flow { withContext(Dispatchers.IO) { emit(1) } }`는 정상 동작할까?
- `flowOn(Dispatchers.IO)`는 자신의 **위쪽** 연산자와 **아래쪽** 연산자 중 어느 쪽에 영향을 줄까?

**실험 과제**
1. `map`, `filter`, `transform`, `flatMapMerge`로 파이프라인을 만든다. 단계마다 실행 스레드를 로그로 남긴다.
2. Flow 불변식(invariant)을 어기는 코드를 만들고 `flowOn`으로 고친 코드와 비교한다.

**완료 조건**
- [ ] `flowOn` 적용 범위를 스레드 로그로 증명

---

## 3-3. Backpressure와 buffer `선택`

**목표**: 생산자가 소비자보다 빠를 때 Flow가 어떻게 동작하는지 확인한다.

**실행 전 예측**
- 생산자는 100ms마다 emit하고 소비자는 값 하나에 300ms씩 걸린다. 값 10개를 처리하는 데 몇 초 걸릴까?
- `buffer()`, `conflate()`, `collectLatest`를 쓰면 각각 몇 초 걸릴까? 어떤 값이 처리될까?

**실험 과제**
1. 위 시나리오를 `runTest`의 가상 시간으로 측정한다.
2. 방식마다 처리된 값 목록과 총 시간을 assert한다.

**완료 조건**
- [ ] 방식별 시간과 처리된 값 비교 테스트

**열린 질문**
- Reactor의 backpressure(`request(n)`)와 Flow의 backpressure는 동작 방식이 어떻게 다른가?

---

## 3-4. 예외, 취소, 완료 `선택`

**목표**: Flow 파이프라인에서 실패와 종료를 다루는 방법을 익힌다.

**실행 전 예측**
- `catch` 연산자는 자기보다 **아래쪽**에서 난 예외도 잡을까?
- `collect` 도중 바깥 코루틴이 취소되면 업스트림의 `flow {}` 블록은 어떻게 될까?

**실험 과제**
1. `catch`, `onCompletion`, `retry`, `retryWhen`이 어디까지 적용되는지 테스트한다.
2. 수집 도중 취소한다. 업스트림의 정리 코드(`onCompletion`, `finally`)가 실행되는지 확인한다.

**완료 조건**
- [ ] 예외 처리 연산자의 적용 범위를 정리한 테스트

---

## 3-5. Hot flow: StateFlow와 SharedFlow `선택`

**목표**: hot stream은 구독자가 있든 없든 값을 흘려보낸다. 이런 stream이 서버에서 어디에 쓰이는지 이해한다.

**실행 전 예측**
- 구독자가 없을 때 `SharedFlow`에 emit한 값은 어디로 갈까?
- `StateFlow`에 같은 값을 연속으로 emit하면 구독자는 몇 번 받을까?

**실험 과제**
1. `replay`, `extraBufferCapacity`, `onBufferOverflow` 설정에 따라 동작이 어떻게 바뀌는지 확인한다.
2. 서버 시나리오를 구현한다. 주문 상태 변경 이벤트를 여러 구독자(SSE 클라이언트)에게 브로드캐스트한다.

**완료 조건**
- [ ] 설정별 동작 테스트
- [ ] 브로드캐스트 프로토타입(4-8에서 HTTP로 노출)

**판단 가이드에 남길 것**: "Flow, Channel, `List<suspend 결과>` 중 무엇을 반환할지 고르는 기준"
