package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Language;
import com.example.backend.entity.MovieShowTimeTag;
import com.example.backend.enumerate.SeatState;
import com.example.backend.query.MovieShowTimeListQuery;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.response.MovieShowTimeList;
import com.example.backend.response.UserSelectSeat;
import com.example.backend.response.chart.StatisticsOfDailyMovieScreenings;
import com.example.backend.response.showTime.MovieShowTimeDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface MovieShowTimeMapper extends BaseMapper<MovieShowTime> {
  UserSelectSeat userSelectSeat(Integer userId, Integer movieShowTimeId, Integer seatState);
  UserSelectSeat userSelectSeatWithoutSpec(Integer userId, Integer movieShowTimeId, Integer seatState);
  List<Language> getMovieShowTimeSubtitle(List<Integer> languageId);
  List<MovieShowTimeTag> getMovieShowTimeTags(List<Integer> tagsId);

  IPage<MovieShowTimeList> movieShowTimeList(MovieShowTimeListQuery query, Integer orderState, Page<MovieShowTime> page);
  List<MovieShowTimeList> movieShowTimeList(MovieShowTimeListQuery query, Integer orderState);
  List<StatisticsOfDailyMovieScreenings>  StatisticsOfDailyMovieScreenings();
  MovieShowTimeDetail movieShowTimeDetail(Integer id);

  /**
   * 尝试获取事务级咨询锁（非阻塞）。返回 false 表示锁被占用（如爬虫导入正在写 movie_show_time），
   * 调用方应跳过本轮。锁在事务结束时自动释放。key 必须与爬虫端
   * (cinema-crawler import-data-pg.ts SHOWTIME_REFRESH_LOCK_KEY) 一致。
   */
  @Select("SELECT pg_try_advisory_xact_lock(#{key})")
  Boolean tryAdvisoryXactLock(@Param("key") long key);

  /**
   * 集合式重算场次放映状态：只更新「当前状态 != 应有状态」且时间字段合法(定长 19)的行。
   * start_time/end_time 为定长 'yyyy-MM-dd HH:mm:ss' 文本，可直接按字典序与 now 比较。
   * @return 受影响行数
   */
  @Update("UPDATE movie_show_time SET status = #{status}, update_time = now() " +
      "WHERE deleted = 0 AND status <> #{status} " +
      "AND start_time IS NOT NULL AND char_length(start_time) = 19 " +
      "AND end_time IS NOT NULL AND char_length(end_time) = 19 " +
      "AND start_time <= #{now} AND end_time > #{now}")
  int markScreening(@Param("status") int status, @Param("now") String now);

  @Update("UPDATE movie_show_time SET status = #{status}, update_time = now() " +
      "WHERE deleted = 0 AND status <> #{status} " +
      "AND start_time IS NOT NULL AND char_length(start_time) = 19 " +
      "AND end_time IS NOT NULL AND char_length(end_time) = 19 " +
      "AND end_time <= #{now}")
  int markEnded(@Param("status") int status, @Param("now") String now);

  @Update("UPDATE movie_show_time SET status = #{status}, update_time = now() " +
      "WHERE deleted = 0 AND status <> #{status} " +
      "AND start_time IS NOT NULL AND char_length(start_time) = 19 " +
      "AND end_time IS NOT NULL AND char_length(end_time) = 19 " +
      "AND start_time > #{now}")
  int markNotStarted(@Param("status") int status, @Param("now") String now);

  /** publish_at 已到 → open 置 true（只补未公开的行；不会把已公开改回）。 */
  @Update("UPDATE movie_show_time SET open = true, update_time = now() " +
      "WHERE deleted = 0 AND (open IS NULL OR open = false) " +
      "AND publish_at IS NOT NULL AND char_length(publish_at) = 19 AND publish_at <= #{now}")
  int openByPublishAt(@Param("now") String now);

  /** 无 sale_open_at（null/空）→ 视为立即可售：can_sale 置 true。 */
  @Update("UPDATE movie_show_time SET can_sale = true, update_time = now() " +
      "WHERE deleted = 0 AND (can_sale IS NULL OR can_sale = false) " +
      "AND (sale_open_at IS NULL OR sale_open_at = '')")
  int enableSaleWhenNoSaleOpenAt();

  /** sale_open_at 已到 → can_sale 置 true。 */
  @Update("UPDATE movie_show_time SET can_sale = true, update_time = now() " +
      "WHERE deleted = 0 AND (can_sale IS NULL OR can_sale = false) " +
      "AND sale_open_at IS NOT NULL AND char_length(sale_open_at) = 19 AND sale_open_at <= #{now}")
  int enableSaleWhenSaleOpenAtReached(@Param("now") String now);

  /** sale_open_at 未到 → can_sale 置 false。 */
  @Update("UPDATE movie_show_time SET can_sale = false, update_time = now() " +
      "WHERE deleted = 0 AND (can_sale IS NULL OR can_sale = true) " +
      "AND sale_open_at IS NOT NULL AND char_length(sale_open_at) = 19 AND sale_open_at > #{now}")
  int disableSaleWhenSaleOpenAtNotReached(@Param("now") String now);
}