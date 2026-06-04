-- ============================================================
-- V23: seed agreement (USER_TERMS / PRIVACY_POLICY / THIRD_PARTY_SDK)
--      into public.agreement and crawl.agreement.
--
-- 背景：
--   * V1 创建了 agreement 表，但 seed 数据是历史上手工往 public 插入的（仅 zh）。
--   * V3 通过 LIKE INCLUDING ALL 给 crawl 建了同名表，但没拷数据。
--   * dev 后端用 currentSchema=crawl 连库，所以 /agreement/detail
--     永远返回 null，App 端被迫走离线 fallback，看到的内容很简短。
--   * 同时，ja / en 两种语言一直没有 seed，外语用户也只能看离线 fallback。
--
-- 本迁移：
--   1) 把 public.agreement 中已有的 zh 三份协议同步到 crawl.agreement；
--   2) 写入 USER_TERMS / PRIVACY_POLICY / THIRD_PARTY_SDK 的 ja、en 版本，
--      同时落到 public.agreement 与 crawl.agreement，保持双 schema 一致；
--   3) 全部以 (code, language, version) 为去重键幂等；已存在的版本不会被覆盖。
--
-- 占位符约定：
--   * {{APP_NAME}} / {{EFFECTIVE_DATE}} / {{LAST_UPDATED}} 由 App 端在渲染时
--     替换；其余 {{COMPANY_NAME}} / {{DPO_EMAIL}} 等待运营信息确定后，
--     由后续 V?? 迁移或后台编辑功能 update 替换。
-- ============================================================

SET timezone = 'Asia/Tokyo';

-- ---- 1. zh: public -> crawl 同步 -----------------------------------
INSERT INTO crawl.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT a.code, a.language, a.title, a.content, a.version, a.status,
       a.is_required_accept, a.effective_at, a.published_at,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM public.agreement a
WHERE a.deleted = 0
  AND a.language = 'zh'
  AND a.code IN ('USER_TERMS', 'PRIVACY_POLICY', 'THIRD_PARTY_SDK')
  AND NOT EXISTS (
    SELECT 1 FROM crawl.agreement c
     WHERE c.code = a.code
       AND c.language = a.language
       AND c.version = a.version
       AND c.deleted = 0
  );

-- ---- 2. ja: USER_TERMS ---------------------------------------------
WITH new_row AS (
  SELECT
    'USER_TERMS'::varchar AS code,
    'ja'::varchar         AS language,
    '利用規約'::varchar    AS title,
    '1.0.0'::varchar      AS version,
    'PUBLISHED'::varchar  AS status,
    TRUE                  AS is_required_accept,
    $body$
# {{APP_NAME}} 利用規約

**バージョン**：1.0.0
**施行日**：{{EFFECTIVE_DATE}}
**最終更新日**：{{LAST_UPDATED}}

{{APP_NAME}}（以下「本サービス」といいます）をご利用いただきありがとうございます。本利用規約（以下「本規約」といいます）は、{{COMPANY_NAME}}（以下「当社」といいます）と本サービスをご利用になる方（以下「利用者」または「お客様」といいます）との間の権利義務関係を定めるものです。

ご利用前に、本規約の全条項（特に**免責、責任制限、返金・変更、紛争解決および準拠法**に関する条項）を必ずご確認ください。本サービスを登録・ログインまたは利用された時点で、本規約に同意したものとみなします。

---

## 1. 規約の適用と効力

1.1 本規約は、{{APP_NAME}} アプリ、公式ウェブサイトおよびこれらに付随するサービス（以下総称して「本サービス」）のすべての機能に適用されます。
1.2 アカウント登録、Google / Apple アカウントによるログイン、または注文の完了により、本規約に同意したものとみなします。
1.3 当社は法令または運営上の必要に応じて本規約を改定することがあります。改定後はアプリ内告知、お知らせまたはプッシュ通知により周知し、引き続きご利用される場合は変更後の規約に同意したものとみなします。同意いただけない場合は速やかにご利用を中止してください。

## 2. アカウント

2.1 メールアドレスとパスワード、または Google / Apple アカウントを利用してご登録・ログインいただけます。
2.2 お客様は正確かつ完全な情報を提供し、ご自身でアカウントとパスワードを適切に管理する責任を負います。アカウント上で行われた行為は原則としてご本人の行為とみなされ、その結果はお客様自身が負担するものとします。
2.3 アカウントの貸与、譲渡、売却その他第三者への提供を禁じます。
2.4 アカウントの不正利用や不審なログインに気付かれた場合は、直ちに当社へご連絡ください。
2.5 アカウントの退会は「マイページ - アカウント設定」から申請できます。退会後の個人情報の取扱いは《プライバシーポリシー》に従います。

## 3. サービス内容

3.1 本サービスは、映画情報の閲覧、劇場・上映回の検索、座席選択、オンラインチケット販売、電子発券、特典付与、注文管理、レビュー・評価、お知らせ通知などの機能を提供します。
3.2 当社は、チケット情報の提供および取引のマッチング役を担うにとどまり、**実際の上映サービスは劇場が提供します**。上映品質、座席の快適性、上映スケジュール等については劇場が直接の責任を負います。
3.3 法令、提携先の変更、安全または運営上の必要に応じて、機能の追加・調整・一時停止・終了を行うことがあります。重要な変更は事前に合理的な方法でお知らせします。

## 4. 注文・支払い・発券

4.1 商品価格は注文時に表示される金額（チケット代、サービス料、法令上の税金等を含む）が基準となります。
4.2 決済は第三者決済機関（クレジットカード、Apple Pay、PayPay 等）により処理されます。決済過程で発生した紛争は、お客様と当該決済機関との間で解決していただきます。
4.3 決済完了後に電子チケットまたは発券コードが発行されます。上映前に劇場へお越しいただき、劇場のルールに従って入場手続きを行ってください。
4.4 入場時刻の超過、誤った上映回への来場、発券情報の紛失等の事由による不利益については、当社および劇場は**返金・振替を行いません**。

## 5. 返金・変更ポリシー

5.1 **重要：映画チケットは「特定の上映回に対する商品」であり、原則として任意のキャンセル・変更はできません**。具体的な条件は以下のとおりです。

- **未発券かつ上映開始まで {{REFUND_HOURS}} 時間以上**：返金可。決済方法に応じて元の経路で返金します。
- **発券済みまたは上映開始まで {{REFUND_HOURS}} 時間未満**：返金不可。
- **劇場側の都合による上映回の中止・変更**：全額返金（場合により補償を行うことがあります）。
- **当社の責による事象（システム障害、二重販売等）**：全額返金、状況に応じて補償。

5.2 サービス料および特典付与に関する費用の返金可否は、注文画面に表示されたルールに従います。
5.3 返金は決済機関の処理に依存し、通常 1〜15 営業日程度を要します。

## 6. 特典・クーポン・キャンペーン

6.1 当社は、特典、クーポン、ポイント等の利用者特典を提供することがあります。発行条件、有効期限、利用ルールは各キャンペーンページの記載に従います。
6.2 虚偽取引、機械的な大量取得、脆弱性を利用した取得・使用等の不正行為は禁止します。違反が確認された場合、当社は当該特典の取消・回収・控除等を行うとともに、法的責任を追及する権利を留保します。

## 7. 利用者の禁止事項

7.1 利用者は、本サービスの利用に際して以下の行為を行わないものとします。

1. 法令または公序良俗に違反する行為。
2. チケットの転売（ダフ屋行為）、買い占め、市場秩序を乱す行為。
3. ロボット、クローラー、自動化スクリプト、改変クライアント等を用いて本サービスへアクセスし、または操作する行為。
4. 虚偽情報、広告、迷惑行為、第三者の権利を侵害する内容、不適切なコンテンツの投稿。
5. 当社のセキュリティ機構への攻撃、解析、リバースエンジニアリング、回避。
6. その他、法令または本規約に違反する行為。

7.2 違反があった場合、当社は警告、機能制限、アカウントの一時停止または抹消、損害賠償請求等の措置を講じることができます。

## 8. ユーザー投稿コンテンツ（UGC）

8.1 利用者が本サービス上に投稿するレビュー、評価、画像等のコンテンツ（以下「ユーザーコンテンツ」）の著作権は利用者に帰属します。利用者は、ユーザーコンテンツに関し、当社に対して**非独占的・無償・サブライセンス可能・地域無制限**の利用権（本サービスの提供・運営・宣伝のため）を許諾するものとします。
8.2 ユーザーコンテンツの内容については利用者が責任を負うものとし、第三者の権利を侵害しないよう留意してください。当社は違反コンテンツを削除・非表示・制限・公開停止する権利を留保します。

## 9. 知的財産権

9.1 本サービス上で表示される映画ポスター、スチル、動画、商標等の権利は、それぞれの製作会社・ライセンスホルダーに帰属します。本サービスの範囲を超えてダウンロード、転載、商用利用することはできません。
9.2 本サービスのロゴ、UI デザイン、ソースコード、データベース構造等の著作権は当社に帰属します。

## 10. 第三者サービス

10.1 本サービスは、Google ログイン、Apple ログイン、地図、プッシュ通知、統計分析、決済等の第三者サービスを利用することがあります。これらのサービスの提供および責任は、それぞれの第三者が負うものとします。
10.2 第三者サービスをご利用になる場合、当該第三者の利用規約およびプライバシーポリシーに従っていただく必要があります。具体的なリストは《サードパーティ SDK 一覧》をご参照ください。

## 11. 免責・責任の制限

11.1 以下の事由によりサービスの中断または利用者の損害が発生した場合、当社は責任を負いません。

1. 不可抗力（地震、火災、戦争、政府の措置等）。
2. 通信回線、電力、ネットワーク事業者など当社に起因しない要因による障害。
3. 利用者ご自身の過失、アカウント情報の漏えい。
4. 第三者サービスの異常。

11.2 法令で許される最大限の範囲において、利用者が本サービスの利用により被った損害に対する当社の賠償責任の総額は、**当該注文について実際にお支払いいただいた金額を上限**とし、間接損害、付随的損害、懲罰的損害については一切責任を負いません。

## 12. 規約の変更・終了

12.1 以下のいずれかに該当する場合、当社は利用者へのサービス提供を終了することがあります。

1. 利用者が本規約または法令に重大に違反した場合。
2. 長期間ご利用がなく、未完了の注文も存在しない場合。
3. 利用者が自ら退会を申請した場合。
4. 法令上の要請または司法・行政機関の決定があった場合。

12.2 規約終了後も、終了前に発生した注文は本規約に従い履行されます。終了前に投稿されたユーザーコンテンツに関する利用許諾は、規約終了後も効力を維持します。

## 13. 紛争解決と準拠法

13.1 本規約の成立、効力、履行、解釈および紛争解決には、**日本国の法令**が適用されます。
13.2 本規約に関連して生じた紛争については、当事者間で誠実に協議のうえ解決を図るものとし、解決に至らない場合は **{{JURISDICTION_COURT}}** を専属的合意管轄裁判所とします。
13.3 本規約は日本語、中国語、英語の各バージョンを提供します。**各言語版の意味に齟齬がある場合は、日本語版を正本**とします。

## 14. お問い合わせ

- 運営会社：{{COMPANY_NAME}}
- 登記住所：{{COMPANY_ADDRESS}}
- カスタマーサポート：{{SUPPORT_EMAIL}}
- お問い合わせ電話：{{SUPPORT_PHONE}}
- 受付時間：{{SUPPORT_HOURS}}
$body$::text AS content
)
INSERT INTO public.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM public.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);

-- ---- 3. ja: PRIVACY_POLICY -----------------------------------------
WITH new_row AS (
  SELECT
    'PRIVACY_POLICY'::varchar AS code,
    'ja'::varchar             AS language,
    'プライバシーポリシー'::varchar AS title,
    '1.0.0'::varchar          AS version,
    'PUBLISHED'::varchar      AS status,
    TRUE                      AS is_required_accept,
    $body$
# {{APP_NAME}} プライバシーポリシー

**バージョン**：1.0.0
**施行日**：{{EFFECTIVE_DATE}}
**最終更新日**：{{LAST_UPDATED}}

{{COMPANY_NAME}}（以下「当社」といいます）は、お客様の個人情報の保護を最も重要な責務の一つと位置付けています。本プライバシーポリシー（以下「本ポリシー」といいます）は、{{APP_NAME}}（以下「本サービス」といいます）のご利用に際し、**どのような情報を、なぜ、どのように収集・利用・共有・保管し、お客様がどのような権利を行使できるか**を説明するものです。

本サービスをご利用になる前に、本ポリシーの全条項を必ずご確認ください。本サービスをご利用された時点で、本ポリシーに従って個人情報を処理することに同意したものとみなします。

---

## 1. 当社について

- 個人情報取扱事業者：{{COMPANY_NAME}}
- 登記住所：{{COMPANY_ADDRESS}}
- プライバシー担当窓口：{{DPO_EMAIL}}

## 2. 取得する情報

サービス提供に必要な範囲に限定して、以下の情報を取得・処理します。

### 2.1 お客様自身が提供する情報

| 場面 | 情報項目 | 必要性 |
|---|---|---|
| メール会員登録 | メールアドレス、パスワード（暗号化保存）、ニックネーム | 必須 |
| Google / Apple ログイン | OpenID（`sub`）、メールアドレス、ニックネーム、アイコン URL | 必須 |
| プロフィール編集 | アイコン、ニックネーム、性別、生年月日 | 任意 |
| 注文・発券 | 連絡先情報、映画、上映回、座席、注文金額 | 必須 |
| レビュー・お問い合わせ | テキスト、画像、動画 | 任意 |

### 2.2 サービス利用時に当社が自動的に取得する情報

| 種別 | 情報項目 | 利用目的 |
|---|---|---|
| 端末情報 | 端末型番、OS およびバージョン、`deviceId`（アプリが端末ローカルで生成）、アプリバージョン | 不正対策、Refresh Token のバインド、互換性対応 |
| ネットワーク情報 | IP アドレス、接続種別 | 不正防止、API ルーティング |
| ログ情報 | API アクセス時刻、主要操作の記録 | 障害解析、サービス改善（**現バージョンでは第三者クラッシュ分析サービスは未導入**） |
| 位置情報（**お客様の許可を得た場合のみ**） | `geolocator` による緯度経度、OS 標準の逆ジオコーディング（`geocoding`）による市区町村名 | 近くの劇場のおすすめ、最寄り席の選択 |
| 写真・カメラ（**お客様が能動的に選択した場合のみ**） | アップロード用に選択された画像、または撮影された画像。`image_picker` で選択し、`image_editor` で端末ローカル上のみクロップ加工してから送信されます。 | アイコン設定、お問い合わせ画像、レビュー画像 |
| 端末ローカル保管 | `accessToken` / `refreshToken` / `deviceId`（`flutter_secure_storage` により Keychain / Keystore に暗号化保存）、言語・UI 設定（`shared_preferences`） | ログイン状態の維持、設定の保持。**端末内のみに保存され、第三者へは送信されません。** |

> 現バージョンの App は **Firebase Cloud Messaging / APNs 等のプッシュ SDK を未導入**であり、FCM / APNs Token を取得していません。今後導入する場合は、本ポリシーおよび《サードパーティ SDK 一覧》を更新したうえで、改めてお客様の同意を取得します。

### 2.3 取得しない情報

- クレジットカード番号・CVV・有効期限等の決済機微情報は**当社のサーバを経由せず**、将来オンライン決済を導入する際は資格を有する第三者決済機関が直接処理します。
- 当社は、お客様の写真ライブラリ全体、連絡帳、SMS、クリップボードを**無断で読み取りません**。アップロードされる画像はお客様自身が選択した 1 枚に限定されます。

## 3. 利用目的

取得した個人情報は以下の目的の範囲内でのみ利用します。

1. アカウント登録、ログイン、本人確認の実施。
2. 注文の履行：注文受付、決済の取次、発券、入場時の認証、返金・変更、カスタマーサポート。
3. 安全性の確保：アカウントの不正利用防止、ダフ屋および機械的買い占めの抑止、リスク監査。
4. サービス改善：**匿名化・仮名化処理を行ったデータ**による統計および機能改善。
5. マーケティング通知（プッシュ通知をオフにされていない場合に限ります）。
6. 法令上の義務の履行。

## 4. 共有・委託・開示

当社は、お客様の個人情報を**正当な理由なく第三者へ販売することはありません**。以下の場合に限り共有または委託処理を行います。

### 4.1 劇場・興行会社との共有

注文の履行および入場認証のため、注文番号、上映回、座席、電子発券コード、必要に応じて氏名・電話番号下 4 桁を該当劇場へ共有します。

### 4.2 決済機関との共有（**現バージョンではオンライン決済は未開始**）

将来オンライン決済を開始した際は、決済処理・照合・返金のため、必要な情報（注文番号、金額、通貨、決済手段の識別子）を第三者決済機関に共有します。具体的な提携先は導入前に本ポリシーおよび《サードパーティ SDK 一覧》を更新してご案内します。

### 4.3 委託処理（受託先は当社の指示に従って処理します）

- クラウドサービス：{{CLOUD_PROVIDERS}}
- カスタマーサポート / メール / SMS：{{COMMS_PROVIDERS}}

これらの受託先とはデータ処理契約を締結し、合意した目的の範囲内でのみ処理させ、当社と同等の保護措置を講じることを義務付けています。

> 現バージョンでは、第三者プッシュ（FCM / APNs）、エラー監視（Sentry 等）、行動分析（Firebase Analytics 等）のいずれも**未導入**です。今後導入する際は本ポリシーを更新し、お客様の同意を取得します。

### 4.4 第三者ログイン

Google / Apple ログインをご利用になる場合、当該プラットフォームは「お客様が本サービスを利用している事実」を取得し、必要な範囲のアカウント識別子・メールアドレス・ニックネーム・アイコンを当社へ提供します。当該プロセスは第三者のプライバシーポリシーにも従います（《サードパーティ SDK 一覧》参照）。

### 4.5 法令に基づく開示

法令、司法機関または行政機関の適法な要請に基づき、必要な範囲で開示します。法令上禁止されない限り、お客様への通知に努めます。

## 5. 越境移転

お客様が日本国外で本サービスをご利用になる場合、または当社が国外サーバ（{{OVERSEAS_LOCATIONS}}）で個人情報を処理する場合、お客様の個人情報が当該地域へ移転されることがあります。日本《個人情報保護法》、《GDPR》等の適用法令に基づき、標準契約条項（SCC）の締結など必要な保護措置を講じます。

## 6. 保管とセキュリティ

6.1 お客様の個人情報は {{STORAGE_LOCATIONS}} のサーバに保管され、当該地域の法令に従ってセキュリティ要件を遵守します。

6.2 当社は以下のセキュリティ対策を講じています。

- 通信は TLS により暗号化。
- パスワードや機微な認証情報はソルト付きハッシュで保管。
- ロールベースのアクセス制御と最小権限の原則。
- 操作ログと監査の運用。
- 脆弱性監視とインシデント対応のフロー整備。

6.3 個人情報のセキュリティ事故が発生した場合、法令に定める方法と期限内にお客様および監督機関へ通知します。

## 7. 保管期間

| 情報種別 | 保管期間 |
|---|---|
| アカウント情報 | 退会日から {{ACCOUNT_RETENTION_DAYS}} 日以内に削除または匿名化（法令に別段の定めがある場合を除く）。 |
| 注文・取引記録 | 日本《電子帳簿保存法》および税法に従い**最低 7 年間**保管。 |
| ログイン・操作ログ | {{LOG_RETENTION_DAYS}} 日。 |
| サポート対応履歴 | {{SUPPORT_RETENTION_DAYS}} 日。 |
| 端末ローカル Token | Keychain / Keystore に保管。ログアウトまたはアプリのアンインストールにより消去されます。 |

## 8. お客様の権利

適用法令に基づき、お客様はご自身の個人情報について以下の権利を行使できます。

1. **開示・複写**：「マイページ - アカウント設定」から閲覧・エクスポート。
2. **訂正・追加**：「マイページ - プロフィール」から変更。
3. **削除・退会**：退会後の処理は本ポリシーに従います。
4. **同意の撤回**：プッシュ通知、位置情報、パーソナライズなど任意項目をオフに切替。
5. **苦情**：プライバシー担当窓口 {{DPO_EMAIL}} までメールにてご連絡ください。{{DPO_RESPONSE_DAYS}} 営業日以内に回答します。
6. **監督機関への申し立て**：日本「個人情報保護委員会」、EU 域内の各国監督機関、その他お客様の所在地域における該当機関。

## 9. 第三者 SDK / Cookie

本サービスが使用する第三者 SDK、取得する情報、利用目的および各社プライバシーポリシーへのリンクは、**《サードパーティ SDK 一覧》**（コード：`THIRD_PARTY_SDK`）をご参照ください。

## 10. 未成年者の保護

10.1 本サービスは **{{MIN_AGE}} 歳以上**の方を対象としています。{{MIN_AGE}} 歳未満の方は、保護者の指導のもと、保護者が本ポリシーに同意したうえでご利用ください。

10.2 保護者の同意なく未成年者の個人情報を取得していたことが判明した場合、速やかに削除します。

## 11. 本ポリシーの変更

11.1 本ポリシーは適宜更新します。重要な変更については、アプリ内ポップアップやメール等の顕著な方法でお知らせします。軽微な変更はアプリ内告知とバージョン更新により対応します。

11.2 機微情報の処理、共有先、越境移転に関する重要な変更については、お客様の**改めての明示的な同意**を経て該当機能を提供します。

## 12. お問い合わせ

- 運営会社：{{COMPANY_NAME}}
- 登記住所：{{COMPANY_ADDRESS}}
- プライバシー担当窓口：{{DPO_EMAIL}}
- カスタマーサポート：{{SUPPORT_EMAIL}}
- お問い合わせ電話：{{SUPPORT_PHONE}}
$body$::text AS content
)
INSERT INTO public.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM public.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);

-- ---- 4. ja: THIRD_PARTY_SDK ----------------------------------------
WITH new_row AS (
  SELECT
    'THIRD_PARTY_SDK'::varchar AS code,
    'ja'::varchar              AS language,
    'サードパーティ SDK 一覧'::varchar AS title,
    '1.0.0'::varchar           AS version,
    'PUBLISHED'::varchar       AS status,
    FALSE                      AS is_required_accept,
    $body$
# {{APP_NAME}} サードパーティ SDK 一覧

**バージョン**：1.0.0

**施行日**：{{EFFECTIVE_DATE}}

**最終更新日**：{{LAST_UPDATED}}

{{APP_NAME}} のアカウント登録、ログイン、現在地に基づく劇場検索、アイコン／レビュー画像のアップロード等の機能を提供するため、本アプリは以下のサードパーティ SDK およびシステム機能を利用しています。本一覧はあくまで透明性確保のための告知であり、改めて個別の同意を求めるものではありません。具体的な情報の取扱いは《プライバシーポリシー》もあわせてご参照ください。

> 本一覧はアプリ実体の依存関係（`pubspec.yaml`）と一致するよう維持し、リリース時に追加・削除があれば同期して更新します。

---

## 一、ログイン・認証

| SDK / サービス | 提供元 | 利用目的 | 取得・利用する情報 | トリガー | プライバシーポリシー |
|---|---|---|---|---|---|
| `google_sign_in` | Google LLC | Google アカウントでのログイン／登録 | Google アカウント `sub`、メールアドレス、ニックネーム、アイコン URL | 「Google でログイン」を押下時 | https://policies.google.com/privacy |
| `sign_in_with_apple` | Apple Inc. | Apple ID でのログイン／登録（iOS のみ） | Apple アカウント `sub`、メールアドレス（Apple のリレーアドレスの場合あり）、ニックネーム | iOS で「Apple でログイン」を押下時 | https://www.apple.com/legal/privacy/ |
| `flutter_appauth` | Maks O. (OSS) | X（旧 Twitter）アカウントでの OAuth 2.0 PKCE ログイン | システムブラウザを介して認可コードを交換するために必要な一時パラメータ。クライアント側に `client_secret` を保存しません。 | 「X でログイン」を押下時 | https://x.com/privacy |

---

## 二、デバイス機能・位置情報

| SDK / サービス | 提供元 | 利用目的 | 取得・利用する情報 | トリガー | プライバシーポリシー |
|---|---|---|---|---|---|
| `geolocator` | Baseflow | 端末の概算位置の取得 | GPS / ネットワーク測位による緯度経度（端末側で許可された場合のみ） | 近くの劇場のおすすめ、最寄り席の検索 | https://baseflow.com/privacy-statement/ |
| `geocoding` | Baseflow（OS 標準機能ベース） | 緯度経度から市区町村名への逆ジオコーディング | 緯度経度 | 同上、市区町村名の表示にのみ使用 | 同上 |
| `image_picker` | Flutter 公式プラグイン | アイコン／お問い合わせ画像の選択・撮影 | システムフォトライブラリでお客様が選択した画像、またはカメラで撮影した画像（自動で写真ライブラリ全体を読みません） | アイコン変更、お問い合わせ画像、レビュー画像の投稿 | Android / iOS のシステムプライバシー権限に従う |
| `image_editor` | Flutter コミュニティ | アイコンのクロップ／回転 | 選択された画像（端末ローカルでのみ加工） | `image_picker` と同様、加工後にサーバへ送信 | 端末ローカルで処理し、第三者へ送信しません |

> 上記の位置・写真・カメラ機能は Android / iOS のシステム権限ダイアログを通じて事前にお客様の許可を得ます。許可はいつでも端末のシステム設定からオフにできます。

---

## 三、端末ローカルの保存（端末外へは送信されません）

| SDK / サービス | 提供元 | 利用目的 | 保存される情報 |
|---|---|---|---|
| `flutter_secure_storage` | Flutter コミュニティ | iOS Keychain / Android Keystore による機微な認証情報の暗号化保存 | `accessToken` / `refreshToken` / `deviceId` |
| `shared_preferences` | Flutter 公式プラグイン | 機微でない設定の保存 | 言語、ホームタブ、ガイダンス既読フラグ等 |

これらの情報は**第三者へ送信されることはなく、端末ローカル**にのみ保存されます。アプリのアンインストールにより削除されます。

---

## 四、ネットワーク通信・コンテンツ表示

| SDK / サービス | 提供元 | 利用目的 | 関連する情報 |
|---|---|---|---|
| `dio` | Flutter コミュニティ | アプリと当社バックエンド（`{{API_DOMAIN}}`）の HTTPS 通信 | リクエストパラメータ、Bearer Token、`deviceId`（リクエストヘッダー） |
| `extended_image` | Flutter コミュニティ | ポスター・スチール・アイコンの読み込みとディスクキャッシュ | 端末ローカルキャッシュのみ。第三者へは送信されません |
| `flutter_markdown` | Flutter 公式プラグイン | サーバから配信される規約本文等の Markdown レンダリング | 端末ローカルでの表示にのみ使用 |
| `url_launcher` | Flutter 公式プラグイン | 外部リンク（第三者プライバシーポリシー、サポート用メールアドレス等）をブラウザ／メール App で開く | お客様が該当エントリをタップした場合のみシステムアプリを起動 |
| `share_plus` | Flutter 公式プラグイン | 映画／注文／特典の共有時に、システム共有シートを呼び出し | お客様がシェアを選んだ場合のみ、対応するテキスト・画像が一時ファイル経由でシステムへ渡されます |

---

## 五、補助ツール（個人情報を取得しません）

| SDK / サービス | 用途 |
|---|---|
| `package_info_plus` | アプリ自体のバージョン番号を読み取り、バージョン確認・表示に使用 |
| `uuid` | 端末ローカルの `deviceId` 生成（Refresh Token のバインドに使用） |
| `crypto` | ローカルでのハッシュ計算 |
| `logger` / `pretty_dio_logger` | 開発・デバッグ環境でのログ出力（リリース版では出力しません） |
| `flutter_screenutil` / `fluttertoast` / `easy_refresh` / `card_swiper` / `carousel_slider` / `pretty_qr_code` / `flutter_sticky_header` / `loading_animation_widget` / `jiffy` / `flutter_svg` / `cupertino_icons` | UI レンダリング、アニメーション、時刻フォーマット等のフロントエンド機能。ネットワーク通信や個人情報取得は行いません。 |

---

## 六、現時点で未導入のサードパーティサービス

誤解を避けるため、以下の機能は**現時点で未導入**です。今後導入する際は本一覧と関連プライバシーポリシーを更新し、合わせてお知らせします。

- メッセージプッシュ（Firebase Cloud Messaging / APNs）
- オンライン決済（Stripe / PayPay / Apple Pay / Google Pay 等）
- クラッシュ監視・エラーログ（Sentry / Firebase Crashlytics）
- 行動分析（Firebase Analytics / 友盟 / GA4 等）
- 地図コンポーネント（Google Maps / Apple MapKit JS）

---

## 七、メンテナンス方針

1. 本一覧は、アプリの当該リリースバージョンの `pubspec.yaml` 実依存関係に基づいて整理しており、アプリのバージョン更新に同期します。
2. 個人情報の取得を伴うサードパーティ SDK を新たに導入する際は、以下を行います。
   - 本一覧および関連プライバシーポリシーの更新。
   - 重要な変更についてはアプリ内ポップアップやメールで顕著にお知らせし、必要に応じて改めて同意を取得。
3. 本一覧の各項目についてご質問がある場合、{{DPO_EMAIL}} までお問い合わせください。
$body$::text AS content
)
INSERT INTO public.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM public.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);

-- ---- 5. en: USER_TERMS ---------------------------------------------
WITH new_row AS (
  SELECT
    'USER_TERMS'::varchar AS code,
    'en'::varchar         AS language,
    'User Terms'::varchar AS title,
    '1.0.0'::varchar      AS version,
    'PUBLISHED'::varchar  AS status,
    TRUE                  AS is_required_accept,
    $body$
# {{APP_NAME}} User Terms

**Version**: 1.0.0
**Effective Date**: {{EFFECTIVE_DATE}}
**Last Updated**: {{LAST_UPDATED}}

Welcome to {{APP_NAME}} (the "Service"). These User Terms ("Terms") form a binding agreement between you ("you" or "User") and {{COMPANY_NAME}} ("we", "us" or the "Company") governing your access to and use of the Service.

Please read these Terms carefully before using the Service, with particular attention to the sections on **disclaimers, limitation of liability, refunds and changes, dispute resolution, and governing law**. By registering, signing in, or otherwise using the Service you acknowledge that you have read, understood, and agreed to be bound by these Terms.

---

## 1. Scope and Effect

1.1 These Terms apply to all features of the Service, including the {{APP_NAME}} mobile applications, official websites, and related products.
1.2 Completing account registration, signing in via Google / Apple, or placing an order constitutes your acceptance of these Terms.
1.3 We may amend these Terms from time to time as required by law or operational needs. Updates will be communicated via in-app notice, in-app message, or push notification. Continuing to use the Service after the effective date of any update constitutes your acceptance; if you do not agree, please stop using the Service.

## 2. Account

2.1 You may sign up using an email and password, or via Google / Apple Sign-In.
2.2 You agree to provide truthful, accurate, and complete registration information, and to safeguard your credentials. Activities under your account are deemed to be your own actions, for which you are responsible.
2.3 You must not lend, transfer, sell, or otherwise share your account with any third party.
2.4 If you become aware of unauthorized access to your account, please notify us immediately.
2.5 You may request account deletion via "Profile - Account Settings". Personal information following deletion will be processed in accordance with our Privacy Policy.

## 3. The Service

3.1 The Service provides movie information, theatre and showtime browsing, online seat selection and ticket purchase, electronic ticket codes, premiums, order management, ratings and reviews, and notifications.
3.2 The platform serves only as the provider of ticketing information and order matching; **the actual screening service is delivered by the cinema, which is solely responsible for screening quality, seat condition, and showtime arrangements**.
3.3 We may add, modify, suspend, or discontinue features as required by law, partner changes, security, or operational needs, with reasonable advance notice where practical.

## 4. Orders, Payments, and Ticketing

4.1 Prices are determined at checkout and may include the ticket price, service fees, and statutory taxes.
4.2 Payment is processed by third-party payment institutions (e.g. credit cards, Apple Pay, PayPay). Disputes arising in the payment process shall be resolved between you and the relevant payment institution.
4.3 Upon successful payment, an electronic ticket or pickup code will be issued. You should arrive before showtime and complete entry verification according to the cinema's rules.
4.4 We and the cinema **will not refund or reschedule** tickets where you fail to arrive on time, attend the wrong showtime, or lose your ticket credentials.

## 5. Refund and Change Policy

5.1 **Important: movie tickets are tied to a specific showtime and cannot be cancelled or changed at will**. Specific conditions are as follows:

- **Unredeemed and {{REFUND_HOURS}}+ hours before showtime**: refundable; refunds will be issued via the original payment channel.
- **Already redeemed, or less than {{REFUND_HOURS}} hours before showtime**: non-refundable.
- **Cancelled or rescheduled by the cinema**: full refund (with possible compensation in some cases).
- **Caused by our fault (e.g. system error, overselling)**: full refund and possible compensation depending on circumstances.

5.2 Whether service fees and premium fees are refundable follows the rules displayed on the order page.
5.3 Refunds depend on the payment channel and typically arrive within 1 to 15 business days.

## 6. Premiums, Coupons, and Promotions

6.1 We may provide premiums, coupons, points, and similar benefits, subject to the issuance conditions, validity, and rules shown on the relevant campaign pages.
6.2 You shall not obtain or use such benefits via fraudulent transactions, automated bulk acquisition, exploitation of bugs, or similar abusive means. Where such conduct is identified, we may revoke, recall, or deduct the relevant benefits and reserve the right to pursue legal remedies.

## 7. User Conduct

7.1 You agree not to engage in any of the following:

1. Any conduct that violates applicable laws or public order and morals.
2. Ticket scalping, bulk hoarding, or other behaviour disrupting the market.
3. Accessing or operating the Service via robots, crawlers, automated scripts, or modified clients.
4. Posting false information, advertisements, harassing content, infringing content, or other inappropriate content.
5. Attacking, reverse engineering, decrypting, or otherwise circumventing platform security measures.
6. Other conduct violating applicable laws or these Terms.

7.2 In case of violation, we may impose measures including warnings, feature limitations, account suspension or termination, and pursuit of legal liability.

## 8. User-Generated Content

8.1 You retain ownership of content you submit (e.g. reviews, ratings, images) ("User Content"). You hereby grant us a **non-exclusive, royalty-free, sublicensable, worldwide** license to use such content for the operation, display, and promotion of the Service.
8.2 You are responsible for the User Content you submit and warrant that it does not infringe any third-party rights. We may remove, hide, throttle, or disable any User Content that violates these Terms.

## 9. Intellectual Property

9.1 Movie posters, stills, clips, trademarks, and similar materials displayed via the Service belong to their respective producers or licensees. You may use them only within the Service and may not download, redistribute, or use them commercially without authorisation.
9.2 The Service's logo, UI design, source code, and database structures are owned by {{COMPANY_NAME}}.

## 10. Third-Party Services

10.1 The Service integrates third-party services such as Google Sign-In, Apple Sign-In, maps, push notifications, analytics, and payments. Each third party is responsible for its own services.
10.2 Use of third-party services may be subject to such third party's terms and privacy policies. See the Third-party SDK List for details.

## 11. Disclaimer and Limitation of Liability

11.1 We are not liable for any service interruption or loss arising from:

1. Force majeure (earthquake, fire, war, governmental action, etc.).
2. Failure of telecom lines, electricity, network providers, or other causes outside our control.
3. Your own error or compromise of your account credentials.
4. Anomalies of third-party services.

11.2 To the maximum extent permitted by law, our aggregate liability for any losses arising out of or relating to your use of the Service shall not exceed **the amount you actually paid for the relevant order**, and we shall not be liable for indirect, incidental, or punitive damages.

## 12. Changes and Termination

12.1 We may terminate the Service to you in any of the following cases:

1. Material breach of these Terms or applicable laws by you.
2. Long-term inactivity with no outstanding orders.
3. Voluntary deletion request submitted by you.
4. Required by laws or by judicial / administrative authorities.

12.2 Upon termination, orders placed prior to termination shall continue to be governed by these Terms. The license you granted in respect of User Content shall survive termination.

## 13. Governing Law and Dispute Resolution

13.1 These Terms shall be governed by and construed in accordance with the **laws of Japan**.
13.2 Any dispute arising out of or relating to these Terms shall first be resolved through good-faith negotiation; failing which, **{{JURISDICTION_COURT}}** shall have exclusive jurisdiction.
13.3 These Terms are provided in Chinese, Japanese, and English. **In case of any discrepancy among versions, the Japanese version shall prevail**.

## 14. Contact Us

- Operator: {{COMPANY_NAME}}
- Registered Address: {{COMPANY_ADDRESS}}
- Customer Support: {{SUPPORT_EMAIL}}
- Support Phone: {{SUPPORT_PHONE}}
- Support Hours: {{SUPPORT_HOURS}}
$body$::text AS content
)
INSERT INTO public.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM public.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);

-- ---- 6. en: PRIVACY_POLICY -----------------------------------------
WITH new_row AS (
  SELECT
    'PRIVACY_POLICY'::varchar AS code,
    'en'::varchar             AS language,
    'Privacy Policy'::varchar AS title,
    '1.0.0'::varchar          AS version,
    'PUBLISHED'::varchar      AS status,
    TRUE                      AS is_required_accept,
    $body$
# {{APP_NAME}} Privacy Policy

**Version**: 1.0.0
**Effective Date**: {{EFFECTIVE_DATE}}
**Last Updated**: {{LAST_UPDATED}}

{{COMPANY_NAME}} ("we" or "us") regards the protection of your personal information as one of our core responsibilities. This Privacy Policy explains, when you use {{APP_NAME}} (the "Service"), **what information we collect, why we collect it, how we use, share, and retain it, and the rights you can exercise**.

Please read this Privacy Policy carefully before using the Service. By using the Service you agree that we may process your personal information in accordance with this Policy.

---

## 1. Who We Are

- Personal information controller: {{COMPANY_NAME}}
- Registered Address: {{COMPANY_ADDRESS}}
- Privacy contact: {{DPO_EMAIL}}

## 2. Information We Collect

We only collect and process information necessary to provide the Service.

### 2.1 Information You Provide

| Scenario | Items | Required |
|---|---|---|
| Email registration | Email address, password (stored encrypted), nickname | Yes |
| Google / Apple Sign-In | OpenID (`sub`), email, nickname, avatar URL | Yes |
| Profile editing | Avatar, nickname, gender, date of birth | Optional |
| Order / Ticketing | Contact details, movie, showtime, seat, order amount | Yes |
| Reviews / Support | Text, images, videos | Optional |

### 2.2 Information Collected Automatically When You Use the Service

| Type | Items | Purpose |
|---|---|---|
| Device | Device model, OS and version, `deviceId` (generated locally by the app), app version | Anti-fraud, refresh-token binding, compatibility |
| Network | IP address, connection type | Anti-fraud, API routing |
| Logs | API access timestamps, key operation logs | Troubleshooting and product improvement (**no third-party crash analytics SDK is integrated in the current version**) |
| Location (**only with your permission**) | GPS / network coordinates from `geolocator`; reverse geocoding to city / region by `geocoding` | Nearby cinema recommendations, nearest seat selection |
| Photos / Camera (**only when you actively choose**) | Images you select via `image_picker` and crop locally via `image_editor` before upload | Avatar, support attachments, review images |
| Local storage | `accessToken` / `refreshToken` / `deviceId` (encrypted in Keychain / Keystore via `flutter_secure_storage`); language and UI preferences via `shared_preferences` | Stays on the device only; not transmitted to third parties |

> The current build of the App **does not integrate** Firebase Cloud Messaging / APNs or similar push SDKs, and therefore does not collect FCM / APNs tokens. We will update this Policy and the Third-party SDK List, and request your renewed consent, when such SDKs are introduced.

### 2.3 Information We Do Not Collect

- Full bank card numbers, CVV, or expiry are **not transmitted to our servers**. When online payment is introduced in the future, qualified third-party payment providers will handle these directly.
- We **do not** scan your photo library, contacts, SMS, or clipboard. Image uploads are limited to the specific image you choose.

## 3. How We Use Your Information

We use information only for the following purposes:

1. Account registration, login, and identity verification.
2. Order fulfilment: order placement, payment relay, ticket issuance, entry verification, refunds / changes, and customer support.
3. Security: account anti-fraud, anti-scalping, risk audit.
4. Service improvement: statistics and feature optimisation using **anonymised or pseudonymised** data.
5. Marketing notifications (only if you have not turned off push notifications).
6. Performance of legal obligations.

## 4. Sharing, Processors, and Disclosure

We **do not sell** your personal information to unrelated third parties. We share or commission processing only in the situations below.

### 4.1 Sharing with Cinemas / Distributors

For order fulfilment and entry verification, we share order number, showtime, seat, electronic pickup code, and where necessary the last four digits of name / phone with the relevant cinema.

### 4.2 Sharing with Payment Providers (**not yet active in the current version**)

When online payment is introduced, we will share necessary information (order number, amount, currency, payment method identifier) with the third-party payment provider for processing, reconciliation, and refunds. Specific providers will be disclosed in this Policy and the Third-party SDK List before activation.

### 4.3 Processors (acting on our instructions)

- Cloud services: {{CLOUD_PROVIDERS}}
- Customer support / Email / SMS: {{COMMS_PROVIDERS}}

We sign data processing agreements with the above processors limiting their handling to the agreed purpose and requiring equivalent protection measures.

> The current version **does not integrate** third-party push (FCM / APNs), error monitoring (Sentry, etc.), or behavioural analytics (Firebase Analytics, etc.). When introduced, this Policy will be updated and your consent will be obtained.

### 4.4 Third-Party Login

When you use Google / Apple Sign-In, the relevant platform learns that you use the Service and returns the necessary account identifier, email, nickname, and avatar to us. Such process is also subject to the third party's privacy policy (see the Third-party SDK List).

### 4.5 Disclosure Required by Law

We may disclose information when required by laws, courts, or administrative authorities. Where not legally prohibited, we will use reasonable efforts to notify you.

## 5. Cross-Border Transfers

If you use the Service outside Japan, or if we use overseas servers ({{OVERSEAS_LOCATIONS}}) to process data, your personal information may be transferred to such regions. We will adopt the necessary protection measures (e.g. Standard Contractual Clauses) under Japan's Act on the Protection of Personal Information, the GDPR, and other applicable laws.

## 6. Storage and Security

6.1 Your personal information is stored on servers in {{STORAGE_LOCATIONS}}, in compliance with the security requirements of applicable local laws.

6.2 Security measures include:

- TLS encryption in transit.
- Salted hashing for passwords and sensitive credentials.
- Role-based access control and least-privilege.
- Operation logging and audit.
- Vulnerability monitoring and incident response procedures.

6.3 If a personal information security incident occurs, we will notify you and the supervisory authority within the time and in the manner required by law.

## 7. Retention Periods

| Category | Retention period |
|---|---|
| Account information | Deleted or anonymised within {{ACCOUNT_RETENTION_DAYS}} days after account closure (unless otherwise required by law). |
| Order and transaction records | Retained for **at least 7 years** under Japan's Electronic Books Preservation Act and tax regulations. |
| Login and operation logs | {{LOG_RETENTION_DAYS}} days. |
| Customer support records | {{SUPPORT_RETENTION_DAYS}} days. |
| Local Tokens | Stored in Keychain / Keystore; cleared on logout or app uninstallation. |

## 8. Your Rights

Subject to applicable law, you may exercise the following rights:

1. **Access and copy**: review or export under "Profile - Account Settings".
2. **Correction**: edit under "Profile - Profile Information".
3. **Deletion / closure**: post-closure handling follows this Policy.
4. **Withdrawal of consent**: turn off optional permissions such as push, location, and personalised recommendations.
5. **Complaints**: email our privacy contact at {{DPO_EMAIL}}; we will respond within {{DPO_RESPONSE_DAYS}} business days.
6. **Lodging a complaint with the regulator**: e.g. Japan's Personal Information Protection Commission, EU member-state DPAs, or relevant authorities in your jurisdiction.

## 9. Third-Party SDKs and Cookies

For the third-party SDKs we integrate, the information they collect, the purposes for which we use them, and links to their privacy policies, please refer to the **Third-party SDK List** (code: `THIRD_PARTY_SDK`).

## 10. Protection of Minors

10.1 The Service is intended for users aged **{{MIN_AGE}} or above**. Users under {{MIN_AGE}} should use the Service under the guidance and consent of a guardian.

10.2 If we discover that we have collected personal information from a minor without verifiable guardian consent, we will delete it as soon as practicable.

## 11. Changes to This Policy

11.1 We may update this Policy from time to time. Material changes will be notified prominently via in-app pop-ups, email, or similar channels. Minor changes will be announced in the app together with version updates.

11.2 Material changes that involve sensitive personal information processing, sharing, or cross-border transfers will only take effect for the relevant features after we obtain your **renewed explicit consent**.

## 12. Contact Us

- Operator: {{COMPANY_NAME}}
- Registered Address: {{COMPANY_ADDRESS}}
- Privacy contact: {{DPO_EMAIL}}
- Customer Support: {{SUPPORT_EMAIL}}
- Support Phone: {{SUPPORT_PHONE}}
$body$::text AS content
)
INSERT INTO public.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM public.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);

-- ---- 7. en: THIRD_PARTY_SDK ----------------------------------------
WITH new_row AS (
  SELECT
    'THIRD_PARTY_SDK'::varchar     AS code,
    'en'::varchar                  AS language,
    'Third-party SDK List'::varchar AS title,
    '1.0.0'::varchar               AS version,
    'PUBLISHED'::varchar           AS status,
    FALSE                          AS is_required_accept,
    $body$
# {{APP_NAME}} Third-party SDK List

**Version**: 1.0.0

**Effective Date**: {{EFFECTIVE_DATE}}

**Last Updated**: {{LAST_UPDATED}}

To support account registration, login, location-based cinema discovery, avatar / review uploads, and similar features, the App integrates the third-party SDKs and system services listed below. This list is provided for transparency and notice purposes only and does not require your separate consent. Detailed handling rules are described in the Privacy Policy.

> This list is kept consistent with the actual app dependencies declared in `pubspec.yaml` and is updated alongside any addition or removal in subsequent releases.

---

## 1. Login and Authentication

| SDK / Service | Provider | Purpose | Information collected / used | Trigger | Privacy Policy |
|---|---|---|---|---|---|
| `google_sign_in` | Google LLC | Google account sign-in / sign-up | Google `sub`, email, nickname, avatar URL | Tap "Sign in with Google" | https://policies.google.com/privacy |
| `sign_in_with_apple` | Apple Inc. | Apple ID sign-in / sign-up (iOS only) | Apple `sub`, email (may be Apple's relay address), nickname | Tap "Sign in with Apple" on iOS | https://www.apple.com/legal/privacy/ |
| `flutter_appauth` | Maks O. (OSS) | OAuth 2.0 PKCE login via X (formerly Twitter) | Temporary parameters required to exchange authorization code via the system browser. No `client_secret` is stored on the client. | Tap "Sign in with X" | https://x.com/privacy |

---

## 2. Device Capabilities and Location

| SDK / Service | Provider | Purpose | Information collected / used | Trigger | Privacy Policy |
|---|---|---|---|---|---|
| `geolocator` | Baseflow | Approximate device location | GPS / network coordinates (only after user authorisation) | Nearby cinema recommendations, nearest seat | https://baseflow.com/privacy-statement/ |
| `geocoding` | Baseflow (OS-level) | Reverse geocoding from coordinates to city / region | Coordinates only | Same as above; only used to display the city name | Same as above |
| `image_picker` | Flutter official plugin | Pick / capture avatar or support / review images | Image selected by you in the system photo library or captured by the camera (we do not auto-scan the library) | Avatar change, support uploads, review images | Subject to Android / iOS system privacy permissions |
| `image_editor` | Flutter community | Crop / rotate avatar | The image you selected (processed locally only) | Same as `image_picker`, then uploaded after local processing | Local processing only; not transmitted to third parties |

> Location, photo library, and camera capabilities are gated by Android / iOS system permission prompts. You may revoke them at any time in the system settings.

---

## 3. Local-Only Storage (does not leave your device)

| SDK / Service | Provider | Purpose | Stored information |
|---|---|---|---|
| `flutter_secure_storage` | Flutter community | Store sensitive credentials encrypted in iOS Keychain / Android Keystore | `accessToken` / `refreshToken` / `deviceId` |
| `shared_preferences` | Flutter official plugin | Store non-sensitive preferences | Language, home tab, onboarding flags, etc. |

The above information **never leaves your device** and is removed when you uninstall the app.

---

## 4. Networking and Content Display

| SDK / Service | Provider | Purpose | Information involved |
|---|---|---|---|
| `dio` | Flutter community | HTTPS communication between the app and our backend (`{{API_DOMAIN}}`) | Request payload, Bearer token, `deviceId` (request header) |
| `extended_image` | Flutter community | Loading and disk-caching posters, stills, avatars | Local cache only; not reported to any third party |
| `flutter_markdown` | Flutter official plugin | Rendering Markdown content (e.g. agreement bodies) sent by the server | Local rendering only |
| `url_launcher` | Flutter official plugin | Opening external links (e.g. third-party privacy policies, support email) in the browser or mail app | Triggered only when you tap the relevant entry |
| `share_plus` | Flutter official plugin | Invoke the system share sheet for sharing movies / orders / premiums | Only the text / image you choose to share is passed to the system via a temporary file |

---

## 5. Helper Tools (no personal data collected)

| SDK / Service | Purpose |
|---|---|
| `package_info_plus` | Read the app's own version number for version checks and display |
| `uuid` | Generate the device-local `deviceId` used for refresh-token binding |
| `crypto` | Local hash computations |
| `logger` / `pretty_dio_logger` | Log output in development / debug only; release builds emit nothing |
| `flutter_screenutil` / `fluttertoast` / `easy_refresh` / `card_swiper` / `carousel_slider` / `pretty_qr_code` / `flutter_sticky_header` / `loading_animation_widget` / `jiffy` / `flutter_svg` / `cupertino_icons` | UI rendering, animation, time formatting, and similar pure front-end capabilities. They do not access the network or collect personal data. |

---

## 6. Third-Party Services Not Currently Integrated

To avoid any misunderstanding, the following capabilities are **not currently integrated**. We will update this list and the relevant Privacy Policy when they are added in the future.

- Push messaging (Firebase Cloud Messaging / APNs)
- Online payment (Stripe / PayPay / Apple Pay / Google Pay, etc.)
- Crash monitoring and error logs (Sentry / Firebase Crashlytics)
- Behavioural analytics (Firebase Analytics / Umeng / GA4, etc.)
- Third-party map components (Google Maps / Apple MapKit JS)

---

## 7. Maintenance Notes

1. This list is curated based on the actual `pubspec.yaml` dependencies of the published app version and is kept in sync with app releases.
2. When adding any third-party SDK that involves personal information collection, we will:
   - Update this list and the relevant Privacy Policy section;
   - Provide prominent notice via in-app pop-ups / email for material changes and request your renewed consent where required.
3. If you have questions about any item on this list, please contact us at {{DPO_EMAIL}}.
$body$::text AS content
)
INSERT INTO public.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM public.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);

-- ---- 8. ja / en: public -> crawl 同步 ------------------------------
INSERT INTO crawl.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT a.code, a.language, a.title, a.content, a.version, a.status,
       a.is_required_accept, a.effective_at, a.published_at,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM public.agreement a
WHERE a.deleted = 0
  AND a.language IN ('ja', 'en')
  AND a.code IN ('USER_TERMS', 'PRIVACY_POLICY', 'THIRD_PARTY_SDK')
  AND NOT EXISTS (
    SELECT 1 FROM crawl.agreement c
     WHERE c.code = a.code
       AND c.language = a.language
       AND c.version = a.version
       AND c.deleted = 0
  );
