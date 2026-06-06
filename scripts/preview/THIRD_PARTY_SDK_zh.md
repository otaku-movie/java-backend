# シネコ 第三方 SDK 一览

**版本**：1.1.0

**生效日期**：2026/06/06

**最后更新**：2026/06/06

为支持账号注册、登录、基于位置的影院搜索、头像/评论图片上传、使用情况统计与崩溃报告收集等功能，本应用使用以下第三方 SDK 及系统能力。

> 本一览与 `pubspec.yaml` 实际依赖保持一致，随版本发布同步更新。

---

## 一、登录与认证

| SDK / 服务 | 提供方 | 用途 | 收集/使用的信息 | 触发时机 | 隐私政策 |
|---|---|---|---|---|---|
| `google_sign_in` | Google LLC | Google 账号登录/注册 | Google `sub`、邮箱、昵称、头像 URL | 点击「Google 登录」 | https://policies.google.com/privacy |
| `sign_in_with_apple` | Apple Inc. | Apple ID 登录（仅 iOS） | Apple `sub`、邮箱、昵称 | iOS 点击「Apple 登录」 | https://www.apple.com/legal/privacy/ |
| `flutter_appauth` | Maks O. (OSS) | X OAuth 2.0 PKCE 登录 | 授权码交换所需的临时参数 | 点击「X 登录」 | https://x.com/privacy |

---

## 二、统计分析 / 崩溃监控

| SDK / 服务 | 提供方 | 用途 | 收集/使用的信息 | 触发时机 | 隐私政策 |
|---|---|---|---|---|---|
| `firebase_analytics` | Google LLC | 页面浏览与自定义事件统计、购买漏斗优化 | 事件名与参数、Firebase 实例 ID、设备 OS/型号/应用版本；登录后关联用户 ID | 应用启动、页面跳转、主要操作（自动） | https://policies.google.com/privacy |
| `firebase_crashlytics` | Google LLC | 崩溃检测、分析与质量改进 | 崩溃堆栈、设备 OS/型号/应用版本、Firebase 实例 ID | 应用崩溃时（自动） | https://policies.google.com/privacy |
| `firebase_core` | Google LLC | Firebase SDK 初始化基础 | 与上述 Firebase 服务联动的初始化信息 | 应用启动时 | https://policies.google.com/privacy |

> Firebase 数据由 Google LLC（美国）服务器处理。详见《隐私政策》第 4.3、5 节。

---

## 三、设备能力与位置

| SDK / 服务 | 提供方 | 用途 | 收集/使用的信息 | 触发时机 | 隐私政策 |
|---|---|---|---|---|---|
| `geolocator` | Baseflow | 获取设备大致位置 | GPS/网络定位坐标（经授权） | 附近影院推荐 | https://baseflow.com/privacy-statement/ |
| `geocoding` | Baseflow（系统） | 逆地理编码 | 经纬度 | 显示城市名 | 同上 |
| `image_picker` | Flutter 官方 | 选择/拍摄图片 | 您选择的图片 | 换头像、评论图片 | 系统权限 |
| `image_editor` | Flutter 社区 | 裁剪/旋转 | 所选图片（仅本地） | 同上 | 仅本地处理 |

---

## 四、仅本地存储（不出设备）

| SDK / 服务 | 提供方 | 用途 | 存储内容 |
|---|---|---|---|
| `flutter_secure_storage` | Flutter 社区 | 加密保存凭证 | `accessToken`/`refreshToken`/`deviceId` |
| `shared_preferences` | Flutter 官方 | 保存偏好 | 语言、首页 Tab、引导已读等 |

---

## 五、网络与内容展示

| SDK / 服务 | 提供方 | 用途 | 涉及信息 |
|---|---|---|---|
| `dio` | Flutter 社区 | 与后端（`api.cineko.app`）HTTPS 通信 | 请求体、Bearer Token、`deviceId` |
| `extended_image` | Flutter 社区 | 图片加载与磁盘缓存 | 仅本地 |
| `flutter_markdown` | Flutter 官方 | 渲染协议 Markdown | 仅本地 |
| `url_launcher` | Flutter 官方 | 打开外部链接 | 点击时 |
| `share_plus` | Flutter 官方 | 系统分享 | 您选择分享时 |

---

## 六、辅助工具（不收集个人信息）

| SDK / 服务 | 用途 |
|---|---|
| `package_info_plus` | 读取应用版本 |
| `uuid` | 生成本地 `deviceId` |
| `crypto` | 本地哈希 |
| `logger` / `pretty_dio_logger` | 仅调试日志 |
| UI 插件群 | 界面渲染等，不联网、不采集个人信息 |

---

## 七、当前未接入的第三方服务

以下**尚未接入**，接入时将更新本一览及相关隐私政策，并按需重新征得同意：

- 消息推送（Firebase Cloud Messaging / APNs）
- 在线支付（Stripe / PayPay / Apple Pay / Google Pay 等）
- 地图组件（Google Maps / Apple MapKit JS）

---

## 八、维护说明

1. 本一览基于 `pubspec.yaml` 实际依赖，随应用版本同步更新。
2. 新增涉及个人信息采集的 SDK 时，将更新本一览与隐私政策，重大变更将重新征得同意。
3. 疑问请联系 privacy@cineko.app。

---

## 更新日志

### 1.1.0（2026/06/06）

- 接入 Firebase Analytics（Google LLC）用于使用情况统计
- 接入 Firebase Crashlytics（Google LLC）用于崩溃报告收集
- 同步更新隐私政策与第三方 SDK 一览

### 1.0.0

- 初始版本发布
