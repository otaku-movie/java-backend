package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.Language;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.LanguageMapper;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Data
class LanguageSaveQuery {
  Integer id;
  @NotEmpty(message = "validator.saveLanguage.name.required")
  String name;
  @NotEmpty(message = "validator.saveLanguage.code.required")
  String code;
}


@Data
class LanguageListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  private Integer id;

  public LanguageListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@RestController
public class LanguageController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private LanguageMapper languageMapper;

  @PostMapping(ApiPaths.Common.Language.LIST)
  public RestBean<List<Language>> list(@RequestBody LanguageListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<Language> page = new Page<>(query.getPage(), query.getPageSize());

    if (query.getName() != null && query.getName() != "") {
      wrapper.like("name", query.getName());
    }
    if (query.getId() != null && query.getId() != 0) {
      wrapper.eq("id", query.getId());
    }
    wrapper.orderByDesc("update_time");

    IPage list = languageMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }

  @GetMapping(ApiPaths.Common.Language.DETAIL)
  public RestBean<Language> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));
    QueryWrapper<Language> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Language result = languageMapper.selectOne(queryWrapper);

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @SaCheckLogin
  @CheckPermission(code = "language.remove")
  @DeleteMapping(ApiPaths.Admin.Language.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    languageMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
  @SaCheckLogin
  @CheckPermission(code = "language.save")
  @PostMapping(ApiPaths.Admin.Language.SAVE)
  public RestBean<List<Object>> save(@RequestBody @Validated LanguageSaveQuery query) {
    Language data = new Language();
    data.setName(query.getName());
    data.setCode(query.getCode());

    QueryWrapper<Language> wrapper = new QueryWrapper<>();
    wrapper.eq("code", query.getCode());

    String repeatMessage = MessageUtils.getMessage("error.repeat", MessageUtils.getMessage("repeat.languageCode"));

    if (query.getId() == null) {
      // 新增操作
      List<Language> list = languageMapper.selectList(wrapper);
      if (list.isEmpty()) {
        languageMapper.insert(data);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      }
    } else {
      // 更新操作
      wrapper.ne("id", query.getId());
      Language find = languageMapper.selectOne(wrapper);

      if (find != null) {
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      } else {
        data.setId(query.getId());
        languageMapper.updateById(data);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      }
    }
  }
}
