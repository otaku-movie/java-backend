## 重映排片支持改造方案（落地文档）

### 背景与目标
当前系统已存在“重映（`re_release`）”管理能力，但影院端新增场次（`movie_show_time`）无法绑定重映信息，导致：
- 无法为重映电影排片
- 同一电影多次重映、重映特殊版本信息缺少承载点
- App “正在上映”列表无法稳定包含重映（尤其在排片断档时）

本改造目标：
- **影院端新增场次支持选择重映计划**
- **重映支持多次**（同一 `movie_id` 可有多条重映记录）
- **重映支持特殊版本/覆盖信息**（可选：展示名、海报、版本说明、限制条件）
- **App 正在上映/即将上映正确包含重映**（按“是否存在未来公开排片”合并，并返回重映标识字段供前端展示）
- **App 电影详情补齐重映历史信息**（可查看该电影所有重映批次）

---

### 核心设计
以 Movie 为主体，重映信息**复用 Movie**，差异信息由重映计划承载：

- `movie`：影片主体（名称、海报、时长等长期信息）
- `re_release`：重映计划（每次重映一条记录，承载差异信息与有效期）
- `movie_show_time`：场次增加 `re_release_id`（可空），用于标识该场次属于哪次重映

> `movie_show_time.re_release_id IS NULL`：普通上映场次  
> `movie_show_time.re_release_id IS NOT NULL`：重映场次（关联到某次重映计划）

---

### 数据库改动
#### 1）`re_release` 表补齐字段（向后兼容）
- `start_date` / `end_date`：重映有效期（end 可空）
- `version_info`：特殊版本说明（可空）
- `display_name_override`：展示名覆盖（可空）
- `poster_override`：海报覆盖（可空）
- `status`：启用/停用（默认启用）
- `time_override`：时长覆盖（可空，单位分钟）

#### 2）`movie_show_time` 表增加字段
- `re_release_id`：关联 `re_release.id`（可空）

---

### 后端改动点（java-backend）
#### A. 重映管理
- 允许同一 `movie_id` 存在多条 `re_release` 记录
- 管理端保存/列表接口补齐字段：有效期、版本信息、启用状态、覆盖信息

#### B. 新增/保存场次
- 入参支持 `reReleaseId`（可空）
- `reReleaseId != null` 时校验：
  - 记录存在且启用
  - `re_release.movie_id == movie_show_time.movie_id`
  - 场次日期落在重映有效期内（`start_date <= show_date <= end_date(若不空)`）

> 备注：数据库层面不加外键约束（初始化与迁移均不创建 FK），仅通过业务校验保证一致性。

#### C. App 正在上映列表
“正在上映”合并口径：
- 原有上映逻辑保持
- 追加：合并 **存在未来公开排片** 的重映条目
  - `re_release.status=启用`
  - 存在 `movie_show_time.re_release_id = re_release.id` 且 `open=TRUE` 且 `start_time >= CURRENT_TIMESTAMP` 的场次
  - 当前实现 **不再以 `re_release.start_date/end_date` 作为门槛**（只要有场次就展示）

返回附加信息（用于前端展示角标/跳转特典等）：
- `isReRelease`
- `reReleaseId`
- `reReleaseVersionInfo`
- `name` / `cover`：优先使用 `display_name_override` / `poster_override`（若为空则回退 movie 字段）
- `presaleId` / `hasPresaleTicket` / `hasBonus`

#### D. App 即将上映列表
“即将上映”合并口径：
- 原有即将上映逻辑保持
- 追加：合并 **未来重映批次**，并返回重映标识字段
  - `re_release.status=启用`
  - `re_release.start_date > today`
  - 存在未来公开排片（`movie_show_time.open=TRUE` 且 `start_time > CURRENT_TIMESTAMP`）
- 重映条目的 `startDate` 使用 `re_release.start_date`（`YYYY-MM-DD`），不再沿用 `movie.start_date`

#### E. App 场次接口：按重映批次过滤
为避免普通上映与重映场次混入，App 场次接口支持可选参数 `reReleaseId`：
- 传 `reReleaseId`：只返回该重映批次的场次（`movie_show_time.re_release_id = reReleaseId`）
- 不传 `reReleaseId`：只返回普通上映场次（`movie_show_time.re_release_id IS NULL`）

#### F. App 电影详情：重映历史接口
新增接口用于电影详情页展示“重映历史”：
- `GET /api/app/movie/reReleaseHistory?movieId=...`
- 返回该 `movieId` 的所有重映批次（按 `start_date` 倒序），字段包含：
  - `id/startDate/endDate/status/versionInfo/displayNameOverride/posterOverride/timeOverride`

---

### 管理后台改动点（admin）
#### 影院端新增场次页面
- 选择电影后，可选“普通上映 / 重映”
- 选择重映时：使用“表格弹窗选择”具体 `re_release`（展示展示名/版本信息/开始结束/启用状态/时长覆盖）
- 保存场次时带上 `reReleaseId`

#### 重映列表展示优化
- 重映列表按 `movieId` 合并展示（主表一行一个电影，可展开查看该电影的重映批次列表）

#### 重映新增/编辑表单
- 结束时间 `endDate` 可为空
- 海报覆盖 `posterOverride` 使用图片上传组件
- 支持配置 `timeOverride`（仅覆盖时长；原名/简介不做覆盖）

---

### App 改动点（Flutter）
- 正在上映列表：合并重映条目，并展示“重映”角标 + `reReleaseVersionInfo`
- 即将上映列表：重映条目按 `re_release.start_date` 分组/排序，并展示“重映”角标（修复标签溢出）
- 场次页：
  - 从“正在上映”的重映条目进入时，携带 `reReleaseId`，按批次过滤场次
  - 场次数据包含 `reReleaseId` / `reReleaseVersionInfo`（用于页面展示与后续能力扩展）
- 电影详情页：新增“重映历史”区块（纯信息展示：开始/结束/时长/启用状态），并补齐 i18n

#### i18n（App）
- 新增 key（中/日/英）用于重映历史与重映标签：
  - `movieDetail_reReleaseHistory_*`
  - `movieList_tag_reRelease`

---

### 测试清单（关键用例）
- 同一电影新增多条重映计划，均可被选择排片
- 重映停用后不可排片、App 不展示
- 场次绑定重映计划时有效期校验生效
- App 正在上映：重映批次即使 `start_date > today`，只要有未来公开场次也必须可见
- App 场次页：带 `reReleaseId` 仅返回该批次场次；不带 `reReleaseId` 仅返回普通上映场次
- App 电影详情：重映历史可展示结束时间（若有）、时长覆盖（若有）、未启用状态，并且文案多语言正确

