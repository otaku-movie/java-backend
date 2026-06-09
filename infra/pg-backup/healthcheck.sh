#!/bin/bash
# 备份新鲜度健康检查：
# - 看 BACKUP_DIR 里最新 *.dump 的修改时间，超过阈值则判定 unhealthy（exit 1）。
# - 容器刚部署、还没到首个备份周期（目录为空）时返回 healthy，避免新部署误报。
# 阈值默认 26 小时（适配每天一次的 cron，留 2 小时缓冲）。
set -euo pipefail

DIR="${BACKUP_DIR:-/backups}"
MAX_AGE_HOURS="${HEALTHCHECK_MAX_AGE_HOURS:-26}"
MAX_AGE_SECONDS=$(( MAX_AGE_HOURS * 3600 ))

LATEST_EPOCH="$(find "${DIR}" -maxdepth 1 -name '*.dump' -type f -printf '%T@\n' 2>/dev/null \
  | sort -n | tail -1 || true)"

if [ -z "${LATEST_EPOCH}" ]; then
  echo "healthcheck: 暂无备份文件（可能尚未到首个备份周期），视为 healthy"
  exit 0
fi

NOW=$(date +%s)
AGE=$(( NOW - ${LATEST_EPOCH%.*} ))

if [ "${AGE}" -gt "${MAX_AGE_SECONDS}" ]; then
  echo "healthcheck: 最新备份已 ${AGE}s（> ${MAX_AGE_SECONDS}s 阈值），UNHEALTHY"
  exit 1
fi

echo "healthcheck: 最新备份 ${AGE}s 前，OK"
exit 0
