# Part 2. 공유 상태와 동시성 제어

> 모듈: `basics`
>
> 코루틴도 멀티스레드 위에서 돌기 때문에 동시성 버그가 그대로 생긴다. 스레드를 막지 않는 도구(Mutex, Semaphore, Channel)를 쓸 수 있다는 점이 다르다. 이 Part에서는 이 도구들이 무엇을 어디까지 해결하는지 확인한다.

---

## 2-1. 코루틴에서도 race condition은 생긴다 `핵심`

**목표**: 코루틴에서 race condition을 재현한다. 각 해결책이 어디까지 통하는지 확인한다.

**실행 전 예측**
- `Dispatchers.Default`에서 코루틴 1,000개가 `counter++`를 1,000번씩 하면, 최종값은 1,000,000일까?
- 같은 코드를 `runBlocking` 단일 스레드에서 돌리면 결과가 달라질까?
- `@Volatile`을 붙이면 해결될까?

**실험 과제**
1. Default 디스패처와 단일 스레드에서 각각 race condition을 재현한다.
2. 세 가지 방법으로 고쳐보고 결과를 비교한다: `@Volatile`, `AtomicInteger`, 스레드 한정(`limitedParallelism(1)`).

**완료 조건**
- [ ] 해결책별 정확성과 성능 비교

**공식 문서**: [Shared mutable state and concurrency](https://kotlinlang.org/docs/shared-mutable-state-and-concurrency.html)

---

## 2-2. Mutex와 synchronized `핵심`

**목표**: 코루틴 안에서 `synchronized`를 쓰면 어떤 문제가 생기는지 확인한다. `Mutex`는 무엇이 다른지 비교한다.

**실행 전 예측**
- `synchronized` 블록 안에서 `delay()`를 호출할 수 있을까? 컴파일러는 뭐라고 할까?
- `Mutex`는 재진입(reentrant)이 될까? 같은 코루틴이 `withLock` 안에서 다시 `withLock`을 호출하면?
- 락을 기다리는 동안 `Mutex`는 스레드를 점유할까?

**실험 과제**
1. `synchronized`와 `Mutex`로 같은 임계 구역을 보호한다. 임계 구역 안에서 중단 함수를 호출해본다.
2. `Mutex` 재진입 데드락을 재현한다.
3. 락을 기다리는 스레드의 상태를 비교한다. 0-3처럼 스레드 덤프로 확인한다.

**완료 조건**
- [ ] "락을 기다리는 동안 스레드를 점유하는가"를 관찰 근거로 정리
- [ ] Mutex 재진입 데드락 재현 테스트

**열린 질문**
- Part 5의 VT pinning과 이 장의 `synchronized` 문제는 어떤 관계일까?

---

## 2-3. 동시 실행 수 제한하기 `핵심`

**목표**: "외부 API는 동시에 10개까지만 호출한다" 같은 요구사항을 구현하는 여러 방법을 비교한다.

**실행 전 예측**
- `Semaphore(10)`와 `Dispatchers.IO.limitedParallelism(10)`은 같은 효과를 낼까? 중단(suspend) 중인 코루틴도 한도에 포함될까?
- 1,000건을 처리하는 두 가지 방법이 있다. `async`로 한꺼번에 띄우고 Semaphore로 제한하는 방법과, 10개씩 묶어서(chunk) 처리하는 방법이다. 총 소요 시간이 같을까?

**실험 과제**
1. 지연 100ms짜리 가짜 API를 1,000건 호출하되, 동시에 10개까지만 실행한다. Semaphore, `limitedParallelism`, chunk 방식으로 각각 구현한다.
   - Channel 워커풀 방식은 2-4에서 추가한다.
2. 호출 지연이 들쭉날쭉한 경우(50~500ms 랜덤)에도 방식별 총 소요 시간을 비교한다.

**완료 조건**
- [ ] "Semaphore와 limitedParallelism이 제한하는 대상의 차이"를 테스트로 증명
- [ ] 방식별 소요 시간 비교

**열린 질문**
- 실무에서 외부 API의 rate limit을 지켜야 한다면 어떤 방식을 고르겠는가? 그 이유는?

---

## 2-4. Channel: 코루틴 간 통신 `선택`

**목표**: 공유 메모리 대신 메시지 전달로 동시성을 다루는 방법을 익힌다.

**실행 전 예측**
- 버퍼가 없는(rendezvous) Channel에 `send`했는데 아무도 `receive`하지 않으면 어떻게 될까?
- `CONFLATED` Channel은 느린 소비자에게 어떤 값을 줄까?
- 생산자가 `close()`하지 않으면 `for (x in channel)` 루프는 어떻게 될까?

**실험 과제**
1. 버퍼 종류별(RENDEZVOUS, BUFFERED, CONFLATED, UNLIMITED)로 생산자-소비자 동작을 비교한다.
2. fan-out(워커 N개)과 fan-in(결과 수집) 패턴을 구현한다.
3. 소비자가 실패하거나 취소되면 생산자는 어떻게 되는지 확인한다.
4. 2-3의 "1,000건을 동시 10개로" 문제를 Channel 워커풀로 풀고 2-3 비교표에 추가한다.

**완료 조건**
- [ ] 버퍼 종류별 동작 테스트
- [ ] Channel 워커풀 구현과 2-3 비교표 갱신

**공식 문서**: [Channels](https://kotlinlang.org/docs/channels.html)

**판단 가이드에 남길 것**: "동시 실행 수를 제한할 때는 무엇을 쓰는가", "코루틴 안에서 `synchronized`를 써도 되는 경우와 안 되는 경우"
