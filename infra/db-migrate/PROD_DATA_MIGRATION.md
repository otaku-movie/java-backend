# prod_movie 上线方案（最小化迁移）

> **最终决策**：只迁 6 张影院相关表（`areas`/`brand`/`cinema`/`cinema_spec`/`cinema_spec_spec`/`theater_hall`，
> 因含后台人工编辑），其余数据**不迁**。
> 生产库从空开始，靠「迁移这 6 张表 + Flyway 种子 + 爬虫重建 + 后台重配 + 新建管理员」拉起。

---

## 决策汇总

| 项 | 决策 |
|---|---|
| 目标库 | `prod_movie`（全新空库） |
| Schema | `crawl` |
| **影院 / 品牌 / 地区**：`brand`/`areas`/`cinema`/`cinema_spec`/`cinema_spec_spec`/`theater_hall` | **要迁**（含后台人工编辑，保留原 id） |
| 电影 / 演职员 / 场次 | **不迁**，上线后爬虫重建 |
| 权限 / 菜单 / 字典 / 协议 / 语言 / 等级 | **不迁**，Flyway 种子自动写入 |
| 座位 / 票价 / 促销 / 特典 / 预售 | **不迁**（数据是假的，功能用时后台重配） |
| 管理员账号 | **不迁**，生产新建强密码管理员；停用种子里的测试账号 |
| 交易 / 评论 / 收藏 / OAuth 绑定等用户数据 | **不迁**，上线后自然产生 |

→ 结论：**只迁 6 张影院相关表（保留人工编辑），其余靠种子 / 爬虫 / 后台重建。**

> 为什么这 6 张要迁：影院信息、影厅改名（`theater_hall.crawl_name` vs `name`）、
> 字段锁（`cinema.manual_locked_fields`）等都是后台人工修正，重爬不会还原。
> 保留原 id 迁过去后，爬虫仍能靠 `cinema_key` / `(cinema_id, crawl_name)` 匹配更新，不产生重复。

---

## 三类数据的来源

### 1. Flyway 启动自动种子（建库即有）

| 内容 | 种子脚本 |
|---|---|
| RBAC：`role`/`menu`/`button`/`api`/`role_menu`/`role_button` | `V11` |
| 管理员账号 `users` + `user_role` | `V11`（⚠️ 见下方安全处理） |
| `areas` 地区 | `V12` |
| `agreement` 协议 | `V23` |
| `language` | `V15` |
| `level` | `V7` |
| 各类 `dict` / `dict_item` | `V16`/`V22`/`V32` |
| `movie_manual_extras` 种子 | `V10` |

### 2. 爬虫重建（上线跑一遍）

`movie`、`movie_version`、`movie_show_time`、`movie_show_time_tag`、
`staff`、`position`、`movie_staff`、`character`、`movie_character`、
`movie_version_character`、`movie_version_character_staff`、`re_release`、
`crawl_movie_master*`

> 注：`cinema`/`theater_hall`/`brand`/`areas` 虽然爬虫也会写，但因含人工编辑改为**先迁后爬**
> （见下方「需要迁移的 6 张表」）。爬虫在已迁数据基础上更新，不会重复。

### 3. 后台手动配置（用到对应功能时再配）

座位 `seat`/`seat_area`/`seat_aisle`、票价 `cinema_price_config`/`cinema_price_rules_config`、
支付 `payment_method`/`payment_methods`、促销 `promotion*`/`pricing_rule`、
特典 `benefit*`、预售 `presale*`

---

## ⚠️ 安全处理：种子管理员账号

`V11` 种子里的账号（`diy4869`、`123478` 等）密码哈希**明文存在于仓库**，
生产**绝不能直接用**。上线后立刻执行：

```sql
-- 1) 新建生产管理员（密码哈希按后端实际加密算法生成，勿用明文）
INSERT INTO crawl.users (name, password, email, data_scope, deleted, create_time, update_time)
VALUES ('你的管理员名', '<后端算法生成的密码哈希>', 'admin@yourdomain.com', 'platform', 0, now(), now());

-- 2) 给新管理员绑定 system 角色（role id 64 = system，由 V11 种子提供）
INSERT INTO crawl.user_role (user_id, role_id, create_time, update_time, deleted)
SELECT u.id, 64, now(), now(), 0
FROM crawl.users u
WHERE u.name = '你的管理员名';

-- 3) 停用 V11 种子里的测试账号（软删除，避免被登录）
UPDATE crawl.users
SET deleted = 1, update_time = now()
WHERE name IN ('diy4869', '123478', '111111', '123456', '234234', 'last_order', 'aaaaaa');
```

> 密码哈希生成方式需对齐后端登录校验逻辑（确认是 MD5 / BCrypt / 自定义）。
> 最稳妥：上线后用后台「新增用户」功能创建管理员，再用上面的 SQL 停用种子账号。

---

## 需要迁移的 6 张表（影院相关，保留人工编辑）

迁移对象（均在 `crawl` schema）：
`areas`、`brand`、`cinema`、`cinema_spec`、`cinema_spec_spec`、`theater_hall`

**时机**：在 prod backend 跑完 Flyway（表已建好）之后、**首次爬虫之前**执行。
这样爬虫会在已迁数据上更新，不会插重复。

### 步骤 1：从旧库导出（保留原 id，幂等）

```bash
# 在能连到 test_movie 的机器上执行；同实例 / 不同实例都适用（产物是 SQL 文件）
pg_dump \
  -h <旧库host> -p 5432 -U postgres -d test_movie \
  --data-only --column-inserts --on-conflict-do-nothing \
  -t crawl.areas -t crawl.brand -t crawl.cinema \
  -t crawl.cinema_spec -t crawl.cinema_spec_spec -t crawl.theater_hall \
  > cinema_seed_data.sql
```

- `--column-inserts`：逐行 INSERT 且带列名，**保留原始 id**。
- `--on-conflict-do-nothing`：和 Flyway 种子（如 `areas` 由 `V12` 预置）撞 id 时跳过，不报错。

### 步骤 2：导入新库

```bash
psql -h <新库host> -p 5432 -U postgres -d prod_movie -f cinema_seed_data.sql
```

### 步骤 3：重置 identity 序列（必做）

`areas`/`brand`/`cinema`/`theater_hall` 是 identity 主键，导入后必须把序列推到 `max(id)+1`，
否则后台/爬虫新增会从 1 起撞already存在的 id。`cinema_spec`/`cinema_spec_spec` 是联合主键、无序列，跳过。

```sql
-- 连到 prod_movie 执行
SELECT setval(pg_get_serial_sequence('crawl.areas','id'),        COALESCE((SELECT MAX(id) FROM crawl.areas),0)+1,        false);
SELECT setval(pg_get_serial_sequence('crawl.brand','id'),        COALESCE((SELECT MAX(id) FROM crawl.brand),0)+1,        false);
SELECT setval(pg_get_serial_sequence('crawl.cinema','id'),       COALESCE((SELECT MAX(id) FROM crawl.cinema),0)+1,       false);
SELECT setval(pg_get_serial_sequence('crawl.theater_hall','id'), COALESCE((SELECT MAX(id) FROM crawl.theater_hall),0)+1, false);
```

> 备份容器镜像自带 `psql`/`pg_dump`，也可在 `movie-pg-backup` 容器里执行这些命令。

---

## 唯一性 / 防冲突保证（重要）

迁移后「Flyway 种子 + 迁移影院数据 + 爬虫」三方合并，冲突只可能出现在 3 个层面，逐一封堵：

### A. 主键 id 冲突（硬报错）

| 表 | 是否与种子重叠 | 处理 |
|---|---|---|
| `areas` | ⚠️ 由 `V12` 种子预置同 id | `--on-conflict-do-nothing` 跳过，保留种子行（种子本就源自 test_movie，内容一致） |
| `brand`/`cinema`/`cinema_spec`/`cinema_spec_spec`/`theater_hall` | 否，新库为空 | 直接插入，无冲突 |

→ 导出命令已带 `--on-conflict-do-nothing`，**不会因 id 重复报错**。

### B. 自然键重复（爬虫插出第二行 = 业务重复）⚠️ 最易踩

爬虫靠自然键 upsert：`cinema.cinema_key`、`theater_hall (cinema_id, crawl_name)`。
**迁移的行必须带齐这些键**，否则爬虫匹配不到会插重复。迁移前在 test_movie 自查：

```sql
-- 1) cinema 是否有 cinema_key 为空（这些行爬虫会插重复）
SELECT id, name FROM crawl.cinema WHERE deleted = 0 AND cinema_key IS NULL;

-- 2) theater_hall 是否有 crawl_name 为空（V43 应已回填，残留的需补）
SELECT id, cinema_id, name FROM crawl.theater_hall WHERE deleted = 0 AND crawl_name IS NULL;

-- 3) 同一影院下 crawl_name 是否已存在重复（迁过去会撞唯一索引）
SELECT cinema_id, crawl_name, COUNT(*) FROM crawl.theater_hall
WHERE deleted = 0 AND crawl_name IS NOT NULL
GROUP BY cinema_id, crawl_name HAVING COUNT(*) > 1;
```

- 查询 1/2 若有结果：先在旧库补键（`theater_hall` 可 `UPDATE ... SET crawl_name = name`），再导出。
- 查询 3 若有结果：旧库本身就有重复影厅，需先清理，否则 `psql` 导入会因 `uk_crawl_theater_hall_cinema_crawl_name` 报错。

### C. 序列冲突（未来新增撞已存在 id）

`areas`/`brand`/`cinema`/`theater_hall` 必须按上方步骤 3 重置序列到 `max(id)+1`。

### 导入后最终校验（在 prod_movie 执行，应全部返回 0 行）

```sql
SELECT 'dup cinema_key' t, cinema_key FROM crawl.cinema
  WHERE cinema_key IS NOT NULL AND deleted=0
  GROUP BY cinema_key HAVING COUNT(*)>1
UNION ALL
SELECT 'dup hall', cinema_id::text||'/'||crawl_name FROM crawl.theater_hall
  WHERE crawl_name IS NOT NULL AND deleted=0
  GROUP BY cinema_id, crawl_name HAVING COUNT(*)>1;
```

---

## 上线步骤（最小化）

```bash
# 1) 起生产栈（首次会自动建空库 prod_movie，backend 启动跑 Flyway 建表 + 种子）
docker compose --env-file .env.prod \
  -f docker-compose.prod.yml up -d --build

# 2) 确认 Flyway 迁移成功（看 backend 日志，无 migration 报错）
docker logs -f movie-backend-prod

# 3) 处理管理员账号安全（见上方 SQL），并验证后台能用新账号登录

# 4) 迁移 6 张影院相关表（见上方「需要迁移的 6 张表」：导出 → 导入 → 重置序列）
#    必须在首次爬虫之前完成

# 5) 启动爬虫，重建电影 / 场次数据，并在已迁影院数据上更新
#    （按 cinema-crawler 的部署方式运行 crawl:all 或定时任务）

# 6) 起备份服务（叠加 backup overlay）
docker compose --env-file .env.backup \
  -f docker-compose.prod.yml -f docker-compose.backup.yml up -d --build pg-backup

# 7) 按需在后台重配座位 / 票价 / 促销 / 特典 / 预售
```

---

## 注意事项

- **schema 一致性**：生产已统一 `currentSchema=crawl`（`application-prod.yml` + `docker-compose.prod.yml`）。
- **序列 reset**：仅迁移的 6 张表中 `areas`/`brand`/`cinema`/`theater_hall` 需手动重置序列（见步骤 3）；其余表无手动塞 id，序列从种子 `max(id)+1` 起（V11 末尾已推进），不会冲突。
- **迁移时机**：6 张影院表必须在「Flyway 建表后、首次爬虫前」迁移，否则爬虫会先插入新 id 的影院，再迁旧 id 就可能重复。
- **R2 凭证**：生产建议把图片存储和数据库备份各用一把最小权限 key，详见 `infra/pg-backup/README.md`。
