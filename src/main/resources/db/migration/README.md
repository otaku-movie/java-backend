# 数据库迁移（Flyway）

本目录由 [Flyway](https://flywaydb.org/) 自动加载执行，应用启动时按文件名版本号递增执行新脚本。

## 命名规范

```
V<版本号>__<简短描述>.sql        -- 一次性版本化迁移，按版本号递增执行
R__<描述>.sql                   -- 可重复迁移，checksum 变更后自动重跑（用于种子数据/视图）
U<版本号>__<描述>.sql            -- 回滚脚本（社区版不会自动执行，仅作记录）
```

- 版本号使用纯整数，从 V1 开始递增；不要插入中间版本号。
- 描述用 snake_case 短语，避免使用日期作版本号。
- 一旦合并入库 / 部署到任意环境，**禁止修改已存在的 V 脚本**。需要补救请新增 V 脚本。
- 重复运行需要幂等的脚本，请使用 `R__` 前缀，由 Flyway 通过 checksum 控制重跑。

## 已有脚本

| 版本 | 文件                                | 说明                                                                   |
|------|-------------------------------------|------------------------------------------------------------------------|
| V1   | `V1__baseline_schema.sql`           | 基线 schema（原 `sql/init_schema.sql`）                                |
| V2   | `V2__order_id_bigint.sql`           | 订单主键升级到 `bigint`，同步选座外键                                  |
| V3   | `V3__crawl_schema.sql`              | 新增 `crawl` schema，镜像业务表并承载爬虫入库数据                       |
| V4   | `V4__presale_tables.sql`            | `crawl.presale` / `crawl.presale_specification` 预售券表                |
| V5   | `V5__public_extra_tables.sql`       | 把历史上绕过 Flyway 落到 `public` 的 11 张业务表（benefit / pricing 等）补登记 |
| V6   | `V6__crawl_mirror_extra_tables.sql` | 将 V5 的 11 张表镜像到 `crawl` schema，保持 V3 的"镜像 public 全表"约束    |
| V7   | `V7__crawl_level_seed.sql`          | 初始化 `crawl.level` 的日本映倫分级（G / PG-12 / R-15 / R-18），供 crawler 写 `movie.level_id` |
| V8   | `V8__crawl_movie_show_time_tag_uk.sql` | 给 `crawl.movie_show_time_tag` 增加唯一键                                |
| V9   | `V9__crawl_movie_manual_extras.sql` | `crawl.movie_manual_extras` 手工补录表结构                              |
| V10  | `V10__crawl_movie_manual_extras_seed.sql` | `crawl.movie_manual_extras` 初始 5 条手工补录数据                       |
| V11  | `V11__crawl_permission_seed.sql`    | 把 public 的 RBAC / 鉴权表（users、role、menu、button、api、user_role、role_menu、role_button、user_oauth_binding、user_cinema）以及字典表（dict、dict_item）数据写死成种子直接 INSERT 进 crawl schema，并对齐 IDENTITY 序列 |
| V12  | `V12__crawl_areas_seed.sql`         | 从 public.areas 同步 327 行行政区字典到 crawl.areas、清理爬虫遗留的 12 行非标 prefecture（id 617-628）映射、根据 address 前缀给 crawl.cinema 回填 prefecture_id / region_id |

## 接入策略

`application.yml` 已设置 `spring.flyway.baseline-on-migrate: true`，对老环境友好：

- **全新数据库**：启动时 Flyway 会从 V1 开始顺序执行所有脚本。
- **已有数据的数据库**：首次启动 Flyway 会创建 `flyway_schema_history` 表，并把当前结构标记为 `baseline-version`（默认 0），随后从 V1 开始追加执行。
  - 因为 V1 全部使用 `CREATE TABLE IF NOT EXISTS`，对老表是 no-op，对新增对象会自动建。
  - V2 中的 `ALTER COLUMN ... TYPE BIGINT` 在已有库才有实际效果。

## 常用操作

```bash
# 仅编译，启动时会自动跑 migrate
./mvnw -DskipTests compile

# 启动应用（会触发 Flyway migrate）
./mvnw spring-boot:run

# 校验 schema 与脚本是否一致（不实际执行）
./mvnw flyway:info -Dflyway.url=jdbc:postgresql://localhost:5432/test_movie \
                  -Dflyway.user=postgres -Dflyway.password=postgre

# 修复 checksum 不一致 / 失败状态（谨慎使用）
./mvnw flyway:repair
```

> 如果只跑 `mvn flyway:*`，需要在 `pom.xml` 里加 `flyway-maven-plugin`。
> 目前项目默认通过 Spring Boot 启动时执行迁移，不强依赖 Maven 插件。

## 不在 Flyway 里管理的脚本

`src/main/resources/sql/` 目录仍保留以下脚本：

- `createDatabase.sql`：在 Docker `docker-entrypoint-initdb.d` 中创建数据库本身，先于 Flyway 运行。
- `init_display_type_dict.sql` / `init_refund_state_dict.sql`：旧的字典种子脚本。
  - **注意**：这两个脚本使用了 `dict_item.name/code` 列，而当前 `V1__baseline_schema.sql` 里 `dict_item` 表定义的是 `label/value`。
  - 接入 Flyway 前需先确认实际线上列名再以 `R__seed_xxx_dict.sql` 形式重写并放入本目录。
