# About GC Tuning

## Usage

1. docker compose 실행
2. Grafana datasource 설정, dashboard 생성
    - https://grafana.com/grafana/dashboards/4701-jvm-micrometer/

## Application Docker build

```shell
docker login

docker build -f Dockerfile.serial -t girr311/agt-app:serial .
docker build -f Dockerfile.g1gc   -t girr311/agt-app:g1gc .
docker build -f Dockerfile.zgc    -t girr311/agt-app:zgc .
# docker run -d -p 3355:8080 girr311/agt-app

docker push girr311/agt-app:serial
docker push girr311/agt-app:g1gc
docker push girr311/agt-app:zgc
```
