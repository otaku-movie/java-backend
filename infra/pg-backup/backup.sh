#!/bin/bash
set -euo pipefail

: "${PGHOST:?PGHOST 未设置}"
: "${PGUSER:?PGUSER 未设置}"
: "${PGPASSWORD:?PGPASSWORD 未设置}"
: "${PGDATABASE:?PGDATABASE 未设置}"

TS=$(date +%Y%m%d_%H%M%S)
DUMP_FILE="${BACKUP_DIR}/${PGDATABASE}_${TS}.dump"

log() { echo "[$(date '+%F %T')] [backup] $*"; }

log "开始备份 ${PGDATABASE}@${PGHOST}:${PGPORT:-5432} -> ${DUMP_FILE}"

PGPASSWORD="${PGPASSWORD}" pg_dump \
  -h "${PGHOST}" \
  -p "${PGPORT:-5432}" \
  -U "${PGUSER}" \
  -d "${PGDATABASE}" \
  --format=custom \
  --compress=9 \
  --no-owner \
  --no-privileges \
  --file "${DUMP_FILE}"

SIZE=$(du -h "${DUMP_FILE}" | cut -f1)
log "本地落盘完成: ${DUMP_FILE} (${SIZE})"

if [ -n "${S3_BUCKET:-}" ]; then
  AWS_ARGS=()
  if [ -n "${S3_ENDPOINT:-}" ]; then
    AWS_ARGS+=("--endpoint-url" "${S3_ENDPOINT}")
  fi
  S3_KEY="${S3_PREFIX:-pg-backup}/${PGDATABASE}/${PGDATABASE}_${TS}.dump"
  log "上传到 s3://${S3_BUCKET}/${S3_KEY}"
  AWS_ACCESS_KEY_ID="${S3_ACCESS_KEY:-}" \
  AWS_SECRET_ACCESS_KEY="${S3_SECRET_KEY:-}" \
  aws "${AWS_ARGS[@]}" s3 cp "${DUMP_FILE}" "s3://${S3_BUCKET}/${S3_KEY}"
  log "上传完成"
else
  log "未配置 S3_BUCKET，跳过远端上传"
fi

DELETED=$(find "${BACKUP_DIR}" -maxdepth 1 -name "${PGDATABASE}_*.dump" -type f -mtime "+${BACKUP_RETENTION_DAYS}" -print -delete | wc -l | tr -d ' ')
if [ "${DELETED}" -gt 0 ]; then
  log "已清理本地过期备份 ${DELETED} 个（>${BACKUP_RETENTION_DAYS} 天）"
fi

log "完成"
