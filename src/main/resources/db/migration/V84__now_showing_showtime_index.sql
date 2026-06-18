-- 正在上映列表：未来可售场次的 start_time 范围扫描索引。
-- start_time 为 ISO 格式 varchar（YYYY-MM-DD HH24:MI:SS），与 to_char(CURRENT_TIMESTAMP, ...) 比较可走 B-tree。
CREATE INDEX IF NOT EXISTS idx_mst_open_future_start
    ON crawl.movie_show_time (start_time)
    WHERE deleted = 0 AND open = TRUE;

COMMENT ON INDEX crawl.idx_mst_open_future_start IS
    'nowShowing / 热映列表：deleted=0 且 open 的未来场次按 start_time 过滤';
