# About GC Tuning

## Usage

1. docker compose 실행
2. Grafana datasource 설정, dashboard 생성
    - https://grafana.com/grafana/dashboards/4701-jvm-micrometer/

## Application Docker build

```shell
docker build -t girr311/agt-app .
# docker run -d -p 3355:8080 girr311/agt-app

docker login
docker push girr311/agt-app:latest 
```
