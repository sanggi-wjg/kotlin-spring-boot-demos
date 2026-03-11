# K8s QoS OOM Test

Kubernetes Pod에서 Spring Boot 실행 시 **Guaranteed QoS**와 **Burstable QoS** 설정에 따른 OOM 발생/방지 동작을 테스트하는 데모 프로젝트.

## Setup

```sh
k3d cluster create springboot-qos
kubectx k3d-springboot-qos
```

## Build

```sh
./gradlew build
docker build . -t k8s-qos-app:latest

k3d image import k8s-qos-app:latest -c springboot-qos
kubectl rollout restart deploy/qos-guaranteed deploy/qos-burstable -n qos-demo
```

## Deploy

```sh
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/guaranteed/deployment.yaml
kubectl apply -f k8s/burstable/deployment.yaml

kubectl apply -f k8s/monitoring
```

## Port Forwarding

```sh
kubectl port-forward -n qos-demo deploy/qos-guaranteed 8201:8080
kubectl port-forward -n qos-demo deploy/qos-burstable 8202:8080

kubectl port-forward -n qos-demo svc/prometheus 9090:9090
kubectl port-forward -n qos-demo svc/grafana 3000:3000
```

## Monitoring

1. http://localhost:3000 접속
2. prometheus datasource 생성 `http://prometheus:9090`
3. 대시보드 생성
    - https://grafana.com/grafana/dashboards/4701-jvm-micrometer/
