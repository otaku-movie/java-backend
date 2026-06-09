# 轻量备份镜像：Alpine + PostgreSQL 客户端 + aws-cli + BusyBox crond
# 用于以独立容器形式按 cron 触发 pg_dump，可选上传到 S3/MinIO
FROM alpine:3.20

RUN apk add --no-cache \
        bash \
        tini \
        ca-certificates \
        coreutils \
        findutils \
        tzdata \
        postgresql17-client \
        aws-cli

ENV TZ=Asia/Tokyo \
    BACKUP_CRON="0 3 * * *" \
    BACKUP_RETENTION_DAYS=30 \
    BACKUP_DIR=/backups \
    BACKUP_RUN_ON_START=false \
    PGPORT=5432

COPY entrypoint.sh /entrypoint.sh
COPY backup.sh /usr/local/bin/pg-backup
COPY restore.sh /usr/local/bin/pg-restore-from-dump
COPY healthcheck.sh /usr/local/bin/pg-backup-health
RUN chmod +x /entrypoint.sh /usr/local/bin/pg-backup /usr/local/bin/pg-restore-from-dump /usr/local/bin/pg-backup-health

WORKDIR /backups
VOLUME ["/backups"]

ENTRYPOINT ["/sbin/tini", "--", "/entrypoint.sh"]
