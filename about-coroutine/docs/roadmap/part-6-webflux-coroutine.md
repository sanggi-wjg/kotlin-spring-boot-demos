# Part 6. WebFlux + 코루틴

> 모듈: `webflux` (+ `stub-server`)
>
> 요청부터 DB까지 모두 논블로킹인 환경에서 코루틴을 써본다. 코루틴이 원래 의도대로 동작하는 무대다. Part 4~5의 결론을 대조군으로 놓고 무엇이 달라지는지 확인한다.

---

## 6-1. 이벤트 루프와 블로킹의 대가 `핵심`

**목표**: Netty 이벤트 루프 모델에서 블로킹 호출 하나가 서버 전체에 어떤 영향을 주는지 확인한다.

**실행 전 예측**
- WebFlux 서버에서 요청을 처리하는 스레드(reactor-http-nio)는 몇 개일까?
- `suspend` 핸들러에서 `Thread.sleep(1000)`을 하면, 동시 사용자 100명일 때 TPS는 얼마일까? MVC(0-6 기준선)보다 좋을까, 나쁠까?
- 그 상태에서 **다른** 가벼운 엔드포인트(헬스 체크)의 응답 시간은 어떻게 될까?

**실험 과제**
1. `delay` 버전과 `Thread.sleep` 버전을 k6로 측정한다.
2. 블로킹하는 동안 다른 엔드포인트도 함께 느려지는 현상을 재현한다.
3. 테스트에 BlockHound를 붙여 블로킹 호출을 자동으로 잡아낸다.

**완료 조건**
- [ ] 이벤트 루프 블로킹 재현 수치
- [ ] BlockHound 탐지 테스트

**공식 문서**
- [Spring WebFlux, Concurrency Model](https://docs.spring.io/spring-framework/reference/web/webflux/new-framework.html)
- [BlockHound](https://github.com/reactor/BlockHound)

---

## 6-2. 외부 API 호출: 끝까지 논블로킹 `핵심`

**목표**: 4-2와 같은 시나리오를 WebFlux에서 구현하고 결과를 비교한다.

**실행 전 예측**
- WebFlux에서 Feign을 쓰면 어떻게 될까? 6-1에서 확인한 내용과 연결해서 답해본다.
- `@HttpExchange` + WebClient의 `suspend` 호출 3개를 병렬로 실행하면, 요청 1건이 점유하는 스레드는 몇 개일까?

**실험 과제**
1. `@HttpExchange` + WebClient의 suspend 클라이언트로 공통 시나리오를 구현한다.
2. 4-3의 취소, 타임아웃 실험을 다시 한다. 취소 전파가 MVC와 다른지 본다.

**완료 조건**
- [ ] 4-2, 4-3과의 비교표

---

## 6-3. R2DBC와 suspend 트랜잭션 `핵심`

> 필요한 인프라: R2DBC MySQL 드라이버

**목표**: 논블로킹 DB 접근과 코루틴 트랜잭션이 어떻게 동작하는지 확인하고 4-5, 4-6과 비교한다.

**실행 전 예측**
- R2DBC 환경에서 `suspend fun`에 `@Transactional`을 붙이면 4-6과 결과가 다를까? 트랜잭션 정보가 ThreadLocal에 담기지 않는다면 어디에 담길까?
- 트랜잭션 안에서 `withContext(Dispatchers.IO)`로 스레드를 바꿔도 트랜잭션이 유지될까?
- WebFlux에서 JPA를 쓰면 어떤 일이 생길까?

**실험 과제**
1. `CoroutineCrudRepository`로 주문 조회를 구현한다.
2. `@Transactional` suspend 함수와 `TransactionalOperator.executeAndAwait`로 롤백 시나리오를 테스트한다.
3. 4-6에서 만든 "트랜잭션이 깨지는 상황" 매트릭스를 R2DBC 기준으로 다시 채운다.

**완료 조건**
- [ ] 4-6과 비교한 트랜잭션 매트릭스
- [ ] 4-6에서 남긴 예측 확인

**공식 문서**
- [Spring Data, Coroutines 지원](https://docs.spring.io/spring-data/relational/reference/kotlin/coroutines.html)
- [Spring, Coroutines: Transactions](https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html)

---

## 6-4. Reactor ↔ 코루틴 브리지와 컨텍스트 `선택`

**목표**: Reactor 타입과 코루틴을 서로 변환하는 방법을 익힌다. 이때 컨텍스트(MDC, Security, Tracing)가 어떻게 전파되는지도 이해한다.

**실행 전 예측**
- `mono { }` 빌더 안에서 `ReactorContext`에 담긴 값을 읽을 수 있을까?
- 4-7의 `MDCContext` 방식이 WebFlux에서도 그대로 통할까?

**실험 과제**
1. `Mono.awaitSingle()`, `mono { }`, `Flux.asFlow()`, `Flow.asFlux()` 변환을 각각 써본다.
2. WebFilter에서 넣은 값(traceId, 인증 정보)을 코루틴 핸들러에서 읽는다.
3. 4-7의 컨텍스트 전파 매트릭스를 WebFlux 기준으로 다시 채운다.

**완료 조건**
- [ ] 변환 API 정리
- [ ] 4-7과 비교한 컨텍스트 전파 매트릭스

**공식 문서**: [kotlinx-coroutines-reactor](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-reactor/)

---

## 6-5. 최종 벤치마크: MVC vs VT vs WebFlux `핵심`

**목표**: 공통 시나리오(DB + 외부 API 3개)를 방식별로 구현한 결과를 한 번에 비교한다.

**실행 전 예측**
- 동시 사용자 5,000명에서 MVC + 코루틴, MVC + VT, WebFlux + 코루틴의 순위는 어떻게 될까?
- DB가 병목(커넥션 풀 10개)이라면 순위가 바뀔까?

**실험 과제**
1. 같은 k6 스크립트와 같은 stub 지연으로 측정한다. 비교 대상은 **DB 조회와 외부 API 3개를 모두 포함한** 구현이다.
   - MVC + 블로킹, MVC + 코루틴: 4-5 구현(JPA 포함)
   - MVC + VT: 5-2 구현(JPA 포함)
   - WebFlux + 코루틴: 6-3 구현(R2DBC 포함)
   - 0-6과 6-2는 DB가 없다. 조건이 같지 않으므로 이 비교에서 뺀다.
2. 병목을 바꿔가며 다시 측정한다. 외부 API 지연이 클 때와 DB 풀이 작을 때를 각각 본다.

**완료 조건**
- [ ] 최종 비교표와 그래프
- [ ] "어떤 병목에서 어떤 방식이 유리한가" 정리

**판단 가이드에 남길 것**: "WebFlux로 전환할 가치가 있는 조건"
