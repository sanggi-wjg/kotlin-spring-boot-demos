# Part 7. 관측성과 운영

> 모듈: 전체
>
> 코루틴은 스레드 덤프만으로는 잘 보이지 않는다. 운영 중에 "코루틴이 어디서 멈춰 있는가", "디스패처가 포화되었는가"를 알아내는 방법을 익힌다. 종료, 장애 격리, 타임아웃처럼 운영에서 마주치는 상황도 함께 다룬다.

---

## 7-1. 코루틴 디버깅 기본기 `핵심`

**목표**: 로그, 스택 트레이스, IDE에서 코루틴을 추적하는 방법을 익힌다.

**실행 전 예측**
- `withContext`를 여러 번 거친 뒤 예외가 나면, 스택 트레이스에 호출한 쪽의 프레임이 보일까?
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
- 이미 공부한 [about-circuit-breaker](../../../about-circuit-breaker/README.md)를 코루틴 관점에서 다시 본다.
- 실험 환경은 Docker만 쓴다. k8s의 종료와 CPU 제한도 결국 Docker와 같은 메커니즘(SIGTERM, cgroup)으로 동작하기 때문이다.

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
