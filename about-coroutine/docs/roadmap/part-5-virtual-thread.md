# Part 5. Virtual Thread

> 모듈: `mvc` (+ `stub-server`)
>
> Virtual Thread(VT)는 블로킹 코드를 그대로 두고 스레드를 싸게 만든다. Java 21부터 정식 기능이다. 코루틴은 코드를 중단 가능한 형태로 바꾼다.
> 이 Part에서는 같은 문제를 다른 방식으로 푸는 둘을 같은 시나리오에서 비교한다.

---

## 5-1. VT 켜기: 코드 변경 없이 얻는 것 `핵심`

**목표**: 0-6의 블로킹 순차 버전은 그대로 두고 VT만 켠 뒤 무엇이 달라지는지 측정한다.

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

| 구현 | 외부 호출 | 병렬화 |
|---|---|---|
| A. 플랫폼 스레드 | 블로킹 순차 | 없음(4-5의 블로킹 버전) |
| B. 플랫폼 스레드 + 코루틴 | 4-2에서 가장 좋았던 방식 | `async` |
| C. VT | 블로킹 | ? (VT에서 fan-out은 어떻게 표현하는가?) |

> 모든 구현은 **4-5의 DB 조회(JPA)를 포함한다.** 6-5 최종 벤치마크에서 이 구현을 그대로 다시 쓰기 때문이다.
>
> D안(VT + 코루틴 조합)은 5-4에서 VT 디스패처를 익힌 뒤 이 표에 추가한다.

**실행 전 예측**
- 동시 사용자 100명, 1,000명, 5,000명일 때 B와 C의 TPS 순위는 어떻게 될까?
- 메모리는 어느 쪽이 더 적게 쓸까?
- 코드를 처음 읽는 동료는 어느 쪽을 더 쉽게 이해할까?

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
- VT가 있는데도 코루틴을 써야 하는 이유가 남아 있다면 무엇인가? 반대로 VT로 충분한 경우는?
- 기존 블로킹 서비스에 B(코루틴)나 C(VT)를 적용하면 각각 어떤 비용이 드는가? 코드 변경 범위, 팀 학습, 배포와 운영 리스크를 따져보자.

---

## 5-3. Pinning: VT의 함정은 아직 남아 있는가 `핵심`

**배경**
- VT가 carrier thread에 고정(pinning)되면, VT가 블로킹하는 동안 carrier thread도 함께 붙잡힌다. 그러면 VT의 장점이 사라진다.
- Java 21에서는 `synchronized` 블록 안에서 블로킹하는 경우가 대표적인 원인이었다. 그래서 "VT를 쓰려면 `synchronized`를 `ReentrantLock`으로 바꿔야 한다"는 조언이 널리 퍼졌다.
- JDK 24의 JEP 491이 이 원인을 해소했다.

**목표**: Java 25에서 pinning이 어디까지 해소되었는지 직접 확인한다. 아직 남아 있는 pinning 상황도 찾는다. 인터넷에 있는 "VT 주의사항" 중 무엇이 낡은 정보인지 가려내는 것이 목적이다.

**실행 전 예측**
- Java 25에서 `synchronized` 블록 안에서 블로킹 IO를 하는 코드를 VT로 1,000개 동시에 실행하면 pinning이 생길까?
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
