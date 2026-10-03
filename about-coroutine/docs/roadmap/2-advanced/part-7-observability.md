# Part 7. 관측성과 운영

> 모듈: 전체
>
> 코루틴은 스레드 덤프만으로는 잘 보이지 않는다. 운영 중에 "코루틴이 어디서 멈춰 있는가", "디스패처가 포화되었는가"를 알아내는 방법을 익힌다. 종료, 장애 격리, 타임아웃처럼 운영에서 마주치는 상황도 함께 다룬다.

---

## 7-1. 코루틴 디버깅 기본기 `핵심`

**목표**: 로그, 스택 트레이스, IDE에서 코루틴을 추적하는 방법을 익힌다.

**핵심 개념**
- 코루틴은 중단과 재개를 거치며 여러 스레드를 옮겨 다닐 수 있다. 같은 코루틴의 코드라도 실행 구간마다 다른 스레드 위에서 돌 수 있다.
- 코루틴 debug 모드는 `-Dkotlinx.coroutines.debug` 시스템 프로퍼티로 켠다. JVM 어서션(`-ea`)이 켜져 있어도 자동으로 켜진다. Gradle 테스트는 기본으로 `-ea`를 켜서 실행한다.
- debug 모드에서는 스레드 이름 뒤에 코루틴 식별자(`@coroutine#N`)가 붙는다. `CoroutineName`으로 이름을 주면 그 이름이 함께 찍힌다.
- IntelliJ의 Coroutines 탭은 스레드와 별개로 코루틴 목록과 각 코루틴의 상태를 보여준다.

**실행 전 예측**
- `withContext`를 세 번 거친 깊이에서 예외가 난다. debug 모드를 켰을 때와 껐을 때 스택 트레이스의 프레임 수는 각각 얼마쯤일까? 복구된 프레임과 원래 프레임을 출력에서 구분할 수 있을까?
- `-Dkotlinx.coroutines.debug`를 켜면 성능에 영향이 있을까?

**실험 과제**
1. `CoroutineName`과 debug 모드를 써서 로그에 코루틴 식별자를 남긴다.
2. debug 모드를 켰을 때와 껐을 때 스택 트레이스 복구(stacktrace recovery)가 어떻게 다른지 비교한다.
3. IntelliJ의 코루틴 디버거(Coroutines 탭)에서 중단된 코루틴 목록을 확인한다.

**완료 조건**
- [ ] debug 모드 켬/끔 비교
- [ ] 운영 환경에서 켤 설정과 끌 설정 결정

**공식 문서**: [Debug coroutines using IntelliJ IDEA](https://kotlinlang.org/docs/debug-coroutines-with-idea.html)

---

## 7-2. DebugProbes와 코루틴 누수 `핵심`

**목표**: 끝나지 않고 남아 있는 코루틴(누수)을 찾아낸다.

**핵심 개념**
- 코루틴 누수는 작업이 끝났어야 할 시점이 지났는데도 완료되지 않고 남아 있는 코루틴이다. 구조화된 동시성(1-5, 1-6)에서는 자식이 부모 스코프보다 오래 살 수 없다. 이 부모 관계가 끊긴 곳에서 누수가 생긴다.
- `DebugProbes`는 코루틴이 생성, 중단, 재개될 때마다 기록해두는 장치다. `DebugProbes.install()`을 호출한 **이후에** 생성된 코루틴만 추적한다.
- 코루틴 덤프는 코루틴마다 상태(CREATED, RUNNING, SUSPENDED), 마지막 중단 지점, 생성 위치를 보여준다.
- 생성 위치 스택을 모으는 데는 비용이 든다. 그래서 끌 수 있는 옵션(`enableCreationStackTraces`)이 있다. 운영 중인 앱에 붙일지 판단할 때 이 비용을 고려한다.

**실행 전 예측**
- 1-6에서 재현한 `GlobalScope` 누수를 스레드 덤프로 찾을 수 있을까?
- 테스트가 끝났는데 아직 살아 있는 코루틴이 있으면, 그 테스트를 실패로 만들 수 있을까?

**실험 과제**
1. `kotlinx-coroutines-debug`의 `DebugProbes.dumpCoroutines()`로 코루틴 덤프를 뜬다. 스레드 덤프와 비교한다.
2. 누수를 잡는 테스트를 만든다. 이때 `runTest`가 어떻게 동작하는지도 함께 확인한다.
3. 실행 중인 Spring 앱에서 코루틴 덤프를 뜨는 방법을 만든다. 엔드포인트가 아니어도 된다.

**완료 조건**
- [ ] 코루틴 덤프와 스레드 덤프 비교
- [ ] 누수 탐지 테스트

**공식 문서**: [kotlinx-coroutines-debug](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-debug/)

---

## 7-3. 메트릭: 디스패처 포화를 어떻게 볼 것인가 `핵심`

**목표**: 코루틴 기반 서버에서 꼭 봐야 할 지표를 정하고 대시보드를 만든다.

**핵심 개념**
- 포화(saturation)는 처리할 작업이 실행 자원보다 많아 대기가 생기는 상태다. 사용률(바쁜 스레드 / 전체 스레드), 대기열 길이, 대기 시간을 함께 봐야 알 수 있다.
- Micrometer는 누군가 바인더(`MeterBinder`)로 등록한 지표만 수집한다. Spring Boot Actuator가 자동으로 등록하는 지표는 `/actuator/metrics`에서 확인할 수 있다.
- 지표 종류에 따라 읽는 법이 다르다. 현재 값인 gauge, 누적값인 counter는 `rate()`로 초당 변화량을 본다. timer와 histogram으로는 분포와 백분위수(p99)를 본다.
- 요청 하나는 Tomcat 스레드, 코루틴 디스패처, 커넥션 풀처럼 여러 풀을 차례로 지난다. 대기열이 쌓이는 풀이 병목이다. 그래서 대시보드는 풀마다 같은 기준(사용률, 대기)으로 나란히 보여줘야 한다.

**실행 전 예측**
- `Dispatchers.IO`가 포화되었는지 Micrometer 기본 지표만으로 알 수 있을까?
- 전용 디스패처를 `ExecutorService` 기반으로 만들면 무엇을 측정할 수 있게 될까?

**실험 과제**
1. 전용 디스패처에 계측(`ExecutorServiceMetrics`)을 붙인다.
2. Tomcat, Hikari, JVM 스레드, 디스패처 큐 지표를 Grafana 대시보드 하나로 묶는다.

**완료 조건**
- [ ] "코루틴 서버 운영 대시보드" 1개

---

## 7-4. 장애 리허설 `선택`

**목표**: 원인을 모르는 상태에서 지표와 덤프만 보고 원인을 찾는 연습을 한다.

**핵심 개념**
- 장애 분석은 "증상 → 가설 → 확인" 순서로 진행한다. 지표는 무엇이 나빠졌는지(증상)를 보여주고, 덤프는 어디서 멈춰 있는지(원인 위치)를 보여준다.
- 처리량, 지연(p99), 포화(풀 사용률과 대기열), 에러율은 함께 봐야 한다. 하나만 보면 원인을 잘못 짚기 쉽다.
- 덤프는 한 번이 아니라 몇 초 간격으로 여러 번 떠서 비교한다. 계속 같은 곳에 머무는 스레드와 코루틴을, 잠깐 지나가는 것과 구분하기 위해서다.
- 이상을 판단하려면 비교 대상이 필요하다. 같은 부하에서 정상 상태의 지표를 먼저 기록해둔다.

**실험 과제**
1. 4-4의 안티패턴들을 엔드포인트 하나 뒤에 숨긴다.
   - 앱이 시작할 때 **무작위로 하나를 고르고, 무엇을 골랐는지는 로그에 남기지 않는다.** 직접 고르면 답을 알고 시작하게 되기 때문이다.
   - 정답은 나중에 별도 엔드포인트로 확인한다.
2. Claude는 부하 조건(VU 수, stub 지연, 실패율)만 정해준다.
3. 7-3 대시보드와 7-2 코루틴 덤프만 보고 원인을 추론한다.
4. 추론을 기록한 뒤 정답을 확인한다.

**완료 조건**
- [ ] 원인 추론 기록(어떤 지표를 보고 무엇을 의심했는가)

**판단 가이드에 남길 것**: "코루틴 서비스를 운영할 때 기본으로 켜둘 관측 설정"

---

## 7-5. 운영 회복탄력성: 종료, 장애 격리, 타임아웃 예산 `핵심`

**목표**: 배포, 종료, 외부 장애 상황에서 코루틴 서비스가 어떻게 동작하는지 확인한다.
- 이미 공부한 [about-circuit-breaker](../../../../about-circuit-breaker/README.md)를 코루틴 관점에서 다시 본다.
- 실험 환경은 Docker만 쓴다. k8s의 종료와 CPU 제한도 결국 Docker와 같은 메커니즘(SIGTERM, cgroup)으로 동작하기 때문이다.

**핵심 개념**
- 컨테이너 종료는 SIGTERM으로 시작한다. 유예 시간 안에 프로세스가 끝나지 않으면 SIGKILL로 강제 종료된다. `docker compose stop`의 기본 유예 시간은 10초다.
- SIGTERM을 받으면 JVM은 shutdown hook을 실행하고, Spring은 이때 애플리케이션 컨텍스트를 닫는다. 닫는 순서는 이렇다. 먼저 `SmartLifecycle` 빈들을 phase 역순으로 멈춘다. 그다음 빈들의 `@PreDestroy`를 호출한다. 웹 서버의 graceful shutdown도 이 순서 안에서 일어난다.
- Spring Boot의 graceful shutdown은 새 요청 받기를 멈추고, 처리 중인 요청을 `spring.lifecycle.timeout-per-shutdown-phase`(기본 30초)까지 기다린다.
- CircuitBreaker는 실패율에 따라 CLOSED, OPEN, HALF_OPEN 상태를 오가며 호출을 차단한다. Bulkhead는 동시 호출 수를 제한한다. Retry는 실패한 호출을 다시 시도한다.
- 타임아웃 예산은 요청 전체에 주어진 시간 안에 하위 호출의 타임아웃과 재시도를 맞춰 넣는 설계다. 바깥 제한과 안쪽 제한이 각각 언제부터 시간을 재는지 구분해야 한다.

**실행 전 예측**
- graceful shutdown이 켜진 상태에서 SIGTERM을 받으면, 처리 중이던 suspend 요청은 끝까지 처리될까?
- 애플리케이션 레벨 `CoroutineScope`(1-6 열린 질문)에서 돌던 백그라운드 코루틴은 종료할 때 어떻게 될까?
- Resilience4j CircuitBreaker로 suspend 함수를 감싸려면 무엇이 필요할까? 블로킹 함수를 감쌀 때와 무엇이 다를까?
- 요청 전체 제한이 2초인데 외부 호출마다 타임아웃 1초와 재시도 2회를 걸면, 최악의 경우 응답 시간은 얼마일까?

**실험 과제**
1. **종료**: 앱을 컨테이너로 띄운다. 지연 5초짜리 suspend 요청을 처리하는 중에 `docker compose stop`(SIGTERM)으로 종료한다.
   - graceful shutdown 설정(`server.shutdown`, `spring.lifecycle.timeout-per-shutdown-phase`)에 따라 응답이 완료되는지 확인한다.
   - Compose의 `stop_grace_period`를 앱의 종료 대기 시간보다 짧게 잡으면 무슨 일이 생기는지 확인한다. k8s의 `terminationGracePeriodSeconds`와 같은 역할이다.
   - 백그라운드 스코프를 정리하는 코드(`SmartLifecycle`, `@PreDestroy` 등)를 넣기 전과 후를 비교한다.
2. **장애 격리**: stub의 실패율을 높인 상태에서 suspend 호출에 CircuitBreaker, Bulkhead, Retry를 적용한다. 2-3에서 만든 동시 실행 수 제한과 Bulkhead가 무엇이 다른지 비교한다.
3. **타임아웃 예산**: 요청 단위 `withTimeout`, 호출 단위 타임아웃, 재시도를 조합한다. 예산을 넘지 않는 설계를 만들고 테스트로 증명한다.
4. (선택) **CPU 제한과 스레드 수**: Compose의 `cpus`를 1, 2, 4로 바꿔가며 관찰한다. 운영 환경의 컨테이너 CPU limit과 바로 이어지는 실험이다.
   - 관찰 대상은 JVM이 인식하는 코어 수(`availableProcessors`), `Dispatchers.Default` 크기, VT carrier thread 수다.
   - carrier thread는 Part 5의 VT 구현을 그대로 띄워서 관찰한다.
   - Docker Desktop VM에 할당된 CPU보다 큰 값은 의미가 없다. 먼저 Docker Desktop 설정에서 VM CPU 수를 확인한다.

**완료 조건**
- [ ] 종료 시나리오별 결과(요청 완료 여부, 백그라운드 작업 처리)
- [ ] Resilience4j를 suspend 함수에 적용한 코드와 장애 시 지표
- [ ] 타임아웃 예산 설계와 테스트
- [ ] (선택) `cpus` 값별 코어 수, 디스패처 크기, carrier thread 수 관찰표

**공식 문서**
- [Spring Boot, Graceful Shutdown](https://docs.spring.io/spring-boot/reference/web/graceful-shutdown.html)
- [Resilience4j, Kotlin](https://resilience4j.readme.io/docs/getting-started-4)

**판단 가이드에 남길 것**: "코루틴 서비스의 종료, 장애 격리, 타임아웃 설계 규칙"
