-- 爬虫结构化产物（原 data/**/*.json）的 DB 存储，替代落盘。
-- artifact_path：相对 CRAWLER_DATA_ROOT 的正斜杠路径，如 toho/cinemas/.../schedule/2026-06-01.json

CREATE TABLE IF NOT EXISTS crawl.crawl_artifact (
  artifact_path TEXT         PRIMARY KEY,
  content       TEXT         NOT NULL,
  content_hash  CHAR(64)     NOT NULL,
  update_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_crawl_artifact_prefix
  ON crawl.crawl_artifact (artifact_path text_pattern_ops);

COMMENT ON TABLE crawl.crawl_artifact IS
  'Crawler JSON artifacts (schedule/info/movies/…); replaces data/**/*.json when CRAWLER_PERSIST_JSON=0';
