#!/bin/bash
set -euo pipefail

usage() {
  cat <<USAGE
用法: $(basename "$0") <dump_file> [target_database]

  <dump_file>        容器内的 dump 路径，例如 /backups/test_movie_20260528_030001.dump
  [target_database]  可选，默认使用环境变量 PGDATABASE

注意：
  --clean --if-exists 会先删除目标库中已存在的对象再重建。
  建议先用一个新数据库验证恢复流程，再决定是否覆盖正式库。

示例：
  docker compose -f docker-compose.backup.yml exec pg-backup \\
      pg-restore-from-dump /backups/test_movie_20260528_030001.dump
USAGE
}

if [ $# -lt 1 ]; then
  usage
  exit 1
fi

DUMP_FILE="$1"
TARGET_DB="${2:-${PGDATABASE:-}}"

if [ -z "${TARGET_DB}" ]; then
  echo "未指定目标数据库，且 PGDATABASE 未设置"
  exit 1
fi

if [ ! -f "${DUMP_FILE}" ]; then
  echo "找不到 dump 文件: ${DUMP_FILE}"
  exit 1
fi

echo "[restore] 即将把 ${DUMP_FILE} 还原到 ${PGHOST}:${PGPORT:-5432}/${TARGET_DB}"
echo "[restore] 当前目标库内容将被覆盖（--clean --if-exists），5 秒后开始..."
sleep 5

PGPASSWORD="${PGPASSWORD}" pg_restore \
  -h "${PGHOST}" \
  -p "${PGPORT:-5432}" \
  -U "${PGUSER}" \
  -d "${TARGET_DB}" \
  --clean \
  --if-exists \
  --no-owner \
  --no-privileges \
  --exit-on-error \
  "${DUMP_FILE}"

echo "[restore] 完成"
