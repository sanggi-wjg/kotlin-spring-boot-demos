## Simple one

```shell
locust -f main.py \
    --host http://127.0.0.1:10012 \
    --users 1000 \
    --spawn-rate 50 \
    --run-time 10m \
    --stop-timeout 2s
```

## Using master and worker

```shell
locust -f main.py \
    --master \
    --host http://127.0.0.1:10012 \
    --users 10000 \
    --spawn-rate 500 \
    --run-time 10m \
    --stop-timeout 2s

# in another terminal
locust -f main.py --worker
```
