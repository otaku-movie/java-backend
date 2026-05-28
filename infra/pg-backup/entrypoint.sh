#!/bin/bash
set -euo pipefail

mkdir -p "${BACKUP_DIR}"

CRON_LINE="${BACKUP_CRON} /usr/local/bin/pg-backup >> /var/log/pg-backup.log 2>&1"
mkdir -p /etc/crontabs
echo "${CRON_LINE}" > /etc/crontabs/root

touch /var/log/pg-backup.log

echo "[entrypoint] $(date '+%F %T') 备份容器启动"
echo "[entrypoint] target = ${PGUSER:-?}@${PGHOST:-?}:${PGPORT}/${PGDATABASE:-?}"
echo "[entrypoint] cron   = ${BACKUP_CRON}"
echo "[entrypoint] retain = ${BACKUP_RETENTION_DAYS} 天 (本地)"

if [ "${BACKUP_RUN_ON_START}" = "true" ]; then
  echo "[entrypoint] BACKUP_RUN_ON_START=true，立即执行一次备份"
  /usr/local/bin/pg-backup >> /var/log/pg-backup.log 2>&1 || true
fi

tail -F /var/log/pg-backup.log &
exec crond -f -d 8
