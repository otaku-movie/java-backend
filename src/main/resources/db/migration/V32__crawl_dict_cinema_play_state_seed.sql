-- ============================================================
-- V32: seed `cinemaPlayState` dict into crawl.dict so the admin
--      dict API (`/dict/specify`, which the app reads from the
--      `crawl` schema via currentSchema=crawl) returns it.
--
-- Symptom fixed: 排期甘特图 / 详情里场次状态显示成原始数字（如 "3"），
-- 因为 crawl.dict 里只有 crawler 自带的 dimensionType / dubbingVersion /
-- crawlTicketStatus，缺少 cinemaPlayState，前端字典查不到只能回退 code。
--
-- cinemaPlayState.code 对应 movie_show_time.status：1=未開始 / 2=放映中 / 3=放映終了。
-- 名称最终由 i18n 按 locale 翻译，这里存的 JP 名仅作兜底。
--
-- 幂等：dict.code 与 dict_item.(dict_id, code) 已存在则跳过。
-- 注意：crawl.dict 会被 import:data TRUNCATE 后重建，importer 同步加了
--       cinemaPlayState 的重种逻辑（import-data-pg.ts auxDicts）。
-- ============================================================

SET timezone = 'Asia/Tokyo';

CREATE SCHEMA IF NOT EXISTS crawl;

-- ---- public.dict（绝大多数生产库已有，幂等兜底） -----------------------
INSERT INTO public.dict (name, code)
SELECT v.name, v.code
FROM (VALUES ('放映状態', 'cinemaPlayState')) AS v(name, code)
WHERE NOT EXISTS (
  SELECT 1 FROM public.dict d WHERE d.code = v.code
);

INSERT INTO public.dict_item (dict_id, name, description, code)
SELECT d.id, v.name, NULL, v.code
FROM public.dict d
JOIN (VALUES
        ('cinemaPlayState', '未開始',   1),
        ('cinemaPlayState', '放映中',   2),
        ('cinemaPlayState', '放映終了', 3)
     ) AS v(dict_code, name, code) ON v.dict_code = d.code
WHERE NOT EXISTS (
  SELECT 1 FROM public.dict_item di
   WHERE di.dict_id = d.id AND di.code = v.code
);

-- ---- crawl.dict（应用实际读取的 schema） ------------------------------
INSERT INTO crawl.dict (name, code)
SELECT v.name, v.code
FROM (VALUES ('放映状態', 'cinemaPlayState')) AS v(name, code)
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.dict d WHERE d.code = v.code
);

INSERT INTO crawl.dict_item (dict_id, name, description, code)
SELECT d.id, v.name, NULL, v.code
FROM crawl.dict d
JOIN (VALUES
        ('cinemaPlayState', '未開始',   1),
        ('cinemaPlayState', '放映中',   2),
        ('cinemaPlayState', '放映終了', 3)
     ) AS v(dict_code, name, code) ON v.dict_code = d.code
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.dict_item di
   WHERE di.dict_id = d.id AND di.code = v.code
);
