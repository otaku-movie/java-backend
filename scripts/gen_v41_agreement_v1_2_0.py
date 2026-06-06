#!/usr/bin/env python3
"""Generate V41__agreement_v1_2_0_no_payment.sql migration.

基于 V39 的 1.1.0 全文，做「信息聚合 / 当前不提供应用内在线支付」定位的
1.2.0 修订：

  * USER_TERMS：
      - 第 3 条补「数据来源 / 非官方声明 / 信息准确性」三条
      - 第 4、5 条改写为「当前不提供应用内在线支付，购票与支付由影院或
        第三方渠道完成；未来接入时更新协议」
      - 第 11.2 责任上限改为不依赖「订单支付金额」
      - 去掉占位的客服电话
  * PRIVACY_POLICY：弱化「应用内支付」措辞（接入后才包含支付/退款取次）
  * THIRD_PARTY_SDK：仅版本号同步（在线支付/推送本就列为未接入）

采用「导入 V39 文档字符串 + 精准 replace」的方式，避免重抄全文；任何一段
没有匹配到原文都会直接抛错，防止静默改错。
"""
import importlib.util
import os

HERE = os.path.dirname(__file__)

# --- 导入 V39 模块，复用其文档字符串与 format_doc -------------------------
_spec = importlib.util.spec_from_file_location(
    "gen_v39_agreement", os.path.join(HERE, "gen_v39_agreement.py")
)
v39 = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(v39)

OUT = os.path.join(
    HERE, "..", "src", "main", "resources", "db", "migration",
    "V41__agreement_v1_2_0_no_payment.sql",
)

NEW_VERSION = "1.2.0"

# --- 版本号元信息行（每种语言一处） ---------------------------------------
VER_BUMP = {
    "ja": ("**バージョン**：1.1.0", "**バージョン**：1.2.0"),
    "en": ("**Version**: 1.1.0", "**Version**: 1.2.0"),
    "zh": ("**版本**：1.1.0", "**版本**：1.2.0"),
}

# ===========================================================================
# USER_TERMS 替换
# ===========================================================================

TERMS_REPL = {
    "ja": [
        # 1.2 接受方式：去掉「注文の完了」
        (
            "1.2 アカウント登録、Google / Apple アカウントによるログイン、または注文の完了により、本規約に同意したものとみなします。",
            "1.2 アカウント登録、Google / Apple アカウントによるログイン、または本サービスの継続利用により、本規約に同意したものとみなします。",
        ),
        # 3.1 サービス内容：支払いは外部チャネルで行われることを明示
        (
            "3.1 本サービスは、映画情報の閲覧、劇場・上映回の検索、座席選択、オンラインチケット販売、電子発券、特典付与、注文管理、レビュー・評価、お知らせ通知などの機能を提供します。",
            "3.1 本サービスは映画情報を集約して提供するアプリであり、映画情報の閲覧、劇場・上映回の検索、座席選択、電子発券、注文管理、レビュー・評価、お知らせ通知などの機能を提供します。実際の支払い・購入の決済は、劇場または第三者の購入チャネルで行われます。",
        ),
        # 3.3 末尾に 3.4 / 3.5 / 3.6 を追加
        (
            "3.3 法令、提携先の変更、安全または運営上の必要に応じて、機能の追加・調整・一時停止・終了を行うことがあります。重要な変更は事前に合理的な方法でお知らせします。",
            "3.3 法令、提携先の変更、安全または運営上の必要に応じて、機能の追加・調整・一時停止・終了を行うことがあります。重要な変更は事前に合理的な方法でお知らせします。\n"
            "3.4 **データの出所**：本サービスで表示する映画情報、上映スケジュール、劇場・上映回等のデータは、各劇場の公式サイトおよび公開情報に基づくものであり、閲覧・参考の目的で提供されます。\n"
            "3.5 **非公式に関する表明**：本サービスは、いかなる劇場、配給会社、製作会社またはチケット販売プラットフォームの公式アプリでもありません。画面上で明示する場合を除き、これらの主体との公式な提携・許諾関係はありません。\n"
            "3.6 **情報の正確性**：上映スケジュール、価格、残席、上映時刻等は随時変動する可能性があり、当社はリアルタイムでの正確性を保証しません。最終的には劇場または実際の購入チャネルの表示が優先されます。",
        ),
        # 第 4 条・第 5 条をまとめて書き換え
        (
            """## 4. 注文・支払い・発券

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
5.3 返金は決済機関の処理に依存し、通常 1〜15 営業日程度を要します。""",
            """## 4. チケット購入と支払い

4.1 **本サービスは現在、アプリ内でのオンライン決済または直接の代金収受を行っていません**。チケットの購入および支払いは、劇場の窓口または第三者の購入チャネルで行っていただく必要があり、取引条件・価格・チケット規則は当該劇場またはチャネルが定めます。
4.2 外部チャネルを通じて購入された場合、当該取引はお客様と劇場／チャネルとの間で直接成立します。購入・支払い・入場等に関して生じた紛争は、当該主体との間で解決していただきます。
4.3 当社は価格や残席等の参考情報を表示することがありますが、これは申込みや取引の確約を構成するものではなく、最終的には劇場または購入チャネルの表示が優先されます。
4.4 将来、本サービスがアプリ内注文またはオンライン決済機能を導入する場合、本規約を更新し、支払い・返金に関する条項について別途お知らせのうえ同意を取得します。

## 5. 返金・変更

5.1 本サービスは現在アプリ内での代金収受を行っていないため、**購入後の返金・振替・変更は、該当する劇場または購入チャネルが定める規則に従って処理されます**。詳細は購入チャネルが公表する規則をご確認ください。
5.2 映画チケットは特定の上映回に対応する商品であることが多く、多くのチャネルでは任意のキャンセル・変更ができません。購入前に上映回を必ずご確認ください。
5.3 将来、本サービスがオンライン決済を導入した際は、本サービスの注文に適用される返金・変更規則をここに追記します。""",
        ),
        # 11.2 責任の上限：注文支払額に依存しない表現へ
        (
            "11.2 法令で許される最大限の範囲において、利用者が本サービスの利用により被った損害に対する当社の賠償責任の総額は、**当該注文について実際にお支払いいただいた金額を上限**とし、間接損害、付随的損害、懲罰的損害については一切責任を負いません。",
            "11.2 法令で許される最大限の範囲において、当社は、本サービスの利用に関連して生じた間接損害、付随的損害、懲罰的損害について責任を負いません。本サービスは無償の情報集約サービスであるため、当社の賠償責任は法令上強行的に定められる範囲を上限とします。",
        ),
        # 12.2：注文に関する文言を削除
        (
            "12.2 規約終了後も、終了前に発生した注文は本規約に従い履行されます。終了前に投稿されたユーザーコンテンツに関する利用許諾は、規約終了後も効力を維持します。",
            "12.2 終了前に投稿されたユーザーコンテンツに関する利用許諾は、規約終了後も効力を維持します。",
        ),
        # 連絡先：占位の電話番号を削除
        (
            "- カスタマーサポート：{{SUPPORT_EMAIL}}\n- お問い合わせ電話：{{SUPPORT_PHONE}}\n- 受付時間：{{SUPPORT_HOURS}}",
            "- カスタマーサポート：{{SUPPORT_EMAIL}}\n- 受付時間：{{SUPPORT_HOURS}}",
        ),
    ],
    "en": [
        (
            "1.2 Completing account registration, signing in via Google / Apple, or placing an order constitutes your acceptance of these Terms.",
            "1.2 Completing account registration, signing in via Google / Apple, or continuing to use the Service constitutes your acceptance of these Terms.",
        ),
        (
            "3.1 The Service provides movie information, theatre and showtime browsing, online seat selection and ticket purchase, electronic ticket codes, premiums, order management, ratings and reviews, and notifications.",
            "3.1 The Service is a movie information aggregation app that provides movie information, theatre and showtime browsing, online seat selection, electronic ticket codes, order management, ratings and reviews, and notifications. Actual payment and purchase settlement are completed at the cinema or via third-party purchase channels.",
        ),
        (
            "3.3 We may add, modify, suspend, or discontinue features as required by law, partner changes, security, or operational needs, with reasonable advance notice where practical.",
            "3.3 We may add, modify, suspend, or discontinue features as required by law, partner changes, security, or operational needs, with reasonable advance notice where practical.\n"
            "3.4 **Data Source**: Movie information, showtimes, and theatre / showtime data displayed in the Service are derived from cinemas' official websites and publicly available information, and are provided for browsing and reference only.\n"
            "3.5 **Non-Official Statement**: The Service is not the official app of any cinema, distributor, production company, or ticketing platform. Unless expressly stated on the relevant screen, there is no official partnership or authorization with such parties.\n"
            "3.6 **Accuracy**: Showtimes, prices, seat availability, and screening times may change at any time, and we do not guarantee real-time accuracy. The information displayed by the cinema or the actual purchase channel shall prevail.",
        ),
        (
            """## 4. Orders, Payments, and Ticketing

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
5.3 Refunds depend on the payment channel and typically arrive within 1 to 15 business days.""",
            """## 4. Ticket Purchase and Payment

4.1 **The Service does not currently provide in-app online payment or direct collection of funds.** Ticket purchase and payment must be completed at the cinema or via third-party purchase channels, and the relevant transaction terms, prices, and ticketing rules are determined by the applicable cinema or channel.
4.2 Where you purchase via an external channel, that transaction is formed directly between you and the cinema / channel, and any dispute regarding purchase, payment, or entry shall be resolved with the relevant party.
4.3 We may display reference information such as prices and seat availability, but this does not constitute an offer or a commitment to transact; the cinema or purchase channel shall prevail.
4.4 If the Service introduces in-app ordering or online payment in the future, we will update these Terms and separately notify you of, and obtain your consent to, the provisions concerning payment and refunds.

## 5. Refunds and Changes

5.1 As the Service does not currently collect payment in-app, **refunds, rescheduling, and changes after purchase are handled by the applicable cinema or purchase channel in accordance with its rules**. Please refer to the rules published by the purchase channel.
5.2 Movie tickets are usually tied to a specific showtime, and most channels do not allow arbitrary cancellation or change. Please confirm the showtime before purchasing.
5.3 When the Service introduces online payment in the future, refund and change rules applicable to Service orders will be added here.""",
        ),
        (
            "11.2 To the maximum extent permitted by law, our aggregate liability for any losses arising out of or relating to your use of the Service shall not exceed **the amount you actually paid for the relevant order**, and we shall not be liable for indirect, incidental, or punitive damages.",
            "11.2 To the maximum extent permitted by law, we shall not be liable for any indirect, incidental, or punitive damages arising out of or relating to your use of the Service. As the Service is a free information-aggregation service, our liability is limited to the extent mandatorily required by applicable law.",
        ),
        (
            "12.2 Upon termination, orders placed prior to termination shall continue to be governed by these Terms. The license you granted in respect of User Content shall survive termination.",
            "12.2 The license you granted in respect of User Content shall survive termination.",
        ),
        (
            "- Customer Support: {{SUPPORT_EMAIL}}\n- Support Phone: {{SUPPORT_PHONE}}\n- Support Hours: {{SUPPORT_HOURS}}",
            "- Customer Support: {{SUPPORT_EMAIL}}\n- Support Hours: {{SUPPORT_HOURS}}",
        ),
    ],
    "zh": [
        (
            "1.2 完成账号注册、通过 Google / Apple 登录或完成下单，即视为您接受本协议。",
            "1.2 完成账号注册、通过 Google / Apple 登录或继续使用本服务，即视为您接受本协议。",
        ),
        (
            "3.1 本服务提供电影信息浏览、影院与场次查询、在线选座购票、电子取票、特典发放、订单管理、评分评论及通知等功能。",
            "3.1 本服务是一款电影信息聚合应用，提供电影信息浏览、影院与场次查询、在线选座、电子取票、订单管理、评分评论及通知等功能；实际支付与购票结算由影院或第三方购票渠道完成。",
        ),
        (
            "3.3 我们可因法律、合作方变更、安全或运营需要调整、暂停或终止部分功能，重大变更将提前合理告知。",
            "3.3 我们可因法律、合作方变更、安全或运营需要调整、暂停或终止部分功能，重大变更将提前合理告知。\n"
            "3.4 **数据来源**：本服务展示的电影信息、排片、影院及场次等数据来源于各影院官方网站及公开信息，仅供查询与参考。\n"
            "3.5 **非官方声明**：本服务并非任何影院、发行方、制片方或票务平台的官方应用，除页面明确标注外，与上述主体不存在官方合作或授权关系。\n"
            "3.6 **信息准确性**：排片、价格、余票、放映时间等可能随时变化，我们不保证实时准确，最终以影院或实际购票渠道展示为准。",
        ),
        (
            """## 4. 下单、支付与取票

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
5.3 退款到账时间取决于支付渠道，通常 1～15 个工作日。""",
            """## 4. 购票与支付

4.1 **本服务当前不提供应用内在线支付或直接收款**。购票与支付需在影院现场或第三方购票渠道完成，相关交易条款、价格与票务规则由对应影院或渠道决定。
4.2 若您通过外部渠道购票，该交易由您与影院/渠道直接成立，因购票、支付、入场等产生的纠纷应与对应主体解决。
4.3 我们可能展示价格、余票等参考信息，但不构成要约或交易承诺，最终以影院或购票渠道为准。
4.4 未来如本服务接入应用内下单或在线支付功能，我们将更新本协议，并就涉及支付、退款的条款另行告知并征得同意。

## 5. 退款与变更

5.1 由于本服务当前不在应用内收款，**购票后的退款、改期与变更由对应影院或购票渠道按其规则处理**，请以购票渠道公布的规则为准。
5.2 电影票通常对应特定场次，多数渠道不支持随意取消或变更，请购票前务必确认场次信息。
5.3 未来本服务接入在线支付后，将在此补充适用于本服务订单的退款与变更规则。""",
        ),
        (
            "11.2 在法律允许的最大范围内，我们对您因使用本服务产生的损害赔偿责任总额以**该笔订单您实际支付金额为上限**，不对间接、附带或惩罚性损害负责。",
            "11.2 在法律允许的最大范围内，对于因使用本服务产生的损害，我们不承担间接、附带或惩罚性损害赔偿责任；由于本服务为免费的信息聚合服务，我们的赔偿责任以法律强制规定的范围为限。",
        ),
        (
            "12.2 终止前已产生的订单仍按本协议履行；终止前用户内容的授权在终止后仍然有效。",
            "12.2 终止前用户内容的授权在终止后仍然有效。",
        ),
        (
            "- 客服邮箱：{{SUPPORT_EMAIL}}\n- 客服电话：{{SUPPORT_PHONE}}\n- 服务时间：{{SUPPORT_HOURS}}",
            "- 客服邮箱：{{SUPPORT_EMAIL}}\n- 服务时间：{{SUPPORT_HOURS}}",
        ),
    ],
}

# ===========================================================================
# PRIVACY_POLICY 替换（弱化「应用内支付」措辞）
# ===========================================================================

PRIV_REPL = {
    "ja": [
        (
            "2. 注文の履行：注文受付、決済の取次、発券、入場時の認証、返金・変更、カスタマーサポート。",
            "2. 注文の履行：注文受付、発券、入場時の認証、カスタマーサポート（オンライン決済の導入後は、決済の取次・返金を含みます）。",
        ),
    ],
    "en": [
        (
            "2. Order fulfilment: order placement, payment relay, ticket issuance, entry verification, refunds / changes, and customer support.",
            "2. Order fulfilment: order placement, ticket issuance, entry verification, and customer support (including payment relay and refunds once online payment is introduced).",
        ),
    ],
    "zh": [
        (
            "2. 订单履行：下单、支付、出票、入场核验、退改与客服；",
            "2. 订单履行：下单、出票、入场核验与客服（接入在线支付后将包含支付与退款取次）；",
        ),
    ],
}

# ===========================================================================
# 1.2.0 更新日志（含 1.1.0 / 1.0.0 历史）
# ===========================================================================

CHANGELOG_JA = """\
---

## 更新履歴

### 1.2.0（{{LAST_UPDATED}}）

- 本サービスが映画情報集約アプリであることを明確化し、「データの出所」「非公式に関する表明」「情報の正確性」の条項を追加
- 現在アプリ内でのオンライン決済を行わず、購入・支払いは劇場または第三者チャネルで行われることを明確化
- 上記に合わせて返金および責任に関する条項を調整
- カスタマーサポート連絡先メールを統一

### 1.1.0

- Firebase Analytics（Google LLC）による利用状況の統計分析を導入
- Firebase Crashlytics（Google LLC）によるクラッシュレポートの収集を導入
- 関連するプライバシーポリシーおよびサードパーティ SDK 一覧を更新

### 1.0.0

- 初版を公開
"""

CHANGELOG_EN = """\
---

## Changelog

### 1.2.0 ({{LAST_UPDATED}})

- Clarified that the Service is a movie information aggregation app; added "Data Source", "Non-Official Statement", and "Accuracy" clauses
- Clarified that no in-app online payment is provided; purchase and payment are completed at the cinema or via third-party channels
- Adjusted refund and liability clauses accordingly
- Unified the customer support contact email

### 1.1.0

- Integrated Firebase Analytics (Google LLC) for usage statistics
- Integrated Firebase Crashlytics (Google LLC) for crash reporting
- Updated Privacy Policy and Third-party SDK List accordingly

### 1.0.0

- Initial release
"""

CHANGELOG_ZH = """\
---

## 更新日志

### 1.2.0（{{LAST_UPDATED}}）

- 明确本服务为电影信息聚合应用，新增「数据来源」「非官方声明」「信息准确性」条款
- 明确当前不提供应用内在线支付，购票与支付由影院或第三方渠道完成
- 据此调整退款与责任相关条款
- 统一客服联系邮箱

### 1.1.0

- 接入 Firebase Analytics（Google LLC）用于使用情况统计
- 接入 Firebase Crashlytics（Google LLC）用于崩溃报告收集
- 同步更新隐私政策与第三方 SDK 一览

### 1.0.0

- 初始版本发布
"""

CHANGELOGS = {"ja": CHANGELOG_JA, "en": CHANGELOG_EN, "zh": CHANGELOG_ZH}


# ===========================================================================
# 组装
# ===========================================================================

def apply_repls(text: str, repls):
    for old, new in repls:
        if old not in text:
            raise SystemExit(
                "[gen_v41] 替换失败：在文档中找不到以下片段，请核对 V39 原文是否变动：\n"
                + "-" * 60 + "\n" + old + "\n" + "-" * 60
            )
        text = text.replace(old, new, 1)
    return text


def bump_version(text: str, lang: str) -> str:
    old, new = VER_BUMP[lang]
    if old not in text:
        raise SystemExit(f"[gen_v41] 版本号行未找到（{lang}）：{old}")
    return text.replace(old, new, 1)


# code -> {lang: 源文档}
SRC = {
    "USER_TERMS": {
        "ja": v39.USER_TERMS_JA,
        "en": v39.USER_TERMS_EN,
        "zh": v39.USER_TERMS_ZH,
    },
    "PRIVACY_POLICY": {
        "ja": v39.PRIVACY_JA,
        "en": v39.PRIVACY_EN,
        "zh": v39.PRIVACY_ZH,
    },
    "THIRD_PARTY_SDK": {
        "ja": v39.SDK_JA,
        "en": v39.SDK_EN,
        "zh": v39.SDK_ZH,
    },
}

# (code, lang, title, is_required_accept)
META = [
    ("USER_TERMS", "ja", "利用規約", True),
    ("USER_TERMS", "en", "User Terms", True),
    ("USER_TERMS", "zh", "用户协议", True),
    ("PRIVACY_POLICY", "ja", "プライバシーポリシー", True),
    ("PRIVACY_POLICY", "en", "Privacy Policy", True),
    ("PRIVACY_POLICY", "zh", "隐私政策", True),
    ("THIRD_PARTY_SDK", "ja", "サードパーティ SDK 一覧", False),
    ("THIRD_PARTY_SDK", "en", "Third-party SDK List", False),
    ("THIRD_PARTY_SDK", "zh", "第三方 SDK 一览", False),
]


def build_body(code: str, lang: str) -> str:
    text = SRC[code][lang]
    text = bump_version(text, lang)
    if code == "USER_TERMS":
        text = apply_repls(text, TERMS_REPL[lang])
    elif code == "PRIVACY_POLICY":
        text = apply_repls(text, PRIV_REPL[lang])
    # THIRD_PARTY_SDK：仅版本号同步
    return v39.format_doc(text, CHANGELOGS[lang])


def sql_insert(schema, code, lang, title, content, required):
    req = "TRUE" if required else "FALSE"
    return f"""
-- ---- {schema}: {code} / {lang} / {NEW_VERSION} -----------------------------------
WITH new_row AS (
  SELECT
    '{code}'::varchar AS code,
    '{lang}'::varchar AS language,
    '{title}'::varchar AS title,
    '{NEW_VERSION}'::varchar AS version,
    'PUBLISHED'::varchar AS status,
    {req} AS is_required_accept,
    $body$
{content}$body$::text AS content
)
INSERT INTO {schema}.agreement
  (code, language, title, content, version, status,
   is_required_accept, effective_at, published_at,
   create_time, update_time, deleted)
SELECT n.code, n.language, n.title, n.content, n.version, n.status,
       n.is_required_accept, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM new_row n
WHERE NOT EXISTS (
  SELECT 1 FROM {schema}.agreement a
   WHERE a.code = n.code AND a.language = n.language
     AND a.version = n.version AND a.deleted = 0
);
"""


def main():
    parts = [
        """\
-- ============================================================
-- V41: agreement 1.2.0 — 信息聚合定位 / 当前不提供应用内在线支付
--
-- 变更摘要（相对 1.1.0）：
--   * USER_TERMS：
--       - 第 3 条新增「数据来源 / 非官方声明 / 信息准确性」
--       - 第 4、5 条改写为「当前不提供应用内在线支付，购票与支付由影院或
--         第三方渠道完成；未来接入时更新协议」
--       - 第 11.2 责任上限不再依赖订单支付金额
--       - 去掉占位客服电话
--   * PRIVACY_POLICY：弱化「应用内支付」措辞（接入后才含支付/退款取次）
--   * THIRD_PARTY_SDK：仅同步版本号
--
-- 版本策略：
--   * 新记录 version = 1.2.0, status = PUBLISHED
--   * 旧 1.1.0 标记为 ARCHIVED
--   * 同时写入 public.agreement 与 crawl.agreement，幂等
-- ============================================================

SET timezone = 'Asia/Tokyo';

-- ---- 1. 归档旧版 1.1.0 --------------------------------------------
UPDATE public.agreement
   SET status = 'ARCHIVED', update_time = CURRENT_TIMESTAMP
 WHERE deleted = 0 AND version = '1.1.0'
   AND code IN ('USER_TERMS', 'PRIVACY_POLICY', 'THIRD_PARTY_SDK');

UPDATE crawl.agreement
   SET status = 'ARCHIVED', update_time = CURRENT_TIMESTAMP
 WHERE deleted = 0 AND version = '1.1.0'
   AND code IN ('USER_TERMS', 'PRIVACY_POLICY', 'THIRD_PARTY_SDK');

""",
    ]

    for schema in ("public", "crawl"):
        for code, lang, title, required in META:
            body = build_body(code, lang)
            parts.append(sql_insert(schema, code, lang, title, body, required))

    out_path = os.path.normpath(OUT)
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(parts))
    print(f"Written {out_path} ({os.path.getsize(out_path)} bytes)")


if __name__ == "__main__":
    main()
