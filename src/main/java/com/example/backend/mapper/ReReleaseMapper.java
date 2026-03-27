package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.entity.ReRelease;
import com.example.backend.query.MovieListQuery;
import com.example.backend.response.app.ReReleaseHistoryResponse;
import com.example.backend.response.reRelease.ReReleaseListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ReReleaseMapper extends BaseMapper<ReRelease> {
    IPage<ReReleaseListResponse> reReleaseList(MovieListQuery query, IPage<ReRelease> page);

    /** App：获取当前日期有效的重映电影列表（用于合并到正在上映） */
    List<com.example.backend.response.app.NowMovieShowingResponse> activeNowShowing(@Param("today") String today);

    /** App：获取未来即将开始的重映电影列表（用于合并到即将上映） */
    List<com.example.backend.response.app.MovieComingSoonResponse> upcomingComingSoon(@Param("today") String today);

    /** App：电影详情页 - 重映历史 */
    List<ReReleaseHistoryResponse> historyByMovieId(@Param("movieId") Integer movieId);
}