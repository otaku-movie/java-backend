package com.example.backend.service;

import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.mapper.MovieMergeMapper;
import com.example.backend.query.MovieDuplicateQuery;
import com.example.backend.query.MovieMergeFieldOverrides;
import com.example.backend.query.MovieMergeQuery;
import com.example.backend.query.MoviePendingMatchResolveQuery;
import com.example.backend.response.movie.MovieDuplicateGroup;
import com.example.backend.response.movie.MovieDuplicateGroupKey;
import com.example.backend.response.movie.MovieDuplicateItem;
import com.example.backend.response.movie.MovieMergeDetail;
import com.example.backend.response.movie.MovieMergeResult;
import com.example.backend.response.movie.MoviePendingMatch;
import com.example.backend.response.movie.MoviePendingMatchRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台「电影去重合并」业务。
 *
 * <p>合并语义：把 {@code loserIds} 的全部业务外键重指向 {@code survivorId}，
 * loser 软删，survivor 复活（deleted=0）并可选改名。所有外键表见
 * {@link #MOVIE_ID_TABLES}（与生产 prod_compact_movie_ids.sql 的清单一致）。</p>
 */
@Slf4j
@Service
public class MovieMergeService {

  @Autowired
  private MovieMergeMapper mapper;

  /** 合并会批量更新 movie_show_time 等表，并发请求易触发死锁，串行化合并操作。 */
  private static final Object MERGE_LOCK = new Object();

  private static final int MERGE_DEADLOCK_MAX_RETRIES = 3;

  /**
   * 所有带 movie_id 外键、需要在合并时重指向的业务表。
   * 该列表是固定常量（非用户输入），故 mapper 里用 ${table} 拼接安全。
   */
  private static final List<String> MOVIE_ID_TABLES = List.of(
    "benefit",
    "hello_movie",
    "movie_character",
    "movie_comment",
    "movie_rate",
    "movie_reply",
    "movie_show_time",
    "movie_spec",
    "movie_staff",
    "movie_tag_tags",
    "movie_version",
    "presale",
    "re_release"
  );

  /** 自动模式：候选组总数。 */
  public long countDuplicateGroups() {
    return mapper.countDuplicateGroupKeys();
  }

  /** 待确认匹配总数。 */
  public long countPendingMatches() {
    return mapper.countPendingMatches();
  }

  /** 自动模式：分页候选组（每组含成员行 + 推荐存活行）。 */
  public List<MovieDuplicateGroup> listDuplicateGroups(MovieDuplicateQuery query) {
    int pageSize = query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
    int page = query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
    int offset = (page - 1) * pageSize;

    List<MovieDuplicateGroupKey> keys = mapper.listDuplicateGroupKeys(pageSize, offset);
    List<MovieDuplicateGroup> groups = new ArrayList<>();
    for (MovieDuplicateGroupKey key : keys) {
      List<MovieDuplicateItem> items = mapper.listGroupItems(key.getGroupType(), key.getGroupValue());
      if (items == null || items.size() < 2) {
        continue;
      }
      MovieDuplicateGroup group = new MovieDuplicateGroup();
      group.setReason(key.getGroupType());
      group.setGroupValue(key.getGroupValue());
      group.setItems(items);
      group.setRecommendedSurvivorId(recommendSurvivor(items));
      groups.add(group);
    }
    return groups;
  }

  /** 详情对比页：批量拉取候选电影的完整信息（基础 + 计数 + 场次分布 + staff/角色样本）。 */
  public List<MovieMergeDetail> mergeDetails(List<Integer> ids) {
    List<MovieMergeDetail> result = new ArrayList<>();
    if (ids == null) {
      return result;
    }
    // 去重并保持入参顺序，便于前端按提交顺序并排展示。
    for (Integer id : new LinkedHashSet<>(ids)) {
      if (id == null) {
        continue;
      }
      MovieMergeDetail detail = mapper.getMergeDetailBase(id);
      if (detail == null) {
        continue;
      }
      detail.setStaff(mapper.listStaffBrief(id, 40));
      detail.setCharacters(mapper.listCharacterNames(id, 40));
      detail.setVersions(mapper.listVersions(id, 40));
      detail.setTags(mapper.listTagNames(id, 40));
      detail.setSpecs(mapper.listSpecNames(id, 40));
      result.add(detail);
    }
    return result;
  }

  /** 手动模式：按关键词搜索。 */
  public List<MovieDuplicateItem> searchMovies(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return new ArrayList<>();
    }
    String kw = keyword.trim();
    Integer kwNum = null;
    try {
      kwNum = Integer.valueOf(kw);
    } catch (NumberFormatException ignored) {
      // 非纯数字关键词，仅按名字 / movie_key 搜
    }
    return mapper.searchMovies(kw, kwNum);
  }

  /** 待确认模式：分页读取 crawler 写入的灰区匹配。 */
  public List<MoviePendingMatch> listPendingMatches(MovieDuplicateQuery query) {
    int pageSize = query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
    int page = query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
    int offset = (page - 1) * pageSize;

    List<MoviePendingMatchRow> rows = mapper.listPendingMatches(pageSize, offset);
    List<MoviePendingMatch> result = new ArrayList<>();
    for (MoviePendingMatchRow row : rows) {
      MovieDuplicateItem candidate = mapper.getItemById(row.getCandidateMovieId());
      MovieDuplicateItem matched = mapper.getItemById(row.getMatchedMovieId());
      if (candidate == null || matched == null) {
        continue;
      }
      List<MovieDuplicateItem> items = List.of(candidate, matched);
      MoviePendingMatch item = new MoviePendingMatch();
      item.setId(row.getId());
      item.setConfidence(row.getConfidence());
      item.setMatchReason(row.getMatchReason());
      item.setStatus(row.getStatus());
      item.setCreateTime(row.getCreateTime());
      item.setUpdateTime(row.getUpdateTime());
      item.setItems(items);
      item.setRecommendedSurvivorId(recommendSurvivor(items));
      result.add(item);
    }
    return result;
  }

  /**
   * 关联表去重白名单：表名 -> 业务去重键列。
   * 与详情页「合并后结果」的并集去重口径一致：
   * 版本按 (版本码, 语言)、标签按 标签。
   * staff / 角色是复合主键表，需在重指向前清理冲突，不能放在这里。
   * 规格是无 id 主键的集合表，重指向后单独按 (movie_id, spec_id) 去重。
   * 评论/评分等计数型关系按外键累加，不在这里去重。
   */
  private static final List<Map.Entry<String, List<String>>> DEDUP_RELATIONS = List.of(
    Map.entry("movie_version", List.of("version_code", "language_id")),
    Map.entry("movie_tag_tags", List.of("movie_tag_id"))
  );

  /** 对保留行的关联表逐张去重（仅在合并事务内调用）。 */
  private void dedupSurvivorRelations(Integer survivorId) {
    for (Map.Entry<String, List<String>> rel : DEDUP_RELATIONS) {
      int removed = mapper.dedupRelationByKeys(survivorId, rel.getKey(), rel.getValue());
      if (removed > 0) {
        log.info("merge dedup: survivor={} table={} removed={}", survivorId, rel.getKey(), removed);
      }
    }
    // 场次按物理键（影院+影厅+开始时间）单独去重：合并前分属两部电影的同一场次
    // 被重指到 survivor 后会并存（状态常停在 on_sale / pre_sale），需保留可售那条、软删其余。
    int removedShows = mapper.dedupSurvivorShowTimes(survivorId);
    if (removedShows > 0) {
      log.info("merge dedup: survivor={} table=movie_show_time removed={}", survivorId, removedShows);
    }
    // movie_spec 无 id 主键，按 (movie_id, spec_id) 用 ctid 去重，避免详情页规格标签重复。
    int removedSpecs = mapper.dedupSurvivorSpecs(survivorId);
    if (removedSpecs > 0) {
      log.info("merge dedup: survivor={} table=movie_spec removed={}", survivorId, removedSpecs);
    }
  }

  /**
   * 推荐存活行：优先「带 tmdb_id」的规范行；其次场次数最多；再次活跃；最后最小 id。
   * 列表本身已按 (tmdb 优先, 活跃优先, id 升序) 排好，这里在此基础上再考虑场次数。
   */
  private Integer recommendSurvivor(List<MovieDuplicateItem> items) {
    MovieDuplicateItem best = null;
    for (MovieDuplicateItem it : items) {
      if (best == null) {
        best = it;
        continue;
      }
      if (rank(it) > rank(best)) {
        best = it;
      }
    }
    return best == null ? null : best.getId();
  }

  private long rank(MovieDuplicateItem it) {
    long score = 0;
    if (it.getTmdbId() != null) score += 1_000_000_000L;
    if (it.getDeleted() != null && it.getDeleted() == 0) score += 100_000_000L;
    score += (it.getShowCount() == null ? 0 : it.getShowCount());
    return score;
  }

  /**
   * 执行合并。整个过程单事务，任一步抛异常则全部回滚。
   *
   * @throws IllegalArgumentException 入参非法（survivor 在 loser 列表里 / 行不存在等）
   */
  @Transactional(rollbackFor = Exception.class)
  public MovieMergeResult merge(MovieMergeQuery query) {
    synchronized (MERGE_LOCK) {
      for (int attempt = 1; attempt <= MERGE_DEADLOCK_MAX_RETRIES; attempt++) {
        try {
          return doMerge(query);
        } catch (DeadlockLoserDataAccessException e) {
          if (attempt == MERGE_DEADLOCK_MAX_RETRIES) {
            throw e;
          }
          log.warn("movie merge deadlock, retry {}/{}: survivor={} losers={}",
              attempt, MERGE_DEADLOCK_MAX_RETRIES, query.getSurvivorId(), query.getLoserIds(), e);
        }
      }
      throw new IllegalStateException("movie merge retry exhausted");
    }
  }

  private MovieMergeResult doMerge(MovieMergeQuery query) {
    Integer survivorId = query.getSurvivorId();
    Set<Integer> losers = new LinkedHashSet<>(query.getLoserIds());
    losers.remove(survivorId);
    if (losers.isEmpty()) {
      throw new IllegalArgumentException("loserIds 去掉 survivor 后为空");
    }

    MovieDuplicateItem survivor = mapper.getItemById(survivorId);
    if (survivor == null) {
      throw new IllegalArgumentException("survivor 不存在: " + survivorId);
    }

    Integer operatorUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsInt() : null;
    int merged = 0;
    for (Integer loserId : losers) {
      MovieDuplicateItem loser = mapper.getItemById(loserId);
      if (loser == null) {
        throw new IllegalArgumentException("loser 不存在: " + loserId);
      }

      // 1) 先清理唯一键 / 复合主键冲突，避免重指向时违反约束
      mapper.precleanMovieRate(survivorId, loserId);
      mapper.precleanPresale(survivorId, loserId);
      mapper.precleanReRelease(survivorId, loserId);
      mapper.precleanMovieStaff(survivorId, loserId);
      mapper.precleanMovieCharacter(survivorId, loserId);

      // 2) 全部外键重指向 survivor
      for (String table : MOVIE_ID_TABLES) {
        mapper.repointMovieId(table, survivorId, loserId);
      }

      // 3) loser 软删
      mapper.softDeleteMovie(loserId);
      mapper.insertMergeLog(survivor, loser, operatorUserId, "admin movie merge");
      merged++;
    }

    // 3.5) 重指向后保留行可能出现重复关联行（如多条「原版」、重复标签），
    //      按业务键去重（保留最早一行、其余软删），使库内结果与详情页「合并后结果」一致。
    dedupSurvivorRelations(survivorId);

    // 4) survivor 复活并可选改名。
    //    名字优先取「逐项应用」里覆盖的 name，其次取 newName；都为空则保留原名。
    MovieMergeFieldOverrides overrides = query.getFieldOverrides();
    String effectiveName = query.getNewName();
    if (overrides != null && overrides.getName() != null && !overrides.getName().isBlank()) {
      effectiveName = overrides.getName();
    }
    mapper.reactivateAndRename(survivorId, effectiveName);

    // 5) 详情对比页「逐项应用」：把字段级覆盖写回 survivor（name 已在上一步处理）。
    if (overrides != null) {
      mapper.applySurvivorOverrides(survivorId, overrides);
    }

    MovieDuplicateItem after = mapper.getItemById(survivorId);
    if (after != null) {
      for (Integer loserId : losers) {
        MovieDuplicateItem loser = mapper.getItemById(loserId);
        if (loser != null && loser.getName() != null && !loser.getName().isBlank()) {
          mapper.upsertMergeAlias(loser.getName(), after);
        }
      }
      if (effectiveName != null && !effectiveName.isBlank()) {
        mapper.upsertMergeAlias(effectiveName, after);
      }
    }
    MovieMergeResult result = new MovieMergeResult();
    result.setSurvivorId(survivorId);
    result.setSurvivorName(after == null ? null : after.getName());
    result.setSurvivorShowCount(after == null ? null : after.getShowCount());
    result.setMergedCount(merged);
    log.info("movie merge done: survivor={} losers={} merged={}", survivorId, losers, merged);
    return result;
  }

  /** 处理待确认项：merge 会复用同一个合并事务，ignore 只改状态。 */
  @Transactional(rollbackFor = Exception.class)
  public MovieMergeResult resolvePending(MoviePendingMatchResolveQuery query) {
    Integer operatorUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsInt() : null;
    String action = query.getAction() == null ? "" : query.getAction().trim().toLowerCase();
    if ("ignore".equals(action)) {
      mapper.resolvePendingMatch(query.getPendingId(), "IGNORED", operatorUserId);
      MovieMergeResult result = new MovieMergeResult();
      result.setMergedCount(0);
      return result;
    }
    if (!"merge".equals(action)) {
      throw new IllegalArgumentException("未知待确认处理动作: " + query.getAction());
    }
    MovieMergeQuery mergeQuery = new MovieMergeQuery();
    mergeQuery.setSurvivorId(query.getSurvivorId());
    mergeQuery.setLoserIds(query.getLoserIds());
    mergeQuery.setNewName(query.getNewName());
    mergeQuery.setFieldOverrides(query.getFieldOverrides());
    MovieMergeResult result = merge(mergeQuery);
    mapper.resolvePendingMatch(query.getPendingId(), "MERGED", operatorUserId);
    return result;
  }
}
