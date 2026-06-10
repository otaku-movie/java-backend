-- ============================================================
-- V52: 自托管图片地址去域名化（存相对路径，不存完整链接）
--
-- 约定：数据库统一存「相对路径」（/images/...，不含域名），完整 URL 由各端
-- 按各自 imageBase 拼接（App CustomExtendedImage / admin getURL /
-- movie-web getImageUrl），与历史 users.cover=`/2024-.../x.png` 一致。
--
-- 本迁移把已镜像到 Cloudflare R2、之前写成完整 `https://images.otaku-movie.com/...`
-- 的字段统一改回相对路径。仅命中该域名的值会被改写；第三方外链（TMDb /
-- moviewalker 等）与 presale.source_url（官网购买链接）不受影响。
--
-- 幂等：相对路径不含该域名，重复执行不会再变化。
-- ============================================================

SET timezone = 'Asia/Tokyo';

-- 电影封面（cover / cover_url 均可能被 mirror:images 写过）
UPDATE crawl.movie
   SET cover = regexp_replace(cover, '^https?://images\.otaku-movie\.com', '')
 WHERE cover ~ '^https?://images\.otaku-movie\.com';
UPDATE crawl.movie
   SET cover_url = regexp_replace(cover_url, '^https?://images\.otaku-movie\.com', '')
 WHERE cover_url ~ '^https?://images\.otaku-movie\.com';

-- 演职员头像
UPDATE crawl.staff
   SET cover = regexp_replace(cover, '^https?://images\.otaku-movie\.com', '')
 WHERE cover ~ '^https?://images\.otaku-movie\.com';

-- 预售券封面
UPDATE crawl.presale
   SET cover = regexp_replace(cover, '^https?://images\.otaku-movie\.com', '')
 WHERE cover ~ '^https?://images\.otaku-movie\.com';

-- 预售券图集（数组，保序改写）
UPDATE crawl.presale p
   SET gallery = sub.arr
  FROM (
    SELECT id,
           array_agg(regexp_replace(elem, '^https?://images\.otaku-movie\.com', '') ORDER BY ord) AS arr
      FROM crawl.presale, unnest(gallery) WITH ORDINALITY AS t(elem, ord)
     GROUP BY id
  ) sub
 WHERE p.id = sub.id
   AND array_to_string(p.gallery, ',') ~ 'https?://images\.otaku-movie\.com';

-- 规格图集（数组，保序改写）
UPDATE crawl.presale_specification s
   SET images = sub.arr
  FROM (
    SELECT id,
           array_agg(regexp_replace(elem, '^https?://images\.otaku-movie\.com', '') ORDER BY ord) AS arr
      FROM crawl.presale_specification, unnest(images) WITH ORDINALITY AS t(elem, ord)
     GROUP BY id
  ) sub
 WHERE s.id = sub.id
   AND array_to_string(s.images, ',') ~ 'https?://images\.otaku-movie\.com';

-- 规格特典图集（数组，保序改写）
UPDATE crawl.presale_specification s
   SET bonus_images = sub.arr
  FROM (
    SELECT id,
           array_agg(regexp_replace(elem, '^https?://images\.otaku-movie\.com', '') ORDER BY ord) AS arr
      FROM crawl.presale_specification, unnest(bonus_images) WITH ORDINALITY AS t(elem, ord)
     GROUP BY id
  ) sub
 WHERE s.id = sub.id
   AND array_to_string(s.bonus_images, ',') ~ 'https?://images\.otaku-movie\.com';
