# Spring Batch REST API + Argo Workflows

EKS CronJob 기반 Spring Batch 운영을 **상주 API 서버 + Argo Workflows 호출 방식**으로 전환하기 위한 MVP 데모.
로컬 k3d 환경에서 전체 흐름(REST API -> Job 실행 -> Argo 오케스트레이션)을 검증한다.

## 기술 스택

- Kotlin 2.2, Spring Boot 4.0, Spring Batch 6.0
- MySQL 8.0 (Spring Batch JobRepository)
- k3d (로컬 K8s), Argo Workflows 3.6
- Docker, Gradle (Kotlin DSL)

## Quick Start

### 사전 요구사항

- JDK 17+, Docker
- [k3d](https://k3d.io), kubectl, [argo CLI](https://github.com/argoproj/argo-workflows/releases)

### 원커맨드 셋업

```bash
./scripts/setup.sh       # k3d 클러스터 + MySQL + 앱 + Argo 전체 셋업
./scripts/test-workflow.sh  # REST API + Argo Workflow 테스트
./scripts/cleanup.sh     # 전체 정리
```

### 개발 중 앱만 재배포

```bash
./scripts/deploy-app.sh  # Gradle 빌드 -> Docker 빌드 -> k3d import -> rollout restart
```

## REST API

| Method | Path | 설명 | 응답 코드 |
|--------|------|------|-----------|
| GET | `/api/batch/jobs` | 등록된 Job 목록 | 200 |
| POST | `/api/batch/jobs/{jobName}` | Job 비동기 실행 | 202 |
| GET | `/api/batch/jobs/executions/{id}` | 실행 상태 조회 | 200 |
| POST | `/api/batch/jobs/executions/{id}/stop` | 실행 중지 | 200 |
| POST | `/api/batch/jobs/executions/{id}/restart` | 실패한 Job 재시작 | 200 |

```bash
# 예시
curl http://localhost:8080/api/batch/jobs
curl -X POST http://localhost:8080/api/batch/jobs/simpleJob
curl -X POST http://localhost:8080/api/batch/jobs/failableJob \
  -H "Content-Type: application/json" \
  -d '{"shouldFail":"true"}'
```

## Job 목록

| Job | 유형 | Executor | 설명 |
|-----|------|----------|------|
| `simpleJob` | Tasklet | Light (core=5, max=10) | 5초 sleep + 후처리 (2 Step) |
| `failableJob` | Chunk | Heavy (core=3, max=5) | shouldFail 파라미터로 실패 유발, retryLimit=3 |

## Argo Workflows 흐름

### simpleJob Workflow

```
trigger-job (POST /api/batch/jobs/simpleJob)
    |
    v
poll-job-completion (GET /executions/{id}) -- 10초 간격 폴링
    |
    v
  COMPLETED
```

### failableJob Workflow (shouldFail=true)

```
trigger-job (POST /api/batch/jobs/failableJob)
    |
    v
poll-job-completion -- FAILED 감지
    |
    v
restart-job (POST /executions/{id}/restart)
    |
    v
poll-job-completion -- COMPLETED
```

### 폴링 패턴

`poll-job-completion` 템플릿은 `retryStrategy`를 폴링 루프로 활용한다:

1. `successCondition`에서 status가 `COMPLETED` 또는 `FAILED`인지 확인
2. 조건 미충족 (STARTED/STARTING) -> Argo가 "실패"로 판단
3. `retryStrategy`에 의해 10초 후 재시도
4. 최대 60회 (10분) 반복

### CronWorkflow

`simple-job-cron`: 매 2분마다 simpleJob 자동 실행 (`*/2 * * * *`)

## 프로젝트 구조

```
spring-batch-argo-workflows/
├── src/main/kotlin/com/raynor/demo/springbatchargoworkflows/
│   ├── config/
│   │   ├── BatchInfraConfig.kt    # Heavy/Light TaskExecutor + JobOperator
│   │   ├── DatabaseConfig.kt      # HikariCP 설정
│   │   └── JpaConfig.kt           # JPA, 트랜잭션 설정
│   ├── controller/
│   │   ├── BatchJobController.kt  # 5개 REST 엔드포인트
│   │   └── dto/
│   │       └── BatchJobResponses.kt
│   ├── job/
│   │   ├── SimpleJobConfig.kt     # Tasklet 기반 Job (2 Step)
│   │   └── FailableJobConfig.kt   # Chunk 기반 Job (Retry)
│   └── listener/
│       └── JobCompletionListener.kt
│
├── k8s/
│   ├── base/                       # K8s 기본 리소스
│   │   ├── namespace.yaml
│   │   ├── mysql.yaml              # ConfigMap + Secret + StatefulSet
│   │   ├── deployment.yaml         # Spring Batch API 서버
│   │   └── service.yaml
│   └── argo/                       # Argo Workflows 리소스
│       ├── templates/
│       │   └── batch-common.yaml   # 공통 WorkflowTemplate (trigger/poll/restart)
│       ├── workflows/
│       │   ├── simple-job-workflow.yaml
│       │   └── failable-job-workflow.yaml
│       └── cron-workflows/
│           └── simple-job-cron.yaml
│
├── docker/
│   ├── docker-compose.yaml         # 로컬 Docker 환경
│   └── init.sql                    # Spring Batch 6 MySQL 스키마
│
├── scripts/
│   ├── setup.sh                    # 전체 셋업 (k3d + MySQL + 앱 + Argo)
│   ├── deploy-app.sh               # 앱만 빌드/배포
│   ├── test-workflow.sh            # REST API + Argo 테스트
│   └── cleanup.sh                  # k3d 클러스터 삭제
│
├── Dockerfile
├── build.gradle.kts
└── settings.gradle.kts
```

## 새 Job 추가하기

1. **Job Bean 작성**: `src/.../job/` 아래에 `@Configuration` 클래스 생성

```kotlin
@Configuration
class MyNewJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val jobCompletionListener: JobCompletionListener,
) {
    companion object {
        const val JOB_NAME = "myNewJob"
    }

    @Bean
    fun myNewJob(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .listener(jobCompletionListener)
            .start(myStep())
            .build()
    }

    // Step 정의 ...
}
```

2. **Argo Workflow 작성**: `k8s/argo/workflows/` 에 Workflow YAML 추가

```yaml
# batch-common.yaml의 trigger-job, poll-job-completion 재사용
- name: trigger
  templateRef:
    name: batch-common
    template: trigger-job
  arguments:
    parameters:
      - name: job-name
        value: myNewJob
```

3. **배포**: `./scripts/deploy-app.sh` 실행 후 Workflow 적용

```bash
./scripts/deploy-app.sh
kubectl apply -f k8s/argo/workflows/my-new-job-workflow.yaml
```
