package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.BenefitTheaterStock;
import com.example.backend.query.benefit.BenefitCinemaAvailabilityQuery;
import com.example.backend.response.benefit.BenefitCinemaAvailabilityRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BenefitTheaterStockMapper extends BaseMapper<BenefitTheaterStock> {

  long countAvailability(@Param("q") BenefitCinemaAvailabilityQuery q);

  List<BenefitCinemaAvailabilityRow> listAvailability(
    @Param("q") BenefitCinemaAvailabilityQuery q,
    @Param("limit") long limit,
    @Param("offset") long offset
  );

  int countAvailableCinemasForBenefit(@Param("benefitId") Integer benefitId);
}
