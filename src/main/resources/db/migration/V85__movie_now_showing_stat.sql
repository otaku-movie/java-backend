-- 正在上映列表预聚合：避免 nowShowing 每次请求扫描全量未来场次（~5 万行 / 12s+）。
CREATE TABLE IF NOT EXISTS crawl.movie_now_showing_stat (
    movie_id         INTEGER PRIMARY KEY,
    show_time_count  INTEGER      NOT NULL DEFAULT 0,
    next_show_time   TIMESTAMP    NOT NULL,
    refreshed_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mnss_rank
    ON crawl.movie_now_showing_stat (show_time_count DESC, next_show_time ASC);

COMMENT ON TABLE crawl.movie_now_showing_stat IS
    '正在上映热映榜预聚合；由后端定时任务刷新，供 /api/app/movie/nowShowing 读取';
