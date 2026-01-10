package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.Areas;
import com.example.backend.entity.Brand;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.AreasMapper;
import com.example.backend.mapper.BrandMapper;
import com.example.backend.response.AreaResponse;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.backend.constants.ApiPaths;


@RestController
public class AreasController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private AreasMapper areasMapper;

  public List<AreaResponse> buildAreaResponseTree(List<Areas> allAreas) {
    // 先转换为 AreaResponse
    List<AreaResponse> responses = allAreas.stream()
      .map(area -> {
        AreaResponse r = new AreaResponse();
        r.setId(area.getId());
        r.setName(area.getName());
        r.setNameKana(area.getNameKana());
        r.setParentId(area.getParentId());
        return r;
      }).collect(Collectors.toList());

    // 构建 map
    Map<Integer, AreaResponse> map = new HashMap<>();
    for (AreaResponse r : responses) {
      r.setChildren(new ArrayList<>());
      map.put(r.getId(), r);
    }

    List<AreaResponse> roots = new ArrayList<>();
    for (AreaResponse r : responses) {
      Integer pid = r.getParentId();
      if (pid == null || pid == 0 || !map.containsKey(pid)) {
        roots.add(r);
      } else {
        map.get(pid).getChildren().add(r);
      }
    }

    return roots;
  }

  @GetMapping(ApiPaths.Common.Areas.TREE)
  public RestBean<List<AreaResponse>> list(@RequestParam(value = "id", required = false) Integer id) {
    List<Areas> allAreas = areasMapper.selectList(null);


    return RestBean.success(buildAreaResponseTree(allAreas), MessageUtils.getMessage("success.get"));
  }


}
