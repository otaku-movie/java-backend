# 票价预览与报价设计

本文档描述：**场次列表、选座、选票种**等购票流程中的票价展示与计算方案。  
与 [PRICE_STRATEGY.md](./PRICE_STRATEGY.md)（叠加公式与表结构）、[TICKET_TYPE_REQUIREMENTS.md](./TICKET_TYPE_REQUIREMENTS.md)（票种归属与排期字段）互补；本文聚焦**产品边界、页面分工、预览 API 与落地分期**。

---

## 1. 产品定位：报价，不是比价

| 概念 | 含义 | 本方案是否覆盖 |
|------|------|----------------|
| **报价 / 票价预览（Price Quote）** | 在**单一影院、单一场次**下，按规则算出可参考的票价区间或明细 | ✅ 核心 |
| **算价（Pricing）** | 用户选票种、选座位后，得到可下单的准确金额 | ✅ 下单链路已有，H5 待接 |
| **比价（Price Compare）** | 同一商品跨影院、跨平台比较「哪里更便宜」 | ⚠️ 仅场次列表跨影院展示统一口径「¥X 起」时，算**轻度参考**，不是完整比价引擎 |
| **渠道比价** | 本平台 vs 官网 vs ムビチケ 等 | ❌ 不在本文范围 |

**命名建议（接口 / 代码）**

- 使用 `price-preview`、`price-quote`，避免 `price-compare`。
- 对用户文案使用「参考价」「本场适用」，避免「最低价」「保证价」。

---

## 2. 价格来源分层

日本影院票价不是单一标价，而是多层规则叠加。展示与算价须先识别「当前处于哪一层」。

```
┌─────────────────────────────────────────────────────────────┐
│ L4  座位区域加价（area_price）         选座后才知道           │
├─────────────────────────────────────────────────────────────┤
│ L3  放映类型 + 规格加价（3D / IMAX / MX4D）  场次维度       │
├─────────────────────────────────────────────────────────────┤
│ L2  票种基础价（movie_ticket_type）    按影院 + 场次时间过滤  │
│     ├ 常设：一般、大学生、シニア…（schedule_type = null）    │
│     ├ 服务日：ウェンズデイ、レイトショー…（schedule_type 有值）│
│     └ 会员价：プラス会員、TOHO-ONEメンバーデイ…（同表一条记录）│
├─────────────────────────────────────────────────────────────┤
│ L1  场次定价模式（movie_show_time）                          │
│     ├ pricing_mode = 2 → fixed_amount 整场固定价            │
│     ├ pricing_mode = 1 → pricing_rule 活动规则（按人群）     │
│     └ movie_show_time_ticket_type → 单场禁用票种 / 覆盖价    │
└─────────────────────────────────────────────────────────────┘

单座应付（下单权威）= L1 基础价 + L3 加价 + L4 区域价
```

### 2.1 票种数据（L2）

- 表：`movie_ticket_type`，按 `cinema_id` 归属，**不同影院名称、档位、顺序均可不同**。
- 官网抓取经 `cinema-crawler` → `import:prices` 入库；服务日映射见 `import-prices-pg.ts` 注释。
- 关键字段：

| 字段 | 说明 |
|------|------|
| `price` | 2D 基础价（円） |
| `schedule_type` | null = 常设；1 周 / 2 月 / 3 每日时段 / 4 特定日期 |
| `applicable_weekdays` 等 | 与 `schedule_type` 配合，决定何时出现在「本场可用票种」 |
| `order_num` | 越小越靠前；爬虫导入时 **adult 档通常为 0** |
| `description` | 持证、年龄、会员条件等 |

爬虫 JSON 中有 `category: adult | member | …`，已写入 `movie_ticket_type.audience_category`（V79 + `import:prices`）。

### 2.2 会员价

会员价在数据模型上**不是单独模块**，而是 `movie_ticket_type` 中的一条（或多条）记录，例如：

- AEON：`ハッピーマンデー（プラス会員）`
- TOHO：`TOHO-ONEメンバーデイ`（`note`：毎週火曜日・会員限定）

**系统不知道用户是否持卡**，因此：

- 列表/预览：可展示「会员参考价」，须标注「会員限定」。
- 下单：**不得**默认套用会员价；由用户在选票种页自行选择。

### 2.3 场次限价（L1）

| 机制 | 存储 | 行为 |
|------|------|------|
| 整场固定价 | `pricing_mode = 2`，`fixed_amount` | 不展示票种选择；基础价 = `fixed_amount` |
| 单场票种覆盖 | `movie_show_time_ticket_type` | `enabled = false` 禁用某票种；`override_price` 覆盖该场该票种价格 |
| 活动规则 | `pricing_mode = 1`，`pricing_rule` | 按 `audience_type` 匹配规则价（与票种表并行，运营配置） |

**现状缺口**（实现前须知晓）：

- `listByShowtime` 已根据 `movie_show_time_ticket_type` **过滤禁用票种**。
- `override_price` 仅在管理端场次详情回填，**尚未合并进** `ticketType/list` 与 `TicketPriceService` 算价。

---

## 3. 计算公式（与现有服务一致）

权威算价见 [PRICE_STRATEGY.md](./PRICE_STRATEGY.md)：

```
最终票价 = 票种基础价（或 fixed_amount / 规则价）
         + 3D 加价 + 规格加价
         + 座位区域加价（area_price）
```

| 组件 | 入口 |
|------|------|
| 票种 + 3D + 规格 | `TicketPriceService.calculatePrice()` |
| 含场次模式、前售券 | `TicketPriceService.calculatePriceByShowtime()` |
| 本场可用票种列表 | `MovieTicketTypeService.listByShowtime()` |
| 选座页场次信息 | `GET /api/movie_show_time/user_select_seat` |
| 下单 | `POST /api/movieOrder/create` → `MovieOrderService` |

App 选票页（`SelectMovieTicket`）在客户端按相同公式汇总；**下单以后端为准**。

---

## 4. 页面场景与职责

不同页面承担不同职责，**不能把影院详情「票价表」原样搬到选座页**。

### 4.1 影院详情 — 只读参考

- 接口：`POST /api/cinema/ticketType/list`（全量常设 + 服务日说明）。
- 展示：票价、特殊规格加价、影厅信息（已有 `CinemaDetailPanels`）。
- 作用：查价参考、跳转官网；**不参与算价**。

### 4.2 场次列表 — 票价预览（已落地：预估价 + 会员标记，不选票种）

- 页面：H5 `movie/[id]/showtimes.vue`、`cinema/[id]/showtimes.vue`。
- **不展开票种列表、不做选票种**；每场卡片展示参考价即可（组件 `ShowtimePriceLine`）。
- 接口：`GET /api/movie_show_time/price-preview`、`POST .../price-preview/batch`。

当前展示（对齐 §11 业界「作品块一行参考价」的简化版）：

```
19:30  MX4D
¥2,600 起                    ← referencePrice（公众基准 + 本场加价）
水曜 ¥1,300 · 会員 ¥1,100    ← 优惠 / 会员小标签（非承诺价）
```

固定价场次：

```
¥1,500 起                    ← pricing_mode=2 的 fixed_amount + 加价
```

跨影院并排时，用户可横向对比「起价」——此为**同片跨影院参考**，仍非完整比价。

**不做**：作品级「特別料金」弹窗或独立入口（§11 截图仅作**内容参考**，见 §11 说明）。

### 4.3 选座页 — 只选座

- 页面：H5 `showtime/[id]/seats.vue`。
- 展示场次头（电影、厅、规格、**预估参考价** via `price-preview`）。
- **不选票种**；确认按钮仍提示支付未开放。

### 4.4 选票种页 — 算价交互（**本期不做**，对齐 App 为二期）

- 流程：选座 → **选票种** → 确认订单。
- 接口：
  - `POST /api/movie_show_time/ticketType/list`（本场可用票种）
  - `GET /api/movie_show_time/user_select_seat`（规格加价、区域价）
- 交互：
  - **每座一张卡**，点击弹出票种 BottomSheet（App 已实现）。
  - 票种列表建议分组（见 §5）。
  - `pricing_mode = 2` 时隐藏票种选择，仅显示固定价。

### 4.5 购票流程（本期 vs 二期）

**本期（预估价，不选票种）**

```
场次列表 ──price-preview──► 选座（头部仍展示参考价）
```

**二期（完整下单）**

```
场次列表 ──参考价──► 选座 ──► 选票种 ──► 创建订单
```

---

## 5. 票种展示与默认规则

### 5.1 不能全局写死「一般」

各影院成人基准票名称不一（一般、大人、通常会員…）。  
**禁止**前端 `name === '一般'` 作为唯一逻辑。

### 5.2 基准票种（reference / 默认预选）

在**本场 `listByShowtime` 返回列表**中选取：

1. `schedule_type IS NULL`（常设票档）；
2. 其中 `order_num` 最小的一条（爬虫导入时 adult 档优先）。

若无常设票档，取列表中 `order_num` 最小者。  
后端可在响应中增加 `defaultTicketTypeId`（推荐）。

### 5.3 票种分组（选票种 UI）

| 分组 | 识别方式（过渡期） | 示例 |
|------|-------------------|------|
| 通常 | `schedule_type = null` 且非会员名 | 一般、大学生 |
| 本场优惠 | `schedule_type != null` 且非会员名 | ウェンズデイ、レイトショー |
| 会员限定 | `member_required = true` 或 `audience_category = member` 或 `service_day_code` 以 `_member` 结尾；旧数据仍用名称正则兜底 | プラス会員、TOHO-ONEメンバーデイ |

**默认预选**：「通常」分组的基准票（§5.2）。  
**不自动预选**：优惠票种、会员票种。

### 5.4 用户资格

会员价、合作方价（Ponta、JERA 等）、学生价等，均在 `description` 说明条件。  
MVP：全部列出 + 徽标；二期可增加「我是会员」开关仅影响排序/折叠，**不改变默认选中**。

---

## 6. 票价预览 API（已实现）

### 6.1 单场预览

**接口**：`GET /api/movie_show_time/price-preview?id={movieShowTimeId}`

**用途**：场次列表批量展示、选座页头部。

**当前响应字段**（`ShowTimePricePreviewResponse`）：

```json
{
  "movieShowTimeId": 34701,
  "pricingMode": 0,
  "fixedAmount": null,
  "referencePrice": 2600,
  "promoFromPrice": 1800,
  "promoLabel": "TOHOウェンズデイ",
  "memberFromPrice": 1800
}
```

### 6.2 字段定义

| 字段 | 说明 |
|------|------|
| `pricingMode` | 0 默认票种 / 1 活动规则 / 2 固定价 |
| `fixedAmount` | 固定价模式基础价（未含 L3 加价） |
| `referencePrice` | **公众基准**：`schedule_type=null` 且非会员票种中 `order_num` 最小者 + L3 加价 |
| `promoFromPrice` | 非会员票种最低价 + 加价；仅当 **低于** `referencePrice` 时返回 |
| `promoLabel` | 达成 `promoFromPrice` 的服务日票种名称（截断） |
| `memberFromPrice` | 会员票种（名称含 会員/メンバー/Ponta 等）最低价 + 加价 |

**算法要点**：

- 票种列表：`MovieTicketTypeService.listByShowtime(id)`。
- 加价：`TicketPriceService.getSurchargeForShowtime(showtime)`，含 **3D**（`cinema_price_config`）与 **IMAX / Dolby 等规格**（`cinema_spec_spec.plus_price`，按场次 `spec_ids` 累加）。`MovieShowTime` 须 `autoResultMap=true` 方能正确读出 `spec_ids`。
- `referencePrice` **不得**用全场 `min(price)`（避免大学生价、会员价误作起价）。
- `override_price`（场次覆盖价）**尚未合并**（见 §8）。

### 6.3 批量预览

**接口**：`POST /api/movie_show_time/price-preview/batch`  
**body**：`{ "movieShowTimeIds": [34701, 34702, ...] }`  
**上限**：单次 ≤50；H5 composable 自动分批。

### 6.4 与现有接口关系

| 接口 | 关系 |
|------|------|
| `ticketType/list` | 预览内部调用；二期选票种页用完整列表 |
| `user_select_seat` | 含区域价；预览不含 L4 |
| `movie_show_time/detail` | 含 `ticketTypeOverrides`（管理回填） |

---

## 7. 文案与合规

| 场景 | 推荐文案 | 避免 |
|------|----------|------|
| 场次卡主价 | `¥2,600 起` / `参考价 ¥2,600` | `特价`、`最低价` |
| 服务日标签 | `水曜 ¥1,300` | 暗示用户一定能买 |
| 会员价 | `会員 ¥1,100（要持会員证）` | 默认按会员价算合计 |
| 固定价场 | `本场定价 ¥1,500` | `起` |
| 选票种页合计 | `合计 ¥4,400` | 未选票种时显示确定金额 |
| 页脚 | `实际票价以所选票种、座位及影院规则为准` | — |

---

## 8. 数据与后端待办

| 优先级 | 项 | 说明 |
|--------|-----|------|
| ~~P0~~ | ~~`price-preview` 接口~~ | ✅ 已实现 |
| ~~P1~~ | ~~H5 场次卡预估价 + 会员标记~~ | ✅ `ShowtimePriceLine` |
| P0 | `override_price` 并入算价与 `ticketType/list` | 场次特价才准确 |
| ~~P1~~ | ~~`audience_category` / `price_kind` / `service_day_code` / `member_required`~~ | ✅ V79 + `import:prices`；预览服务用列替代名称正则 |
| P2 | `special_pricing` 结构化入库（可选） | 供算价/运营参考，**不要求 H5 展示弹窗**（§11） |
| P2 | `promoSuppressed` 等预览规则 | 特別定价与サービスデー冲突时抑制误导性低价标签 |
| P2 | H5 选票种页 | 对齐 App |
| P2 | 用户会员身份（可选） | 仅影响排序/高亮，不自动改价 |
| P3 | 跨平台比价 | 独立项目 |

---

## 9. 落地分期

### 阶段 1 — 参考价展示（**已完成**）

- 后端：`price-preview`（单场 + 批量）。
- H5：场次列表、选座头展示 `referencePrice` + 优惠标签 + `memberFromPrice` 标记。
- **不做选票种**。

### 阶段 2 — 完整选票种与数据增强

- 新增选票种页；`ticketType/list` + `user_select_seat` 客户端算价。
- 对接 `movieOrder/create`（若 H5 开放支付）。
- 可选：`special_pricing` 入库后用于修正 `price-preview`（如抑制服务日标签），**不在 H5 做弹窗**。

### 阶段 3 — 体验与其它

- 区域价在选座页实时合计；`override_price` 全链路（§8 P0）。

---

## 10. 相关文档与代码

| 文档 / 代码 | 说明 |
|-------------|------|
| [PRICE_STRATEGY.md](./PRICE_STRATEGY.md) | 叠加公式、表、下单接口 |
| [TICKET_TYPE_REQUIREMENTS.md](./TICKET_TYPE_REQUIREMENTS.md) | 票种排期、启用禁用 |
| [PRICING_RULES.md](./PRICING_RULES.md) | 简版叠加规则 |
| `TicketPriceService` | 算价服务 |
| `MovieTicketTypeService.listByShowtime` | 本场可用票种 |
| `cinema-crawler/scripts/prices/import-prices-pg.ts` | 官网价入库 |
| App `SelectMovieTicket.dart` | 选票种 UI 参考实现 |
| H5 `CinemaDetailPanels.vue` | 影院详情票价展示 |
| H5 `ShowtimePriceLine.vue` | 场次卡预估价 + 会员标记 |
| H5 `showtimes.vue` / `seats.vue` | 已接 `price-preview` |
| `ShowTimePricePreviewService` | 预览算价实现 |
| `cinema-crawler` KINEZO `special_pricing` | 官网「特別料金」原文抓取（§11） |

---

## 11. 业界参考：新宿バルト9（KINEZO）— 特別料金**内容**（非 UI 方案）

> **用途说明**：本节截图来自官网排片页，用于理解日本影院「特別料金」**文案长什么样、有哪些规则类型**，便于数据建模与 `price-preview` 边界判断。  
> **产品结论**：本平台 **不做「特別料金」弹窗或按钮**；场次页继续用 `ShowtimePriceLine`（预估价 + 会员标记）即可。

### 11.1 官网信息结构（仅供对照）

```
┌─ 日期条 ─────────────────────────────────────────────┐
│  6/17(水) 水曜サービスデー  │  6/18(木) KINEZO会員 …  │  ← 影院级「今日语境」
├─ 作品块（同一电影可有多版本）──────────────────────────┤
│  海报 + 片名 + 时长                                      │
│  [作品詳細]  [特別料金]   ← 仅该片有特殊定价时出现        │
│  一行参考价（或规格块内标价）                             │
│  ┌ 场次按钮 × N ─────────────────────────────────┐   │
│  │ 19:25~21:05  シアター3  [購入] / [満席]          │   │
│  └────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────┘
```

与本文 §2「价格分层」的对应关系：

| 官网呈现 | 含义 | 本系统层级 |
|----------|------|------------|
| 日期条「水曜サービスデー」 | 当天影院适用服务日 | L2 `schedule_type` + 星期过滤 |
| 日期条「KINEZO会員」 | 会员日提示（不代替下单验资） | L2 会员票种 + 日期 |
| 作品块「一般 ¥1,500」「その他通常料金」 | 公众参考 + 其余走常设价表 | `referencePrice` + 影院详情价表 |
| 【4DX】一般 ¥2,700（4DX料金含む） | 规格/格式已并入标价 | L3 加价已含在展示文案或 `referencePrice` |
| **特別料金** 文案（官网用弹窗承载） | **作品/活动级**例外规则 | L1 固定价 / 覆盖规则 / `special_pricing`；**我们不在 H5 复刻弹窗** |
| 场次按钮 | 仅时间、厅、购态 | 不含票种选择 |

### 11.2 特別料金：典型**内容**模式（截图归纳）

官网在部分作品上有「特別料金」说明（其 UI 为按钮 + 弹窗，**我们仅摘录规则类型**）：

| 类型 | 官网示例摘要 | 与常设价表关系 |
|------|--------------|----------------|
| **一般 + 其他通常** | 一般 ¥2,000／その他：通常料金；割引・サービスデー**可**用 | 仅一般被锁定，学生等仍走价表 |
| **一律 + 其他通常** | 一律・大学生 ¥1,500／その他：通常料金 | 特定人群一口价，其余走价表 |
| **多档特別（排除优惠）** | 一般 ¥2,200／高校生・小人・障がい者 ¥1,200；各種割引・サービスデー**不可** | **覆盖**当日服务日；与 L2 冲突须显式声明 |
| **一律 + ペア割** | 一律 ¥2,200；2名でひとり ¥1,500；サービスデー不可 | 组合票规则；**单座列表无法自动算最低价** |
| **规格块内标价** | 【4DX】一般 ¥2,700（4DX料金含む）／その他通常料金 | 同片分版本块，每块一行参考价 |

共性附注（爬虫 `special_pricing_note` 常见）：

- 「※イベント付き上映など一部上映では料金が異なる場合がございます」
- 「招待券・シネマチケット利用可/不可」

**对本文档的意义**：帮助识别何时 `referencePrice` 不够用、何时须用 `pricing_mode=2` / 场次覆盖价、何时应**抑制**服务日低价标签——**不是**要做同款弹窗 UI。

### 11.3 与本期 H5 方案的对照

| 官网侧信息 | 本平台 H5 |
|------------|-----------|
| 场次上一行「一般 ¥X」 | ✅ `referencePrice` + 「起」 |
| 服务日 / 会员更低价 | ✅ `promoFromPrice` / `memberFromPrice` 小标签 |
| 特別料金全文（弹窗） | ❌ **不展示**；内容仅作后端/爬虫参考 |
| ペア割 / 组合价 | ❌ 不自动算进预览价 |
| 割引・サービスデー「不可」 | ⚠️ 待 `price-preview` 规则增强（`promoSuppressed`） |
| 同片 4DX / 通常分块 | ✅ 按场次 `spec` 各自 `referencePrice` |

### 11.4 爬虫与数据现状（KINEZO / T-Joy）

`cinema-crawler` 已在新宿バルト9 等 KINEZO 影院抓取 **特別料金**：

- 来源：排片页 `a.modal4-click` 的 `data-spec` HTML（无需点击弹窗）。
- 字段：`films[].special_pricing: { html, text } | null`（见 `kinezo/scheduleDom.ts`、`AGENTS.md`）。
- 普通作品为 `null`，等价「その他通常料金」。
- 结构化拆分（`FLAT` / `PAIR` / `GROUP`）见 `DATA_MODEL.md` §9.10 — **尚未全量入库**。

**若后续入库**（可选，服务算价而非展示）：

| 官网概念 | 建议存储 | 对 `price-preview` 的影响 |
|----------|----------|---------------------------|
| 有特別料金的作品 | `movie.source_extras.tjoy.special_pricing` | 可设 `hasSpecialPricing` 内部标记 |
| 原文 | `text` / `html` | 运营/规则解析用，**不对用户展示** |
| 一律价 | 解析或场次 `fixed_amount` | `referencePrice` 可取特別一般价 |
| サービスデー不可 | `special_pricing_note` | 抑制 `promoFromPrice` 展示 |

### 11.5 边界：特別料金 vs 影院常设价表

| 问题 | 处理方式 |
|------|----------|
| 周三排片页写着「水曜サービスデー」，但某片特別料金写「サービスデー不可」 | **作品级规则优先**；预览需抑制服务日低价标签 |
| 特別料金只写「一般 ¥2,000」，学生价写「その他通常料金」 | 学生价仍查 `movie_ticket_type`；无需在 H5 复述全文 |
| ペア割 | `promoFromPrice` / `memberFromPrice` **不**取ペア单价 |
| 4DX 块写「料金含む」 | `referencePrice` 已含 L3 时，标签注明「含 4DX」避免重复加价 |

---

*文档版本：2026-06-17；§11 特別料金为内容参考（不做弹窗）。阶段 1 已落地。*
