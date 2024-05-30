package com.example.backend.controller.admin;

import com.example.backend.entity.RestBean;
import com.example.backend.mapper.CharacterMapper;
import com.example.backend.query.CharacterSaveQuery;
import com.example.backend.service.StaffCharacterService;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminCharacterController {
  @Autowired
  private CharacterMapper characterMapper;

  @Autowired
  private StaffCharacterService staffCharacterService;

  @DeleteMapping("/api/admin/character/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    characterMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
  @PostMapping("/api/admin/character/save")
  public RestBean<Object> save(@RequestBody @Validated CharacterSaveQuery query)  {
    return  staffCharacterService.saveStaffCharacter(query);
  }
}
