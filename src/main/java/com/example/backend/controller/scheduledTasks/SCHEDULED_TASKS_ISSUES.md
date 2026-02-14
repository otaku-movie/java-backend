# 定时任务当前问题说明

**订单超时**已与 Redis + RabbitMQ 方案统一：由 `MovieOrderService.processOrderTimeout(orderId)` 统一执行（订单置超时 + 清理 Redis locked + 更新 select_seat 表）；RabbitMQ 消费者与定时任务兜底均调用该方法，超时时间统一使用 `order.payment-timeout`（秒）。

## 1. 严重 / 易出错

### 1.1 更新电影放映状态 `updateMovieScreeningState`
- **NPE 风险**：`item.getStartTime()` / `item.getEndTime()` 为 null 时，`LocalDateTime.parse(...)` 会抛异常，导致整批任务失败。应对单条做 null/空串校验，解析失败则跳过该条并打日志。

### 1.2 更新订单状态 `updateMovieOrderState`
- **NPE 风险**：`item.getCreateTime()` 为 null 时，`createTime.toInstant()` 会抛异常。需在 filter 内判空，跳过 createTime 为 null 的订单。

### 1.3 选座状态任务 `updateMovieSeatSelectionState`
- **未实现**：方法体只有 log + 注释掉的代码，实际没有「超时释放座位」逻辑。若业务需要，需在此实现选座超时释放；若暂不需要，建议注明「预留」或移除/禁用该任务。

---

## 2. 逻辑 / 行为

### 2.1 更新电影上映日期状态 `updateMovieState`
- **全量更新**：`result` 包含所有通过日期格式过滤的电影，未做「仅更新有变更」的过滤，每天会对整表做一次 update。可改为只收集 status 发生变化的项再 updateBatchById，减少写入。
- **仅 endDate 有值**：当前只处理了「start+end」和「仅 start」两种分支，若只有 endDate 没有 startDate，不会更新 status，若业务需要可补分支。

### 2.2 场次定时公开 / 可购票 `updateShowTimePublishState`
- **全表扫描**：`eq("deleted", 0)` 会查出所有未删除场次，再在内存里根据 publish_at / sale_open_at 判断。若表很大且只有少量配置了定时，可改为 SQL 条件（如 `publish_at IS NOT NULL OR sale_open_at IS NOT NULL`）先缩小范围，再在内存里算 open/can_sale。

---

## 3. 时区与时间

- **统一用服务器本地时间**：各处使用 `LocalDateTime.now()` 或 `ZoneId.systemDefault()`，若部署环境与业务时区（如日本 JST）不一致，会导致「定时公开 / 开放购票 / 放映状态」在错误时刻切换。建议统一用业务时区（如 `ZoneId.of("Asia/Tokyo")`）或约定库内时间为该时区后再比较。

---

## 4. 代码质量

- **Raw 类型**：多处 `QueryWrapper` / `UpdateWrapper` 未指定泛型，会有类型安全与 lint 告警，建议改为 `QueryWrapper<Entity>` / `UpdateWrapper<Entity>`。
- **未使用导入**：如 `MovieShowTimeService`、`Controller`、`Map`、`Collectors` 等未使用，可删除以免干扰阅读。
- **HTTP 暴露**：带 `@PostMapping` 的定时方法会同时被 cron 和 HTTP 调用，若需防止外部误触发，可考虑权限校验或仅保留在一个内部入口上执行。

---

## 5. 小结与建议优先级

| 优先级 | 项 | 建议 |
|--------|----|------|
| 高 | 放映状态 startTime/endTime 空值 | 单条 null/空校验，解析失败跳过并打日志 |
| 高 | 订单 createTime 空值 | filter 内判空，避免 NPE |
| 高 | 选座超时释放 | 要么实现逻辑，要么注释说明预留/禁用 |
| 中 | 时区统一 | 与业务约定时区，用 ZonedDateTime 或固定 ZoneId |
| 中 | 场次任务查询范围 | 用 SQL 缩小 publish_at/sale_open_at 范围 |
| 低 | 电影状态只更新有变更 | 只把 status 变化的加入 result |
| 低 | 泛型与未使用 import | 顺手清理 |
