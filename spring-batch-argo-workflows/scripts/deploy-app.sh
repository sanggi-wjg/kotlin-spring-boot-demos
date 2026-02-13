#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

CLUSTER_NAME="batch-demo"
IMAGE_NAME="batch-api-demo"
IMAGE_TAG="local"

echo "=== Gradle 빌드 ==="
"$PROJECT_DIR/gradlew" -p "$PROJECT_DIR" bootJar -x test

echo ""
echo "=== Docker 이미지 빌드 ==="
docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" "$PROJECT_DIR"

echo ""
echo "=== k3d로 이미지 로드 ==="
k3d image import "${IMAGE_NAME}:${IMAGE_TAG}" -c "$CLUSTER_NAME"

echo ""
echo "=== K8s 리소스 적용 ==="
kubectl apply -f "$PROJECT_DIR/k8s/base/deployment.yaml"
kubectl apply -f "$PROJECT_DIR/k8s/base/service.yaml"

echo ""
echo "=== Deployment 롤링 재시작 ==="
kubectl rollout restart deployment/batch-api-server -n batch
kubectl rollout status deployment/batch-api-server -n batch --timeout=120s

echo ""
echo "앱 배포 완료."
