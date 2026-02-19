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

kubectl-ctx-switch batch-argo

cd k8s
kubectl apply -f .
kubectl port-forward svc/batch-api-server 8080:8080

# 배포 테스트 (이후 배포 진행 테스트시 사용)
kubectl rollout restart deployment/batch-api-server -n batch
```

## API 목록

`job_api.http` 파일 참고

## Job 목록

| Job           | 유형      | Executor               | 설명                                   |
|---------------|---------|------------------------|--------------------------------------|
| `simpleJob`   | Tasklet | Light (core=5, max=10) | 5초 sleep + 후처리 (2 Step)              |
| `failableJob` | Chunk   | Heavy (core=3, max=5)  | shouldFail 파라미터로 실패 유발, retryLimit=3 |

## Argo Workflows 
