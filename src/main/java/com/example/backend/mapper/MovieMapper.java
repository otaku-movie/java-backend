package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.entity.Movie;
import com.example.backend.query.MovieListQuery;
import com.example.backend.query.app.AppMovieListQuery;
import com.example.backend.query.app.getMovieShowTimeQuery;
import com.example.backend.response.Spec;
import com.example.backend.response.app.*;
import com.example.backend.response.movie.HelloMovie;
import com.example.backend.response.movie.MovieResponse;
import com.example.backend.response.MovieStaffResponse;
import com.example.backend.response.movie.Tags;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface MovieMapper extends BaseMapper<Movie> {
    void lockMovieCredits(@Param("movieId") Integer movieId);

    void upsertMovieManualExtras(
      @Param("movieKey") String movieKey,
      @Param("name") String name,
      @Param("originalName") String originalName,
      @Param("cover") String cover,
      @Param("description") String description,
      @Param("runtimeMin") Integer runtimeMin,
      @Param("levelId") Integer levelId,
      @Param("homePage") String homePage,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate
    );

    /**
     * 首页精选：按「未来排片量」热度取片。
     * 仅统计未删除、已公开、start_time 在当前时间之后的场次，按 distinct 场次数倒序，
     * 场次数相同再按想看人数倒序。只返回有封面的 kind='movie'，limit 控制条数。
     */
    List<Movie> homeHighlightByShowTime(@Param("limit") int limit);

    Integer getAllCinemaCount(Integer movieId);
    Integer getAllTheaterCount(Integer movieId);
    Integer getMovieCommentCount(Integer movieId);
    List<HelloMovie> getHelloMovie(Integer movieId);
    List<Spec> getMovieSpec(Integer movieId);
    // 根据电影id 获取电影标签
    List<Tags> getMovieTags(Integer movieId);
    IPage<MovieResponse> movieList(MovieListQuery query, IPage<MovieResponse> page);
    MovieResponse movieDetail(Integer id);
    MovieResponse getMovieRate(Integer id);
    // 热映电影
    IPage<NowMovieShowingResponse> nowMovieShowing(AppMovieListQuery query, IPage<MovieMapper> page);
    List<AppMovieStaffResponse> appMovieStaff(Integer movieId);
    // 批量获取Hello Movie信息
    List<com.example.backend.response.movie.HelloMovie> getHelloMoviesByMovieIds(List<Integer> movieIds);
    // 批量获取監督（监督）信息，position.name = '監督'
    List<MovieDirectorRow> getDirectorsByMovieIds(List<Integer> movieIds);
    // 批量获取出演演员，position.name LIKE '%出演%'（含「出演」「声の出演」）
    List<MovieCastRow> getCastByMovieIds(List<Integer> movieIds);
    // 批量获取电影上映规格（IMAX/4DX 等），来源 movie_spec
    List<com.example.backend.response.app.MovieSpecRow> getSpecsByMovieIds(List<Integer> movieIds);
    IPage<com.example.backend.response.app.MovieComingSoonResponse> getMovieComingSoon(AppMovieListQuery query, IPage<MovieMapper> page);

    List<MovieStaffResponse> movieStaffList(Integer id);

    List<MovieStaffResponse> movieCharacterList(Integer id);

    /**
     * 电影详情页"场次列表"原始查询。
     *
     * 故意 <b>不</b> 走 {@code PaginationInnerInterceptor} 的自动分页：
     * 分页拦截器只能给 SQL 末尾追加 {@code LIMIT N}，但这个接口语义是
     * "按影院分页"——10 条 raw 场次记录会被一家热门影院的多个场次塞满，
     * 让其余影院完全消失。调用方拿到全量列表后，应在 Java 层先
     * 按 {@code cinema_id} 唯一化排序，再 skip/limit 取出当前页要展示
     * 的 N 家影院，并保留这些影院的全部场次。
     */
    List<AppBeforeMovieShowTimeResponse> getMovieShowTime(
      getMovieShowTimeQuery query,
      Integer showTimeState
    );

    /**
     * 该电影（含 reReleaseId 区分）全部未来场次实际出现过的 distinct 字幕语言 id。
     * 用于「按实际场次动态返回」的字幕筛选项——只按 movieId/reReleaseId/时间限定，
     * 不应用字幕/标签/地区等其它筛选，保证选项是该电影的固定全集、不会自我消除。
     */
    List<Integer> getMovieShowTimeSubtitleIds(
      getMovieShowTimeQuery query,
      Integer showTimeState
    );

    /**
     * 该电影（含 reReleaseId 区分）全部未来场次实际出现过的 distinct 上映标签 id。
     * 语义同 {@link #getMovieShowTimeSubtitleIds}。
     */
    List<Integer> getMovieShowTimeTagIds(
      getMovieShowTimeQuery query,
      Integer showTimeState
    );


//    IPage<Movie> selectList(Page<Object> page, QueryWrapper wrapper);
}