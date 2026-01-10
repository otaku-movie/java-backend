package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.Api;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.ApiMapper;

import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Data
class ApiSaveQuery {
  Integer id;
  String name;
  String path;
  @NotEmpty(message = "{validator.saveApi.code.required}")
  String code;
}

@Data
class ApiListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;

  public ApiListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@RestController
public class ApiController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private ApiMapper apiMapper;

  @PostMapping(ApiPaths.Admin.Api.LIST)
  public RestBean<List<Object>> list(@RequestBody ApiListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<Api> page = new Page<>(query.getPage(), query.getPageSize());

    wrapper.orderByDesc("update_time");

    IPage list = apiMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping(ApiPaths.Admin.Api.DETAIL)
  public RestBean<Api> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage(MessageKeys.Error.PARAMETER));
    QueryWrapper<Api> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Api result = apiMapper.selectOne(queryWrapper);

    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "api.remove")
  @DeleteMapping(ApiPaths.Admin.Api.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage(MessageKeys.Error.PARAMETER));

    apiMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.REMOVE_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "api.save")
  @PostMapping(ApiPaths.Admin.Api.SAVE)
  public RestBean<List<Object>> save(@RequestBody @Validated ApiSaveQuery query)  {
    Api data = new Api();

    data.setName(query.getName());
    if (query.getPath() != null) {
      data.setPath(query.getPath());
    }

    data.setCode(query.getCode());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("code", query.getPath());
      List<Api> list = apiMapper.selectList(wrapper);

      if (list.size() == 0) {
        apiMapper.insert(data);
        return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Error.REPEAT));
      }
    } else {
      data.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      Api old = apiMapper.selectById(query.getId());

      if (Objects.equals(old.getCode(), query.getCode()) && old.getId() == query.getId()) {
        apiMapper.update(data, updateQueryWrapper);
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("code", query.getPath());
        Api find = apiMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Error.REPEAT));
        } else {
          apiMapper.update(data, updateQueryWrapper);
        }
      }

      return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
    }
  }

}
