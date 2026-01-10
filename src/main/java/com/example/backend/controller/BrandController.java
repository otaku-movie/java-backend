package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.Brand;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.BrandMapper;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Data
class BrandSaveQuery {
  Integer id;
  @NotEmpty(message = "validator.saveBrand.name.required")
  String name;
}


@Data
class BrandListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  private Integer id;

  public BrandListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@RestController
public class BrandController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private BrandMapper brandMapper;

  @PostMapping(ApiPaths.Common.Brand.LIST)
  public RestBean<List<Brand>> list(@RequestBody BrandListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<Brand> page = new Page<>(query.getPage(), query.getPageSize());

    if (query.getName() != null && query.getName() != "") {
      wrapper.like("name", query.getName());
    }
    if (query.getId() != null && query.getId() != 0) {
      wrapper.eq("id", query.getId());
    }
    wrapper.orderByDesc("update_time");

    IPage list = brandMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }

  @GetMapping(ApiPaths.Common.Brand.DETAIL)
  public RestBean<Brand> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Brand result = brandMapper.selectOne(queryWrapper);

    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "brand.remove")
  @DeleteMapping(ApiPaths.Admin.Brand.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    brandMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.REMOVE_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "brand.save")
  @PostMapping(ApiPaths.Admin.Brand.SAVE)
  public RestBean<List<Object>> save(@RequestBody @Validated BrandSaveQuery query) {
    Brand data = new Brand();
    data.setName(query.getName());

    QueryWrapper<Brand> wrapper = new QueryWrapper<>();
    wrapper.eq("name", query.getName());

    String repeatMessage = MessageUtils.getMessage(MessageKeys.Admin.REPEAT_ERROR, MessageUtils.getMessage(MessageKeys.Admin.Repeat.BRAND_NAME));

    if (query.getId() == null) {
      // 新增操作
      List<Brand> list = brandMapper.selectList(wrapper);
      if (list.isEmpty()) {
        brandMapper.insert(data);
        return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      }
    } else {
      // 更新操作
      wrapper.ne("id", query.getId());
      Brand find = brandMapper.selectOne(wrapper);

      if (find != null) {
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      } else {
        data.setId(query.getId());
        brandMapper.updateById(data);
        return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
      }
    }
  }
}
