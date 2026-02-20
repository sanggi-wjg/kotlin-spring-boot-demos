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

## Local (optional)

```bash
cd docker 
docker-compose up -d
```

## Kubernetes

```shell
# build
./gradlew build
docker build . -t sbaw-batch-app:latest

# k3d
k3d cluster create batch-argo
kubectl-ctx-switch batch-argo
k3d image import sbaw-batch-app:latest -c batch-argo 

kubectl apply -f k8s/base/.
kubectl -n batch port-forward svc/batch-api-server 8080:8080

# 배포
kubectl rollout restart deployment/batch-api-server -n batch
```

## Argo Workflows

- https://argo-workflows.readthedocs.io/en/latest/quick-start/

### 설치

```shell
brew install argo

kubectl create namespace argo
kubectl apply -n argo -f "https://github.com/argoproj/argo-workflows/releases/download/v3.7.10/quick-start-minimal.yaml"

# 삭제
kubectl delete -n argo -f "https://github.com/argoproj/argo-workflows/releases/download/v3.7.10/quick-start-minimal.yaml"
```

### 워크플로우

```shell
kubectl delete -f k8s/argo/templates/batch-common.yaml
kubectl delete -f k8s/argo/cron-workflows/.

kubectl apply -f k8s/argo/templates/batch-common.yaml
kubectl apply -f k8s/argo/cron-workflows/.

kubectl apply -f k8s/argo/cron-workflows/simple-job-cron.yaml
kubectl apply -f k8s/argo/cron-workflows/failable-job-cron.yaml

argo cron list -n batch

argo cron get simple-job-cron -n batch
(or kubectl get cronworkflows -n batch)
```

#### Delete workflow

```shell
argo delete <workflow-name> -n batch                                                                                           
argo delete --completed -n batch  
argo delete --all -n batch
```

### 웹 콘솔

```shell
kubectl -n argo port-forward svc/argo-server 2746:2746
```

https://localhost:2746/
