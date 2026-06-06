#!/usr/bin/env python3
"""Export agreement docs as local Markdown previews (placeholders resolved).

复用 gen_v39_agreement.py 中的文案与排版逻辑，将占位符替换为与 App 端
_resolvePlaceholders 一致的默认值，导出到 scripts/preview/ 供本地预览。
不写数据库、不生成迁移。
"""
import datetime
import os

import gen_v39_agreement as g

OUT_DIR = os.path.join(os.path.dirname(__file__), "preview")

TODAY = datetime.date.today().strftime("%Y/%m/%d")

# 与 app/lib/pages/agreement/agreement_page.dart 的 _resolvePlaceholders 对齐
COMMON = {
    "APP_NAME": "シネコ",
    "EFFECTIVE_DATE": TODAY,
    "LAST_UPDATED": TODAY,
    "JURISDICTION_COURT": "東京地方裁判所",
    "SUPPORT_EMAIL": "support@cineko.app",
    "SUPPORT_PHONE": "03-0000-0000",
    "DPO_EMAIL": "privacy@cineko.app",
    "DPO_RESPONSE_DAYS": "15",
    "REFUND_HOURS": "1",
    "ACCOUNT_RETENTION_DAYS": "30",
    "LOG_RETENTION_DAYS": "180",
    "SUPPORT_RETENTION_DAYS": "365",
    "MIN_AGE": "13",
    "COMMS_PROVIDERS": "SendGrid / Twilio",
    "API_DOMAIN": "api.cineko.app",
}

LOCALIZED = {
    "zh": {
        "COMPANY_NAME": "シネコ 运营团队",
        "COMPANY_ADDRESS": "日本国东京都（运营信息确定后更新）",
        "SUPPORT_HOURS": "工作日 10:00 - 18:00 (JST)",
        "CLOUD_PROVIDERS": "AWS（东京区域）",
        "OVERSEAS_LOCATIONS": "日本境外（包括美国、欧洲）",
        "STORAGE_LOCATIONS": "日本（东京区域）",
    },
    "en": {
        "COMPANY_NAME": "Cineko Operations Team",
        "COMPANY_ADDRESS": "Tokyo, Japan (to be updated after confirmation)",
        "SUPPORT_HOURS": "Weekdays 10:00 - 18:00 (JST)",
        "CLOUD_PROVIDERS": "AWS (Tokyo Region)",
        "OVERSEAS_LOCATIONS": "outside Japan (including the US and Europe)",
        "STORAGE_LOCATIONS": "Japan (Tokyo Region)",
    },
    "ja": {
        "COMPANY_NAME": "シネコ 運営チーム",
        "COMPANY_ADDRESS": "日本国東京都（運営情報の確定後に更新）",
        "SUPPORT_HOURS": "平日 10:00 - 18:00 (JST)",
        "CLOUD_PROVIDERS": "AWS（東京リージョン）",
        "OVERSEAS_LOCATIONS": "日本国外（米国・欧州を含む）",
        "STORAGE_LOCATIONS": "日本（東京リージョン）",
    },
}


def resolve(text: str, lang: str) -> str:
    mapping = {**COMMON, **LOCALIZED[lang]}
    out = text
    for key, value in mapping.items():
        out = out.replace("{{" + key + "}}", value)
    return out


def main() -> None:
    os.makedirs(OUT_DIR, exist_ok=True)
    written = []
    for code, lang, title, content, _required in g.AGREEMENTS:
        doc = g.format_doc(content, g.CHANGELOGS[lang])
        doc = resolve(doc, lang)
        fname = f"{code}_{lang}.md"
        path = os.path.join(OUT_DIR, fname)
        with open(path, "w", encoding="utf-8") as f:
            f.write(doc)
        written.append(fname)
    print(f"Wrote {len(written)} files to {OUT_DIR}:")
    for name in written:
        print("  -", name)


if __name__ == "__main__":
    main()
