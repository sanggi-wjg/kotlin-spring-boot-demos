#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

API_PORT=8080
PF_PID=""

cleanup() {
  if [ -n "$PF_PID" ]; then
    kill "$PF_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT

echo "=========================================="
echo " Spring Batch + Argo Workflows 테스트"
echo "=========================================="

# ── 1. Port Forward 시작 ──
echo ""
echo "=== 1/5. Port Forward 시작 ==="
kubectl -n batch port-forward svc/batch-api-server ${API_PORT}:8080 &
PF_PID=$!
sleep 3
echo "Port Forward 시작 (PID: $PF_PID)"

# ── 2. REST API 직접 테스트 ──
echo ""
echo "=== 2/5. REST API 테스트 ==="

echo ""
echo "--- 등록된 Job 목록 ---"
curl -s http://localhost:${API_PORT}/api/batch/jobs | jq .

echo ""
echo "--- simpleJob 실행 ---"
RESPONSE=$(curl -s -X POST http://localhost:${API_PORT}/api/batch/jobs/simpleJob \
  -H "Content-Type: application/json" \
  -d '{"message": "hello from test script"}')
echo "$RESPONSE" | jq .
EXEC_ID=$(echo "$RESPONSE" | jq -r '.executionId')

echo ""
echo "6초 대기 후 상태 확인..."
sleep 6
echo "--- simpleJob 실행 결과 ---"
curl -s http://localhost:${API_PORT}/api/batch/jobs/executions/"$EXEC_ID" | jq .

echo ""
echo "--- failableJob 실행 (shouldFail=true) ---"
RESPONSE=$(curl -s -X POST http://localhost:${API_PORT}/api/batch/jobs/failableJob \
  -H "Content-Type: application/json" \
  -d '{"shouldFail": "true"}')
echo "$RESPONSE" | jq .
FAIL_EXEC_ID=$(echo "$RESPONSE" | jq -r '.executionId')

echo ""
echo "5초 대기 후 상태 확인..."
sleep 5
echo "--- failableJob 실행 결과 (FAILED 예상) ---"
curl -s http://localhost:${API_PORT}/api/batch/jobs/executions/"$FAIL_EXEC_ID" | jq .

echo ""
echo "--- failableJob 재시작 ---"
RESPONSE=$(curl -s -X POST http://localhost:${API_PORT}/api/batch/jobs/executions/"$FAIL_EXEC_ID"/restart)
echo "$RESPONSE" | jq .

# Port Forward 종료
cleanup
PF_PID=""

# ── 3. Argo Workflow: simpleJob ──
echo ""
echo "=== 3/5. Argo Workflow - simpleJob ==="
argo submit "$PROJECT_DIR/k8s/argo/workflows/simple-job-workflow.yaml" -n batch --watch

# ── 4. Argo Workflow: failableJob ──
echo ""
echo "=== 4/5. Argo Workflow - failableJob (shouldFail=true) ==="
argo submit "$PROJECT_DIR/k8s/argo/workflows/failable-job-workflow.yaml" -n batch \
  -p shouldFail="true" --watch || true

# ── 5. CronWorkflow 상태 확인 ──
echo ""
echo "=== 5/5. CronWorkflow 상태 ==="
argo cron list -n batch
echo ""
echo "--- 최근 Workflow 목록 ---"
argo list -n batch

echo ""
echo "=========================================="
echo " 테스트 완료!"
echo "=========================================="
echo ""
echo "  Argo UI에서 상세 확인:"
echo "    kubectl -n argo port-forward svc/argo-server 2746:2746"
echo "    https://localhost:2746"
echo ""
