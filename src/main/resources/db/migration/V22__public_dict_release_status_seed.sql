-- ============================================================
-- V22: seed `releaseStatus` dict into `public.dict`
--
-- 历史遗留：V11 把 releaseStatus 的字典本体 + dict_item 写进了
-- crawl.dict（id=2, items code 1/2/3），并在注释里假定
-- "public 既有 id (2..35068)" 已存在；但实际首次部署的 public.dict
-- 是空的，导致 `/dict/specify` 拿不到 releaseStatus 字典——
-- app 端 MovieDetail 上映状态 Dict(name:'releaseStatus') 渲染为空。
--
-- 这里只往 public 补这一份；dict_item.name 留中文占位，
-- 接口侧会用 messages_*.yml 的 dict.releaseStatus.item.{1,2,3}
-- 覆盖翻译，不影响最终 UI 文本。
-- ============================================================

SET timezone = 'Asia/Tokyo';

INSERT INTO public.dict (name, code)
SELECT v.name, v.code
FROM (VALUES ('上映状态', 'releaseStatus')) AS v(name, code)
WHERE NOT EXISTS (
  SELECT 1 FROM public.dict d WHERE d.code = v.code
);

INSERT INTO public.dict_item (dict_id, name, description, code)
SELECT d.id, v.name, NULL, v.code
FROM public.dict d
JOIN (VALUES
        ('releaseStatus', '未上映',   1),
        ('releaseStatus', '上映中',   2),
        ('releaseStatus', '上映结束', 3)
     ) AS v(dict_code, name, code) ON v.dict_code = d.code
WHERE NOT EXISTS (
  SELECT 1 FROM public.dict_item di
   WHERE di.dict_id = d.id AND di.code = v.code
);
