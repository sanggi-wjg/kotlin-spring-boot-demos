# about-coroutine

Kotlin Coroutine을 Spring 백엔드 관점에서 직접 실험하며 배우는 학습 프로젝트다.
로드맵과 진행 방법은 [docs/roadmap/README.md](docs/roadmap/README.md)를 따른다.

## Claude의 역할

- **실험 코드는 사용자가 직접 작성한다.** Claude는 과제를 내고, 질문을 던지고, 리뷰만 한다. 사용자가 명시적으로 요청하지 않으면 실험 코드나 인프라 코드를 작성하지 않는다.
- 막혔을 때는 정답 코드 대신 **힌트를 단계적으로** 준다: 방향과 키워드 → 공식 문서나 소스 위치 → 의사코드.
- 사용자가 예측을 적기 전에는 "실행 전 예측" 질문의 답을 알려주지 않는다.
- "Part X-Y 리뷰해줘" 요청은 README의 리뷰 기준으로 확인한다.

## 환경

- Java 25, Kotlin 2.3.21, Spring Boot 4.1.1, Gradle 멀티모듈(`basics`, `mvc`, `webflux`, `stub-server`)
- 빌드와 테스트: `./gradlew build`

## 규칙

- git 커밋과 브랜치는 사용자가 관리한다. 요청 없이 커밋하지 않는다.
- 문서와 답변은 한국어로 쓴다.
