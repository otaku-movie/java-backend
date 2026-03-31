package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.entity.Benefit;
import com.example.backend.query.benefit.BenefitMovieListQuery;
import com.example.backend.response.benefit.BenefitMovieListItemResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BenefitMapper extends BaseMapper<Benefit> {
  @Select("""
    SELECT
      b.movie_id AS movieId,
      COALESCE(m.name, '') AS movieName,
      m.cover AS movieCover,
      COUNT(1) AS benefitCount
    FROM benefit b
    LEFT JOIN movie m ON m.id = b.movie_id
    WHERE b.deleted = 0
      AND (
        COALESCE(CAST(#{q.movieName} AS text), '') = ''
        OR m.name ILIKE CONCAT('%', CAST(#{q.movieName} AS text), '%')
      )
    GROUP BY b.movie_id, m.name, m.cover
    ORDER BY COUNT(1) DESC, b.movie_id ASC
    """)
  IPage<BenefitMovieListItemResponse> listMoviesWithBenefit(
    IPage<BenefitMovieListItemResponse> page,
    @Param("q") BenefitMovieListQuery query
  );
}
