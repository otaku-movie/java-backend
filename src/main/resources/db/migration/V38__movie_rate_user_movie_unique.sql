-- 每用户对每部电影仅允许一条评分记录
CREATE UNIQUE INDEX IF NOT EXISTS uk_movie_rate_user_movie
    ON movie_rate (user_id, movie_id);
