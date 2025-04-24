# About GC Tuning

## Usage

1. docker compose 실행
2. Grafana datasource 설정, dashboard 생성
    - http://agt-prometheus:9090
    - https://grafana.com/grafana/dashboards/4701-jvm-micrometer/

## Application Docker build

```shell
docker login

docker build -f Dockerfile -t girr311/agt-app:latest .
#docker build -f Dockerfile -t girr311/agt-app:optimize .

docker push girr311/agt-app:latest
#docker push girr311/agt-app:optimize
```
