package com.example.backend.mapper;

import com.example.backend.query.MovieMergeFieldOverrides;
import com.example.backend.response.movie.MovieDuplicateGroupKey;
import com.example.backend.response.movie.MovieDuplicateItem;
import com.example.backend.response.movie.MovieMergeDetail;
import com.example.backend.response.movie.MoviePendingMatchRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 后台「电影去重合并」专用 mapper（独立于巨型 MovieMapper，便于维护）。
 *
 * <p>注意：业务 datasource 走 crawl schema，故表名不加 schema 前缀。
 * 这里大量需要「看见软删行」，因此全部走自定义 SQL，不复用 MyBatis-Plus
 * 的逻辑删除自动过滤。</p>
 */
@Mapper
public interface MovieMergeMapper {

  /** 自动模式：重复候选组总数（同 tmdb_id 或同名字）。 */
  long countDuplicateGroupKeys();

  /** 自动模式：分页取出组键。 */
  List<MovieDuplicateGroupKey> listDuplicateGroupKeys(
    @Param("limit") int limit,
    @Param("offset") int offset
  );

  /** 按组键拉取组内全部行（含软删）。 */
  List<MovieDuplicateItem> listGroupItems(
    @Param("groupType") String groupType,
    @Param("groupValue") String groupValue
  );

  /** 手动模式：按关键词搜索可合并电影。 */
  List<MovieDuplicateItem> searchMovies(
    @Param("keyword") String keyword,
    @Param("keywordNum") Integer keywordNum
  );

  /** 单行查询（合并前校验存在性 / 合并后回包）。 */
  MovieDuplicateItem getItemById(@Param("id") Integer id);

  /** 详情对比页：单部电影的基础信息 + 结构化关联计数（含软删行）。 */
  MovieMergeDetail getMergeDetailBase(@Param("id") Integer id);

  /** 详情对比页：场次按影院分布（取前 N 家）。 */
  List<MovieMergeDetail.ShowtimeByCinema> listShowtimeByCinema(
    @Param("id") Integer id,
    @Param("limit") int limit
  );

  /** 详情对比页：staff 名单样本（含职位，前 N 条）。 */
  List<MovieMergeDetail.StaffBrief> listStaffBrief(
    @Param("id") Integer id,
    @Param("limit") int limit
  );

  /** 详情对比页：角色名单样本（前 N 条）。 */
  List<String> listCharacterNames(
    @Param("id") Integer id,
    @Param("limit") int limit
  );

  /** 详情对比页：版本列表（原版 / 配音 + 语言，前 N 条）。 */
  List<MovieMergeDetail.VersionBrief> listVersions(
    @Param("id") Integer id,
    @Param("limit") int limit
  );

  /** 详情对比页：标签名列表（前 N 条）。 */
  List<String> listTagNames(
    @Param("id") Integer id,
    @Param("limit") int limit
  );

  /** 详情对比页：规格名列表（IMAX/4DX 等，前 N 条）。 */
  List<String> listSpecNames(
    @Param("id") Integer id,
    @Param("limit") int limit
  );

  /** 待确认匹配总数。 */
  long countPendingMatches();

  /** 待确认匹配分页。 */
  List<MoviePendingMatchRow> listPendingMatches(
    @Param("limit") int limit,
    @Param("offset") int offset
  );

  /**
   * 把某张业务表里指向 loser 的 movie_id 重指向 survivor。
   * {@code table} 来自 service 内的白名单常量（非用户输入），故用 ${} 拼表名安全。
   */
  int repointMovieId(
    @Param("table") String table,
    @Param("survivorId") Integer survivorId,
    @Param("loserId") Integer loserId
  );

  /**
   * 合并前清理 movie_rate 冲突：uk(user_id, movie_id) 是「含软删」的全量唯一键，
   * 无法靠软删规避，故同一用户对 survivor 已有评分时，硬删 loser 的对应评分行。
   */
  int precleanMovieRate(
    @Param("survivorId") Integer survivorId,
    @Param("loserId") Integer loserId
  );

  /**
   * 合并前清理 presale 冲突：uk(movie_id) WHERE deleted=0。
   * survivor 已有活跃 presale 时，软删 loser 的活跃 presale（不再迁移）。
   */
  int precleanPresale(
    @Param("survivorId") Integer survivorId,
    @Param("loserId") Integer loserId
  );

  /**
   * 合并前清理 re_release 冲突：uk(movie_id, display_name_override) WHERE deleted=0。
   * survivor 已有同名 override 的活跃重映时，软删 loser 的同名活跃重映。
   */
  int precleanReRelease(
    @Param("survivorId") Integer survivorId,
    @Param("loserId") Integer loserId
  );

  /** 合并前清理 movie_staff 冲突：复合主键 (movie_id, staff_id, position_id) 无法靠软删规避。 */
  int precleanMovieStaff(
    @Param("survivorId") Integer survivorId,
    @Param("loserId") Integer loserId
  );

  /** 合并前清理 movie_character 冲突：复合主键 (movie_id, character_id) 无法靠软删规避。 */
  int precleanMovieCharacter(
    @Param("survivorId") Integer survivorId,
    @Param("loserId") Integer loserId
  );

  /**
   * 重指向后对保留行的某张关联表去重：在 {@code movie_id = survivorId} 的存活行里，
   * 按业务键 {@code keyCols} 分组，仅保留 id 最小的一行，其余软删（deleted=1）。
   * {@code table} / {@code keyCols} 均来自 service 白名单常量（非用户输入），故 ${} 拼接安全。
   */
  int dedupRelationByKeys(
    @Param("survivorId") Integer survivorId,
    @Param("table") String table,
    @Param("keyCols") List<String> keyCols
  );

  /**
   * 合并后对 survivor 的场次按物理键（影院+影厅+开始时间）去重：
   * 每组保留「可售/开放/最近更新/id 最大」的一条，其余软删（deleted=1）。
   * 解决合并把两部电影的同一场次都指向 survivor 后并存成重复时间的问题。
   */
  int dedupSurvivorShowTimes(@Param("survivorId") Integer survivorId);

  /** 软删某行（deleted=1）。 */
  int softDeleteMovie(@Param("id") Integer id);

  /** 复活存活行（deleted=0）并可选改名（newName 为空则保留原名）。 */
  int reactivateAndRename(
    @Param("id") Integer id,
    @Param("newName") String newName
  );

  /**
   * 详情对比页「逐项应用」：把字段级覆盖写回 survivor。
   * 仅覆盖 {@code o} 中非 null 的字段（name 另由 {@link #reactivateAndRename} 处理）。
   */
  int applySurvivorOverrides(
    @Param("survivorId") Integer survivorId,
    @Param("o") MovieMergeFieldOverrides o
  );

  /** 记录一次后台人工合并。 */
  int insertMergeLog(
    @Param("survivor") MovieDuplicateItem survivor,
    @Param("loser") MovieDuplicateItem loser,
    @Param("operatorUserId") Integer operatorUserId,
    @Param("note") String note
  );

  /** 把 loser 名字固化成人工别名规则，供 crawler 后续导入复用。 */
  int upsertMergeAlias(
    @Param("aliasTitle") String aliasTitle,
    @Param("survivor") MovieDuplicateItem survivor
  );

  /** 处理待确认项：MERGED / IGNORED。 */
  int resolvePendingMatch(
    @Param("id") Long id,
    @Param("status") String status,
    @Param("operatorUserId") Integer operatorUserId
  );
}
