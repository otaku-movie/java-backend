-- ============================================================
-- V39: agreement 1.1.0 — Firebase Analytics / Crashlytics 接入披露
--
-- 变更摘要（相对 1.0.0）：
--   * PRIVACY_POLICY：补充 Firebase Analytics / Crashlytics 数据采集说明
--   * THIRD_PARTY_SDK：新增 Firebase SDK 条目，从未导入清单移除
--   * USER_TERMS：第 10 条第三方服务补充 Firebase 说明
--   * 新增 zh 三份协议全文（历史上仅在 DB 手工维护，现纳入迁移）
--
-- 版本策略：
--   * 新记录 version = 1.1.0, status = PUBLISHED
--   * 旧 1.0.0 标记为 ARCHIVED（不再被 /agreement/latest 选中）
--   * 同时写入 public.agreement 与 crawl.agreement
-- ============================================================

SET timezone = 'Asia/Tokyo';

-- ---- 1. 归档旧版 1.0.0 --------------------------------------------
UPDATE public.agreement
   SET status = 'ARCHIVED', update_time = CURRENT_TIMESTAMP
 WHERE deleted = 0 AND version = '1.0.0'
   AND code IN ('USER_TERMS', 'PRIVACY_POLICY', 'THIRD_PARTY_SDK');

UPDATE crawl.agreement
   SET status = 'ARCHIVED', update_time = CURRENT_TIMESTAMP
 WHERE deleted = 0 AND version = '1.0.0'
   AND code IN ('USER_TERMS', 'PRIVACY_POLICY', 'THIRD_PARTY_SDK');



-- ---- public: USER_TERMS / ja / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'USER_TERMS'::varchar AS code,
    'ja'::varchar AS language,
    '利用規約'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} 利用規約

**バージョン**：1.1.0

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

10.1 本サービスは、Google ログイン、Apple ログイン、地図、決済等の第三者サービスに加え、**Firebase Analytics（Google LLC）による利用状況の統計分析**および**Firebase Crashlytics（Google LLC）によるクラッシュレポートの収集**を行います。これらのサービスの提供および責任は、それぞれの第三者が負うものとします。
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

---

## 更新履歴

### 1.1.0（{{LAST_UPDATED}}）

- Firebase Analytics（Google LLC）による利用状況の統計分析を導入
- Firebase Crashlytics（Google LLC）によるクラッシュレポートの収集を導入
- 関連するプライバシーポリシーおよびサードパーティ SDK 一覧を更新

### 1.0.0

- 初版を公開
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


-- ---- public: USER_TERMS / en / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'USER_TERMS'::varchar AS code,
    'en'::varchar AS language,
    'User Terms'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} User Terms

**Version**: 1.1.0

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

10.1 The Service integrates third-party services such as Google Sign-In, Apple Sign-In, maps, and payments. In addition, we use **Firebase Analytics (Google LLC) for usage statistics** and **Firebase Crashlytics (Google LLC) for crash reporting**. Each third party is responsible for its own services.
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

---

## Changelog

### 1.1.0 ({{LAST_UPDATED}})

- Integrated Firebase Analytics (Google LLC) for usage statistics
- Integrated Firebase Crashlytics (Google LLC) for crash reporting
- Updated Privacy Policy and Third-party SDK List accordingly

### 1.0.0

- Initial release
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


-- ---- public: USER_TERMS / zh / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'USER_TERMS'::varchar AS code,
    'zh'::varchar AS language,
    '用户协议'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} 用户协议

**版本**：1.1.0

**生效日期**：{{EFFECTIVE_DATE}}

**最后更新**：{{LAST_UPDATED}}

欢迎使用 {{APP_NAME}}（以下简称「本服务」）。本用户协议（以下简称「本协议」）由 {{COMPANY_NAME}}（以下简称「我们」或「运营方」）与您（以下简称「用户」或「您」）之间订立，规范您对本服务的访问与使用。

使用前请仔细阅读本协议全部条款，尤其关注**免责、责任限制、退款与变更、争议解决及适用法律**相关条款。注册、登录或继续使用本服务，即视为您已阅读、理解并同意受本协议约束。

---

## 1. 适用范围与效力

1.1 本协议适用于 {{APP_NAME}} 移动应用、官方网站及其附属功能的全部内容。
1.2 完成账号注册、通过 Google / Apple 登录或完成下单，即视为您接受本协议。
1.3 我们可因法律或运营需要修订本协议，并通过应用内公告、消息或推送通知告知。修订后继续使用的，视为接受变更后的协议；若不同意，请立即停止使用。

## 2. 账号

2.1 您可使用邮箱与密码，或通过 Google / Apple 账号注册与登录。
2.2 您应提供真实、准确、完整的注册信息，并妥善保管账号与密码。账号下的行为原则上视为您本人行为，相关后果由您自行承担。
2.3 禁止出借、转让、出售或以其他方式向第三方提供账号。
2.4 如发现账号被盗用或存在异常登录，请立即联系我们。
2.5 您可在「我的 - 账号设置」申请注销账号。注销后的个人信息处理遵循《隐私政策》。

## 3. 服务内容

3.1 本服务提供电影信息浏览、影院与场次查询、在线选座购票、电子取票、特典发放、订单管理、评分评论及通知等功能。
3.2 我们仅提供票务信息与交易撮合，**实际上映服务由影院提供**，上映质量、座位舒适度、排片安排等由影院直接负责。
3.3 我们可因法律、合作方变更、安全或运营需要调整、暂停或终止部分功能，重大变更将提前合理告知。

## 4. 下单、支付与取票

4.1 商品价格以下单时展示金额为准（含票价、服务费及法定税费等）。
4.2 支付由第三方机构（信用卡、Apple Pay、PayPay 等）处理，支付纠纷由您与相应机构自行解决。
4.3 支付成功后发放电子票或取票码，请按影院规则在开场前到场办理入场。
4.4 因迟到、走错场次、丢失取票信息等导致的损失，我们及影院**不予退款或改期**。

## 5. 退款与变更政策

5.1 **重要：电影票对应特定场次，原则上不可随意取消或变更**。具体规则如下：

- **未取票且距开场 {{REFUND_HOURS}} 小时以上**：可退款，按原支付渠道退回。
- **已取票或距开场不足 {{REFUND_HOURS}} 小时**：不可退款。
- **因影院原因取消或改期**：全额退款（部分情况可能补偿）。
- **因我方原因（系统故障、超卖等）**：全额退款，视情况补偿。

5.2 服务费及特典相关费用是否可退，以订单页展示规则为准。
5.3 退款到账时间取决于支付渠道，通常 1～15 个工作日。

## 6. 特典、优惠券与活动

6.1 我们可能提供特典、优惠券、积分等福利，具体以活动页规则为准。
6.2 禁止通过虚假交易、批量脚本、漏洞利用等方式获取或使用福利；违规时我们可取消、回收相关权益并保留追责权利。

## 7. 禁止行为

7.1 您在使用本服务时不得：

1. 违反法律法规或公序良俗；
2. 倒卖票券、囤积票券或扰乱市场秩序；
3. 使用机器人、爬虫、自动化脚本或修改客户端访问或操作本服务；
4. 发布虚假信息、广告、骚扰内容、侵权或不适当内容；
5. 攻击、破解、逆向或规避平台安全机制；
6. 其他违反法律或本协议的行为。

7.2 违规时我们可采取警告、功能限制、暂停或注销账号、索赔等措施。

## 8. 用户生成内容（UGC）

8.1 您在本服务发布的评论、评分、图片等内容（「用户内容」）著作权归您所有；您授予我们**非独占、免费、可再许可、全球范围**的使用权，用于本服务的提供、运营与推广。
8.2 用户内容由您自行负责，不得侵犯第三方权利；我们可删除、隐藏或限制违规内容。

## 9. 知识产权

9.1 本服务展示的电影海报、剧照、视频、商标等权利归相应权利人所有，不得超出本服务范围下载、转载或商用。
9.2 本服务标识、界面设计、源代码、数据库结构等著作权归运营方所有。

## 10. 第三方服务

10.1 本服务使用 Google 登录、Apple 登录、地图、支付等第三方服务，并使用 **Firebase Analytics（Google LLC）进行使用情况统计**、**Firebase Crashlytics（Google LLC）收集崩溃报告**。各第三方对其服务自行负责。
10.2 使用第三方服务须遵守其用户协议与隐私政策，详见《第三方 SDK 一览》。

## 11. 免责与责任限制

11.1 因以下原因导致服务中断或损失的，我们不承担责任：

1. 不可抗力（地震、火灾、战争、政府行为等）；
2. 通信、电力、网络运营商等非我方原因；
3. 用户自身过失或账号信息泄露；
4. 第三方服务异常。

11.2 在法律允许的最大范围内，我们对您因使用本服务产生的损害赔偿责任总额以**该笔订单您实际支付金额为上限**，不对间接、附带或惩罚性损害负责。

## 12. 协议变更与终止

12.1 在以下情形我们可终止向您提供服务：

1. 您严重违反本协议或法律；
2. 长期未使用且无未完成订单；
3. 您主动申请注销；
4. 法律或司法、行政机关要求。

12.2 终止前已产生的订单仍按本协议履行；终止前用户内容的授权在终止后仍然有效。

## 13. 争议解决与适用法律

13.1 本协议适用**日本国法律**。
13.2 争议应先友好协商；协商不成的，以 **{{JURISDICTION_COURT}}** 为专属管辖法院。
13.3 本协议提供中文、日文、英文版本；**各语言版本不一致时，以日文版为准**。

## 14. 联系我们

- 运营方：{{COMPANY_NAME}}
- 注册地址：{{COMPANY_ADDRESS}}
- 客服邮箱：{{SUPPORT_EMAIL}}
- 客服电话：{{SUPPORT_PHONE}}
- 服务时间：{{SUPPORT_HOURS}}

---

## 更新日志

### 1.1.0（{{LAST_UPDATED}}）

- 接入 Firebase Analytics（Google LLC）用于使用情况统计
- 接入 Firebase Crashlytics（Google LLC）用于崩溃报告收集
- 同步更新隐私政策与第三方 SDK 一览

### 1.0.0

- 初始版本发布
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


-- ---- public: PRIVACY_POLICY / ja / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'PRIVACY_POLICY'::varchar AS code,
    'ja'::varchar AS language,
    'プライバシーポリシー'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} プライバシーポリシー

**バージョン**：1.1.0

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
| ログ情報 | API アクセス時刻、主要操作の記録 | 障害解析、サービス改善 |
| 位置情報（**お客様の許可を得た場合のみ**） | `geolocator` による緯度経度、OS 標準の逆ジオコーディング（`geocoding`）による市区町村名 | 近くの劇場のおすすめ、最寄り席の選択 |
| 写真・カメラ（**お客様が能動的に選択した場合のみ**） | アップロード用に選択された画像、または撮影された画像 | アイコン設定、お問い合わせ画像、レビュー画像 |
| 端末ローカル保管 | `accessToken` / `refreshToken` / `deviceId`（`flutter_secure_storage` により Keychain / Keystore に暗号化保存）、言語・UI 設定（`shared_preferences`） | ログイン状態の維持、設定の保持。**端末内のみに保存され、第三者へは送信されません。** |
| **Firebase Analytics**（Google LLC） | 画面閲覧イベント、カスタムイベント名とパラメータ（映画 ID、注文フロー ID 等）、Firebase インスタンス ID、端末 OS / モデル / アプリバージョン | サービス改善、利用状況の統計分析、購入フローの最適化 |
| **Firebase Crashlytics**（Google LLC） | クラッシュ発生時のスタックトレース、端末 OS / モデル / アプリバージョン、Firebase インスタンス ID | クラッシュの検知・解析、品質改善 |

> 現バージョンの App は **Firebase Cloud Messaging / APNs 等のプッシュ SDK を未導入**であり、FCM / APNs Token を取得していません。今後導入する場合は、本ポリシーおよび《サードパーティ SDK 一覧》を更新したうえで、改めてお客様の同意を取得します。

### 2.3 取得しない情報

- クレジットカード番号・CVV・有効期限等の決済機微情報は**当社のサーバを経由せず**、将来オンライン決済を導入する際は資格を有する第三者決済機関が直接処理します。
- 当社は、お客様の写真ライブラリ全体、連絡帳、SMS、クリップボードを**無断で読み取りません**。アップロードされる画像はお客様自身が選択した 1 枚に限定されます。
- Firebase Analytics では、お客様の氏名・メールアドレス・電話番号等の直接識別子は**意図的に送信しません**（ログイン成功後にユーザー ID を Firebase に紐付ける場合があります。詳細は第 4 条参照）。

## 3. 利用目的

取得した個人情報は以下の目的の範囲内でのみ利用します。

1. アカウント登録、ログイン、本人確認の実施。
2. 注文の履行：注文受付、決済の取次、発券、入場時の認証、返金・変更、カスタマーサポート。
3. 安全性の確保：アカウントの不正利用防止、ダフ屋および機械的買い占めの抑止、リスク監査。
4. サービス改善：**匿名化・仮名化処理を行ったデータ**による統計および機能改善（Firebase Analytics を含む）。
5. 品質向上：クラッシュレポートの解析と修正（Firebase Crashlytics）。
6. マーケティング通知（プッシュ通知をオフにされていない場合に限ります）。
7. 法令上の義務の履行。

## 4. 共有・委託・開示

当社は、お客様の個人情報を**正当な理由なく第三者へ販売することはありません**。以下の場合に限り共有または委託処理を行います。

### 4.1 劇場・興行会社との共有

注文の履行および入場認証のため、注文番号、上映回、座席、電子発券コード、必要に応じて氏名・電話番号下 4 桁を該当劇場へ共有します。

### 4.2 決済機関との共有（**現バージョンではオンライン決済は未開始**）

将来オンライン決済を開始した際は、決済処理・照合・返金のため、必要な情報（注文番号、金額、通貨、決済手段の識別子）を第三者決済機関に共有します。

### 4.3 委託処理（受託先は当社の指示に従って処理します）

- クラウドサービス：{{CLOUD_PROVIDERS}}
- カスタマーサポート / メール / SMS：{{COMMS_PROVIDERS}}
- **Firebase / Google LLC（米国）**：Firebase Analytics による利用状況の統計分析、Firebase Crashlytics によるクラッシュレポートの収集・解析。Google は [Google プライバシーポリシー](https://policies.google.com/privacy) に従ってデータを処理します。Firebase インスタンス ID および（ログイン後の）ユーザー ID が Google のサーバ（主に米国）へ送信される場合があります。

これらの受託先とはデータ処理契約を締結し、合意した目的の範囲内でのみ処理させ、当社と同等の保護措置を講じることを義務付けています。

### 4.4 第三者ログイン

Google / Apple ログインをご利用になる場合、当該プラットフォームは「お客様が本サービスを利用している事実」を取得し、必要な範囲のアカウント識別子・メールアドレス・ニックネーム・アイコンを当社へ提供します。

### 4.5 法令に基づく開示

法令、司法機関または行政機関の適法な要請に基づき、必要な範囲で開示します。

## 5. 越境移転

お客様の個人情報は {{STORAGE_LOCATIONS}} のサーバに保管されます。Firebase Analytics / Crashlytics のデータは **Google LLC（米国）** のサーバへ送信・保管される場合があります。日本《個人情報保護法》、《GDPR》等の適用法令に基づき、標準契約条項（SCC）の締結など必要な保護措置を講じます。

## 6. 保管とセキュリティ

6.1 当社は TLS 暗号化、ソルト付きハッシュ、ロールベースアクセス制御、操作ログ監査、脆弱性監視等のセキュリティ対策を講じています。
6.2 個人情報のセキュリティ事故が発生した場合、法令に定める方法と期限内にお客様および監督機関へ通知します。

## 7. 保管期間

| 情報種別 | 保管期間 |
|---|---|
| アカウント情報 | 退会日から {{ACCOUNT_RETENTION_DAYS}} 日以内に削除または匿名化。 |
| 注文・取引記録 | 日本《電子帳簿保存法》に従い**最低 7 年間**保管。 |
| ログイン・操作ログ | {{LOG_RETENTION_DAYS}} 日。 |
| Firebase Analytics イベント | Google のデータ保持設定に従い、最長 14 ヶ月（当社設定）。 |
| Firebase Crashlytics レポート | 90 日間（Google デフォルト）。 |
| 端末ローカル Token | ログアウトまたはアプリのアンインストールにより消去。 |

## 8. お客様の権利

適用法令に基づき、お客様はご自身の個人情報について開示・訂正・削除・同意撤回等の権利を行使できます。プライバシー担当窓口 {{DPO_EMAIL}} までご連絡ください（{{DPO_RESPONSE_DAYS}} 営業日以内に回答）。

## 9. 第三者 SDK

本サービスが使用する第三者 SDK の詳細は**《サードパーティ SDK 一覧》**（コード：`THIRD_PARTY_SDK`）をご参照ください。

## 10. 未成年者の保護

本サービスは **{{MIN_AGE}} 歳以上**の方を対象としています。{{MIN_AGE}} 歳未満の方は、保護者の同意のもとご利用ください。

## 11. 本ポリシーの変更

重要な変更については、アプリ内ポップアップやメール等の顕著な方法でお知らせします。機微情報の処理、共有先、越境移転に関する重要な変更については、お客様の**改めての明示的な同意**を経て該当機能を提供します。

## 12. お問い合わせ

- 運営会社：{{COMPANY_NAME}}
- プライバシー担当窓口：{{DPO_EMAIL}}
- カスタマーサポート：{{SUPPORT_EMAIL}}

---

## 更新履歴

### 1.1.0（{{LAST_UPDATED}}）

- Firebase Analytics（Google LLC）による利用状況の統計分析を導入
- Firebase Crashlytics（Google LLC）によるクラッシュレポートの収集を導入
- 関連するプライバシーポリシーおよびサードパーティ SDK 一覧を更新

### 1.0.0

- 初版を公開
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


-- ---- public: PRIVACY_POLICY / en / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'PRIVACY_POLICY'::varchar AS code,
    'en'::varchar AS language,
    'Privacy Policy'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} Privacy Policy

**Version**: 1.1.0

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

### 2.2 Information Collected Automatically

| Type | Items | Purpose |
|---|---|---|
| Device | Device model, OS and version, `deviceId` (generated locally), app version | Anti-fraud, refresh-token binding, compatibility |
| Network | IP address, connection type | Anti-fraud, API routing |
| Logs | API access timestamps, key operation logs | Troubleshooting and product improvement |
| Location (**only with your permission**) | GPS / network coordinates from `geolocator`; reverse geocoding via `geocoding` | Nearby cinema recommendations |
| Photos / Camera (**only when you actively choose**) | Images you select or capture | Avatar, support attachments, review images |
| Local storage | `accessToken` / `refreshToken` / `deviceId` (encrypted via `flutter_secure_storage`); preferences via `shared_preferences` | Stays on the device only |
| **Firebase Analytics** (Google LLC) | Screen-view events, custom event names and parameters (movie ID, purchase flow ID, etc.), Firebase instance ID, device OS / model / app version | Service improvement, usage statistics, purchase funnel optimisation |
| **Firebase Crashlytics** (Google LLC) | Crash stack traces, device OS / model / app version, Firebase instance ID | Crash detection, analysis, and quality improvement |

> The current build **does not integrate** Firebase Cloud Messaging / APNs push SDKs. We will update this Policy and the Third-party SDK List, and request your renewed consent, when push SDKs are introduced.

### 2.3 Information We Do Not Collect

- Full bank card numbers, CVV, or expiry are **not transmitted to our servers**.
- We **do not** scan your photo library, contacts, SMS, or clipboard.
- Firebase Analytics does **not intentionally transmit** your name, email, or phone number (we may link a user ID to Firebase after login; see Section 4).

## 3. How We Use Your Information

1. Account registration, login, and identity verification.
2. Order fulfilment: order placement, payment relay, ticket issuance, entry verification, refunds / changes, and customer support.
3. Security: account anti-fraud, anti-scalping, risk audit.
4. Service improvement: statistics and feature optimisation using **anonymised or pseudonymised** data (including Firebase Analytics).
5. Quality improvement: crash report analysis and fixes (Firebase Crashlytics).
6. Marketing notifications (only if you have not turned off push notifications).
7. Performance of legal obligations.

## 4. Sharing, Processors, and Disclosure

We **do not sell** your personal information to unrelated third parties.

### 4.1 Sharing with Cinemas / Distributors

For order fulfilment and entry verification, we share order number, showtime, seat, electronic pickup code, and where necessary the last four digits of name / phone with the relevant cinema.

### 4.2 Sharing with Payment Providers (**not yet active**)

When online payment is introduced, we will share necessary information with the third-party payment provider.

### 4.3 Processors (acting on our instructions)

- Cloud services: {{CLOUD_PROVIDERS}}
- Customer support / Email / SMS: {{COMMS_PROVIDERS}}
- **Firebase / Google LLC (United States)**: usage statistics via Firebase Analytics, crash report collection and analysis via Firebase Crashlytics. Google processes data in accordance with the [Google Privacy Policy](https://policies.google.com/privacy). Firebase instance IDs and (after login) user IDs may be sent to Google servers (primarily in the United States).

### 4.4 Third-Party Login

When you use Google / Apple Sign-In, the relevant platform returns the necessary account identifier, email, nickname, and avatar to us.

### 4.5 Disclosure Required by Law

We may disclose information when required by laws, courts, or administrative authorities.

## 5. Cross-Border Transfers

Your personal information is stored on servers in {{STORAGE_LOCATIONS}}. Firebase Analytics / Crashlytics data may be sent to and stored on **Google LLC (United States)** servers. We adopt necessary protection measures (e.g. Standard Contractual Clauses) under applicable laws.

## 6. Storage and Security

We employ TLS encryption, salted hashing, role-based access control, operation logging, and vulnerability monitoring. Security incidents will be reported as required by law.

## 7. Retention Periods

| Category | Retention period |
|---|---|
| Account information | Deleted or anonymised within {{ACCOUNT_RETENTION_DAYS}} days after account closure. |
| Order and transaction records | Retained for **at least 7 years** under Japan's Electronic Books Preservation Act. |
| Login and operation logs | {{LOG_RETENTION_DAYS}} days. |
| Firebase Analytics events | Up to 14 months per our Google data retention settings. |
| Firebase Crashlytics reports | 90 days (Google default). |
| Local Tokens | Cleared on logout or app uninstallation. |

## 8. Your Rights

You may exercise access, correction, deletion, and consent withdrawal rights. Contact our privacy team at {{DPO_EMAIL}} (response within {{DPO_RESPONSE_DAYS}} business days).

## 9. Third-Party SDKs

See the **Third-party SDK List** (code: `THIRD_PARTY_SDK`) for details.

## 10. Protection of Minors

The Service is intended for users aged **{{MIN_AGE}} or above**.

## 11. Changes to This Policy

Material changes will be notified prominently. Changes involving sensitive personal information processing, sharing, or cross-border transfers will only take effect after we obtain your **renewed explicit consent**.

## 12. Contact Us

- Operator: {{COMPANY_NAME}}
- Privacy contact: {{DPO_EMAIL}}
- Customer Support: {{SUPPORT_EMAIL}}

---

## Changelog

### 1.1.0 ({{LAST_UPDATED}})

- Integrated Firebase Analytics (Google LLC) for usage statistics
- Integrated Firebase Crashlytics (Google LLC) for crash reporting
- Updated Privacy Policy and Third-party SDK List accordingly

### 1.0.0

- Initial release
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


-- ---- public: PRIVACY_POLICY / zh / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'PRIVACY_POLICY'::varchar AS code,
    'zh'::varchar AS language,
    '隐私政策'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} 隐私政策

**版本**：1.1.0

**生效日期**：{{EFFECTIVE_DATE}}

**最后更新**：{{LAST_UPDATED}}

{{COMPANY_NAME}}（以下简称「我们」）将保护您的个人信息视为核心责任之一。本隐私政策说明您使用 {{APP_NAME}}（以下简称「本服务」）时，**我们收集哪些信息、为何收集、如何使用/共享/保存，以及您可行使哪些权利**。

使用本服务前请仔细阅读本政策。继续使用即视为您同意我们按本政策处理个人信息。

---

## 1. 关于我们

- 个人信息处理者：{{COMPANY_NAME}}
- 注册地址：{{COMPANY_ADDRESS}}
- 隐私联系：{{DPO_EMAIL}}

## 2. 我们收集的信息

### 2.1 您主动提供的信息

| 场景 | 信息项 | 是否必需 |
|---|---|---|
| 邮箱注册 | 邮箱、密码（加密存储）、昵称 | 必需 |
| Google / Apple 登录 | OpenID（`sub`）、邮箱、昵称、头像 URL | 必需 |
| 资料编辑 | 头像、昵称、性别、出生日期 | 可选 |
| 下单/取票 | 联系方式、电影、场次、座位、订单金额 | 必需 |
| 评论/客服 | 文字、图片、视频 | 可选 |

### 2.2 使用服务时自动收集的信息

| 类型 | 信息项 | 用途 |
|---|---|---|
| 设备 | 型号、OS 及版本、本地生成的 `deviceId`、应用版本 | 反欺诈、Token 绑定、兼容性 |
| 网络 | IP 地址、连接类型 | 反欺诈、API 路由 |
| 日志 | API 访问时间、关键操作记录 | 故障排查、产品改进 |
| 位置（**经您授权**） | `geolocator` 经纬度；`geocoding` 反查城市 | 附近影院推荐 |
| 照片/相机（**您主动选择时**） | 您选择或拍摄的图片 | 头像、客服附件、评论图片 |
| 本地存储 | `accessToken`/`refreshToken`/`deviceId`（`flutter_secure_storage` 加密）；语言等偏好（`shared_preferences`） | 仅保存在设备本地 |
| **Firebase Analytics**（Google LLC） | 页面浏览事件、自定义事件名与参数（电影 ID、购买流程 ID 等）、Firebase 实例 ID、设备 OS/型号/应用版本 | 使用统计、购买漏斗优化、产品改进 |
| **Firebase Crashlytics**（Google LLC） | 崩溃堆栈、设备 OS/型号/应用版本、Firebase 实例 ID | 崩溃检测、分析与质量改进 |

> 当前版本**未接入** Firebase Cloud Messaging / APNs 等推送 SDK。未来接入时将更新本政策与《第三方 SDK 一览》并重新征得同意。

### 2.3 我们不收集的信息

- 完整银行卡号、CVV、有效期等**不经我们的服务器**。
- **不会**扫描您的相册、通讯录、短信或剪贴板。
- Firebase Analytics **不会故意发送**您的姓名、邮箱、电话等直接标识符（登录后可能向 Firebase 关联用户 ID，见第 4 节）。

## 3. 使用目的

1. 账号注册、登录与身份验证；
2. 订单履行：下单、支付、出票、入场核验、退改与客服；
3. 安全：反欺诈、反黄牛、风险审计；
4. 产品改进：基于**匿名化/假名化**数据的统计与优化（含 Firebase Analytics）；
5. 质量提升：崩溃报告分析与修复（Firebase Crashlytics）；
6. 营销通知（在您未关闭推送时）；
7. 履行法定义务。

## 4. 共享、委托与披露

我们**不会**向无关第三方出售您的个人信息。

### 4.1 与影院/发行方共享

为履约与入场核验，向相关影院共享订单号、场次、座位、电子取票码及必要联系信息。

### 4.2 与支付机构共享（**当前未上线在线支付**）

未来上线时将共享订单号、金额、支付方式标识等必要信息。

### 4.3 委托处理

- 云服务：{{CLOUD_PROVIDERS}}
- 客服/邮件/SMS：{{COMMS_PROVIDERS}}
- **Firebase / Google LLC（美国）**：Firebase Analytics 使用统计、Firebase Crashlytics 崩溃收集与分析。Google 按 [Google 隐私政策](https://policies.google.com/privacy) 处理数据。Firebase 实例 ID 及（登录后的）用户 ID 可能发送至 Google 服务器（主要在美国）。

### 4.4 第三方登录

使用 Google / Apple 登录时，相应平台会向我们在必要范围内提供账号标识、邮箱、昵称、头像。

### 4.5 依法披露

应法律、司法或行政机关合法要求时，在必要范围内披露。

## 5. 跨境传输

个人信息保存在 {{STORAGE_LOCATIONS}}。Firebase Analytics / Crashlytics 数据可能传输至并保存在 **Google LLC（美国）** 服务器。我们将按适用法律采取标准合同条款（SCC）等必要保护措施。

## 6. 存储与安全

采用 TLS 加密、加盐哈希、基于角色的访问控制、操作日志与漏洞监测等安全措施。发生安全事件将依法通知。

## 7. 保存期限

| 信息类型 | 保存期限 |
|---|---|
| 账号信息 | 注销后 {{ACCOUNT_RETENTION_DAYS}} 日内删除或匿名化 |
| 订单/交易记录 | 依日本《电子账簿保存法》**至少 7 年** |
| 登录/操作日志 | {{LOG_RETENTION_DAYS}} 日 |
| Firebase Analytics 事件 | 依 Google 保留设置，最长 14 个月 |
| Firebase Crashlytics 报告 | 90 日（Google 默认） |
| 本地 Token | 退出登录或卸载应用后清除 |

## 8. 您的权利

您可依法行使查阅、更正、删除、撤回同意等权利。请联系 {{DPO_EMAIL}}（{{DPO_RESPONSE_DAYS}} 个工作日内回复）。

## 9. 第三方 SDK

详见**《第三方 SDK 一览》**（代码：`THIRD_PARTY_SDK`）。

## 10. 未成年人保护

本服务面向 **{{MIN_AGE}} 岁及以上**用户。未满 {{MIN_AGE}} 岁须在监护人同意下使用。

## 11. 政策变更

重大变更将通过应用内弹窗、邮件等显著方式告知。涉及敏感信息处理、共享或跨境传输的重大变更，将在取得您**重新明示同意**后生效。

## 12. 联系我们

- 运营方：{{COMPANY_NAME}}
- 隐私联系：{{DPO_EMAIL}}
- 客服：{{SUPPORT_EMAIL}}

---

## 更新日志

### 1.1.0（{{LAST_UPDATED}}）

- 接入 Firebase Analytics（Google LLC）用于使用情况统计
- 接入 Firebase Crashlytics（Google LLC）用于崩溃报告收集
- 同步更新隐私政策与第三方 SDK 一览

### 1.0.0

- 初始版本发布
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


-- ---- public: THIRD_PARTY_SDK / ja / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'THIRD_PARTY_SDK'::varchar AS code,
    'ja'::varchar AS language,
    'サードパーティ SDK 一覧'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    FALSE AS is_required_accept,
    $body$
# {{APP_NAME}} サードパーティ SDK 一覧

**バージョン**：1.1.0

**施行日**：{{EFFECTIVE_DATE}}

**最終更新日**：{{LAST_UPDATED}}

{{APP_NAME}} のアカウント登録、ログイン、現在地に基づく劇場検索、アイコン／レビュー画像のアップロード、利用状況の統計分析、クラッシュレポートの収集等の機能を提供するため、本アプリは以下のサードパーティ SDK およびシステム機能を利用しています。

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
| `dio` | Flutter コミュニティ | バックエンド（`{{API_DOMAIN}}`）との HTTPS 通信 | リクエスト、Bearer Token、`deviceId` |
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
3. ご質問は {{DPO_EMAIL}} までお問い合わせください。

---

## 更新履歴

### 1.1.0（{{LAST_UPDATED}}）

- Firebase Analytics（Google LLC）による利用状況の統計分析を導入
- Firebase Crashlytics（Google LLC）によるクラッシュレポートの収集を導入
- 関連するプライバシーポリシーおよびサードパーティ SDK 一覧を更新

### 1.0.0

- 初版を公開
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


-- ---- public: THIRD_PARTY_SDK / en / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'THIRD_PARTY_SDK'::varchar AS code,
    'en'::varchar AS language,
    'Third-party SDK List'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    FALSE AS is_required_accept,
    $body$
# {{APP_NAME}} Third-party SDK List

**Version**: 1.1.0

**Effective Date**: {{EFFECTIVE_DATE}}

**Last Updated**: {{LAST_UPDATED}}

To support account registration, login, location-based cinema discovery, avatar / review uploads, usage statistics, and crash reporting, the App integrates the third-party SDKs and system services listed below.

> This list is kept consistent with the actual app dependencies declared in `pubspec.yaml` and is updated alongside any addition or removal in subsequent releases.

---

## 1. Login and Authentication

| SDK / Service | Provider | Purpose | Information collected / used | Trigger | Privacy Policy |
|---|---|---|---|---|---|
| `google_sign_in` | Google LLC | Google account sign-in / sign-up | Google `sub`, email, nickname, avatar URL | Tap "Sign in with Google" | https://policies.google.com/privacy |
| `sign_in_with_apple` | Apple Inc. | Apple ID sign-in (iOS only) | Apple `sub`, email, nickname | Tap "Sign in with Apple" on iOS | https://www.apple.com/legal/privacy/ |
| `flutter_appauth` | Maks O. (OSS) | OAuth 2.0 PKCE login via X | Temporary parameters for authorization code exchange | Tap "Sign in with X" | https://x.com/privacy |

---

## 2. Analytics and Crash Monitoring

| SDK / Service | Provider | Purpose | Information collected / used | Trigger | Privacy Policy |
|---|---|---|---|---|---|
| `firebase_analytics` | Google LLC | Screen-view and custom event statistics, purchase funnel optimisation | Event names and parameters, Firebase instance ID, device OS / model / app version; user ID linked after login | App launch, navigation, key actions (automatic) | https://policies.google.com/privacy |
| `firebase_crashlytics` | Google LLC | Crash detection, analysis, quality improvement | Crash stack traces, device OS / model / app version, Firebase instance ID | App crash (automatic) | https://policies.google.com/privacy |
| `firebase_core` | Google LLC | Firebase SDK initialisation foundation | Initialisation data shared with the above Firebase services | App launch | https://policies.google.com/privacy |

> Firebase data is processed on Google LLC (United States) servers. See Privacy Policy Sections 4.3 and 5 for details.

---

## 3. Device Capabilities and Location

| SDK / Service | Provider | Purpose | Information collected / used | Trigger | Privacy Policy |
|---|---|---|---|---|---|
| `geolocator` | Baseflow | Approximate device location | GPS / network coordinates (after user authorisation) | Nearby cinema recommendations | https://baseflow.com/privacy-statement/ |
| `geocoding` | Baseflow (OS-level) | Reverse geocoding | Coordinates only | City name display | Same as above |
| `image_picker` | Flutter official plugin | Pick / capture images | Image selected by you | Avatar change, review images | System privacy permissions |
| `image_editor` | Flutter community | Crop / rotate images | Selected image (local processing only) | Same as `image_picker` | Local processing only |

---

## 4. Local-Only Storage (does not leave your device)

| SDK / Service | Provider | Purpose | Stored information |
|---|---|---|---|
| `flutter_secure_storage` | Flutter community | Encrypted credential storage | `accessToken` / `refreshToken` / `deviceId` |
| `shared_preferences` | Flutter official plugin | Non-sensitive preferences | Language, home tab, onboarding flags, etc. |

---

## 5. Networking and Content Display

| SDK / Service | Provider | Purpose | Information involved |
|---|---|---|---|
| `dio` | Flutter community | HTTPS communication with our backend (`{{API_DOMAIN}}`) | Request payload, Bearer token, `deviceId` |
| `extended_image` | Flutter community | Image loading and disk caching | Local cache only |
| `flutter_markdown` | Flutter official plugin | Rendering Markdown agreement bodies | Local rendering only |
| `url_launcher` | Flutter official plugin | Opening external links in browser / mail app | Triggered only when you tap |
| `share_plus` | Flutter official plugin | System share sheet | Only when you choose to share |

---

## 6. Helper Tools (no personal data collected)

| SDK / Service | Purpose |
|---|---|
| `package_info_plus` | Read the app's own version number |
| `uuid` | Generate device-local `deviceId` |
| `crypto` | Local hash computations |
| `logger` / `pretty_dio_logger` | Debug logging only; release builds emit nothing |
| UI plugin suite | Rendering, animation, formatting. No network or personal data collection. |

---

## 7. Third-Party Services Not Currently Integrated

The following are **not currently integrated**. We will update this list and the relevant Privacy Policy when they are added.

- Push messaging (Firebase Cloud Messaging / APNs)
- Online payment (Stripe / PayPay / Apple Pay / Google Pay, etc.)
- Third-party map components (Google Maps / Apple MapKit JS)

---

## 8. Maintenance Notes

1. This list is curated based on the actual `pubspec.yaml` dependencies and kept in sync with app releases.
2. When adding any SDK that involves personal information collection, we will update this list and the relevant Privacy Policy, and request your renewed consent where required.
3. Questions: {{DPO_EMAIL}}.

---

## Changelog

### 1.1.0 ({{LAST_UPDATED}})

- Integrated Firebase Analytics (Google LLC) for usage statistics
- Integrated Firebase Crashlytics (Google LLC) for crash reporting
- Updated Privacy Policy and Third-party SDK List accordingly

### 1.0.0

- Initial release
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


-- ---- public: THIRD_PARTY_SDK / zh / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'THIRD_PARTY_SDK'::varchar AS code,
    'zh'::varchar AS language,
    '第三方 SDK 一览'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    FALSE AS is_required_accept,
    $body$
# {{APP_NAME}} 第三方 SDK 一览

**版本**：1.1.0

**生效日期**：{{EFFECTIVE_DATE}}

**最后更新**：{{LAST_UPDATED}}

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
| `dio` | Flutter 社区 | 与后端（`{{API_DOMAIN}}`）HTTPS 通信 | 请求体、Bearer Token、`deviceId` |
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
3. 疑问请联系 {{DPO_EMAIL}}。

---

## 更新日志

### 1.1.0（{{LAST_UPDATED}}）

- 接入 Firebase Analytics（Google LLC）用于使用情况统计
- 接入 Firebase Crashlytics（Google LLC）用于崩溃报告收集
- 同步更新隐私政策与第三方 SDK 一览

### 1.0.0

- 初始版本发布
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


-- ---- crawl: USER_TERMS / ja / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'USER_TERMS'::varchar AS code,
    'ja'::varchar AS language,
    '利用規約'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} 利用規約

**バージョン**：1.1.0

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

10.1 本サービスは、Google ログイン、Apple ログイン、地図、決済等の第三者サービスに加え、**Firebase Analytics（Google LLC）による利用状況の統計分析**および**Firebase Crashlytics（Google LLC）によるクラッシュレポートの収集**を行います。これらのサービスの提供および責任は、それぞれの第三者が負うものとします。
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

---

## 更新履歴

### 1.1.0（{{LAST_UPDATED}}）

- Firebase Analytics（Google LLC）による利用状況の統計分析を導入
- Firebase Crashlytics（Google LLC）によるクラッシュレポートの収集を導入
- 関連するプライバシーポリシーおよびサードパーティ SDK 一覧を更新

### 1.0.0

- 初版を公開
$body$::text AS content
)
INSERT INTO crawl.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);


-- ---- crawl: USER_TERMS / en / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'USER_TERMS'::varchar AS code,
    'en'::varchar AS language,
    'User Terms'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} User Terms

**Version**: 1.1.0

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

10.1 The Service integrates third-party services such as Google Sign-In, Apple Sign-In, maps, and payments. In addition, we use **Firebase Analytics (Google LLC) for usage statistics** and **Firebase Crashlytics (Google LLC) for crash reporting**. Each third party is responsible for its own services.
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

---

## Changelog

### 1.1.0 ({{LAST_UPDATED}})

- Integrated Firebase Analytics (Google LLC) for usage statistics
- Integrated Firebase Crashlytics (Google LLC) for crash reporting
- Updated Privacy Policy and Third-party SDK List accordingly

### 1.0.0

- Initial release
$body$::text AS content
)
INSERT INTO crawl.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);


-- ---- crawl: USER_TERMS / zh / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'USER_TERMS'::varchar AS code,
    'zh'::varchar AS language,
    '用户协议'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} 用户协议

**版本**：1.1.0

**生效日期**：{{EFFECTIVE_DATE}}

**最后更新**：{{LAST_UPDATED}}

欢迎使用 {{APP_NAME}}（以下简称「本服务」）。本用户协议（以下简称「本协议」）由 {{COMPANY_NAME}}（以下简称「我们」或「运营方」）与您（以下简称「用户」或「您」）之间订立，规范您对本服务的访问与使用。

使用前请仔细阅读本协议全部条款，尤其关注**免责、责任限制、退款与变更、争议解决及适用法律**相关条款。注册、登录或继续使用本服务，即视为您已阅读、理解并同意受本协议约束。

---

## 1. 适用范围与效力

1.1 本协议适用于 {{APP_NAME}} 移动应用、官方网站及其附属功能的全部内容。
1.2 完成账号注册、通过 Google / Apple 登录或完成下单，即视为您接受本协议。
1.3 我们可因法律或运营需要修订本协议，并通过应用内公告、消息或推送通知告知。修订后继续使用的，视为接受变更后的协议；若不同意，请立即停止使用。

## 2. 账号

2.1 您可使用邮箱与密码，或通过 Google / Apple 账号注册与登录。
2.2 您应提供真实、准确、完整的注册信息，并妥善保管账号与密码。账号下的行为原则上视为您本人行为，相关后果由您自行承担。
2.3 禁止出借、转让、出售或以其他方式向第三方提供账号。
2.4 如发现账号被盗用或存在异常登录，请立即联系我们。
2.5 您可在「我的 - 账号设置」申请注销账号。注销后的个人信息处理遵循《隐私政策》。

## 3. 服务内容

3.1 本服务提供电影信息浏览、影院与场次查询、在线选座购票、电子取票、特典发放、订单管理、评分评论及通知等功能。
3.2 我们仅提供票务信息与交易撮合，**实际上映服务由影院提供**，上映质量、座位舒适度、排片安排等由影院直接负责。
3.3 我们可因法律、合作方变更、安全或运营需要调整、暂停或终止部分功能，重大变更将提前合理告知。

## 4. 下单、支付与取票

4.1 商品价格以下单时展示金额为准（含票价、服务费及法定税费等）。
4.2 支付由第三方机构（信用卡、Apple Pay、PayPay 等）处理，支付纠纷由您与相应机构自行解决。
4.3 支付成功后发放电子票或取票码，请按影院规则在开场前到场办理入场。
4.4 因迟到、走错场次、丢失取票信息等导致的损失，我们及影院**不予退款或改期**。

## 5. 退款与变更政策

5.1 **重要：电影票对应特定场次，原则上不可随意取消或变更**。具体规则如下：

- **未取票且距开场 {{REFUND_HOURS}} 小时以上**：可退款，按原支付渠道退回。
- **已取票或距开场不足 {{REFUND_HOURS}} 小时**：不可退款。
- **因影院原因取消或改期**：全额退款（部分情况可能补偿）。
- **因我方原因（系统故障、超卖等）**：全额退款，视情况补偿。

5.2 服务费及特典相关费用是否可退，以订单页展示规则为准。
5.3 退款到账时间取决于支付渠道，通常 1～15 个工作日。

## 6. 特典、优惠券与活动

6.1 我们可能提供特典、优惠券、积分等福利，具体以活动页规则为准。
6.2 禁止通过虚假交易、批量脚本、漏洞利用等方式获取或使用福利；违规时我们可取消、回收相关权益并保留追责权利。

## 7. 禁止行为

7.1 您在使用本服务时不得：

1. 违反法律法规或公序良俗；
2. 倒卖票券、囤积票券或扰乱市场秩序；
3. 使用机器人、爬虫、自动化脚本或修改客户端访问或操作本服务；
4. 发布虚假信息、广告、骚扰内容、侵权或不适当内容；
5. 攻击、破解、逆向或规避平台安全机制；
6. 其他违反法律或本协议的行为。

7.2 违规时我们可采取警告、功能限制、暂停或注销账号、索赔等措施。

## 8. 用户生成内容（UGC）

8.1 您在本服务发布的评论、评分、图片等内容（「用户内容」）著作权归您所有；您授予我们**非独占、免费、可再许可、全球范围**的使用权，用于本服务的提供、运营与推广。
8.2 用户内容由您自行负责，不得侵犯第三方权利；我们可删除、隐藏或限制违规内容。

## 9. 知识产权

9.1 本服务展示的电影海报、剧照、视频、商标等权利归相应权利人所有，不得超出本服务范围下载、转载或商用。
9.2 本服务标识、界面设计、源代码、数据库结构等著作权归运营方所有。

## 10. 第三方服务

10.1 本服务使用 Google 登录、Apple 登录、地图、支付等第三方服务，并使用 **Firebase Analytics（Google LLC）进行使用情况统计**、**Firebase Crashlytics（Google LLC）收集崩溃报告**。各第三方对其服务自行负责。
10.2 使用第三方服务须遵守其用户协议与隐私政策，详见《第三方 SDK 一览》。

## 11. 免责与责任限制

11.1 因以下原因导致服务中断或损失的，我们不承担责任：

1. 不可抗力（地震、火灾、战争、政府行为等）；
2. 通信、电力、网络运营商等非我方原因；
3. 用户自身过失或账号信息泄露；
4. 第三方服务异常。

11.2 在法律允许的最大范围内，我们对您因使用本服务产生的损害赔偿责任总额以**该笔订单您实际支付金额为上限**，不对间接、附带或惩罚性损害负责。

## 12. 协议变更与终止

12.1 在以下情形我们可终止向您提供服务：

1. 您严重违反本协议或法律；
2. 长期未使用且无未完成订单；
3. 您主动申请注销；
4. 法律或司法、行政机关要求。

12.2 终止前已产生的订单仍按本协议履行；终止前用户内容的授权在终止后仍然有效。

## 13. 争议解决与适用法律

13.1 本协议适用**日本国法律**。
13.2 争议应先友好协商；协商不成的，以 **{{JURISDICTION_COURT}}** 为专属管辖法院。
13.3 本协议提供中文、日文、英文版本；**各语言版本不一致时，以日文版为准**。

## 14. 联系我们

- 运营方：{{COMPANY_NAME}}
- 注册地址：{{COMPANY_ADDRESS}}
- 客服邮箱：{{SUPPORT_EMAIL}}
- 客服电话：{{SUPPORT_PHONE}}
- 服务时间：{{SUPPORT_HOURS}}

---

## 更新日志

### 1.1.0（{{LAST_UPDATED}}）

- 接入 Firebase Analytics（Google LLC）用于使用情况统计
- 接入 Firebase Crashlytics（Google LLC）用于崩溃报告收集
- 同步更新隐私政策与第三方 SDK 一览

### 1.0.0

- 初始版本发布
$body$::text AS content
)
INSERT INTO crawl.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);


-- ---- crawl: PRIVACY_POLICY / ja / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'PRIVACY_POLICY'::varchar AS code,
    'ja'::varchar AS language,
    'プライバシーポリシー'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} プライバシーポリシー

**バージョン**：1.1.0

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
| ログ情報 | API アクセス時刻、主要操作の記録 | 障害解析、サービス改善 |
| 位置情報（**お客様の許可を得た場合のみ**） | `geolocator` による緯度経度、OS 標準の逆ジオコーディング（`geocoding`）による市区町村名 | 近くの劇場のおすすめ、最寄り席の選択 |
| 写真・カメラ（**お客様が能動的に選択した場合のみ**） | アップロード用に選択された画像、または撮影された画像 | アイコン設定、お問い合わせ画像、レビュー画像 |
| 端末ローカル保管 | `accessToken` / `refreshToken` / `deviceId`（`flutter_secure_storage` により Keychain / Keystore に暗号化保存）、言語・UI 設定（`shared_preferences`） | ログイン状態の維持、設定の保持。**端末内のみに保存され、第三者へは送信されません。** |
| **Firebase Analytics**（Google LLC） | 画面閲覧イベント、カスタムイベント名とパラメータ（映画 ID、注文フロー ID 等）、Firebase インスタンス ID、端末 OS / モデル / アプリバージョン | サービス改善、利用状況の統計分析、購入フローの最適化 |
| **Firebase Crashlytics**（Google LLC） | クラッシュ発生時のスタックトレース、端末 OS / モデル / アプリバージョン、Firebase インスタンス ID | クラッシュの検知・解析、品質改善 |

> 現バージョンの App は **Firebase Cloud Messaging / APNs 等のプッシュ SDK を未導入**であり、FCM / APNs Token を取得していません。今後導入する場合は、本ポリシーおよび《サードパーティ SDK 一覧》を更新したうえで、改めてお客様の同意を取得します。

### 2.3 取得しない情報

- クレジットカード番号・CVV・有効期限等の決済機微情報は**当社のサーバを経由せず**、将来オンライン決済を導入する際は資格を有する第三者決済機関が直接処理します。
- 当社は、お客様の写真ライブラリ全体、連絡帳、SMS、クリップボードを**無断で読み取りません**。アップロードされる画像はお客様自身が選択した 1 枚に限定されます。
- Firebase Analytics では、お客様の氏名・メールアドレス・電話番号等の直接識別子は**意図的に送信しません**（ログイン成功後にユーザー ID を Firebase に紐付ける場合があります。詳細は第 4 条参照）。

## 3. 利用目的

取得した個人情報は以下の目的の範囲内でのみ利用します。

1. アカウント登録、ログイン、本人確認の実施。
2. 注文の履行：注文受付、決済の取次、発券、入場時の認証、返金・変更、カスタマーサポート。
3. 安全性の確保：アカウントの不正利用防止、ダフ屋および機械的買い占めの抑止、リスク監査。
4. サービス改善：**匿名化・仮名化処理を行ったデータ**による統計および機能改善（Firebase Analytics を含む）。
5. 品質向上：クラッシュレポートの解析と修正（Firebase Crashlytics）。
6. マーケティング通知（プッシュ通知をオフにされていない場合に限ります）。
7. 法令上の義務の履行。

## 4. 共有・委託・開示

当社は、お客様の個人情報を**正当な理由なく第三者へ販売することはありません**。以下の場合に限り共有または委託処理を行います。

### 4.1 劇場・興行会社との共有

注文の履行および入場認証のため、注文番号、上映回、座席、電子発券コード、必要に応じて氏名・電話番号下 4 桁を該当劇場へ共有します。

### 4.2 決済機関との共有（**現バージョンではオンライン決済は未開始**）

将来オンライン決済を開始した際は、決済処理・照合・返金のため、必要な情報（注文番号、金額、通貨、決済手段の識別子）を第三者決済機関に共有します。

### 4.3 委託処理（受託先は当社の指示に従って処理します）

- クラウドサービス：{{CLOUD_PROVIDERS}}
- カスタマーサポート / メール / SMS：{{COMMS_PROVIDERS}}
- **Firebase / Google LLC（米国）**：Firebase Analytics による利用状況の統計分析、Firebase Crashlytics によるクラッシュレポートの収集・解析。Google は [Google プライバシーポリシー](https://policies.google.com/privacy) に従ってデータを処理します。Firebase インスタンス ID および（ログイン後の）ユーザー ID が Google のサーバ（主に米国）へ送信される場合があります。

これらの受託先とはデータ処理契約を締結し、合意した目的の範囲内でのみ処理させ、当社と同等の保護措置を講じることを義務付けています。

### 4.4 第三者ログイン

Google / Apple ログインをご利用になる場合、当該プラットフォームは「お客様が本サービスを利用している事実」を取得し、必要な範囲のアカウント識別子・メールアドレス・ニックネーム・アイコンを当社へ提供します。

### 4.5 法令に基づく開示

法令、司法機関または行政機関の適法な要請に基づき、必要な範囲で開示します。

## 5. 越境移転

お客様の個人情報は {{STORAGE_LOCATIONS}} のサーバに保管されます。Firebase Analytics / Crashlytics のデータは **Google LLC（米国）** のサーバへ送信・保管される場合があります。日本《個人情報保護法》、《GDPR》等の適用法令に基づき、標準契約条項（SCC）の締結など必要な保護措置を講じます。

## 6. 保管とセキュリティ

6.1 当社は TLS 暗号化、ソルト付きハッシュ、ロールベースアクセス制御、操作ログ監査、脆弱性監視等のセキュリティ対策を講じています。
6.2 個人情報のセキュリティ事故が発生した場合、法令に定める方法と期限内にお客様および監督機関へ通知します。

## 7. 保管期間

| 情報種別 | 保管期間 |
|---|---|
| アカウント情報 | 退会日から {{ACCOUNT_RETENTION_DAYS}} 日以内に削除または匿名化。 |
| 注文・取引記録 | 日本《電子帳簿保存法》に従い**最低 7 年間**保管。 |
| ログイン・操作ログ | {{LOG_RETENTION_DAYS}} 日。 |
| Firebase Analytics イベント | Google のデータ保持設定に従い、最長 14 ヶ月（当社設定）。 |
| Firebase Crashlytics レポート | 90 日間（Google デフォルト）。 |
| 端末ローカル Token | ログアウトまたはアプリのアンインストールにより消去。 |

## 8. お客様の権利

適用法令に基づき、お客様はご自身の個人情報について開示・訂正・削除・同意撤回等の権利を行使できます。プライバシー担当窓口 {{DPO_EMAIL}} までご連絡ください（{{DPO_RESPONSE_DAYS}} 営業日以内に回答）。

## 9. 第三者 SDK

本サービスが使用する第三者 SDK の詳細は**《サードパーティ SDK 一覧》**（コード：`THIRD_PARTY_SDK`）をご参照ください。

## 10. 未成年者の保護

本サービスは **{{MIN_AGE}} 歳以上**の方を対象としています。{{MIN_AGE}} 歳未満の方は、保護者の同意のもとご利用ください。

## 11. 本ポリシーの変更

重要な変更については、アプリ内ポップアップやメール等の顕著な方法でお知らせします。機微情報の処理、共有先、越境移転に関する重要な変更については、お客様の**改めての明示的な同意**を経て該当機能を提供します。

## 12. お問い合わせ

- 運営会社：{{COMPANY_NAME}}
- プライバシー担当窓口：{{DPO_EMAIL}}
- カスタマーサポート：{{SUPPORT_EMAIL}}

---

## 更新履歴

### 1.1.0（{{LAST_UPDATED}}）

- Firebase Analytics（Google LLC）による利用状況の統計分析を導入
- Firebase Crashlytics（Google LLC）によるクラッシュレポートの収集を導入
- 関連するプライバシーポリシーおよびサードパーティ SDK 一覧を更新

### 1.0.0

- 初版を公開
$body$::text AS content
)
INSERT INTO crawl.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);


-- ---- crawl: PRIVACY_POLICY / en / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'PRIVACY_POLICY'::varchar AS code,
    'en'::varchar AS language,
    'Privacy Policy'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} Privacy Policy

**Version**: 1.1.0

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

### 2.2 Information Collected Automatically

| Type | Items | Purpose |
|---|---|---|
| Device | Device model, OS and version, `deviceId` (generated locally), app version | Anti-fraud, refresh-token binding, compatibility |
| Network | IP address, connection type | Anti-fraud, API routing |
| Logs | API access timestamps, key operation logs | Troubleshooting and product improvement |
| Location (**only with your permission**) | GPS / network coordinates from `geolocator`; reverse geocoding via `geocoding` | Nearby cinema recommendations |
| Photos / Camera (**only when you actively choose**) | Images you select or capture | Avatar, support attachments, review images |
| Local storage | `accessToken` / `refreshToken` / `deviceId` (encrypted via `flutter_secure_storage`); preferences via `shared_preferences` | Stays on the device only |
| **Firebase Analytics** (Google LLC) | Screen-view events, custom event names and parameters (movie ID, purchase flow ID, etc.), Firebase instance ID, device OS / model / app version | Service improvement, usage statistics, purchase funnel optimisation |
| **Firebase Crashlytics** (Google LLC) | Crash stack traces, device OS / model / app version, Firebase instance ID | Crash detection, analysis, and quality improvement |

> The current build **does not integrate** Firebase Cloud Messaging / APNs push SDKs. We will update this Policy and the Third-party SDK List, and request your renewed consent, when push SDKs are introduced.

### 2.3 Information We Do Not Collect

- Full bank card numbers, CVV, or expiry are **not transmitted to our servers**.
- We **do not** scan your photo library, contacts, SMS, or clipboard.
- Firebase Analytics does **not intentionally transmit** your name, email, or phone number (we may link a user ID to Firebase after login; see Section 4).

## 3. How We Use Your Information

1. Account registration, login, and identity verification.
2. Order fulfilment: order placement, payment relay, ticket issuance, entry verification, refunds / changes, and customer support.
3. Security: account anti-fraud, anti-scalping, risk audit.
4. Service improvement: statistics and feature optimisation using **anonymised or pseudonymised** data (including Firebase Analytics).
5. Quality improvement: crash report analysis and fixes (Firebase Crashlytics).
6. Marketing notifications (only if you have not turned off push notifications).
7. Performance of legal obligations.

## 4. Sharing, Processors, and Disclosure

We **do not sell** your personal information to unrelated third parties.

### 4.1 Sharing with Cinemas / Distributors

For order fulfilment and entry verification, we share order number, showtime, seat, electronic pickup code, and where necessary the last four digits of name / phone with the relevant cinema.

### 4.2 Sharing with Payment Providers (**not yet active**)

When online payment is introduced, we will share necessary information with the third-party payment provider.

### 4.3 Processors (acting on our instructions)

- Cloud services: {{CLOUD_PROVIDERS}}
- Customer support / Email / SMS: {{COMMS_PROVIDERS}}
- **Firebase / Google LLC (United States)**: usage statistics via Firebase Analytics, crash report collection and analysis via Firebase Crashlytics. Google processes data in accordance with the [Google Privacy Policy](https://policies.google.com/privacy). Firebase instance IDs and (after login) user IDs may be sent to Google servers (primarily in the United States).

### 4.4 Third-Party Login

When you use Google / Apple Sign-In, the relevant platform returns the necessary account identifier, email, nickname, and avatar to us.

### 4.5 Disclosure Required by Law

We may disclose information when required by laws, courts, or administrative authorities.

## 5. Cross-Border Transfers

Your personal information is stored on servers in {{STORAGE_LOCATIONS}}. Firebase Analytics / Crashlytics data may be sent to and stored on **Google LLC (United States)** servers. We adopt necessary protection measures (e.g. Standard Contractual Clauses) under applicable laws.

## 6. Storage and Security

We employ TLS encryption, salted hashing, role-based access control, operation logging, and vulnerability monitoring. Security incidents will be reported as required by law.

## 7. Retention Periods

| Category | Retention period |
|---|---|
| Account information | Deleted or anonymised within {{ACCOUNT_RETENTION_DAYS}} days after account closure. |
| Order and transaction records | Retained for **at least 7 years** under Japan's Electronic Books Preservation Act. |
| Login and operation logs | {{LOG_RETENTION_DAYS}} days. |
| Firebase Analytics events | Up to 14 months per our Google data retention settings. |
| Firebase Crashlytics reports | 90 days (Google default). |
| Local Tokens | Cleared on logout or app uninstallation. |

## 8. Your Rights

You may exercise access, correction, deletion, and consent withdrawal rights. Contact our privacy team at {{DPO_EMAIL}} (response within {{DPO_RESPONSE_DAYS}} business days).

## 9. Third-Party SDKs

See the **Third-party SDK List** (code: `THIRD_PARTY_SDK`) for details.

## 10. Protection of Minors

The Service is intended for users aged **{{MIN_AGE}} or above**.

## 11. Changes to This Policy

Material changes will be notified prominently. Changes involving sensitive personal information processing, sharing, or cross-border transfers will only take effect after we obtain your **renewed explicit consent**.

## 12. Contact Us

- Operator: {{COMPANY_NAME}}
- Privacy contact: {{DPO_EMAIL}}
- Customer Support: {{SUPPORT_EMAIL}}

---

## Changelog

### 1.1.0 ({{LAST_UPDATED}})

- Integrated Firebase Analytics (Google LLC) for usage statistics
- Integrated Firebase Crashlytics (Google LLC) for crash reporting
- Updated Privacy Policy and Third-party SDK List accordingly

### 1.0.0

- Initial release
$body$::text AS content
)
INSERT INTO crawl.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);


-- ---- crawl: PRIVACY_POLICY / zh / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'PRIVACY_POLICY'::varchar AS code,
    'zh'::varchar AS language,
    '隐私政策'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    TRUE AS is_required_accept,
    $body$
# {{APP_NAME}} 隐私政策

**版本**：1.1.0

**生效日期**：{{EFFECTIVE_DATE}}

**最后更新**：{{LAST_UPDATED}}

{{COMPANY_NAME}}（以下简称「我们」）将保护您的个人信息视为核心责任之一。本隐私政策说明您使用 {{APP_NAME}}（以下简称「本服务」）时，**我们收集哪些信息、为何收集、如何使用/共享/保存，以及您可行使哪些权利**。

使用本服务前请仔细阅读本政策。继续使用即视为您同意我们按本政策处理个人信息。

---

## 1. 关于我们

- 个人信息处理者：{{COMPANY_NAME}}
- 注册地址：{{COMPANY_ADDRESS}}
- 隐私联系：{{DPO_EMAIL}}

## 2. 我们收集的信息

### 2.1 您主动提供的信息

| 场景 | 信息项 | 是否必需 |
|---|---|---|
| 邮箱注册 | 邮箱、密码（加密存储）、昵称 | 必需 |
| Google / Apple 登录 | OpenID（`sub`）、邮箱、昵称、头像 URL | 必需 |
| 资料编辑 | 头像、昵称、性别、出生日期 | 可选 |
| 下单/取票 | 联系方式、电影、场次、座位、订单金额 | 必需 |
| 评论/客服 | 文字、图片、视频 | 可选 |

### 2.2 使用服务时自动收集的信息

| 类型 | 信息项 | 用途 |
|---|---|---|
| 设备 | 型号、OS 及版本、本地生成的 `deviceId`、应用版本 | 反欺诈、Token 绑定、兼容性 |
| 网络 | IP 地址、连接类型 | 反欺诈、API 路由 |
| 日志 | API 访问时间、关键操作记录 | 故障排查、产品改进 |
| 位置（**经您授权**） | `geolocator` 经纬度；`geocoding` 反查城市 | 附近影院推荐 |
| 照片/相机（**您主动选择时**） | 您选择或拍摄的图片 | 头像、客服附件、评论图片 |
| 本地存储 | `accessToken`/`refreshToken`/`deviceId`（`flutter_secure_storage` 加密）；语言等偏好（`shared_preferences`） | 仅保存在设备本地 |
| **Firebase Analytics**（Google LLC） | 页面浏览事件、自定义事件名与参数（电影 ID、购买流程 ID 等）、Firebase 实例 ID、设备 OS/型号/应用版本 | 使用统计、购买漏斗优化、产品改进 |
| **Firebase Crashlytics**（Google LLC） | 崩溃堆栈、设备 OS/型号/应用版本、Firebase 实例 ID | 崩溃检测、分析与质量改进 |

> 当前版本**未接入** Firebase Cloud Messaging / APNs 等推送 SDK。未来接入时将更新本政策与《第三方 SDK 一览》并重新征得同意。

### 2.3 我们不收集的信息

- 完整银行卡号、CVV、有效期等**不经我们的服务器**。
- **不会**扫描您的相册、通讯录、短信或剪贴板。
- Firebase Analytics **不会故意发送**您的姓名、邮箱、电话等直接标识符（登录后可能向 Firebase 关联用户 ID，见第 4 节）。

## 3. 使用目的

1. 账号注册、登录与身份验证；
2. 订单履行：下单、支付、出票、入场核验、退改与客服；
3. 安全：反欺诈、反黄牛、风险审计；
4. 产品改进：基于**匿名化/假名化**数据的统计与优化（含 Firebase Analytics）；
5. 质量提升：崩溃报告分析与修复（Firebase Crashlytics）；
6. 营销通知（在您未关闭推送时）；
7. 履行法定义务。

## 4. 共享、委托与披露

我们**不会**向无关第三方出售您的个人信息。

### 4.1 与影院/发行方共享

为履约与入场核验，向相关影院共享订单号、场次、座位、电子取票码及必要联系信息。

### 4.2 与支付机构共享（**当前未上线在线支付**）

未来上线时将共享订单号、金额、支付方式标识等必要信息。

### 4.3 委托处理

- 云服务：{{CLOUD_PROVIDERS}}
- 客服/邮件/SMS：{{COMMS_PROVIDERS}}
- **Firebase / Google LLC（美国）**：Firebase Analytics 使用统计、Firebase Crashlytics 崩溃收集与分析。Google 按 [Google 隐私政策](https://policies.google.com/privacy) 处理数据。Firebase 实例 ID 及（登录后的）用户 ID 可能发送至 Google 服务器（主要在美国）。

### 4.4 第三方登录

使用 Google / Apple 登录时，相应平台会向我们在必要范围内提供账号标识、邮箱、昵称、头像。

### 4.5 依法披露

应法律、司法或行政机关合法要求时，在必要范围内披露。

## 5. 跨境传输

个人信息保存在 {{STORAGE_LOCATIONS}}。Firebase Analytics / Crashlytics 数据可能传输至并保存在 **Google LLC（美国）** 服务器。我们将按适用法律采取标准合同条款（SCC）等必要保护措施。

## 6. 存储与安全

采用 TLS 加密、加盐哈希、基于角色的访问控制、操作日志与漏洞监测等安全措施。发生安全事件将依法通知。

## 7. 保存期限

| 信息类型 | 保存期限 |
|---|---|
| 账号信息 | 注销后 {{ACCOUNT_RETENTION_DAYS}} 日内删除或匿名化 |
| 订单/交易记录 | 依日本《电子账簿保存法》**至少 7 年** |
| 登录/操作日志 | {{LOG_RETENTION_DAYS}} 日 |
| Firebase Analytics 事件 | 依 Google 保留设置，最长 14 个月 |
| Firebase Crashlytics 报告 | 90 日（Google 默认） |
| 本地 Token | 退出登录或卸载应用后清除 |

## 8. 您的权利

您可依法行使查阅、更正、删除、撤回同意等权利。请联系 {{DPO_EMAIL}}（{{DPO_RESPONSE_DAYS}} 个工作日内回复）。

## 9. 第三方 SDK

详见**《第三方 SDK 一览》**（代码：`THIRD_PARTY_SDK`）。

## 10. 未成年人保护

本服务面向 **{{MIN_AGE}} 岁及以上**用户。未满 {{MIN_AGE}} 岁须在监护人同意下使用。

## 11. 政策变更

重大变更将通过应用内弹窗、邮件等显著方式告知。涉及敏感信息处理、共享或跨境传输的重大变更，将在取得您**重新明示同意**后生效。

## 12. 联系我们

- 运营方：{{COMPANY_NAME}}
- 隐私联系：{{DPO_EMAIL}}
- 客服：{{SUPPORT_EMAIL}}

---

## 更新日志

### 1.1.0（{{LAST_UPDATED}}）

- 接入 Firebase Analytics（Google LLC）用于使用情况统计
- 接入 Firebase Crashlytics（Google LLC）用于崩溃报告收集
- 同步更新隐私政策与第三方 SDK 一览

### 1.0.0

- 初始版本发布
$body$::text AS content
)
INSERT INTO crawl.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);


-- ---- crawl: THIRD_PARTY_SDK / ja / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'THIRD_PARTY_SDK'::varchar AS code,
    'ja'::varchar AS language,
    'サードパーティ SDK 一覧'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    FALSE AS is_required_accept,
    $body$
# {{APP_NAME}} サードパーティ SDK 一覧

**バージョン**：1.1.0

**施行日**：{{EFFECTIVE_DATE}}

**最終更新日**：{{LAST_UPDATED}}

{{APP_NAME}} のアカウント登録、ログイン、現在地に基づく劇場検索、アイコン／レビュー画像のアップロード、利用状況の統計分析、クラッシュレポートの収集等の機能を提供するため、本アプリは以下のサードパーティ SDK およびシステム機能を利用しています。

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
| `dio` | Flutter コミュニティ | バックエンド（`{{API_DOMAIN}}`）との HTTPS 通信 | リクエスト、Bearer Token、`deviceId` |
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
3. ご質問は {{DPO_EMAIL}} までお問い合わせください。

---

## 更新履歴

### 1.1.0（{{LAST_UPDATED}}）

- Firebase Analytics（Google LLC）による利用状況の統計分析を導入
- Firebase Crashlytics（Google LLC）によるクラッシュレポートの収集を導入
- 関連するプライバシーポリシーおよびサードパーティ SDK 一覧を更新

### 1.0.0

- 初版を公開
$body$::text AS content
)
INSERT INTO crawl.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);


-- ---- crawl: THIRD_PARTY_SDK / en / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'THIRD_PARTY_SDK'::varchar AS code,
    'en'::varchar AS language,
    'Third-party SDK List'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    FALSE AS is_required_accept,
    $body$
# {{APP_NAME}} Third-party SDK List

**Version**: 1.1.0

**Effective Date**: {{EFFECTIVE_DATE}}

**Last Updated**: {{LAST_UPDATED}}

To support account registration, login, location-based cinema discovery, avatar / review uploads, usage statistics, and crash reporting, the App integrates the third-party SDKs and system services listed below.

> This list is kept consistent with the actual app dependencies declared in `pubspec.yaml` and is updated alongside any addition or removal in subsequent releases.

---

## 1. Login and Authentication

| SDK / Service | Provider | Purpose | Information collected / used | Trigger | Privacy Policy |
|---|---|---|---|---|---|
| `google_sign_in` | Google LLC | Google account sign-in / sign-up | Google `sub`, email, nickname, avatar URL | Tap "Sign in with Google" | https://policies.google.com/privacy |
| `sign_in_with_apple` | Apple Inc. | Apple ID sign-in (iOS only) | Apple `sub`, email, nickname | Tap "Sign in with Apple" on iOS | https://www.apple.com/legal/privacy/ |
| `flutter_appauth` | Maks O. (OSS) | OAuth 2.0 PKCE login via X | Temporary parameters for authorization code exchange | Tap "Sign in with X" | https://x.com/privacy |

---

## 2. Analytics and Crash Monitoring

| SDK / Service | Provider | Purpose | Information collected / used | Trigger | Privacy Policy |
|---|---|---|---|---|---|
| `firebase_analytics` | Google LLC | Screen-view and custom event statistics, purchase funnel optimisation | Event names and parameters, Firebase instance ID, device OS / model / app version; user ID linked after login | App launch, navigation, key actions (automatic) | https://policies.google.com/privacy |
| `firebase_crashlytics` | Google LLC | Crash detection, analysis, quality improvement | Crash stack traces, device OS / model / app version, Firebase instance ID | App crash (automatic) | https://policies.google.com/privacy |
| `firebase_core` | Google LLC | Firebase SDK initialisation foundation | Initialisation data shared with the above Firebase services | App launch | https://policies.google.com/privacy |

> Firebase data is processed on Google LLC (United States) servers. See Privacy Policy Sections 4.3 and 5 for details.

---

## 3. Device Capabilities and Location

| SDK / Service | Provider | Purpose | Information collected / used | Trigger | Privacy Policy |
|---|---|---|---|---|---|
| `geolocator` | Baseflow | Approximate device location | GPS / network coordinates (after user authorisation) | Nearby cinema recommendations | https://baseflow.com/privacy-statement/ |
| `geocoding` | Baseflow (OS-level) | Reverse geocoding | Coordinates only | City name display | Same as above |
| `image_picker` | Flutter official plugin | Pick / capture images | Image selected by you | Avatar change, review images | System privacy permissions |
| `image_editor` | Flutter community | Crop / rotate images | Selected image (local processing only) | Same as `image_picker` | Local processing only |

---

## 4. Local-Only Storage (does not leave your device)

| SDK / Service | Provider | Purpose | Stored information |
|---|---|---|---|
| `flutter_secure_storage` | Flutter community | Encrypted credential storage | `accessToken` / `refreshToken` / `deviceId` |
| `shared_preferences` | Flutter official plugin | Non-sensitive preferences | Language, home tab, onboarding flags, etc. |

---

## 5. Networking and Content Display

| SDK / Service | Provider | Purpose | Information involved |
|---|---|---|---|
| `dio` | Flutter community | HTTPS communication with our backend (`{{API_DOMAIN}}`) | Request payload, Bearer token, `deviceId` |
| `extended_image` | Flutter community | Image loading and disk caching | Local cache only |
| `flutter_markdown` | Flutter official plugin | Rendering Markdown agreement bodies | Local rendering only |
| `url_launcher` | Flutter official plugin | Opening external links in browser / mail app | Triggered only when you tap |
| `share_plus` | Flutter official plugin | System share sheet | Only when you choose to share |

---

## 6. Helper Tools (no personal data collected)

| SDK / Service | Purpose |
|---|---|
| `package_info_plus` | Read the app's own version number |
| `uuid` | Generate device-local `deviceId` |
| `crypto` | Local hash computations |
| `logger` / `pretty_dio_logger` | Debug logging only; release builds emit nothing |
| UI plugin suite | Rendering, animation, formatting. No network or personal data collection. |

---

## 7. Third-Party Services Not Currently Integrated

The following are **not currently integrated**. We will update this list and the relevant Privacy Policy when they are added.

- Push messaging (Firebase Cloud Messaging / APNs)
- Online payment (Stripe / PayPay / Apple Pay / Google Pay, etc.)
- Third-party map components (Google Maps / Apple MapKit JS)

---

## 8. Maintenance Notes

1. This list is curated based on the actual `pubspec.yaml` dependencies and kept in sync with app releases.
2. When adding any SDK that involves personal information collection, we will update this list and the relevant Privacy Policy, and request your renewed consent where required.
3. Questions: {{DPO_EMAIL}}.

---

## Changelog

### 1.1.0 ({{LAST_UPDATED}})

- Integrated Firebase Analytics (Google LLC) for usage statistics
- Integrated Firebase Crashlytics (Google LLC) for crash reporting
- Updated Privacy Policy and Third-party SDK List accordingly

### 1.0.0

- Initial release
$body$::text AS content
)
INSERT INTO crawl.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);


-- ---- crawl: THIRD_PARTY_SDK / zh / 1.1.0 -----------------------------------
WITH new_row AS (
  SELECT
    'THIRD_PARTY_SDK'::varchar AS code,
    'zh'::varchar AS language,
    '第三方 SDK 一览'::varchar AS title,
    '1.1.0'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    FALSE AS is_required_accept,
    $body$
# {{APP_NAME}} 第三方 SDK 一览

**版本**：1.1.0

**生效日期**：{{EFFECTIVE_DATE}}

**最后更新**：{{LAST_UPDATED}}

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
| `dio` | Flutter 社区 | 与后端（`{{API_DOMAIN}}`）HTTPS 通信 | 请求体、Bearer Token、`deviceId` |
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
3. 疑问请联系 {{DPO_EMAIL}}。

---

## 更新日志

### 1.1.0（{{LAST_UPDATED}}）

- 接入 Firebase Analytics（Google LLC）用于使用情况统计
- 接入 Firebase Crashlytics（Google LLC）用于崩溃报告收集
- 同步更新隐私政策与第三方 SDK 一览

### 1.0.0

- 初始版本发布
$body$::text AS content
)
INSERT INTO crawl.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);
