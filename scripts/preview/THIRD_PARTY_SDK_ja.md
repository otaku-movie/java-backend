# シネコ サードパーティ SDK 一覧

**バージョン**：1.1.0

**施行日**：2026/06/06

**最終更新日**：2026/06/06

シネコ のアカウント登録、ログイン、現在地に基づく劇場検索、アイコン／レビュー画像のアップロード、利用状況の統計分析、クラッシュレポートの収集等の機能を提供するため、本アプリは以下のサードパーティ SDK およびシステム機能を利用しています。

> 本一覧はアプリ実体の依存関係（`pubspec.yaml`）と一致するよう維持し、リリース時に追加・削除があれば同期して更新します。

---

## 一、ログイン・認証

| SDK / サービス | 提供元 | 利用目的 | 取得・利用する情報 | トリガー | プライバシーポリシー |
|---|---|---|---|---|---|
| `google_sign_in` | Google LLC | Google アカウントでのログイン／登録 | Google `sub`、メール、ニックネーム、アイコン URL | 「Google でログイン」押下時 | https://policies.google.com/privacy |
| `sign_in_with_apple` | Apple Inc. | Apple ID ログイン（iOS のみ） | Apple `sub`、メール、ニックネーム | iOS「Apple でログイン」押下時 | https://www.apple.com/legal/privacy/ |
| `flutter_appauth` | Maks O. (OSS) | X OAuth 2.0 PKCE ログイン | 認可コード交換に必要な一時パラメータ | 「X でログイン」押下時 | https://x.com/privacy |

---

## 二、統計分析・クラッシュ監視

| SDK / サービス | 提供元 | 利用目的 | 取得・利用する情報 | トリガー | プライバシーポリシー |
|---|---|---|---|---|---|
| `firebase_analytics` | Google LLC | 画面閲覧・カスタムイベントの統計分析、購入フロー最適化 | イベント名とパラメータ、Firebase インスタンス ID、端末 OS / モデル / アプリバージョン。ログイン後はユーザー ID を紐付け | アプリ起動・画面遷移・主要操作時（自動） | https://policies.google.com/privacy |
| `firebase_crashlytics` | Google LLC | クラッシュの検知・解析、品質改善 | クラッシュスタックトレース、端末 OS / モデル / アプリバージョン、Firebase インスタンス ID | アプリクラッシュ発生時（自動） | https://policies.google.com/privacy |
| `firebase_core` | Google LLC | Firebase SDK の初期化基盤 | 上記 Firebase サービスと連携するための初期化情報 | アプリ起動時 | https://policies.google.com/privacy |

> Firebase データは Google LLC（米国）のサーバで処理されます。詳細は《プライバシーポリシー》第 4.3 条・第 5 条をご参照ください。

---

## 三、デバイス機能・位置情報

| SDK / サービス | 提供元 | 利用目的 | 取得・利用する情報 | トリガー | プライバシーポリシー |
|---|---|---|---|---|---|
| `geolocator` | Baseflow | 端末の概算位置の取得 | GPS / ネットワーク測位による緯度経度（許可時のみ） | 近くの劇場のおすすめ | https://baseflow.com/privacy-statement/ |
| `geocoding` | Baseflow（OS 標準） | 逆ジオコーディング | 緯度経度 | 市区町村名の表示 | 同上 |
| `image_picker` | Flutter 公式 | 画像の選択・撮影 | お客様が選択した画像のみ | アイコン変更、レビュー画像 | システム権限に従う |
| `image_editor` | Flutter コミュニティ | 画像のクロップ／回転 | 選択された画像（端末ローカルのみ） | 同上 | 端末ローカル処理のみ |

---

## 四、端末ローカルの保存（端末外へは送信されません）

| SDK / サービス | 提供元 | 利用目的 | 保存される情報 |
|---|---|---|---|
| `flutter_secure_storage` | Flutter コミュニティ | 認証情報の暗号化保存 | `accessToken` / `refreshToken` / `deviceId` |
| `shared_preferences` | Flutter 公式 | 設定の保存 | 言語、タブ、ガイダンス既読フラグ等 |

---

## 五、ネットワーク通信・コンテンツ表示

| SDK / サービス | 提供元 | 利用目的 | 関連する情報 |
|---|---|---|---|
| `dio` | Flutter コミュニティ | バックエンド（`api.cineko.app`）との HTTPS 通信 | リクエスト、Bearer Token、`deviceId` |
| `extended_image` | Flutter コミュニティ | 画像読み込みとディスクキャッシュ | 端末ローカルのみ |
| `flutter_markdown` | Flutter 公式 | 規約本文等の Markdown レンダリング | 端末ローカルのみ |
| `url_launcher` | Flutter 公式 | 外部リンクをブラウザ／メール App で開く | タップ時のみ |
| `share_plus` | Flutter 公式 | システム共有シート | シェア選択時のみ |

---

## 六、補助ツール（個人情報を取得しません）

| SDK / サービス | 用途 |
|---|---|
| `package_info_plus` | アプリバージョンの読み取り |
| `uuid` | 端末ローカル `deviceId` 生成 |
| `crypto` | ローカルハッシュ計算 |
| `logger` / `pretty_dio_logger` | 開発・デバッグ環境のログ（リリース版では出力なし） |
| UI 系プラグイン群 | 画面描画・アニメーション等。ネットワーク通信や個人情報取得は行いません。 |

---

## 七、現時点で未導入のサードパーティサービス

以下は**現時点で未導入**です。導入時は本一覧と関連プライバシーポリシーを更新し、必要に応じて改めて同意を取得します。

- メッセージプッシュ（Firebase Cloud Messaging / APNs）
- オンライン決済（Stripe / PayPay / Apple Pay / Google Pay 等）
- 地図コンポーネント（Google Maps / Apple MapKit JS）

---

## 八、メンテナンス方針

1. 本一覧は `pubspec.yaml` の実依存関係に基づき、アプリのバージョン更新に同期します。
2. 個人情報の取得を伴う SDK を新たに導入する際は、本一覧および関連プライバシーポリシーを更新し、重要な変更については改めて同意を取得します。
3. ご質問は privacy@cineko.app までお問い合わせください。

---

## 更新履歴

### 1.1.0（2026/06/06）

- Firebase Analytics（Google LLC）による利用状況の統計分析を導入
- Firebase Crashlytics（Google LLC）によるクラッシュレポートの収集を導入
- 関連するプライバシーポリシーおよびサードパーティ SDK 一覧を更新

### 1.0.0

- 初版を公開
