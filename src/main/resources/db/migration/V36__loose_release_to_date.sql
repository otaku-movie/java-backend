-- crawl.loose_release_to_date(text) -> date
--
-- 把 movie.start_date（varchar，App「上映日」展示字段）里的多种写法统一解析成一个
-- 可排序/可比较的「代表日」，供「即将上映」查询过滤(>= 今天)与排序使用。
--
-- 背景：start_date 现在既可能是精确日期，也可能是模糊的日式标签（`2026年秋` /
-- `2026年` / `2026年7月`），后者无法被 TO_DATE 直接解析。导入侧保留日式原文用于
-- 展示（前端显示「2026年秋」），这里把它折算成代表日（季节取中点月、年取 1/1、
-- 年+月取当月 1 日），仅用于排序与状态过滤，不改变展示文案。
--
-- 解析不出来（如 `近日公開`）返回 NULL，调用方按「未知 → 排最后但保留」处理。

CREATE OR REPLACE FUNCTION crawl.loose_release_to_date(s text)
RETURNS date
LANGUAGE plpgsql
IMMUTABLE
AS $func$
DECLARE
  v  text;
  y  int;
  mo int;
  d  int;
BEGIN
  IF s IS NULL THEN RETURN NULL; END IF;
  v := btrim(s);
  IF v = '' THEN RETURN NULL; END IF;

  -- ISO / 斜杠：完整、年月、年
  IF v ~ '^\d{4}-\d{2}-\d{2}' THEN
    RETURN to_date(substr(v, 1, 10), 'YYYY-MM-DD');
  END IF;
  IF v ~ '^\d{4}-\d{1,2}$' THEN
    RETURN to_date(v || '-01', 'YYYY-MM-DD');
  END IF;
  IF v ~ '^\d{4}/\d{1,2}/\d{1,2}' THEN
    RETURN to_date(substring(v from '^\d{4}/\d{1,2}/\d{1,2}'), 'YYYY/MM/DD');
  END IF;
  IF v ~ '^\d{4}/\d{1,2}' THEN
    RETURN to_date(substring(v from '^\d{4}/\d{1,2}') || '/01', 'YYYY/MM/DD');
  END IF;
  IF v ~ '^\d{4}$' THEN
    RETURN make_date(v::int, 1, 1);
  END IF;

  -- 以下只处理 `YYYY年...` 的日式写法
  IF v !~ '^\d{4}年' THEN RETURN NULL; END IF;
  y := substring(v from '^(\d{4})年')::int;

  -- YYYY年M月D日
  IF v ~ '^\d{4}年\d{1,2}月\d{1,2}日' THEN
    mo := substring(v from '年(\d{1,2})月')::int;
    d  := substring(v from '月(\d{1,2})日')::int;
    RETURN make_date(y, mo, d);
  END IF;

  -- YYYY年M月（无日）→ 当月 1 日
  IF v ~ '^\d{4}年\d{1,2}月' THEN
    mo := substring(v from '年(\d{1,2})月')::int;
    RETURN make_date(y, mo, 1);
  END IF;

  -- YYYY年 + 季节/时期 → 代表月 1 日
  IF v ~ '正月|年始|年明け'          THEN RETURN make_date(y, 1, 1);  END IF;
  IF v ~ '初春|早春'                  THEN RETURN make_date(y, 2, 1);  END IF;
  IF v ~ 'ゴールデンウィーク|ＧＷ|GW' THEN RETURN make_date(y, 5, 1);  END IF;
  IF v ~ '春'                         THEN RETURN make_date(y, 4, 1);  END IF;
  IF v ~ '初夏|梅雨'                  THEN RETURN make_date(y, 6, 1);  END IF;
  IF v ~ 'お盆'                       THEN RETURN make_date(y, 8, 1);  END IF;
  IF v ~ '盛夏|真夏|夏'               THEN RETURN make_date(y, 7, 1);  END IF;
  IF v ~ '初秋'                       THEN RETURN make_date(y, 9, 1);  END IF;
  IF v ~ '晩秋'                       THEN RETURN make_date(y, 11, 1); END IF;
  IF v ~ '秋'                         THEN RETURN make_date(y, 10, 1); END IF;
  IF v ~ '初冬|年末'                  THEN RETURN make_date(y, 12, 1); END IF;
  IF v ~ '冬'                         THEN RETURN make_date(y, 12, 1); END IF;

  -- 只有 YYYY年 → 当年 1/1
  RETURN make_date(y, 1, 1);
EXCEPTION
  WHEN others THEN
    RETURN NULL;
END;
$func$;
