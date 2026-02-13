#!/bin/bash
set -euo pipefail

CLUSTER_NAME="batch-demo"

echo "k3d 클러스터 삭제 중..."
k3d cluster delete "$CLUSTER_NAME"
echo "정리 완료."
