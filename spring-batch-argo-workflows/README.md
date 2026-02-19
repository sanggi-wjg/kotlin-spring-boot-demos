# Spring Batch REST API + Argo Workflows

## 기술 스택

- Kotlin + Spring Boot + Spring Batch
- Argo Workflows
- MySQL 8.0
- k3d

## 테스트 전 사전 세팅

```bash
# build
gradle build
docker build . -t sbaw-batch-app:latest

# k3d
k3d cluster create batch-argo
k3d image import sbaw-batch-app:latest -c batch-argo 

cd k8s
kubectl apply -f .
kubectl port-forward svc/sbaw-batch-app 8080:8080
```

## REST API

| Method | Path                                      | 설명          | 응답 코드 |
|--------|-------------------------------------------|-------------|-------|
| GET    | `/api/batch/jobs`                         | 등록된 Job 목록  | 200   |
| POST   | `/api/batch/jobs/{jobName}`               | Job 비동기 실행  | 202   |
| GET    | `/api/batch/jobs/executions/{id}`         | 실행 상태 조회    | 200   |
| POST   | `/api/batch/jobs/executions/{id}/stop`    | 실행 중지       | 200   |
| POST   | `/api/batch/jobs/executions/{id}/restart` | 실패한 Job 재시작 | 200   |

## Job 목록

| Job           | 유형      | Executor               | 설명                                   |
|---------------|---------|------------------------|--------------------------------------|
| `simpleJob`   | Tasklet | Light (core=5, max=10) | 5초 sleep + 후처리 (2 Step)              |
| `failableJob` | Chunk   | Heavy (core=3, max=5)  | shouldFail 파라미터로 실패 유발, retryLimit=3 |

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


