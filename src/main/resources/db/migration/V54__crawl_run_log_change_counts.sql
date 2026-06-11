-- crawl_run_log 增加通用变更明细列：新增 / 更新 / 未变化。
-- 背景：ok_count 只是“处理成功总数”，无法区分本轮到底新增了多少、
-- 更新了多少、多少完全没变。这里提升为正式列，便于直接查询。

ALTER TABLE crawl.crawl_run_log
  ADD COLUMN IF NOT EXISTS inserted_count  INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS updated_count   INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS unchanged_count INTEGER NOT NULL DEFAULT 0;

COMMENT ON COLUMN crawl.crawl_run_log.inserted_count IS
  '本轮新插入的行数。单位随 tasks 不同：schedule=场次 / cinema=影院 / movie=电影 / mubitike=ムビチケ预售';
COMMENT ON COLUMN crawl.crawl_run_log.updated_count IS
  '本轮已有且发生变化的行数。schedule 会精确跳过未变化；mubitike 通过导入前后指纹对比统计';
COMMENT ON COLUMN crawl.crawl_run_log.unchanged_count IS
  '本轮已有且数据一致的行数。schedule / mubitike 精确统计；其它任务按实现能力填写';

-- 重建排查视图，把三列前置展示。
DROP VIEW IF EXISTS crawl.v_crawl_run_log;
CREATE VIEW crawl.v_crawl_run_log AS
SELECT
  id,
  kind,
  scope,
  tasks,
  CASE status
    WHEN 1 THEN 'running'
    WHEN 2 THEN 'success'
    WHEN 3 THEN 'failed'
    WHEN 4 THEN 'partial'
    ELSE 'unknown'
  END AS status_text,
  status,
  started_at,
  finished_at,
  duration_ms,
  ok_count,
  inserted_count,
  updated_count,
  unchanged_count,
  fail_count,
  skipped_count,
  total_count,
  summary_json -> 'by_task' AS by_task,
  summary_json -> 'by_brand' AS by_brand,
  summary_json -> 'by_cinema' AS by_cinema,
  failures_json,
  error_msg,
  host_name,
  create_time
FROM crawl.crawl_run_log;

COMMENT ON VIEW crawl.v_crawl_run_log IS
  '爬虫运行日志的排查视图：将任务类型、状态文本、新增/更新/未变化、按任务/品牌/影院汇总前置展示';
