package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.CharacterMapper;
import com.example.backend.query.CharacterSaveQuery;
import com.example.backend.service.StaffCharacterService;
import com.example.backend.utils.MessageUtils;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminCharacterController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private CharacterMapper characterMapper;

  @Autowired
  private StaffCharacterService staffCharacterService;

  @SaCheckLogin
  @CheckPermission(code = "character.remove")
  @DeleteMapping("/api/admin/character/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    characterMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
  @SaCheckLogin
  @CheckPermission(code = "character.save")
  @PostMapping("/api/admin/character/save")
  public RestBean<Object> save(@RequestBody @Validated CharacterSaveQuery query)  {
    return  staffCharacterService.saveStaffCharacter(query);
  }
}
