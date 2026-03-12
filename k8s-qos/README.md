# K8s QoS OOM Test

Kubernetes Pod에서 Spring Boot 실행 시 **Guaranteed QoS**와 **Burstable QoS** 설정에 따른 OOM 발생/방지 동작을 테스트하는 데모 프로젝트.

## Setup

```sh
k3d cluster create springboot-qos --servers-memory 4g
kubectx k3d-springboot-qos
```

## Container Build & Deploy

```sh
./gradlew build
docker build . -t k8s-qos-app:latest
k3d image import k8s-qos-app:latest -c springboot-qos

# 배포 필요시
kubectl rollout restart deploy/qos-guaranteed -n qos-demo
kubectl rollout restart deploy/qos-burstable -n qos-demo
kubectl rollout restart deploy/qos-guaranteed deploy/qos-burstable -n qos-demo
```

## k8s Deploy

```sh
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/guaranteed/deployment.yaml
kubectl apply -f k8s/burstable/deployment.yaml

kubectl apply -f k8s/monitoring
kubectl create configmap grafana-dashboards -n qos-demo --from-file=k8s/monitoring/dashboards/ --dry-run=client -o yaml | kubectl apply -f -
```

## Port Forwarding

```sh
kubectl port-forward -n qos-demo qos-burstable-d77c48fdc-5w95d 8210:8080
kubectl port-forward -n qos-demo qos-burstable-d77c48fdc-5zhfl 8211:8080
kubectl port-forward -n qos-demo qos-burstable-d77c48fdc-qw4rq 8212:8080

kubectl port-forward -n qos-demo qos-guaranteed-9d9d76d7f-48b87  8210:8080
kubectl port-forward -n qos-demo qos-guaranteed-7895bcb76c-m88zz 8211:8080
kubectl port-forward -n qos-demo qos-guaranteed-7895bcb76c-wdd9f 8212:8080

kubectl port-forward -n qos-demo svc/prometheus 9090:9090
kubectl port-forward -n qos-demo svc/grafana 3000:3000
```

## Monitoring

- http://localhost:3000 접속

