package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Character;
import com.example.backend.entity.RoleMenu;
import com.example.backend.entity.StaffCharacter;
import com.example.backend.mapper.CharacterMapper;
import com.example.backend.query.CharacterListQuery;
import com.example.backend.query.CharacterSaveQuery;
import com.example.backend.response.CharacterList;
import com.example.backend.response.MovieResponse;
import com.example.backend.service.StaffCharacterService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class CharacterController {
  @Autowired
  private CharacterMapper characterMapper;

  @Autowired
  private StaffCharacterService staffCharacterService;

  @PostMapping("/api/character/list")
  public RestBean<List<CharacterList>> list(@RequestBody CharacterListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<CharacterList> page = new Page<>(query.getPage(), query.getPageSize());

    wrapper.orderByDesc("update_time");
    if (query.getName() != null && query.getName() != "") {
      wrapper.like("name", query.getName());
    }

    IPage<CharacterList> list = characterMapper.characterList(query, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }

  @GetMapping("/api/character/detail")
  public RestBean<CharacterList> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");
    QueryWrapper<CharacterList> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    CharacterList result = characterMapper.characterDetail(id);

    return RestBean.success(result, "获取成功");
  }
}
