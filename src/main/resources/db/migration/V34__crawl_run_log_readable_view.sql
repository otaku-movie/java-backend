ALTER TABLE crawl.crawl_run_log
  ADD COLUMN IF NOT EXISTS tasks VARCHAR(255);

COMMENT ON TABLE crawl.crawl_run_log IS
  '爬虫批量作业执行留痕：每行 = 一次执行（crawl:all / import:data / crawl:prices / 定时器一轮 refresh|movies）；不被 import TRUNCATE 清空，用于回答最近何时跑、跑了什么、有没有失败';

COMMENT ON COLUMN crawl.crawl_run_log.id IS
  '主键';
COMMENT ON COLUMN crawl.crawl_run_log.kind IS
  '作业入口/来源（不是具体任务）：import=import:data，crawl_all=crawl:all，crawl_prices=crawl:prices。定时器一轮 refresh/movies 不写本表（只打日志）';
COMMENT ON COLUMN crawl.crawl_run_log.scope IS
  '范围：品牌或 all，如 all / tjoy / aeon,c109';
COMMENT ON COLUMN crawl.crawl_run_log.tasks IS
  '单个业务任务类型：cinema / movie / schedule / prices / posters 等。原则：一次执行一个任务、各落一条日志；import:data 按 cinema/movie/schedule 拆条，crawl:all 按抓取的各任务类型拆条；定时器一轮（refresh/movies）只打控制台日志、不写本表';
COMMENT ON COLUMN crawl.crawl_run_log.status IS
  '状态：1 运行中 / 2 成功 / 3 失败 / 4 部分成功';
COMMENT ON COLUMN crawl.crawl_run_log.started_at IS
  '开始时间';
COMMENT ON COLUMN crawl.crawl_run_log.finished_at IS
  '结束时间（运行中为 NULL）';
COMMENT ON COLUMN crawl.crawl_run_log.duration_ms IS
  '耗时（毫秒）= finished_at - started_at';
COMMENT ON COLUMN crawl.crawl_run_log.ok_count IS
  '成功计数。单位随 tasks 不同：import+schedule=场次数 / import+cinema=影院数 / import+movie=电影数 / crawl_prices=影院数 / crawl_all=抓取子任务数';
COMMENT ON COLUMN crawl.crawl_run_log.fail_count IS
  '失败计数，单位同 ok_count（随 kind 不同）';
COMMENT ON COLUMN crawl.crawl_run_log.skipped_count IS
  '跳过计数：如 crawl_all 中未实现任务、import 中被跳过的场次';
COMMENT ON COLUMN crawl.crawl_run_log.total_count IS
  '总计数 = ok + fail + skipped，单位随 kind 不同';
COMMENT ON COLUMN crawl.crawl_run_log.summary_json IS
  '概览聚合：crawl_all 含 by_brand / by_task；import schedule 含 by_cinema（每家影院 ok/fail/skipped/date_from/date_to）';
COMMENT ON COLUMN crawl.crawl_run_log.failures_json IS
  '失败明细数组 [{brand,task,cinema,error}]';
COMMENT ON COLUMN crawl.crawl_run_log.error_msg IS
  '整体 fatal 信息（非单任务失败）';
COMMENT ON COLUMN crawl.crawl_run_log.host_name IS
  '执行所在主机名';
COMMENT ON COLUMN crawl.crawl_run_log.create_time IS
  '记录创建时间';

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
  '爬虫运行日志的排查视图：将任务类型、状态文本、按任务/品牌/影院汇总前置展示';
