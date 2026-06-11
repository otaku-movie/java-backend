package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.Agreement;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.AgreementMapper;
import com.example.backend.query.PaginationQuery;
import com.example.backend.utils.MessageUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
class AgreementListQuery extends PaginationQuery {
  private String code;
  private String language;
  private String status;
}

@Data
class AgreementSaveQuery {
  private Integer id;
  private String code;
  private String language;
  private String title;
  private String content;
  private String version;
  private String status;
  private Boolean isRequiredAccept;
  private Date effectiveAt;
}

@Data
class AgreementDetailQuery {
  private Integer id;
}

@Data
class AgreementPublishQuery {
  private Integer id;
}

@RestController("adminAgreementController")
public class AgreementController {
  @Autowired
  private AgreementMapper agreementMapper;

  @SaCheckLogin
  @PostMapping(ApiPaths.Admin.Agreement.LIST)
  public RestBean<List<Agreement>> list(@RequestBody AgreementListQuery query) {
    QueryWrapper<Agreement> wrapper = new QueryWrapper<Agreement>().eq("deleted", 0);
    if (StringUtils.hasText(query.getCode())) wrapper.eq("code", query.getCode());
    if (StringUtils.hasText(query.getLanguage())) wrapper.eq("language", query.getLanguage());
    if (StringUtils.hasText(query.getStatus())) wrapper.eq("status", query.getStatus());
    wrapper.orderByDesc("published_at").orderByDesc("id");

    Page<Agreement> page = new Page<>(query.getPage(), query.getPageSize());
    IPage<Agreement> result = agreementMapper.selectPage(page, wrapper);
    return RestBean.success(result.getRecords(), query.getPage(), result.getTotal(), query.getPageSize());
  }

  @SaCheckLogin
  @GetMapping(ApiPaths.Admin.Agreement.DETAIL)
  public RestBean<Agreement> detail(@ModelAttribute AgreementDetailQuery query) {
    Agreement data = agreementMapper.selectOne(new QueryWrapper<Agreement>()
        .eq("id", query.getId())
        .eq("deleted", 0)
        .last("LIMIT 1"));
    return RestBean.success(data, MessageUtils.getMessage("message.get.success"));
  }

  @SaCheckLogin
  @CheckPermission(code = "agreement.save")
  @PostMapping(ApiPaths.Admin.Agreement.SAVE)
  public RestBean<Object> save(@RequestBody AgreementSaveQuery query) {
    if (!StringUtils.hasText(query.getCode()) || !StringUtils.hasText(query.getLanguage())
        || !StringUtils.hasText(query.getTitle()) || !StringUtils.hasText(query.getContent())) {
      return RestBean.error(500, MessageUtils.getMessage("message.parameter.error"));
    }

    Agreement model = new Agreement();
    BeanUtils.copyProperties(query, model);
    model.setVersion(StringUtils.hasText(query.getVersion()) ? query.getVersion() : "1.0.0");
    model.setStatus(StringUtils.hasText(query.getStatus()) ? query.getStatus() : "DRAFT");
    model.setIsRequiredAccept(Boolean.TRUE.equals(query.getIsRequiredAccept()));
    model.setDeleted(0);

    if (model.getId() == null) agreementMapper.insert(model);
    else agreementMapper.updateById(model);

    return RestBean.success(null, MessageUtils.getMessage("message.save.success"));
  }

  @SaCheckLogin
  @CheckPermission(code = "agreement.publish")
  @PostMapping(ApiPaths.Admin.Agreement.PUBLISH)
  public RestBean<Object> publish(@RequestBody AgreementPublishQuery query) {
    if (query.getId() == null) {
      return RestBean.error(500, MessageUtils.getMessage("message.parameter.error"));
    }
    agreementMapper.update(
        null,
        new UpdateWrapper<Agreement>()
            .set("status", "PUBLISHED")
            .set("published_at", new Date())
            .eq("id", query.getId())
            .eq("deleted", 0)
    );
    return RestBean.success(null, MessageUtils.getMessage("message.save.success"));
  }

  @SaCheckLogin
  @CheckPermission(code = "agreement.remove")
  @DeleteMapping(ApiPaths.Admin.Agreement.REMOVE)
  public RestBean<Object> remove(@ModelAttribute AgreementDetailQuery query) {
    agreementMapper.update(
        null,
        new UpdateWrapper<Agreement>()
            .set("deleted", 1)
            .eq("id", query.getId())
            .eq("deleted", 0)
    );
    return RestBean.success(null, MessageUtils.getMessage("message.remove.success"));
  }
}
