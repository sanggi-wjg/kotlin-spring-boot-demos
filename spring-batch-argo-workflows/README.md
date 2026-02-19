# Spring Batch REST API + Argo Workflows

## 기술 스택

- Kotlin + Spring Boot + Spring Batch
- Argo Workflows
- MySQL 8.0
- k3d

## API 목록

`job_api.http` 파일 참고

## Job 목록

- SimpleJobConfig
- FailableJobConfig
- LongRunningJobConfig

---

## k8s

```shell
cd docker 
docker-compose up -d

# build
./gradlew build
docker build . -t sbaw-batch-app:latest

# k3d
k3d cluster create batch-argo
k3d image import sbaw-batch-app:latest -c batch-argo 

kubectl-ctx-switch batch-argo

cd k8s
kubectl apply -f .
kubectl -n batch port-forward svc/batch-api-server 8080:8080

# 배포 테스트 (이후 배포 진행 테스트시 사용)
kubectl rollout restart deployment/batch-api-server -n batch
```

## Argo Workflows

- https://argo-workflows.readthedocs.io/en/latest/quick-start/

### 설치

```shell
brew install argo

kubectl create namespace argo
kubectl apply -n argo -f "https://github.com/argoproj/argo-workflows/releases/download/v4.0.1/quick-start-minimal.yaml"
```

### 워크플로우

```shell
# template 
kubectl apply -f k8s/argo/templates/batch-common.yaml

# cron workflow 
kubectl apply -f k8s/argo/cron-workflows/simple-job-cron.yaml

# workflow submit 
argo submit --watch k8s/argo/workflows/simple-job-workflow.yaml -n batch --watch
(or kubectl create -f k8s/argo/workflows/simple-job-workflow.yaml)

argo submit --watch k8s/argo/workflows/failable-job-workflow.yaml -n batch --watch
argo submit --watch k8s/argo/workflows/failable-job-workflow.yaml -n batch --watch -p shouldFail=False

argo submit --watch k8s/argo/workflows/long-running-job-workflow.yaml -n batch --watch
```

```shell
# 특정 워크플로우 삭제                           
argo delete <workflow-name> -n batch                                                                                           

# 완료된 워크플로우 모두 삭제
argo delete --completed -n batch  

# 전체 삭제
argo delete --all -n batch
```

### 모니터링

```shell
kubectl -n argo port-forward svc/argo-server 2746:2746

argo list -n batch
```