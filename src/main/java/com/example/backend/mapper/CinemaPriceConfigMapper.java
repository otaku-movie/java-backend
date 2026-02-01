package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.CinemaPriceConfig;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 影院票价配置：3D 加价
 */
@Mapper
public interface CinemaPriceConfigMapper extends BaseMapper<CinemaPriceConfig> {

    @Select("SELECT surcharge FROM cinema_price_config WHERE cinema_id = #{cinemaId} AND dimension_type = #{dimensionType} AND deleted = 0 LIMIT 1")
    java.math.BigDecimal getSurcharge(@Param("cinemaId") Integer cinemaId, @Param("dimensionType") Integer dimensionType);

    /** 按影院物理删除加价配置（替换整批配置时使用，避免逻辑删除导致唯一约束冲突） */
    @Delete("DELETE FROM cinema_price_config WHERE cinema_id = #{cinemaId}")
    int physicalDeleteByCinemaId(@Param("cinemaId") Integer cinemaId);
}
