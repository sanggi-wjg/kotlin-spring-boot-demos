# Part 5. Virtual Thread

> 모듈: `mvc` (+ `stub-server`)
>
> Virtual Thread(VT)는 블로킹 코드를 그대로 두고 스레드를 싸게 만든다. Java 21부터 정식 기능이다. 코루틴은 코드를 중단 가능한 형태로 바꾼다.
> 이 Part에서는 같은 문제를 다른 방식으로 푸는 둘을 같은 시나리오에서 비교한다.

---

## 5-1. VT 켜기: 코드 변경 없이 얻는 것 `핵심`

**목표**: 0-6의 블로킹 순차 버전은 그대로 두고 VT만 켠 뒤 무엇이 달라지는지 측정한다.

**핵심 개념**
> 1단계 [D-2](../1-basics/d-spring.md#d-2-전체-그림-스레드-코루틴-vt-webflux)에서 먼저 익힌다.

- VT도 `java.lang.Thread`다. 다만 OS가 아니라 JVM이 스케줄링한다. 코드를 실행할 때는 플랫폼 스레드(carrier thread) 위에 올라가고(mount), 지원되는 블로킹 지점(sleep, 소켓 IO, `java.util.concurrent` 락 등)에서 내려온다(unmount).
- carrier thread는 VT 전용 `ForkJoinPool` 스케줄러가 관리한다. 크기는 `jdk.virtualThreadScheduler.parallelism` 같은 시스템 속성으로 조절한다.
- VT는 풀링하지 않는다. 작업마다 새로 만들고 끝나면 버리는 것이 기본 사용법이다(`Executors.newVirtualThreadPerTaskExecutor()`).
- Spring Boot에서 `spring.threads.virtual.enabled=true`를 켜면 Tomcat 요청 처리와 Boot가 만드는 기본 task executor 등이 VT를 쓴다. 애플리케이션 코드는 바꾸지 않는다.

**실행 전 예측**
- `spring.threads.virtual.enabled=true`만 켜면 0-6 기준선(`threads.max=20`)의 TPS는 어떻게 될까?
- 그때도 `threads.max` 설정이 의미가 있을까?
- 동시 사용자가 1,000명이면 JVM의 플랫폼 스레드(carrier thread)는 몇 개일까?

**실험 과제**
1. VT를 켜고 0-6과 같은 k6 시나리오를 실행한다.
2. 요청 처리 스레드의 이름과 carrier thread 수를 관찰한다.

**완료 조건**
- [ ] 기준선 대비 TPS, p99, 스레드 수 비교
- [ ] "VT를 켜면 바뀌는 것과 바뀌지 않는 것" 정리(예: 외부 API의 응답 지연 자체는 줄어드는가?)

**공식 문서**
- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [Spring Boot, Virtual threads](https://docs.spring.io/spring-boot/reference/features/spring-application.html#features.spring-application.virtual-threads)

---

## 5-2. 플랫폼 스레드 vs 코루틴 vs VT `핵심`

**목표**: 공통 시나리오를 세 방식으로 구현하고 같은 조건에서 비교한다. 이 로드맵의 핵심 비교 실험이다.

**핵심 개념**
> 1단계 [D-2](../1-basics/d-spring.md#d-2-전체-그림-스레드-코루틴-vt-webflux)에서 먼저 익힌다.

- 세 방식의 차이는 **기다리는 동안 무엇이 스레드를 붙잡는가**에 있다. A는 플랫폼 스레드가 그대로 멈춘다. B는 중단 함수 호출 지점에서만 스레드를 놓는다. C는 VT가 carrier thread에서 내려온다.
- 코루틴의 중단 상태는 힙의 continuation 객체로 남는다. VT도 unmount될 때 스택 내용을 힙의 객체로 옮긴다. 둘 다 0-1에서 본 OS 스레드 스택과는 다른 방식으로 상태를 보관한다.
- 세 구현 모두 같은 JPA(블로킹) 호출과 같은 HikariCP 풀을 쓴다. 그래야 측정 차이가 외부 API를 기다리는 방식에서만 나온다.
- 코루틴은 `coroutineScope`/`async`로 부모-자식 관계와 취소를 언어 수준에서 표현한다. JDK의 같은 개념인 구조화된 동시성(`StructuredTaskScope`)은 Java 25에서 아직 preview다.

| 구현 | 외부 호출 | 병렬화 |
|---|---|---|
| A. 플랫폼 스레드 | 블로킹 순차 | 없음(4-5의 블로킹 버전) |
| B. 플랫폼 스레드 + 코루틴 | 4-2에서 가장 좋았던 방식 | `async` |
| C. VT | 블로킹 | ? (VT에서 fan-out은 어떻게 표현하는가?) |

> 모든 구현은 **4-5의 DB 조회(JPA)를 포함한다.** 6-5 최종 벤치마크에서 이 구현을 그대로 다시 쓰기 때문이다.
>
> D안(VT + 코루틴 조합)은 5-4에서 VT 디스패처를 익힌 뒤 이 표에 추가한다.

**실행 전 예측**
- 동시 사용자 100명, 1,000명, 5,000명일 때 B와 C의 TPS와 p99를 각각 숫자로 적어보자. 둘 사이에 차이가 생긴다면 몇 명부터, 어느 자원(Hikari 풀, `Dispatchers.IO`, CPU) 때문일까?
- 메모리는 어느 쪽이 더 적게 쓸까?
- "쿠폰 조회가 실패하면 빈 목록으로 대체하고, 전체는 2초 안에 끝낸다"는 요구사항을 C(VT)로 구현하면 어떤 API가 필요할까? B보다 코드가 가장 많이 길어지는 부분은 어디일까?

**실험 과제**
1. 표에서 C의 `?`를 채워 구현한다.
   - `StructuredTaskScope`는 Java 25에서도 아직 preview 기능이다. 쓰려면 `--enable-preview`가 필요하다.
   - preview API 없이 fan-out을 표현하는 방법도 함께 생각해본다.
2. VU를 단계적으로 늘리며 TPS, p99, 힙, 라이브 스레드 수, CPU를 측정한다.
3. 실패와 취소 시나리오(4-3)를 C에서도 구현한다. 코드 복잡도를 비교한다.

**완료 조건**
- [ ] A~C 구현의 수치 비교표와 그래프(D는 5-4에서 추가)
- [ ] 정성 비교: 취소, 타임아웃, 부분 실패를 얼마나 쉽게 표현하는지, 얼마나 쉽게 디버깅하는지

**열린 질문**
- 1단계 D-2의 가설 1, 2 중 이 장의 측정으로 깨진 것이 있는가? 깨졌다면 어떤 조건(VU, 지연, 풀 크기)에서인가?
- D-2 표의 "기존 코드 변경 비용"을 이 장에서 실제로 바꾼 코드로 다시 매겨보자. 바뀐 파일 수, 새로 생긴 함정(4-6 트랜잭션, 5-3 pinning), 운영에서 새로 봐야 할 지표는 각각 무엇인가?

---

## 5-3. Pinning: VT의 함정은 아직 남아 있는가 `핵심`

**배경**
- VT가 carrier thread에 고정(pinning)되면, VT가 블로킹하는 동안 carrier thread도 함께 붙잡힌다. 그러면 VT의 장점이 사라진다.
- Java 21에서는 `synchronized` 블록 안에서 블로킹하는 경우가 대표적인 원인이었다. 그래서 "VT를 쓰려면 `synchronized`를 `ReentrantLock`으로 바꿔야 한다"는 조언이 널리 퍼졌다.
- JDK 24의 JEP 491이 이 원인을 해소했다.

**목표**: Java 25에서 pinning이 어디까지 해소되었는지 직접 확인한다. 아직 남아 있는 pinning 상황도 찾는다. 인터넷에 있는 "VT 주의사항" 중 무엇이 낡은 정보인지 가려내는 것이 목적이다.

**핵심 개념**
- 고정(pinning) 자체는 비용이 아니다. 문제는 고정된 상태로 **오래 블로킹할 때**다. 그동안 carrier thread가 다른 VT를 실행하지 못한다.
- `synchronized`는 JVM이 직접 구현하는 모니터 락이다. `ReentrantLock`은 `java.util.concurrent`의 락으로 `LockSupport.park`로 기다린다. 두 락은 VT와 맞물리는 방식이 다르다.
- JFR의 `jdk.VirtualThreadPinned` 이벤트는 고정된 상태의 블로킹이 임계값(기본 20ms)보다 길 때만 기록된다. 짧은 블로킹으로 실험하면 이벤트가 안 보일 수 있다.
- JFR은 `-XX:StartFlightRecording`으로 녹화하고, `jfr print --events jdk.VirtualThreadPinned <파일>`이나 JDK Mission Control로 읽는다.

**실행 전 예측**
- VT 1,000개가 **각자 다른** 락 객체로 `synchronized` 블록에 들어가 그 안에서 1초 동안 블로킹한다. 총 소요 시간은 얼마일까? 모든 VT가 **같은** 락 객체를 쓰면 어떻게 될까? 각 경우 JFR `jdk.VirtualThreadPinned` 이벤트는 몇 건쯤 기록될까?
- JEP 491 이후에도 pinning이 남는 경우는 무엇일까? (JEP 491 문서에서 찾아본다)
- 지금 쓰는 MySQL 드라이버와 HikariCP는 Java 25 VT에서 문제가 될까?

**실험 과제**
1. `synchronized` + 블로킹 코드를 VT로 실행한다. JFR의 `jdk.VirtualThreadPinned` 이벤트로 pinning이 생기는지 확인한다. `ReentrantLock` 버전과 처리량도 비교한다.
   - 인터넷 자료에 자주 나오는 `-Djdk.tracePinnedThreads=full`은 JDK 24부터 **아무 효과가 없다**. Java 25에서는 JFR을 쓴다.
2. JEP 491 문서에 나온 "남아 있는 pinning 상황" 중 하나를 재현한다.
3. 4-5의 JPA 시나리오를 VT로 돌리고 JFR로 pinning을 확인한다.

**완료 조건**
- [ ] Java 25 기준 pinning 발생/미발생 매트릭스(JFR 근거)
- [ ] "Java 21 시절 VT 주의사항 중 Java 25에서는 해당하지 않는 것" 목록
- [ ] 2-2(코루틴과 synchronized)와 비교한 정리

**공식 문서**: [JEP 491: Synchronize Virtual Threads without Pinning](https://openjdk.org/jeps/491)

---

## 5-4. 코루틴 디스패처를 VT 위에 올리기 `선택`

**목표**: 코루틴의 블로킹 영역을 VT에 맡기는 조합을 실험한다.

**핵심 개념**
- `Executor.asCoroutineDispatcher()`는 아무 Executor나 코루틴 디스패처로 바꾼다. 코루틴이 시작하거나 재개될 때마다 그 블록이 Executor의 작업 하나로 제출된다.
- `newVirtualThreadPerTaskExecutor()`는 제출된 작업마다 새 VT를 만든다. 그래서 코루틴이 중단 후 재개되면 이전과 다른 VT에서 실행된다.
- 이 디스패처 안에서 블로킹 호출을 하면 그 VT가 carrier thread에서 내려온다. 단, 5-3에서 본 고정 조건에 해당하면 carrier thread가 함께 붙잡힌다.
- Executor로 만든 디스패처는 `close()`로 직접 정리해야 한다. Spring에서 쓴다면 빈으로 등록하고 종료 시점에 닫히게 한다.

**실행 전 예측**
- `Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()`로 디스패처를 만든다. 여기서 블로킹 호출 1만 개를 동시에 실행하면 `Dispatchers.IO`와 비교해 어떨까?
- 이 디스패처가 `Dispatchers.IO`를 대체할 수 있을까? 4-5의 커넥션 풀 문제는 어떻게 될까?

**실험 과제**
1. 4-2의 Feign 병렬 호출을 VT 디스패처와 `Dispatchers.IO`에서 각각 실행하고 비교한다.
2. VT 디스패처에서 pinning이 생기는 경우를 확인한다.
3. 이 조합을 같은 조건으로 측정해 5-2 비교표에 **D. VT + 코루틴**으로 추가한다.

**완료 조건**
- [ ] 두 디스패처 비교 수치
- [ ] 5-2 비교표에 D안 추가
- [ ] 이 조합을 써도 되는 조건 정리

**판단 가이드에 남길 것**: "코루틴 vs VT vs 블로킹 선택 기준"(이 Part의 핵심 결론)
