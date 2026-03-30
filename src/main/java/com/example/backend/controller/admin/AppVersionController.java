package com.example.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.AppVersion;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.AppVersionMapper;
import com.example.backend.query.PaginationQuery;
import com.example.backend.utils.MessageUtils;
import org.springframework.util.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
class AppVersionListQuery extends PaginationQuery {
  private String platform;
}

@Data
class AppVersionSaveQuery {
  private Integer id;
  private String platform;
  private Integer versionCode;
  private String versionName;
  private Integer buildNumber;
  private String downloadUrl;
  private String updateMessage;
  private Boolean forceUpdate;
  private Boolean isForceUpdate;
  private String minSupportedVersion;
  private Boolean isLatest;
  private String releaseNoteZh;
  private String releaseNoteJa;
  private String releaseNoteEn;
  private String releaseNoteInternal;
  private Integer releasePercent;
}

@Data
class AppVersionSetLatestQuery {
  private Integer id;
}

@Data
class AppVersionDetailQuery {
  private Integer id;
}

@RestController("adminAppVersionController")
public class AppVersionController {
  @Autowired
  private AppVersionMapper appVersionMapper;


  @PostMapping(ApiPaths.Admin.AppVersion.LIST)
  public RestBean<List<AppVersion>> list(@RequestBody AppVersionListQuery query) {
    QueryWrapper<AppVersion> wrapper = new QueryWrapper<>();
    Page<AppVersion> page = new Page<>(query.getPage(), query.getPageSize());
    wrapper.eq("deleted", 0);

    if (query.getPlatform() != null) {
      wrapper.eq("platform", query.getPlatform());
    }
    wrapper.orderByDesc("is_latest");
    wrapper.orderByDesc("build_number");
    wrapper.orderByDesc("version_code");
    wrapper.orderByDesc("id");

    IPage<AppVersion> result = appVersionMapper.selectPage(page, wrapper);

    return RestBean.success(result.getRecords(), query.getPage(), result.getTotal(), query.getPageSize());
  }

  @GetMapping(ApiPaths.Admin.AppVersion.DETAIL)
  public RestBean<AppVersion> detail(@ModelAttribute AppVersionDetailQuery query) {
    AppVersion data = appVersionMapper.selectOne(new QueryWrapper<AppVersion>()
        .eq("id", query.getId())
        .eq("deleted", 0)
        .last("LIMIT 1"));
    return RestBean.success(data, MessageUtils.getMessage("message.get.success"));
  }

  @PostMapping(ApiPaths.Admin.AppVersion.SAVE)
  public RestBean<Object> save(@RequestBody AppVersionSaveQuery query) {
    if (!StringUtils.hasText(query.getPlatform())) {
      return RestBean.error(500, MessageUtils.getMessage("message.parameter.error"));
    }
    if (!StringUtils.hasText(query.getVersionName())) {
      return RestBean.error(500, MessageUtils.getMessage("message.parameter.error"));
    }
    Integer buildNumber = query.getBuildNumber();
    if (buildNumber == null) {
      if (query.getId() == null) {
        buildNumber = nextBuildNumberForPlatform(query.getPlatform());
      } else {
        AppVersion existing = appVersionMapper.selectOne(new QueryWrapper<AppVersion>()
            .eq("id", query.getId())
            .eq("deleted", 0)
            .last("LIMIT 1"));
        if (existing == null) {
          return RestBean.error(500, MessageUtils.getMessage("message.parameter.error"));
        }
        buildNumber = existing.getBuildNumber();
      }
    }
    // 同平台 buildNumber 不重复
    QueryWrapper<AppVersion> duplicate = new QueryWrapper<AppVersion>()
        .eq("deleted", 0)
        .eq("platform", query.getPlatform())
        .eq("build_number", buildNumber);
    if (query.getId() != null) duplicate.ne("id", query.getId());
    if (appVersionMapper.selectCount(duplicate) > 0) {
      return RestBean.error(500, "buildNumber duplicated in platform");
    }

    AppVersion model = new AppVersion();
    model.setId(query.getId());
    model.setPlatform(query.getPlatform());
    model.setVersionCode(query.getVersionCode() == null ? buildNumber : query.getVersionCode());
    model.setVersionName(query.getVersionName());
    model.setBuildNumber(buildNumber);
    model.setDownloadUrl(query.getDownloadUrl());
    model.setUpdateMessage(query.getUpdateMessage());
    model.setForceUpdate(Boolean.TRUE.equals(query.getForceUpdate()));
    model.setIsForceUpdate(Boolean.TRUE.equals(query.getIsForceUpdate()) || Boolean.TRUE.equals(query.getForceUpdate()));
    model.setMinSupportedVersion(query.getMinSupportedVersion());
    model.setIsLatest(Boolean.TRUE.equals(query.getIsLatest()));
    model.setReleaseNoteZh(query.getReleaseNoteZh());
    model.setReleaseNoteJa(query.getReleaseNoteJa());
    model.setReleaseNoteEn(query.getReleaseNoteEn());
    model.setReleaseNoteInternal(query.getReleaseNoteInternal());
    model.setReleasePercent(query.getReleasePercent() == null ? 100 : query.getReleasePercent());
    model.setDeleted(0);

    if (Boolean.TRUE.equals(model.getIsLatest())) {
      appVersionMapper.update(
          null,
          new UpdateWrapper<AppVersion>()
              .set("is_latest", false)
              .eq("deleted", 0)
              .eq("platform", model.getPlatform())
      );
    }

    if (model.getId() == null) appVersionMapper.insert(model);
    else appVersionMapper.updateById(model);

    return RestBean.success(null, MessageUtils.getMessage("message.save.success"));
  }

  @DeleteMapping(ApiPaths.Admin.AppVersion.REMOVE)
  public RestBean<Object> remove(@ModelAttribute AppVersionDetailQuery query) {
    appVersionMapper.update(
        null,
        new UpdateWrapper<AppVersion>()
            .set("deleted", 1)
            .eq("id", query.getId())
            .eq("deleted", 0)
    );
    return RestBean.success(null, MessageUtils.getMessage("message.remove.success"));
  }

  @PostMapping(ApiPaths.Admin.AppVersion.SET_LATEST)
  public RestBean<Object> setLatest(@RequestBody AppVersionSetLatestQuery query) {
    if (query.getId() == null) {
      return RestBean.error(500, MessageUtils.getMessage("message.parameter.error"));
    }
    AppVersion target = appVersionMapper.selectOne(new QueryWrapper<AppVersion>()
        .eq("id", query.getId())
        .eq("deleted", 0)
        .last("LIMIT 1"));
    if (target == null) {
      return RestBean.error(500, MessageUtils.getMessage("message.parameter.error"));
    }
    appVersionMapper.update(
        null,
        new UpdateWrapper<AppVersion>()
            .set("is_latest", false)
            .eq("deleted", 0)
            .eq("platform", target.getPlatform())
    );
    appVersionMapper.update(
        null,
        new UpdateWrapper<AppVersion>()
            .set("is_latest", true)
            .eq("id", target.getId())
            .eq("deleted", 0)
    );
    return RestBean.success(null, MessageUtils.getMessage("message.save.success"));
  }

  /** 新建版本未传 buildNumber 时，按平台取当前最大 build + 1 */
  private int nextBuildNumberForPlatform(String platform) {
    AppVersion maxRow = appVersionMapper.selectOne(new QueryWrapper<AppVersion>()
        .eq("deleted", 0)
        .eq("platform", platform)
        .orderByDesc("build_number")
        .last("LIMIT 1"));
    if (maxRow == null || maxRow.getBuildNumber() == null) {
      return 1;
    }
    return maxRow.getBuildNumber() + 1;
  }

}
