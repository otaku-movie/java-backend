#!/bin/bash
set -euo pipefail

: "${PGHOST:?PGHOST 未设置}"
: "${PGUSER:?PGUSER 未设置}"
: "${PGPASSWORD:?PGPASSWORD 未设置}"
: "${PGDATABASE:?PGDATABASE 未设置}"

TS=$(date +%Y%m%d_%H%M%S)
DUMP_FILE="${BACKUP_DIR}/${PGDATABASE}_${TS}.dump"
DUMP_NAME="$(basename "${DUMP_FILE}")"

# 是否把每次备份的执行记录写进数据库表 public.db_backup_log（默认开）。
LOG_TO_DB="${BACKUP_LOG_TO_DB:-true}"
ROW_ID=""

log() { echo "[$(date '+%F %T')] [backup] $*"; }

# 所有写库操作都容错：库不可达或写入失败都不能影响备份主流程。
psql_exec() {
  PGPASSWORD="${PGPASSWORD}" psql \
    -h "${PGHOST}" \
    -p "${PGPORT:-5432}" \
    -U "${PGUSER}" \
    -d "${PGDATABASE}" \
    -v ON_ERROR_STOP=1 \
    -q \
    -At \
    -c "$1"
}

ensure_log_table() {
  [ "${LOG_TO_DB}" = "true" ] || return 0
  psql_exec "CREATE TABLE IF NOT EXISTS public.db_backup_log (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    db_name         TEXT NOT NULL,
    file_name       TEXT NOT NULL,
    file_path       TEXT,
    file_size_bytes BIGINT,
    file_size_human TEXT,
    s3_bucket       TEXT,
    s3_key          TEXT,
    status          TEXT NOT NULL,
    message         TEXT,
    started_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at     TIMESTAMPTZ
  );" >/dev/null 2>&1 || { log "WARN: 备份记录表初始化失败，本次跳过写库"; LOG_TO_DB="false"; }
}

# 备份失败时把记录标成 failed，并保留非零退出码给 cron 日志。
mark_failed() {
  local ec=$?
  if [ "${LOG_TO_DB}" = "true" ] && [ -n "${ROW_ID}" ]; then
    psql_exec "UPDATE public.db_backup_log
      SET status='failed', message='backup failed (exit ${ec})', finished_at=now()
      WHERE id=${ROW_ID};" >/dev/null 2>&1 || true
  fi
  log "备份失败 (exit ${ec})"
  exit "${ec}"
}
trap mark_failed ERR

log "开始备份 ${PGDATABASE}@${PGHOST}:${PGPORT:-5432} -> ${DUMP_FILE}"

ensure_log_table
if [ "${LOG_TO_DB}" = "true" ]; then
  ROW_ID="$(psql_exec "INSERT INTO public.db_backup_log (db_name, file_name, file_path, status)
    VALUES ('${PGDATABASE}', '${DUMP_NAME}', '${DUMP_FILE}', 'running')
    RETURNING id;" 2>/dev/null || true)"
  [ -n "${ROW_ID}" ] && log "已写入备份记录 db_backup_log.id=${ROW_ID}"
fi

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
BYTES=$(stat -c %s "${DUMP_FILE}" 2>/dev/null || echo 0)
log "本地落盘完成: ${DUMP_FILE} (${SIZE})"

S3_KEY=""
if [ -n "${S3_BUCKET:-}" ]; then
  AWS_ARGS=()
  if [ -n "${S3_ENDPOINT:-}" ]; then
    AWS_ARGS+=("--endpoint-url" "${S3_ENDPOINT}")
  fi
  # Cloudflare R2 / 多数 S3 兼容存储要求显式 region；R2 固定用 auto。
  S3_REGION_EFFECTIVE="${S3_REGION:-auto}"
  AWS_ARGS+=("--region" "${S3_REGION_EFFECTIVE}")
  S3_KEY="${S3_PREFIX:-pg-backup}/${PGDATABASE}/${PGDATABASE}_${TS}.dump"
  log "上传到 s3://${S3_BUCKET}/${S3_KEY} (region=${S3_REGION_EFFECTIVE}${S3_ENDPOINT:+, endpoint=${S3_ENDPOINT}})"
  AWS_ACCESS_KEY_ID="${S3_ACCESS_KEY:-}" \
  AWS_SECRET_ACCESS_KEY="${S3_SECRET_KEY:-}" \
  AWS_DEFAULT_REGION="${S3_REGION_EFFECTIVE}" \
  aws "${AWS_ARGS[@]}" s3 cp "${DUMP_FILE}" "s3://${S3_BUCKET}/${S3_KEY}"
  log "上传完成"
else
  log "未配置 S3_BUCKET，跳过远端上传"
fi

if [ "${LOG_TO_DB}" = "true" ] && [ -n "${ROW_ID}" ]; then
  # 空值用 NULLIF 转成 SQL NULL，避免远端未配置时写入空字符串。
  psql_exec "UPDATE public.db_backup_log SET
      status='success',
      file_size_bytes=${BYTES},
      file_size_human='${SIZE}',
      s3_bucket=NULLIF('${S3_BUCKET:-}', ''),
      s3_key=NULLIF('${S3_KEY}', ''),
      finished_at=now()
    WHERE id=${ROW_ID};" >/dev/null 2>&1 || log "WARN: 备份成功但回写 db_backup_log 失败"
fi

DELETED=$(find "${BACKUP_DIR}" -maxdepth 1 -name "${PGDATABASE}_*.dump" -type f -mtime "+${BACKUP_RETENTION_DAYS}" -print -delete | wc -l | tr -d ' ')
if [ "${DELETED}" -gt 0 ]; then
  log "已清理本地过期备份 ${DELETED} 个（>${BACKUP_RETENTION_DAYS} 天）"
fi

log "完成"
