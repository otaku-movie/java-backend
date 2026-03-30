package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.AppVersion;
import com.example.backend.response.app.AppVersionCheckResponse;
import org.apache.ibatis.annotations.Param;

/**
* @author last order
* @description 针对表【api】的数据库操作Mapper
* @createDate 2024-05-24 17:37:24
* @Entity com.example.backend.entity.TheaterHall.Api
*/
public interface AppVersionMapper extends BaseMapper<AppVersion> {
  AppVersionCheckResponse latestForCheck(@Param("platform") String platform, @Param("lang") String lang);

}




