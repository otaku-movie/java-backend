# PostgreSQL 自动备份服务

本目录提供一个独立的备份容器，按 cron 定时执行 `pg_dump`，把备份文件落到挂载目录，并可选上传到 S3/MinIO。设计目标：

- **环境无关**：通过环境变量配置数据库连接、cron、保留策略、S3 信息，本地 / 测试 / 生产共用同一镜像。
- **不依赖应用**：备份在独立容器里跑，应用挂了也不影响备份。
- **可手动恢复**：提供 `pg-restore-from-dump` 脚本，便于把任意 dump 还原到指定数据库。

## 镜像组成

- 基础镜像：`alpine:3.20`
- 主要工具：
  - `postgresql17-client`（`pg_dump`、`pg_restore`）
  - `aws-cli`（同时兼容 MinIO 等 S3 协议）
  - `tini`（信号转发，正确处理容器退出）
  - 内置 BusyBox `crond`（定时触发）

## 文件

| 文件                | 说明                                       |
|---------------------|--------------------------------------------|
| `Dockerfile`        | 镜像构建文件                                |
| `entrypoint.sh`     | 写入 crontab 并前台启动 `crond` + 日志输出 |
| `backup.sh`         | 单次备份：`pg_dump` → 本地 → 可选 S3 → 清理 |
| `restore.sh`        | 手动恢复：从指定 dump 还原到目标库          |

## 备份策略

- 文件名：`<db>_YYYYMMDD_HHMMSS.dump`，PostgreSQL 自定义格式（`-F c`），压缩级别 9。
- 本地保留：`BACKUP_RETENTION_DAYS` 天（默认 30）。每次备份后清理超过保留天数的本地文件。
- 远端保留：建议通过 S3/MinIO 的 lifecycle 规则管理，避免在容器里做不可靠的远端 list+delete。
- 备份内容：默认不含权限（`--no-owner --no-privileges`），便于跨实例恢复。

## 配置（环境变量）

| 变量                       | 默认值          | 说明 |
|----------------------------|-----------------|------|
| `PGHOST`                   | -               | PG 主机；连容器内的 PG 用服务名（如 `postgres`），连宿主机用 `host.docker.internal` |
| `PGPORT`                   | `5432`          | PG 端口 |
| `PGUSER`                   | -               | 用户名 |
| `PGPASSWORD`               | -               | 密码 |
| `PGDATABASE`               | -               | 要备份的数据库 |
| `BACKUP_CRON`              | `0 3 * * *`     | cron 表达式（容器时区 Asia/Tokyo） |
| `BACKUP_RETENTION_DAYS`    | `30`            | 本地保留天数 |
| `BACKUP_RUN_ON_START`      | `false`         | 启动时是否立即跑一次（用于验证） |
| `BACKUP_DIR`               | `/backups`      | 容器内备份目录（宿主机挂载到这里） |
| `S3_ENDPOINT`              | -               | S3/MinIO endpoint；走 AWS 公网时留空 |
| `S3_BUCKET`                | -               | S3 桶名；留空则跳过远端上传 |
| `S3_PREFIX`                | `pg-backup`     | S3 key 前缀 |
| `S3_ACCESS_KEY`            | -               | S3 Access Key |
| `S3_SECRET_KEY`            | -               | S3 Secret Key |

## 启动方式

详细看根目录 [`docker-compose.backup.yml`](../../docker-compose.backup.yml) 注释。常见用法：

```bash
# 准备环境变量
cp .env.backup.example .env.backup
vim .env.backup

# 完整栈（PG 跑容器）：默认 PGBACKUP_HOST=postgres
docker compose --env-file .env.backup \
  -f docker-compose.yml -f docker-compose.backup.yml up -d pg-backup

# 开发环境（PG 跑宿主机）：把 .env.backup 里 PGBACKUP_HOST 改为 host.docker.internal
docker compose --env-file .env.backup \
  -f docker-compose.dev.yml -f docker-compose.backup.yml up -d pg-backup

# 看日志
docker logs -f movie-pg-backup
```

## 立即手动触发一次备份

```bash
# 通过 exec
docker compose -f docker-compose.backup.yml exec pg-backup pg-backup

# 或临时启动一次性容器
docker compose --env-file .env.backup \
  -f docker-compose.backup.yml run --rm pg-backup pg-backup
```

## 恢复

```bash
# 1) 列出本地备份
docker compose -f docker-compose.backup.yml exec pg-backup \
  ls -lh /backups

# 2) 恢复到 PGDATABASE 指向的库（会先清空原库对象再导入）
docker compose -f docker-compose.backup.yml exec pg-backup \
  pg-restore-from-dump /backups/test_movie_20260528_030001.dump

# 3) 或恢复到一个新库（更安全，先做对账）
docker compose -f docker-compose.backup.yml exec pg-backup \
  pg-restore-from-dump /backups/test_movie_20260528_030001.dump test_movie_restore
```

## 监控建议

- **看日志**：容器把 cron 日志输到 stdout，由 docker logs 抓即可。生产建议接入日志聚合（Loki / ELK 等）。
- **关键告警**：监控 `pg-backup` 容器的健康度（异常退出）、最新备份文件 mtime（超过 26h 没新文件就报警）、最新备份大小（突降 > 50% 触发告警）。
- **演练恢复**：定期（建议每月）跑一次"备份 → 新库恢复 → 关键表对账"，避免只有备份没法用。

## 不在脚本里做的事（避免做错）

- **不删除 S3 远端旧文件**：远端清理由 bucket lifecycle 规则做，更可靠也更便宜。
- **不做加密**：开发阶段简单上传；生产请用 S3 SSE 或 KMS 在桶层启用静态加密，必要时在 backup.sh 里加 `gpg --symmetric` 后再上传。
- **不做物理备份**：本方案是逻辑备份（pg_dump），便于跨版本/跨实例恢复。如需 PITR / 大库高频备份，请改用 `pgBackRest` / WAL-G。
