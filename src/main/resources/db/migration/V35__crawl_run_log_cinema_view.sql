DROP VIEW IF EXISTS crawl.v_crawl_run_log_cinema;
CREATE VIEW crawl.v_crawl_run_log_cinema AS
SELECT
  r.id AS run_id,
  r.kind,
  r.scope,
  r.tasks,
  CASE r.status
    WHEN 1 THEN 'running'
    WHEN 2 THEN 'success'
    WHEN 3 THEN 'failed'
    WHEN 4 THEN 'partial'
    ELSE 'unknown'
  END AS status_text,
  r.started_at,
  r.finished_at,
  c.key AS cinema_key,
  c.value ->> 'brand' AS brand,
  c.value ->> 'external_id' AS cinema_external_id,
  c.value ->> 'name' AS cinema_name,
  COALESCE((c.value ->> 'ok')::integer, 0) AS ok_count,
  COALESCE((c.value ->> 'fail')::integer, 0) AS fail_count,
  COALESCE((c.value ->> 'skipped')::integer, 0) AS skipped_count,
  c.value ->> 'date_from' AS date_from,
  c.value ->> 'date_to' AS date_to
FROM crawl.crawl_run_log r
CROSS JOIN LATERAL jsonb_each(COALESCE(r.summary_json -> 'by_cinema', '{}'::jsonb)) AS c
WHERE r.summary_json ? 'by_cinema';

COMMENT ON VIEW crawl.v_crawl_run_log_cinema IS
  '爬虫导入日志按影院展开的查询视图；用于查看每家影院本次 schedule 导入成功/失败/跳过数量及 date_from/date_to';
