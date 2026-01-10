package com.example.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.AppVersion;
import com.example.backend.entity.Areas;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.AppVersionMapper;
import com.example.backend.mapper.AreasMapper;
import com.example.backend.query.MovieListQuery;
import com.example.backend.query.PaginationQuery;
import com.example.backend.response.AreaResponse;
import com.example.backend.response.movie.MovieResponse;
import com.example.backend.utils.MessageUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
class AppVersionListQuery extends PaginationQuery {
  private String platform;
}

@RestController
public class AppVersionController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private AppVersionMapper appVersionMapper;


  @PostMapping(ApiPaths.Admin.AppVersion.LIST)
  public RestBean<List<AppVersion>> list(@RequestBody AppVersionListQuery query) {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page page = new Page<>(query.getPage(), query.getPageSize());

    if (query.getPlatform() != null) {
      wrapper.eq("platform", query.getPlatform());
    }

    IPage<AppVersion> result = appVersionMapper.selectPage(page, wrapper);

    return RestBean.success(result.getRecords(), query.getPage(), result.getTotal(), query.getPageSize());
  }


}
