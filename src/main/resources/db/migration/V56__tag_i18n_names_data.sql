-- ============================================================
-- V56: 预填标签译名（DML，与 V55 的加列 DDL 分离）
--
-- 背景：V55 早先只跑了「加列」DDL，译名 DML 后补进同一脚本时 Flyway 已记录 V55 为
-- 已应用、不会重跑，导致 name_zh/name_en 一直为空、App 始终回退日文。这里把数据预填
-- 独立成新版本，保证能真正执行。
--
-- 幂等：仅在译名为空时 UPDATE（不覆盖后台/手工维护的译名）；movie_tag 先 UPDATE 再
-- INSERT（WHERE NOT EXISTS）。爬虫只写 name，不会动这两列。
-- 注意：show_time_tag 的行由爬虫运行时创建——全新库执行本脚本时表可能为空，UPDATE 命中
-- 0 行属正常，未命中的新标签按设计回退日文，后续可在后台补译名。
-- 业务 datasource 使用 crawl schema，这里直接对 crawl.* 操作（与 V55 同口径）。
-- ============================================================

SET timezone = 'Asia/Tokyo';

-- ------------------------------------------------------------
-- 预填译名：show_time_tag（取自生产库现有标签）。
-- 仅在译名为空时写入，避免覆盖后续在后台/手工维护的译名。
-- ------------------------------------------------------------
UPDATE crawl.movie_show_time_tag AS t
SET name_zh = v.zh, name_en = v.en
FROM (VALUES
  ('応援上映', '应援放映', 'Cheering Screening'),
  ('発声OK', '可发声放映', 'Shout-OK Screening'),
  ('STAY Party', 'STAY Party', 'STAY Party'),
  ('公開記念舞台挨拶', '公映纪念舞台问候', 'Release Stage Greeting'),
  ('全国同時生中継', '全国同步直播', 'Nationwide Live Broadcast'),
  ('超限界突破上映', '超限界突破放映', 'Beyond-Limit Screening'),
  ('公開後スタッフトーク', '放映后主创交流', 'Post-Screening Staff Talk'),
  ('先行上映', '抢先放映', 'Advance Screening'),
  ('周年記念', '周年纪念', 'Anniversary'),
  ('おやこシネマ', '亲子影院', 'Parent-Child Cinema'),
  ('舞台挨拶', '舞台问候', 'Stage Greeting'),
  ('舞台挨拶中継付', '含舞台问候转播', 'With Stage Greeting Broadcast'),
  ('先行', '抢先场', 'Advance'),
  ('舞台挨拶中継', '舞台问候转播', 'Stage Greeting Broadcast'),
  ('ライブビューイング', '现场直播观影', 'Live Viewing'),
  ('初日舞台挨拶中継付', '含首日舞台问候转播', 'First-Day Stage Greeting w/ Broadcast'),
  ('初日舞台挨拶', '首日舞台问候', 'First-Day Stage Greeting'),
  ('初日舞台挨拶中継', '首日舞台问候转播', 'First-Day Stage Greeting Broadcast'),
  ('千秋楽', '闭幕场', 'Final Performance'),
  ('宝塚歌劇', '宝塚歌剧', 'Takarazuka Revue'),
  ('轟音上映', '轰音放映', 'Roaring Sound Screening'),
  ('パブリックビューイング', '公众直播观影', 'Public Viewing'),
  ('ライブフィルム上映', '现场影像放映', 'Live Film Screening'),
  ('発声可能上映', '可发声放映', 'Shout-Allowed Screening'),
  ('特別上映', '特别放映', 'Special Screening'),
  ('舞台挨拶付', '含舞台问候', 'With Stage Greeting'),
  ('映画館ライブ上映会', '影院现场放映会', 'Cinema Live Screening'),
  ('映画館大賞', '影院大奖', 'Cinema Award'),
  ('ライブ音響上映', '现场音响放映', 'Live Sound Screening'),
  ('ライブ音響', '现场音响', 'Live Sound'),
  ('ODS', 'ODS特别影像', 'ODS'),
  ('登壇イベント', '登台活动', 'Stage Appearance Event'),
  ('ライブ中継', '现场转播', 'Live Broadcast'),
  ('特別興行', '特别放映', 'Special Show'),
  ('レイトショー', '深夜场', 'Late Show'),
  ('特別先行上映', '特别抢先放映', 'Special Advance Screening'),
  ('トークイベント付き上映', '含座谈活动放映', 'Screening with Talk Event'),
  ('TXQ FICTION', 'TXQ FICTION', 'TXQ FICTION'),
  ('トークショー付き', '含脱口秀', 'With Talk Show'),
  ('同時応援上映', '同步应援放映', 'Simultaneous Cheering Screening'),
  ('期間限定', '限期放映', 'Limited Time'),
  ('再上映会', '重映会', 'Re-screening'),
  ('ZZZロードショー', 'ZZZ路演', 'ZZZ Roadshow'),
  ('BLシネマフェス', 'BL影展', 'BL Cinema Fest'),
  ('参考上映', '参考放映', 'Reference Screening'),
  ('スペシャルトークショー', '特别脱口秀', 'Special Talk Show'),
  ('アニバーサリーコンサート', '周年纪念音乐会', 'Anniversary Concert'),
  ('特別料金', '特别票价', 'Special Price'),
  ('トークショー', '脱口秀', 'Talk Show')
) AS v(ja, zh, en)
WHERE t.name = v.ja
  AND (t.name_zh IS NULL OR t.name_zh = '');

-- ------------------------------------------------------------
-- 预填 TMDb 标准类型（19 个）到 movie_tag：带中英译名。
-- 爬虫导入电影类型时按 name get-or-create，会复用这些已带译名的行，
-- 因此后续 TMDb 类型可直接本地化。先 UPDATE 已存在的、再 INSERT 缺失的。
-- ------------------------------------------------------------
UPDATE crawl.movie_tag AS t
SET name_zh = v.zh, name_en = v.en
FROM (VALUES
  ('アクション', '动作', 'Action'),
  ('アドベンチャー', '冒险', 'Adventure'),
  ('アニメーション', '动画', 'Animation'),
  ('コメディ', '喜剧', 'Comedy'),
  ('犯罪', '犯罪', 'Crime'),
  ('ドキュメンタリー', '纪录片', 'Documentary'),
  ('ドラマ', '剧情', 'Drama'),
  ('ファミリー', '家庭', 'Family'),
  ('ファンタジー', '奇幻', 'Fantasy'),
  ('履歴', '历史', 'History'),
  ('ホラー', '恐怖', 'Horror'),
  ('音楽', '音乐', 'Music'),
  ('謎', '悬疑', 'Mystery'),
  ('ロマンス', '爱情', 'Romance'),
  ('サイエンスフィクション', '科幻', 'Science Fiction'),
  ('テレビ映画', '电视电影', 'TV Movie'),
  ('スリラー', '惊悚', 'Thriller'),
  ('戦争', '战争', 'War'),
  ('西部劇', '西部', 'Western')
) AS v(ja, zh, en)
WHERE t.name = v.ja
  AND (t.name_zh IS NULL OR t.name_zh = '');

INSERT INTO crawl.movie_tag (name, name_zh, name_en, create_time, update_time, deleted)
SELECT v.ja, v.zh, v.en, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM (VALUES
  ('アクション', '动作', 'Action'),
  ('アドベンチャー', '冒险', 'Adventure'),
  ('アニメーション', '动画', 'Animation'),
  ('コメディ', '喜剧', 'Comedy'),
  ('犯罪', '犯罪', 'Crime'),
  ('ドキュメンタリー', '纪录片', 'Documentary'),
  ('ドラマ', '剧情', 'Drama'),
  ('ファミリー', '家庭', 'Family'),
  ('ファンタジー', '奇幻', 'Fantasy'),
  ('履歴', '历史', 'History'),
  ('ホラー', '恐怖', 'Horror'),
  ('音楽', '音乐', 'Music'),
  ('謎', '悬疑', 'Mystery'),
  ('ロマンス', '爱情', 'Romance'),
  ('サイエンスフィクション', '科幻', 'Science Fiction'),
  ('テレビ映画', '电视电影', 'TV Movie'),
  ('スリラー', '惊悚', 'Thriller'),
  ('戦争', '战争', 'War'),
  ('西部劇', '西部', 'Western')
) AS v(ja, zh, en)
WHERE NOT EXISTS (
  SELECT 1 FROM crawl.movie_tag mt WHERE mt.name = v.ja AND mt.deleted = 0
);
