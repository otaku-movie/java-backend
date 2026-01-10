package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
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
  @DeleteMapping(ApiPaths.Admin.Character.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    characterMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "character.save")
  @PostMapping(ApiPaths.Admin.Character.SAVE)
  public RestBean<Object> save(@RequestBody @Validated CharacterSaveQuery query)  {
    return  staffCharacterService.saveStaffCharacter(query);
  }
}
