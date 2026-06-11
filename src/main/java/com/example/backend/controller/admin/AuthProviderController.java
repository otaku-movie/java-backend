package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.AuthProviderConfig;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.AuthProviderConfigMapper;
import com.example.backend.utils.MessageUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Data
class AuthProviderSaveQuery {
  private Integer id;
  private String code;
  private String name;
  private Boolean enabled;
  private String platform;
  private Integer sort;
  private String minAppVersion;
  private String remark;
}

@RestController("adminAuthProviderController")
public class AuthProviderController {
  @Autowired
  private AuthProviderConfigMapper authProviderConfigMapper;

  @SaCheckLogin
  @PostMapping(ApiPaths.Admin.AuthProvider.LIST)
  public RestBean<List<AuthProviderConfig>> list() {
    List<AuthProviderConfig> list = authProviderConfigMapper.selectList(
        new QueryWrapper<AuthProviderConfig>()
            .eq("deleted", 0)
            .orderByAsc("sort")
            .orderByAsc("id")
    );
    return RestBean.success(list, MessageUtils.getMessage("message.get.success"));
  }

  @SaCheckLogin
  @CheckPermission(code = "authProvider.save")
  @PostMapping(ApiPaths.Admin.AuthProvider.SAVE)
  public RestBean<Object> save(@RequestBody AuthProviderSaveQuery query) {
    if (query == null || query.getId() == null) {
      return RestBean.error(500, MessageUtils.getMessage("message.parameter.error"));
    }
    if (!StringUtils.hasText(query.getCode())) {
      return RestBean.error(500, MessageUtils.getMessage("message.parameter.error"));
    }
    if (!StringUtils.hasText(query.getName())) {
      return RestBean.error(500, MessageUtils.getMessage("message.parameter.error"));
    }

    AuthProviderConfig model = new AuthProviderConfig();
    model.setName(query.getName());
    model.setEnabled(Boolean.TRUE.equals(query.getEnabled()));
    model.setPlatform(StringUtils.hasText(query.getPlatform()) ? query.getPlatform() : "ALL");
    model.setSort(query.getSort() == null ? 0 : query.getSort());
    model.setMinAppVersion(query.getMinAppVersion());
    model.setRemark(query.getRemark());

    authProviderConfigMapper.update(
        model,
        new UpdateWrapper<AuthProviderConfig>()
            .eq("id", query.getId())
            .eq("code", query.getCode())
            .eq("deleted", 0)
    );
    return RestBean.success(null, MessageUtils.getMessage("message.save.success"));
  }
}
