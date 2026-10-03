# Part 0. JVM 동시성 기초

> 모듈: `basics`(0-1 ~ 0-5), `mvc` + `stub-server`(0-6)
>
> 코루틴이 무엇을 해결하는지 알려면 그 문제를 먼저 겪어봐야 한다. 이 Part에서는 세 가지를 직접 확인한다. **스레드는 비싸다. 블로킹은 스레드를 붙잡는다. 스레드 경계를 넘으면 컨텍스트가 사라진다.**
>
> 💨 **빠른 통과**: 0-1 ~ 0-5는 이미 아는 내용일 수 있다. 예측을 근거까지 모두 맞혔다면 측정으로 확인만 하고 넘어간다([README 빠른 통과](./README.md#빠른-통과)). 단, 0-6은 Part 4~6의 기준선이므로 빠른 통과 없이 끝까지 진행한다.

---

## 0-1. 스레드는 얼마나 비싼가 `핵심`

**목표**: 플랫폼 스레드 하나를 만들고 유지하는 비용을 숫자로 확인한다. 메모리, 생성 시간, 만들 수 있는 최대 개수를 잰다.

**핵심 개념**
> 1단계 [A-1](../1-basics/a-why-coroutine.md#a-1-스레드-모델과-블로킹의-비용)에서 먼저 익힌다.

- Java 플랫폼 스레드(`Thread`)는 OS 스레드와 1:1로 대응한다. `start()`를 호출하면 JVM이 OS에 네이티브 스레드를 만들어 달라고 요청한다.
- 스레드는 각자 자기 호출 스택을 가진다. 메서드를 호출할 때마다 스택에 프레임(지역 변수, 돌아갈 위치)이 쌓이고, 스택 크기의 상한은 JVM 옵션 `-Xss`로 정한다.
- 스레드 스택은 Java 힙 바깥의 네이티브 메모리에 있다. 그래서 `-Xmx`로 제한되지 않고, 힙 그래프에도 나타나지 않는다.
- RSS(Resident Set Size)는 프로세스가 지금 물리 메모리에 올려둔 양이다. "메모리를 얼마나 쓰나"를 말할 때는 어떤 지표를 기준으로 하는지 먼저 정해야 한다.
- 이 장에서 잰 스레드 비용이 코루틴(1-1)과 Virtual Thread(5-1)가 줄이려는 대상이다. 이 장의 숫자가 이후 비교의 기준이 된다.

**실행 전 예측**
- 스레드 1만 개를 만들어 각각 10초씩 `Thread.sleep` 시키면, 프로세스 메모리(RSS)는 얼마나 늘어날까?
- 내 노트북에서 스레드를 몇 개까지 만들 수 있을까? 한계에 다다르면 어떤 에러가 날까?
- 스레드 1개의 기본 스택 크기는 얼마일까? 스레드를 만들자마자 그만큼 메모리를 쓸까?

**내 예측**
```
1. RSS 증가량: 약 10GB (스레드당 1MB × 1만)
2. 한계: 4096 또는 8192개 근처 (OS의 프로세스/스레드 제한이 먼저 막을 것 같다). 에러: 모름
3. 기본 스택: 1MB. 스레드를 생성할 때 스택용 메모리가 할당되므로 만들자마자 1MB를 쓴다
```

**실험 과제**
1. 스레드를 N개(100, 1,000, 10,000) 만들어 sleep 시킨다. 생성에 걸린 시간과 프로세스 메모리를 잰다.
2. `-Xss` 값을 바꿔서 같은 실험을 반복한다.
3. 스레드 수를 계속 늘려서 한계 지점을 찾고 그때 나는 에러 메시지를 확인한다.

**완료 조건**
- [ ] N별 생성 시간과 메모리 표
- [ ] 한계 에러 메시지와 그 의미 설명
- [ ] 예약된 메모리(virtual)와 실제로 쓰인 메모리(RSS)의 차이 설명

**열린 질문**
- Tomcat의 기본 `max-threads`는 200이다. 그 이유를 이 실험 결과로 설명할 수 있는가?

**공식 문서**: [Thread (Java 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Thread.html)

---

## 0-2. 스레드풀과 Executor `핵심`

**목표**: 스레드를 재사용하는 방법을 익힌다. 풀이 가득 찼을 때 무슨 일이 생기는지 확인한다.

**핵심 개념**
> 1단계 [A-1](../1-basics/a-why-coroutine.md#a-1-스레드-모델과-블로킹의-비용)에서 먼저 익힌다.

- 스레드풀은 미리 만든 스레드를 재사용해서 생성 비용을 줄인다. 동시에 실행되는 작업 수도 풀 크기로 제한된다.
- `ThreadPoolExecutor`의 주요 설정은 `corePoolSize`, `maximumPoolSize`, `workQueue`, `keepAliveTime`, `RejectedExecutionHandler`다. 작업이 들어올 때 이 설정들이 어떤 순서로 적용되는지는 javadoc의 "Queuing" 절에 나와 있다.
- `Executors.newFixedThreadPool`, `newCachedThreadPool` 같은 팩토리 메서드는 `ThreadPoolExecutor`를 특정 설정으로 만들어 돌려준다. 어떤 설정인지는 javadoc이나 소스에서 확인할 수 있다.
- 작업은 계산이 대부분인 CPU 바운드 작업과 기다림이 대부분인 IO 바운드 작업으로 나눌 수 있다. 풀 크기를 정할 때는 이 둘을 구분해서 생각해야 한다.
- Tomcat의 요청 처리 스레드, Spring `@Async`, 코루틴 디스패처도 모두 스레드풀 위에서 돈다. 이 장의 설정 항목은 Part 4 이후에도 계속 나온다.

**실행 전 예측**
- 이 맥의 코어 수를 N이라 하자. 1개당 약 1초 걸리는 CPU 작업(소수 계산) 100개를 풀 크기 N, 2N, 100으로 각각 돌리면 총 시간은 어떻게 달라질까? 같은 실험을 1초짜리 `sleep` 작업으로 하면?
- `newCachedThreadPool`에 1초짜리 `sleep` 작업을 1만 개 넣으면 어떻게 될까?
- `ThreadPoolExecutor`의 core, max, queue가 모두 차면 무슨 일이 생길까?

**실험 과제**
1. 세 가지 풀에 같은 부하를 넣는다. 풀은 fixed, cached, 그리고 작은 bounded queue로 직접 구성한 `ThreadPoolExecutor`다. 완료 시간, 생성된 스레드 수, 거절 여부를 비교한다.
2. `RejectedExecutionHandler` 4종이 각각 어떻게 동작하는지 확인한다.
3. CPU 작업(소수 계산)과 IO 작업(sleep)을 나눈다. 각각 풀 크기를 바꿔가며 처리 시간을 잰다.

**완료 조건**
- [ ] 풀 종류별 동작 비교표
- [ ] CPU 작업과 IO 작업의 적정 풀 크기가 왜 다른지 측정값으로 설명

**열린 질문**
- 큐 크기에 제한이 없는(unbounded) 풀은 운영 환경에서 어떤 장애로 이어질 수 있는가?

**공식 문서**: [ThreadPoolExecutor](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html)

---

## 0-3. 블로킹 IO는 스레드를 붙잡는다 `핵심`

**목표**: 블로킹 중인 스레드가 어떤 상태인지 스레드 덤프로 직접 확인한다.

**핵심 개념**
> 1단계 [A-1](../1-basics/a-why-coroutine.md#a-1-스레드-모델과-블로킹의-비용)에서 먼저 익힌다.

- 블로킹 호출은 결과가 준비될 때까지 호출한 스레드가 다음 코드로 넘어가지 못하는 호출이다. `Thread.sleep`, 소켓과 파일 IO, 락 대기가 대표적이다.
- Java 스레드 상태(`Thread.State`)는 NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED 여섯 가지다. 각 상태의 정의는 `Thread.State` javadoc에 있다.
- 스레드 덤프는 특정 순간 모든 스레드의 이름, 상태, 스택 트레이스를 찍은 스냅샷이다. `jcmd <pid> Thread.print`나 `jstack <pid>`로 뜬다.
- `synchronized`는 모니터 락이다. 한 번에 한 스레드만 블록에 들어갈 수 있고, 나머지 스레드는 락이 풀릴 때까지 기다린다.
- Tomcat 요청 스레드가 외부 API 응답을 기다리는 동안 어떤 상태인지가 0-6과 Part 4의 출발점이다.

**실행 전 예측**
- 스레드 하나가 `synchronized` 락을 잡은 채 소켓 `read()`로 응답을 기다리고, 같은 락을 기다리는 스레드가 100개 있다. 스레드 덤프 한 장만 보고 "누가 누구를 막고 있는지" 찾을 수 있을까? 덤프의 어느 줄이 그 단서가 될까?
- JVM이 `RUNNABLE`로 보여주는 소켓 `read()` 스레드를 OS는 어떤 상태로 볼까? 그 스레드가 10초 동안 쓴 CPU 시간은 얼마일까?

**실험 과제**
1. `Thread.sleep`, 소켓 `read()`, `synchronized` 대기, 락을 쥔 채 소켓 `read()`로 블로킹하는 상황을 만든다. `jstack`이나 `jcmd <pid> Thread.print`로 스레드 덤프를 뜬다.
2. 스레드 덤프에서 각 스레드의 상태와 스택을 읽고 해석한다. 락 주소(`locked <0x...>`, `waiting to lock <0x...>`)를 따라 막고 있는 스레드를 찾는다.
3. 같은 시점에 OS 쪽 상태와 스레드별 CPU 시간을 함께 본다(macOS: `ps -M <pid>`).

**완료 조건**
- [ ] 상황별 스레드 상태(RUNNABLE, WAITING, TIMED_WAITING, BLOCKED) 정리
- [ ] 덤프에서 락 주인 스레드를 찾아가는 순서 정리
- [ ] "블로킹 = 스레드를 붙잡고 아무것도 안 함"을 스레드 덤프와 CPU 시간 근거로 설명

**열린 질문**
- 덤프 한 장에서 `RUNNABLE`인 스레드가 CPU를 쓰는 중인지 IO를 기다리는 중인지 구분하려면 무엇을 더 봐야 할까?

---

## 0-4. CompletableFuture로 병렬 조합하기 `핵심`

**목표**: 코루틴 이전의 비동기 조합 방식을 직접 써본다. 가독성, 예외 처리, 취소에서 어떤 한계가 있는지 겪어본다.

**핵심 개념**
> 1단계 [A-2](../1-basics/a-why-coroutine.md#a-2-비동기-코드의-진화와-한계)에서 먼저 익힌다.

- `CompletableFuture`는 나중에 완료될 결과를 나타내는 객체다. `thenApply`, `thenCompose`, `thenCombine` 같은 메서드로 다음 작업을 이어 붙여 콜백 체인을 만든다.
- `allOf`, `anyOf`로 여러 future를 하나로 묶을 수 있다.
- `*Async`로 끝나는 메서드에는 `Executor`를 인자로 받는 버전과 받지 않는 버전이 있다. 어느 쪽을 쓰느냐에 따라 작업이 실행되는 스레드가 달라진다.
- 실패는 future 안에 예외로 담겨 전달되고 `exceptionally`, `handle`, `whenComplete`로 처리한다. 결과를 꺼낼 때 `join()`은 `CompletionException`으로, `get()`은 `ExecutionException`으로 원래 예외를 감싸 던진다.
- 1-3과 1-6에서 같은 시나리오를 코루틴으로 다시 짠다. 이 장에서 불편했던 점이 그때 비교 기준이 된다.

**실행 전 예측**
- 이 맥에서 Executor 없이 `supplyAsync`로 1초짜리 블로킹 작업 20개를 한꺼번에 돌리면 모두 끝나는 데 몇 초 걸릴까? 그동안 같은 JVM에서 `parallelStream()` 계산을 돌리면 그 계산은 어떻게 될까?
- 결제가 100ms에 실패하고 배송이 500ms 걸린다. `CompletableFuture.allOf(payment, delivery).join()`으로 기다리면 예외는 몇 ms 뒤에 던져질까? `payment.join()`을 먼저 부르면?
- `supplyAsync { 500ms 작업 }.orTimeout(300ms).thenApply { ... }`에서 타임아웃이 나면, 작업 안의 `finally` 블록과 `thenApply` 단계는 각각 실행될까? 실행된다면 언제일까?

**실험 과제**
1. 공통 시나리오(결제 300ms, 배송 500ms, 쿠폰 200ms)를 sleep으로 흉내 낸다. 순차 버전과 `CompletableFuture` 병렬 버전을 각각 만든다.
   - Executor 없이 블로킹 작업 20개를 돌리면서 같은 JVM에서 `parallelStream()` 계산을 함께 돌려본다.
2. 작업 하나가 실패하는 경우와 타임아웃(`orTimeout`)이 나는 경우를 처리한다. `allOf`로 기다릴 때와 개별 `join()`으로 기다릴 때 예외가 나오는 시점을 비교한다.
3. `cancel(true)`를 호출한 뒤에도 작업이 계속 실행되는지 로그로 확인한다.

**완료 조건**
- [ ] 순차와 병렬의 소요 시간 비교
- [ ] 실패, 타임아웃, 취소 각각에서 나머지 작업이 어떻게 되는지 테스트로 증명
- [ ] 이 코드를 짜며 불편했던 점 목록(Part 1에서 코루틴과 다시 비교한다)

**열린 질문**
- 요청 하나가 끝났을 때 그 요청이 시작한 작업을 모두 정리하려면, `CompletableFuture`만으로 어떤 코드를 짜야 하는가? 직접 짜본 뒤 1-6의 코루틴 버전과 코드 양을 비교해보자.

**공식 문서**: [CompletableFuture](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/CompletableFuture.html)

---

## 0-5. ThreadLocal과 스레드 경계 `핵심`

**목표**: ThreadLocal 값은 다른 스레드로 넘어가면 사라진다. 이것을 직접 확인한다. Part 4의 MDC 문제와 @Transactional 문제를 미리 준비하는 장이다.

**핵심 개념**
> 1단계 [A-2](../1-basics/a-why-coroutine.md#a-2-비동기-코드의-진화와-한계)에서 먼저 익힌다.

- `ThreadLocal`은 스레드마다 따로 값을 저장하는 변수다. 같은 `ThreadLocal` 객체라도 스레드가 다르면 다른 값을 본다.
- `InheritableThreadLocal`은 부모 스레드의 값을 자식 스레드가 물려받게 하는 변형이다. 언제 물려받는지는 javadoc에서 확인한다.
- `ThreadLocal` 값은 `remove()`로 직접 지우지 않으면 그 스레드가 살아 있는 동안 계속 남아 있다.
- 4-7(컨텍스트 전파)에서 이 장의 전파 여부 매트릭스를 코루틴 기준으로 다시 채운다.

**실행 전 예측**
- 스레드 4개짜리 풀이 요청 1,000건을 처리한다. 작업 시작 시 요청 ID를 ThreadLocal에 넣고, `remove()`는 하지 않는다. 요청 중 10%는 ID 없이 들어와서 `set`을 건너뛴다면, 그 100건의 로그에는 무엇이 찍힐까?
- Java 25에서 정식 기능이 된 `ScopedValue`로 같은 값을 넘기면, 새 스레드, 스레드풀, `CompletableFuture` 안에서 각각 그 값이 보일까?

**실험 과제**
1. ThreadLocal과 InheritableThreadLocal을 세 가지 방식으로 읽어본다. 새 스레드, 스레드풀, CompletableFuture에서 각각 읽는다.
2. 스레드풀이 스레드를 재사용할 때, 이전 요청의 값이 남아서 새는 상황을 재현한다.
3. 같은 매트릭스를 `ScopedValue`로 채운다.

**완료 조건**
- [ ] 전파 여부 매트릭스(ThreadLocal, InheritableThreadLocal, ScopedValue × 실행 방식)
- [ ] 값 누수 재현 테스트

**열린 질문**
- Spring의 `@Async`, `@Scheduled`, `@EventListener`로 실행되는 코드에서는 요청 스레드의 MDC, `SecurityContext`, 트랜잭션이 각각 보일까? 문서와 소스에서 근거를 찾아보자.

---

## 0-6. Tomcat 요청 스레드 모델과 스레드 고갈 `핵심`

> 필요한 인프라(직접 준비): `stub-server` 구현, k6, Prometheus + Grafana, mvc 모듈의 Actuator

**목표**: Tomcat은 "요청 하나에 스레드 하나" 모델이다. 이 모델에서 느린 외부 API가 서버 전체 처리량을 얼마나 제한하는지 숫자로 확인한다. **이 장의 결과는 Part 4~6 비교 실험의 기준선(baseline)이 된다.**

**핵심 개념**
> 1단계 [A-1](../1-basics/a-why-coroutine.md#a-1-스레드-모델과-블로킹의-비용)에서 먼저 익힌다.

- Spring MVC는 Tomcat 위에서 thread-per-request 모델로 동작한다. 요청 하나에 요청 처리 스레드 하나가 배정되고, 응답을 보낼 때까지 그 스레드는 다른 요청을 처리하지 못한다.
- Little's Law는 L = λ × W다. 시스템 안에 머무는 평균 요청 수(L)는 처리율(λ)과 평균 체류 시간(W)의 곱이다.
- 부하 테스트 용어: VU는 k6가 동시에 띄우는 가상 사용자 수, TPS는 초당 처리한 요청 수, p99는 응답 시간을 줄 세웠을 때 상위 1%가 시작되는 값이다.
- 기준선은 조건이 같아야 비교할 수 있다. 클라이언트, stub 지연, k6 스크립트를 고정하는 이유가 이것이다.

**실행 전 예측**
- `server.tomcat.threads.max=20`이고 stub 지연이 1초다. 각 VU는 응답을 받자마자 다음 요청을 보낸다. VU를 20, 40, 100으로 늘리면 TPS와 p50, p99는 각각 얼마가 될까? VU에 따라 그래프가 어떤 모양이 될지 그려보자.
- stub 지연을 1초에서 10ms로 줄이면 처리량의 병목은 무엇으로 바뀔까? 그때 CPU 사용률은 어떻게 될까?
- 요청이 스레드를 기다리는 동안, 그 요청은 어디에 머물러 있을까?

**실험 과제**
1. stub-server를 구현한다. 지연과 실패율을 파라미터로 받는다. stub-server를 왜 WebFlux로 만드는 것이 좋은지도 설명한다.
2. mvc에 공통 시나리오의 블로킹 순차 버전을 구현한다. HTTP 클라이언트는 `@HttpExchange` + RestClient로 고정한다.
   - 이 클라이언트는 4-2, 5-1, 5-2에서도 기준으로 쓴다. 클라이언트가 바뀌면 그 차이가 비교 결과에 섞이기 때문이다.
   - Spring Boot에 기본으로 들어 있다. 그래서 Spring Cloud 버전 호환성을 신경 쓰지 않고 바로 시작할 수 있다.
   - Feign은 4-2에서 이 기준선과 비교한다.
3. k6로 동시 사용자 수(VU)를 단계적으로 늘린다. TPS, p99, Tomcat busy 스레드 수, JVM 스레드 수를 잰다.
4. `threads.max`와 stub 지연을 바꿔가며 같은 실험을 반복한다.

**완료 조건**
- [ ] 측정값이 Little's Law("최대 처리량 ≈ 스레드 수 / 요청당 점유 시간")와 맞는지 검증
- [ ] Grafana에서 스레드가 고갈되는 순간(busy = max)과 지연이 치솟는 순간이 겹치는지 확인
- [ ] 기준선 수치 기록(Part 4, 5, 6에서 계속 비교한다)

**열린 질문**
- `threads.max`를 2,000으로 늘리고 VU 1,000으로 다시 재면 TPS, p99, RSS, JVM 스레드 수는 어떻게 될까? 이 시나리오(DB 없음)에서 다음 병목은 어디가 될까?
- 이 상황에서 `accept-count`와 `max-connections`는 각각 어떤 역할을 하는가?

**공식 문서**
- [Little's Law](https://en.wikipedia.org/wiki/Little%27s_law)
- [k6 문서](https://grafana.com/docs/k6/latest/)
- [Spring Boot Actuator, 메트릭](https://docs.spring.io/spring-boot/reference/actuator/metrics.html)

**판단 가이드에 남길 것**: "블로킹 모델의 처리량 한계는 무엇으로 결정되는가"
