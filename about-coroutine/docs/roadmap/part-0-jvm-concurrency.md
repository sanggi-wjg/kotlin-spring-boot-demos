# Part 0. JVM 동시성 기초

> 모듈: `basics`(0-1 ~ 0-5), `mvc` + `stub-server`(0-6)
>
> 코루틴이 무엇을 해결하는지 알려면 그 문제를 먼저 겪어봐야 한다. 이 Part에서는 세 가지를 직접 확인한다. **스레드는 비싸다. 블로킹은 스레드를 붙잡는다. 스레드 경계를 넘으면 컨텍스트가 사라진다.**
>
> 💨 **빠른 통과**: 0-1 ~ 0-5는 이미 아는 내용일 수 있다. 예측을 근거까지 모두 맞혔다면 측정으로 확인만 하고 넘어간다([README 빠른 통과](./README.md#빠른-통과)). 단, 0-6은 Part 4~6의 기준선이므로 빠른 통과 없이 끝까지 진행한다.

---

## 0-1. 스레드는 얼마나 비싼가 `핵심`

**목표**: 플랫폼 스레드 하나를 만들고 유지하는 비용을 숫자로 확인한다. 메모리, 생성 시간, 만들 수 있는 최대 개수를 잰다.

**실행 전 예측**
- 스레드 1만 개를 만들어 각각 10초씩 `Thread.sleep` 시키면, 프로세스 메모리(RSS)는 얼마나 늘어날까?
- 내 노트북에서 스레드를 몇 개까지 만들 수 있을까? 한계에 다다르면 어떤 에러가 날까?
- 스레드 1개의 기본 스택 크기는 얼마일까? 스레드를 만들자마자 그만큼 메모리를 쓸까?

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

**실행 전 예측**
- `Executors.newFixedThreadPool(4)`에 1초짜리 작업 100개를 넣으면, 모두 끝날 때까지 몇 초 걸릴까?
- `newCachedThreadPool`에 같은 작업을 1만 개 넣으면 어떻게 될까?
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

**실행 전 예측**
- 다음 세 스레드는 스레드 덤프에서 각각 어떤 상태로 보일까?
  - `Thread.sleep()` 중인 스레드
  - 소켓 `read()`를 기다리는 스레드
  - `synchronized` 진입을 기다리는 스레드
- 블로킹 중인 스레드도 CPU를 쓸까?

**실험 과제**
1. 세 가지 블로킹 상황을 만든다. `jstack`이나 `jcmd <pid> Thread.print`로 스레드 덤프를 뜬다.
2. 스레드 덤프에서 각 스레드의 상태와 스택을 읽고 해석한다.

**완료 조건**
- [ ] 상황별 스레드 상태(RUNNABLE, WAITING, TIMED_WAITING, BLOCKED) 정리
- [ ] "블로킹 = 스레드를 붙잡고 아무것도 안 함"을 스레드 덤프 근거로 설명

**열린 질문**
- 소켓 read로 블로킹 중인 스레드는 왜 RUNNABLE로 보이는가?

---

## 0-4. CompletableFuture로 병렬 조합하기 `핵심`

**목표**: 코루틴 이전의 비동기 조합 방식을 직접 써본다. 가독성, 예외 처리, 취소에서 어떤 한계가 있는지 겪어본다.

**실행 전 예측**
- `supplyAsync`에 Executor를 지정하지 않으면 어느 스레드에서 실행될까? 그 풀의 크기는?
- 세 작업 중 하나가 예외를 던지면 나머지 두 작업은 어떻게 될까?
- `future.cancel(true)`를 호출하면 이미 실행 중인 작업이 멈출까?

**실험 과제**
1. 공통 시나리오(결제 300ms, 배송 500ms, 쿠폰 200ms)를 sleep으로 흉내 낸다. 순차 버전과 `CompletableFuture` 병렬 버전을 각각 만든다.
2. 작업 하나가 실패하는 경우와 타임아웃(`orTimeout`)이 나는 경우를 처리한다.
3. `cancel(true)`를 호출한 뒤에도 작업이 계속 실행되는지 로그로 확인한다.

**완료 조건**
- [ ] 순차와 병렬의 소요 시간 비교
- [ ] 실패, 타임아웃, 취소 각각에서 나머지 작업이 어떻게 되는지 테스트로 증명
- [ ] 이 코드를 짜며 불편했던 점 목록(Part 1에서 코루틴과 다시 비교한다)

**열린 질문**
- `CompletableFuture`에는 부모-자식 관계가 없다. 이것이 왜 문제인지 실험한 상황으로 설명할 수 있는가?

**공식 문서**: [CompletableFuture](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/CompletableFuture.html)

---

## 0-5. ThreadLocal과 스레드 경계 `핵심`

**목표**: ThreadLocal 값은 다른 스레드로 넘어가면 사라진다. 이것을 직접 확인한다. Part 4의 MDC 문제와 @Transactional 문제를 미리 준비하는 장이다.

**실행 전 예측**
- 메인 스레드에서 ThreadLocal에 값을 넣고 `CompletableFuture.supplyAsync` 안에서 읽으면 무엇이 나올까?
- `InheritableThreadLocal`이면 결과가 달라질까? 스레드풀을 쓸 때도 그럴까?

**실험 과제**
1. ThreadLocal과 InheritableThreadLocal을 세 가지 방식으로 읽어본다. 새 스레드, 스레드풀, CompletableFuture에서 각각 읽는다.
2. 스레드풀이 스레드를 재사용할 때, 이전 요청의 값이 남아서 새는 상황을 재현한다.

**완료 조건**
- [ ] 전파 여부 매트릭스(ThreadLocal 종류 × 실행 방식)
- [ ] 값 누수 재현 테스트

**열린 질문**
- Spring에서 ThreadLocal에 기대는 기능을 3가지 이상 찾아보자. 힌트: 로깅, 트랜잭션, 보안.

---

## 0-6. Tomcat 요청 스레드 모델과 스레드 고갈 `핵심`

> 필요한 인프라(직접 준비): `stub-server` 구현, k6, Prometheus + Grafana, mvc 모듈의 Actuator

**목표**: Tomcat은 "요청 하나에 스레드 하나" 모델이다. 이 모델에서 느린 외부 API가 서버 전체 처리량을 얼마나 제한하는지 숫자로 확인한다. **이 장의 결과는 Part 4~6 비교 실험의 기준선(baseline)이 된다.**

**실행 전 예측**
- `server.tomcat.threads.max=20`이고 stub 지연이 1초다. 동시 사용자가 100명이면 최대 TPS는 얼마일까? p99 응답 시간은?
- 이때 서버의 CPU 사용률은 높을까, 낮을까?
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
- 스레드를 2,000개로 늘리면 문제가 풀리는가? 0-1의 결과와 연결해서 답해보자.
- 이 상황에서 `accept-count`와 `max-connections`는 각각 어떤 역할을 하는가?

**공식 문서**
- [Little's Law](https://en.wikipedia.org/wiki/Little%27s_law)
- [k6 문서](https://grafana.com/docs/k6/latest/)
- [Spring Boot Actuator, 메트릭](https://docs.spring.io/spring-boot/reference/actuator/metrics.html)

**판단 가이드에 남길 것**: "블로킹 모델의 처리량 한계는 무엇으로 결정되는가"
