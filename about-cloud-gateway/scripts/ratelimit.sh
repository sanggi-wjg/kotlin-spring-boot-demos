#!/usr/bin/env bash
# Gateway RequestRateLimiter 검증 스크립트
#
# 사용법: ./scripts/ratelimit.sh [요청 수] [URL]
#   예) ./scripts/ratelimit.sh 30 http://order.localhost:8080/api/v1/orders/1
#
# burstCapacity=20 기준, 연속 요청 시 처음 약 20건은 200, 이후 429가 나와야 한다.

COUNT=${1:-30}
URL=${2:-http://order.localhost:8080/api/v1/orders/1}

ok=0
limited=0
other=0

for i in $(seq 1 "$COUNT"); do
  result=$(curl -s -o /dev/null -D - -w "%{http_code}" "$URL")
  code=${result: -3}
  remaining=$(echo "$result" | grep -i '^x-ratelimit-remaining:' | awk '{print $2}' | tr -d '\r')

  printf "%3d | %s | remaining=%s\n" "$i" "$code" "${remaining:--}"

  case $code in
    200) ok=$((ok + 1)) ;;
    429) limited=$((limited + 1)) ;;
    *) other=$((other + 1)) ;;
  esac
done

echo "----------------------------------------"
echo "total=$COUNT | 200=$ok | 429=$limited | other=$other"
