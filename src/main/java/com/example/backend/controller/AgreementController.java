package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.Agreement;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.UserAgreementAcceptance;
import com.example.backend.mapper.AgreementMapper;
import com.example.backend.mapper.UserAgreementAcceptanceMapper;
import com.example.backend.utils.MessageUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Data
class AgreementDetailRequest {
  private String code;
  private String lang;
}

@Data
class AgreementAcceptRequest {
  private String code;
  private String version;
  private String language;
  private String deviceId;
  private String client;
}

@RestController
public class AgreementController {
  @Autowired
  private AgreementMapper agreementMapper;

  @Autowired
  private UserAgreementAcceptanceMapper acceptanceMapper;

  @GetMapping(ApiPaths.Common.Agreement.DETAIL)
  public RestBean<Agreement> detail(@ModelAttribute AgreementDetailRequest query) {
    Agreement agreement = latestPublished(query.getCode(), query.getLang());
    return RestBean.success(agreement, MessageUtils.getMessage("message.get.success"));
  }

  @GetMapping(ApiPaths.Common.Agreement.LATEST)
  public RestBean<Map<String, Agreement>> latest(@RequestParam String codes, @RequestParam(required = false) String lang) {
    Map<String, Agreement> result = new LinkedHashMap<>();
    for (String code : codes.split(",")) {
      if (!StringUtils.hasText(code)) continue;
      result.put(code.trim(), latestPublished(code.trim(), lang));
    }
    return RestBean.success(result, MessageUtils.getMessage("message.get.success"));
  }

  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Agreement.ACCEPT)
  public RestBean<Object> accept(@RequestBody AgreementAcceptRequest query, HttpServletRequest request) {
    if (!StringUtils.hasText(query.getCode()) || !StringUtils.hasText(query.getVersion())) {
      return RestBean.error(500, MessageUtils.getMessage("message.parameter.error"));
    }
    Integer userId = StpUtil.getLoginIdAsInt();
    String lang = StringUtils.hasText(query.getLanguage()) ? query.getLanguage() : "ja";

    UserAgreementAcceptance data = new UserAgreementAcceptance();
    data.setUserId(userId);
    data.setAgreementCode(query.getCode());
    data.setAgreementVersion(query.getVersion());
    data.setLanguage(lang);
    data.setAcceptedAt(new Date());
    data.setClient(query.getClient());
    data.setDeviceId(query.getDeviceId());
    data.setIp(clientIp(request));
    data.setDeleted(0);

    QueryWrapper<UserAgreementAcceptance> existing = new QueryWrapper<UserAgreementAcceptance>()
        .eq("user_id", userId)
        .eq("agreement_code", query.getCode())
        .eq("agreement_version", query.getVersion())
        .eq("language", lang)
        .eq("deleted", 0)
        .last("LIMIT 1");
    UserAgreementAcceptance old = acceptanceMapper.selectOne(existing);
    if (old == null) {
      acceptanceMapper.insert(data);
    } else {
      acceptanceMapper.update(
          null,
          new UpdateWrapper<UserAgreementAcceptance>()
              .set("accepted_at", data.getAcceptedAt())
              .set("client", data.getClient())
              .set("device_id", data.getDeviceId())
              .set("ip", data.getIp())
              .eq("id", old.getId())
      );
    }
    return RestBean.success(null, MessageUtils.getMessage("message.save.success"));
  }

  private Agreement latestPublished(String code, String lang) {
    String language = StringUtils.hasText(lang) ? lang : "ja";
    return agreementMapper.selectOne(new QueryWrapper<Agreement>()
        .eq("code", code)
        .eq("language", language)
        .eq("status", "PUBLISHED")
        .eq("deleted", 0)
        .orderByDesc("published_at")
        .orderByDesc("id")
        .last("LIMIT 1"));
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (StringUtils.hasText(forwarded)) return forwarded.split(",")[0].trim();
    return request.getRemoteAddr();
  }
}
