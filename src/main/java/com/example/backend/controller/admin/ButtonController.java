package com.example.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.entity.Button;
import com.example.backend.entity.Menu;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.ButtonMapper;
import com.example.backend.response.ButtonResponse;

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
class ButtonSaveQuery {
  Integer id;
  @NotEmpty(message = "{validator.saveButton.i18nKey.required}")
  String i18nKey;
  @NotNull(message = "{validator.saveButton.menuId.required}")
  Integer menuId;
  @NotEmpty(message = "{validator.saveButton.apiCode.required}")
  String apiCode;
}

@Data
class ButtonListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  // 是否平铺
  private Boolean flattern;

  public ButtonListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
    this.flattern = true;
  }
}

@RestController
public class ButtonController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private ButtonMapper buttonMapper;

  @PostMapping("/api/admin/permission/button/list")
  public RestBean<List<ButtonResponse>> list(@RequestBody ButtonListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();

    wrapper.orderByDesc("update_time");

    List list = buttonMapper.buttonList();

    return RestBean.success(list, MessageUtils.getMessage("success.get"));
  }
  @GetMapping("/api/admin/permission/button/detail")
  public RestBean<Button> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));
    QueryWrapper<Button> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Button result = buttonMapper.selectOne(queryWrapper);

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @DeleteMapping("/api/admin/permission/button/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    buttonMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
  @PostMapping("/api/admin/permission/button/save")
  public RestBean<List<Object>> save(@RequestBody @Validated ButtonSaveQuery query)  {
    Button data = new Button();

    data.setI18nKey(query.getI18nKey());
    data.setMenuId(query.getMenuId());
    data.setApiCode(query.getApiCode());

    if (query.getId() == null) {
      buttonMapper.insert(data);
      return RestBean.success(null, MessageUtils.getMessage("success.save"));
    } else {
      data.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      buttonMapper.update(data, updateQueryWrapper);

      return RestBean.success(null, MessageUtils.getMessage("success.save"));
    }
  }
}
