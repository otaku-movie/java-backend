# prod_movie 上线数据迁移 Runbook

> **最终决策（已在本地 `prod_movie` 验证）**：
> 不再用 Flyway 从空库冷启动出生产 schema。当前迁移链与真实 dev 库 / 实体已经分叉，空跑 Flyway 会产出与应用不一致的表结构。
>
> 正式生产库结构以 `test_movie` 的真实 schema 为准：先克隆结构，再回填 Flyway 历史、载入种子数据、迁影院相关数据，最后跑校验和序列推进。

---

## 决策汇总

| 项 | 决策 |
|---|---|
| 目标库 | `prod_movie` |
| 业务 schema | `crawl`（后端 `currentSchema=crawl`） |
| schema 来源 | 克隆 `test_movie` 的真实结构（`public` + `crawl`） |
| Flyway 历史 | 回填已验证的 `public.flyway_schema_history`，避免重跑已分叉的历史脚本 |
| 种子数据 | 从 `test_movie.crawl` 导入权限、菜单、字典、协议、语言、等级、`cinema_spec`、`movie_manual_extras` 等基础数据 |
| 影院数据 | 迁 `areas`、`brand`、`cinema`、`theater_hall`、`cinema_spec_spec` |
| id 策略 | `brand`/`cinema`/`theater_hall` 生产重新自增；`areas` 保留原 id；`cinema_spec` 作为种子保留原 id |
| 电影 / 演职员 / 场次 | 不迁，上线后爬虫重建 |
| 座位 / 票价 / 促销 / 特典 / 预售 | 不迁，功能用到时后台重配 |
| 用户交易 / 评论 / 收藏 / OAuth | 不迁，上线后自然产生 |

本地验证后的核心结果：

| 表 | 行数 | id 策略 |
|---|---:|---|
| `areas` | 327 | 保留原 id（1-352，行政区字典） |
| `brand` | 8 | 生产自增，1-8 |
| `cinema` | 298 | 生产自增，1-298 |
| `theater_hall` | 2672 | 生产自增，1-2672 |
| `cinema_spec_spec` | 441 | 只迁能映射到现存影院的有效行 |

`cinema_spec_spec` 源库有 31470 行，但其中大量行引用历史旧影院 id；迁移脚本只迁能通过 `cinema_key` 映射到现存影院的行，本地验证结果为 441 行，完整性检查通过。

---

## 为什么不能纯跑 Flyway 建库

已对「全新 Flyway 产物」和 `test_movie.crawl` 做过列级 diff，发现多张表与当前实体 / dev 真实库不一致，例如：

- `theater_hall`：Flyway 产物缺 `seat_count`、`cinema_spec_id`、`row_count`、`column_count`、`seat_naming_rules`，实体实际使用这些列。
- `movie_comment`：Flyway 产物是 `user_id/rating` 风格，真实库和实体使用 `comment_user_id/like_count/unlike_count` 风格。
- `promotion_*`：Flyway 产物与实体几乎是两套设计。

因此生产结构必须以真实可运行的 `test_movie` schema 为基准。克隆后已验证 `prod.crawl` 与 `test.crawl` 逐列完全一致。

---

## 文件说明

| 文件 | 用途 |
|---|---|
| `01_prepare_cinema_staging.sql` | 在 `prod_movie` 创建 `staging` schema 和 5 张 staging 表 |
| `02_migrate_cinema_from_staging.sql` | 按业务键迁影院数据，不保留旧 id |
| `03_verify_cinema_migration.sql` | 迁移前后校验重复键、引用缺失、行数 |
| `04_fix_sequences.sql` | 根据当前最大 id 推进所有序列，包含共享序列 `public.auto_increment` |
| `audit_entity_schema.py` | 审计实体期望列与实际数据库列是否匹配 |

---

## 正式执行步骤

以下命令示例默认从一台能同时访问旧库 `test_movie` 和新库 `prod_movie` 的机器执行。生产环境中建议先对 `prod_movie` 做一次完整备份。

### 1. 导出结构和 Flyway 历史

```bash
# 从已验证的 prod_movie 保存 Flyway 历史；正式执行前也可以从本地验证库导出。
pg_dump -h <prod_host> -U postgres -d prod_movie \
  --data-only --column-inserts \
  -t public.flyway_schema_history \
  > prod_flyway_hist.sql

# 从 test_movie 导出真实结构（public + crawl，含序列、函数、索引、视图，不含数据）。
pg_dump -h <old_host> -U postgres -d test_movie \
  --schema-only --no-owner --no-privileges \
  > full_schema.sql
```

> 注意：`prod_flyway_hist.sql` 要来自已经对齐当前迁移脚本 checksum 的库。若直接在正式生产新库执行，需先确保这份历史包含当前仓库已存在的迁移版本。

### 2. 导出种子数据

```bash
pg_dump -h <old_host> -U postgres -d test_movie \
  --data-only --disable-triggers --column-inserts \
  -t crawl.role \
  -t crawl.menu \
  -t crawl.button \
  -t crawl.api \
  -t crawl.role_menu \
  -t crawl.role_button \
  -t crawl.users \
  -t crawl.user_role \
  -t crawl.agreement \
  -t crawl.language \
  -t crawl.level \
  -t crawl.dict \
  -t crawl.dict_item \
  -t crawl.movie_manual_extras \
  -t crawl.cinema_spec \
  > crawl_seed.sql
```

本地验证的种子行数：

| 表 | 行数 |
|---|---:|
| `role` | 4 |
| `menu` | 56 |
| `button` | 79 |
| `api` | 31 |
| `role_menu` | 150 |
| `role_button` | 197 |
| `users` | 7 |
| `user_role` | 2 |
| `agreement` | 27 |
| `language` | 10 |
| `level` | 4 |
| `dict` | 18 |
| `dict_item` | 66 |
| `movie_manual_extras` | 5 |
| `cinema_spec` | 13 |

### 3. 重建生产库结构

```bash
psql -h <prod_host> -U postgres -d prod_movie -v ON_ERROR_STOP=1 <<'SQL'
DROP SCHEMA IF EXISTS crawl CASCADE;
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
SQL

psql -h <prod_host> -U postgres -d prod_movie \
  -v ON_ERROR_STOP=1 \
  -f full_schema.sql
```

结构导入成功后应至少确认：

```sql
SELECT COUNT(*) FROM information_schema.tables
WHERE table_schema = 'public' AND table_type = 'BASE TABLE';

SELECT COUNT(*) FROM information_schema.tables
WHERE table_schema = 'crawl' AND table_type = 'BASE TABLE';

SELECT COUNT(*) FROM information_schema.views
WHERE table_schema = 'crawl';
```

本地验证结果：`public` 73 表、`crawl` 84 表、`crawl` 2 个视图。

### 4. 回填 Flyway 历史并导入种子

```bash
psql -h <prod_host> -U postgres -d prod_movie \
  -v ON_ERROR_STOP=1 \
  -f prod_flyway_hist.sql

psql -h <prod_host> -U postgres -d prod_movie \
  -v ON_ERROR_STOP=1 \
  -f crawl_seed.sql
```

导入后推进所有序列：

```bash
psql -h <prod_host> -U postgres -d prod_movie \
  -f infra/db-migrate/04_fix_sequences.sql
```

`04_fix_sequences.sql` 会解析 `crawl` 下所有 `nextval(...)` 默认值，对每个序列按当前最大 id 统一 `setval`。这一步很重要，因为 `test_movie` 的部分表共享 `public.auto_increment`，仅靠普通 `pg_get_serial_sequence` 会漏掉共享序列。

### 5. 迁影院数据

先准备 staging：

```bash
psql -h <prod_host> -U postgres -d prod_movie \
  -f infra/db-migrate/01_prepare_cinema_staging.sql
```

导出旧库影院相关源数据：

```bash
pg_dump -h <old_host> -U postgres -d test_movie \
  --data-only --column-inserts \
  -t crawl.areas \
  -t crawl.brand \
  -t crawl.cinema \
  -t crawl.theater_hall \
  -t crawl.cinema_spec_spec \
  > cinema_src.sql
```

导入 staging：

```bash
sed 's/crawl\./staging./g' cinema_src.sql | \
  psql -h <prod_host> -U postgres -d prod_movie -v ON_ERROR_STOP=1
```

如果希望 `cinema` / `theater_hall` id 从 1 开始，执行一次独立序列转换：

```sql
CREATE SEQUENCE IF NOT EXISTS crawl.cinema_id_seq OWNED BY crawl.cinema.id;
ALTER TABLE crawl.cinema ALTER COLUMN id SET DEFAULT nextval('crawl.cinema_id_seq');
SELECT setval('crawl.cinema_id_seq', 1, false);

CREATE SEQUENCE IF NOT EXISTS crawl.theater_hall_id_seq OWNED BY crawl.theater_hall.id;
ALTER TABLE crawl.theater_hall ALTER COLUMN id SET DEFAULT nextval('crawl.theater_hall_id_seq');
SELECT setval('crawl.theater_hall_id_seq', 1, false);
```

迁移并校验：

```bash
# 迁移前校验：所有 issue 查询应返回 0 行
psql -h <prod_host> -U postgres -d prod_movie \
  -f infra/db-migrate/03_verify_cinema_migration.sql

# 执行业务键迁移
psql -h <prod_host> -U postgres -d prod_movie \
  -v ON_ERROR_STOP=1 \
  -f infra/db-migrate/02_migrate_cinema_from_staging.sql

# 推进序列，避免显式 id / 共享序列导致后续插入冲突
psql -h <prod_host> -U postgres -d prod_movie \
  -f infra/db-migrate/04_fix_sequences.sql

# 迁移后校验：所有 issue 查询仍应返回 0 行
psql -h <prod_host> -U postgres -d prod_movie \
  -f infra/db-migrate/03_verify_cinema_migration.sql
```

确认无误后清理 staging：

```bash
psql -h <prod_host> -U postgres -d prod_movie \
  -c "DROP SCHEMA IF EXISTS staging CASCADE;"
```

### 6. 结构一致性审计

导出 `prod_movie.crawl` 实际列：

```bash
psql -h <prod_host> -U postgres -d prod_movie \
  -tA -F'|' \
  -c "SELECT table_name, column_name FROM information_schema.columns WHERE table_schema='crawl' ORDER BY 1,2;" \
  > prod_crawl_cols.txt
```

运行实体审计：

```bash
python infra/db-migrate/audit_entity_schema.py \
  src/main/java/com/example/backend/entity \
  prod_crawl_cols.txt
```

还应直接比对 `test_movie.crawl` 与 `prod_movie.crawl`：

```bash
psql -h <old_host> -U postgres -d test_movie \
  -tA -F'|' \
  -c "SELECT table_name, column_name FROM information_schema.columns WHERE table_schema='crawl' ORDER BY 1,2;" \
  > test_crawl_cols.txt

comm -23 test_crawl_cols.txt prod_crawl_cols.txt
comm -13 test_crawl_cols.txt prod_crawl_cols.txt
```

两条 `comm` 都无输出，表示 `prod.crawl` 与 `test.crawl` 逐列一致。本地已验证通过。

---

## 管理员账号安全处理

种子里的账号（如 `diy4869`、`123478` 等）不能直接作为生产管理员使用。上线后应新建生产管理员，并停用种子测试账号。

```sql
-- 1) 新建生产管理员（密码哈希按后端实际加密算法生成，勿用明文）
INSERT INTO crawl.users (name, password, email, data_scope, deleted, create_time, update_time)
VALUES ('你的管理员名', '<后端算法生成的密码哈希>', 'admin@yourdomain.com', 'platform', 0, now(), now());

-- 2) 给新管理员绑定 system 角色（role id 64 = system，来自种子数据）
INSERT INTO crawl.user_role (user_id, role_id, create_time, update_time, deleted)
SELECT u.id, 64, now(), now(), 0
FROM crawl.users u
WHERE u.name = '你的管理员名';

-- 3) 停用种子里的测试账号
UPDATE crawl.users
SET deleted = 1, update_time = now()
WHERE name IN ('diy4869', '123478', '111111', '123456', '234234', 'last_order', 'aaaaaa');
```

密码哈希生成方式需对齐后端登录校验逻辑。最稳妥方式是在后台创建管理员，再执行停用测试账号的 SQL。

---

## 上线顺序

1. 准备 `prod_movie` 数据库，按本 runbook 完成结构克隆、种子导入、影院迁移、校验和序列推进。
2. 启动生产后端，确认 Flyway 校验通过。当前历史回填到 V45，仓库里的 V46/V47 可在首启时幂等补跑。
3. 新建生产管理员并停用种子测试账号。
4. 启动爬虫，重建电影、演职员、场次、上映版本等可重爬数据。
5. 启动备份服务，确认 R2 上传与备份健康检查正常。
6. 按需在后台重配座位、票价、促销、特典、预售等人工配置。

---

## 注意事项

- **不要在正式生产空库上直接依赖 Flyway 冷启动 schema**：历史迁移链已与当前实体 / dev 真实库分叉。
- **迁影院数据必须在首次爬虫前完成**：这样爬虫会基于已迁影院更新，不会先插入一批缺少人工编辑信息的新影院。
- **`areas` 保留旧 id**：`cinema.area_id/prefecture_id/region_id` 直接沿用旧值。
- **`cinema_spec` 作为种子导入**：`cinema_spec_spec.spec_id` 直接沿用旧值。
- **`cinema_spec_spec` 行数少于源库是正常的**：只迁能映射到现存影院的有效行，过滤历史孤儿数据。
- **序列推进必须执行**：尤其是 `public.auto_increment` 这种共享序列，否则种子显式 id 载入后，后续新增可能主键冲突。
- **R2 凭证**：图片存储和数据库备份建议使用不同的最小权限 key，详见 `infra/pg-backup/README.md`。
