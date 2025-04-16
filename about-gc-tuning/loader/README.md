
```shell
locust -f main.py \
    --host http://127.0.0.1:8080 \
    --users 100 \
    --spawn-rate 0.5 \
    --run-time 2m \
    --stop-timeout 2s
```