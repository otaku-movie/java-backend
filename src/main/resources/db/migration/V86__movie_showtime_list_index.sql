-- 场次列表：按 movie_id + 未来 start_time 过滤（status=1 未上映）。
-- 避免 getMovieShowTime 走 movie_id 索引后再对全量行做 start_time::timestamp 过滤。
CREATE INDEX IF NOT EXISTS idx_mst_movie_status_future_start
    ON crawl.movie_show_time (movie_id, start_time)
    WHERE deleted = 0 AND status = 1;

COMMENT ON INDEX crawl.idx_mst_movie_status_future_start IS
    '电影场次列表 / showTimeFilters：movie_id + 未来 start_time（varchar ISO）';
