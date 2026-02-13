#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

CLUSTER_NAME="batch-demo"
ARGO_VERSION="v3.6.4"

echo "=========================================="
echo " Spring Batch + Argo Workflows 셋업"
echo "=========================================="

# ── 1. k3d 클러스터 생성 ──
echo ""
echo "=== 1/7. k3d 클러스터 생성 ==="
if k3d cluster list | grep -q "$CLUSTER_NAME"; then
  echo "클러스터 '$CLUSTER_NAME' 이미 존재합니다. 건너뜁니다."
else
  k3d cluster create "$CLUSTER_NAME"
  echo "클러스터 '$CLUSTER_NAME' 생성 완료."
fi

# ── 2. Namespace 생성 ──
echo ""
echo "=== 2/7. Namespace 생성 ==="
kubectl apply -f "$PROJECT_DIR/k8s/base/namespace.yaml"

# ── 3. MySQL 배포 ──
echo ""
echo "=== 3/7. MySQL 배포 ==="
kubectl apply -f "$PROJECT_DIR/k8s/base/mysql.yaml"
echo "MySQL 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=mysql -n batch --timeout=180s
echo "MySQL 준비 완료."

# ── 4. Spring Batch API 서버 빌드 & 배포 ──
echo ""
echo "=== 4/7. Spring Batch API 서버 빌드 & 배포 ==="
"$SCRIPT_DIR/deploy-app.sh"

# ── 5. Argo Workflows 설치 ──
echo ""
echo "=== 5/7. Argo Workflows 설치 (${ARGO_VERSION}) ==="
kubectl create namespace argo --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -n argo -f "https://github.com/argoproj/argo-workflows/releases/download/${ARGO_VERSION}/quick-start-minimal.yaml"
echo "Argo Controller 준비 대기 중..."
kubectl wait --for=condition=available deployment/argo-server -n argo --timeout=180s
echo "Argo Workflows 설치 완료."

# ── 6. RBAC 설정 ──
echo ""
echo "=== 6/7. Argo RBAC 설정 (batch namespace) ==="
kubectl apply -f - <<'EOF'
apiVersion: v1
kind: ServiceAccount
metadata:
  name: argo-batch
  namespace: batch
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: argo-batch-role
  namespace: batch
subjects:
  - kind: ServiceAccount
    name: argo-batch
    namespace: batch
roleRef:
  kind: ClusterRole
  name: admin
  apiGroup: rbac.authorization.k8s.io
EOF

# ── 7. Argo 템플릿 & CronWorkflow 배포 ──
echo ""
echo "=== 7/7. Argo WorkflowTemplate & CronWorkflow 배포 ==="
kubectl apply -f "$PROJECT_DIR/k8s/argo/templates/"
kubectl apply -f "$PROJECT_DIR/k8s/argo/cron-workflows/"

echo ""
echo "=========================================="
echo " 셋업 완료!"
echo "=========================================="
echo ""
echo "  Batch API:"
echo "    kubectl -n batch port-forward svc/batch-api-server 8080:8080"
echo "    curl http://localhost:8080/api/batch/jobs"
echo ""
echo "  Argo UI:"
echo "    kubectl -n argo port-forward svc/argo-server 2746:2746"
echo "    https://localhost:2746"
echo ""
echo "  테스트:"
echo "    ./scripts/test-workflow.sh"
echo ""
