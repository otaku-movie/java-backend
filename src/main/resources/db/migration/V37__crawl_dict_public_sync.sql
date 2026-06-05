-- ============================================================
-- V37: mirror public dict rows into crawl
--
-- `crawl.dict` is what the admin/app API reads when the backend runs with
-- currentSchema=crawl. The crawler importer can TRUNCATE crawl.dict and rebuild
-- only crawler-owned data, so keep all public dictionaries available here too.
--
-- Match by stable dict.code + dict_item.code instead of copying ids; crawl may
-- also contain crawler-private dictionaries such as crawlTicketStatus.
--
-- 只同步这些表上**实际存在**的列：
--   dict      → (name, code)
--   dict_item → (dict_id, name, code, description)
-- 不要引用 description/create_time/update_time/deleted 之类 dict 上并不存在的列。
-- ============================================================

SET timezone = 'Asia/Tokyo';

CREATE SCHEMA IF NOT EXISTS crawl;

INSERT INTO crawl.dict (name, code)
SELECT pd.name, pd.code
FROM public.dict pd
WHERE NOT EXISTS (
  SELECT 1
  FROM crawl.dict cd
  WHERE cd.code = pd.code
);

UPDATE crawl.dict cd
SET name = pd.name
FROM public.dict pd
WHERE cd.code = pd.code;

INSERT INTO crawl.dict_item (dict_id, name, code, description)
SELECT
  cd.id,
  pdi.name,
  pdi.code,
  pdi.description
FROM public.dict pd
JOIN public.dict_item pdi ON pdi.dict_id = pd.id
JOIN crawl.dict cd ON cd.code = pd.code
WHERE NOT EXISTS (
  SELECT 1
  FROM crawl.dict_item cdi
  WHERE cdi.dict_id = cd.id
    AND cdi.code = pdi.code
);

UPDATE crawl.dict_item cdi
SET
  name = pdi.name,
  description = pdi.description
FROM crawl.dict cd
JOIN public.dict pd ON pd.code = cd.code
JOIN public.dict_item pdi ON pdi.dict_id = pd.id
WHERE cdi.dict_id = cd.id
  AND cdi.code = pdi.code;
