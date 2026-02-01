package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.Character;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.StaffCharacter;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.CharacterMapper;
import com.example.backend.mapper.StaffCharacterMapper;
import com.example.backend.query.CharacterSaveQuery;
import com.example.backend.service.GenericService;
import com.example.backend.service.StaffCharacterService;
import com.example.backend.service.ValidationFunction;
import com.example.backend.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
* @author last order
* @description 针对表【button】的数据库操作Service实现
* @createDate 2024-05-24 17:37:24
*/
@Slf4j
@Service
public class StaffCharacterServiceImpl extends ServiceImpl<StaffCharacterMapper, StaffCharacter>
    implements StaffCharacterService {
  @Autowired
  private CharacterMapper characterMapper;

  @Autowired
  private GenericService<Character> genericService;

  @Autowired
  private StaffCharacterMapper staffCharacterMapper;

  @Transactional
  public void saveCharacter(Character data, CharacterSaveQuery query) {
    Integer id = null;
    if (query.getId() == null) {
      id = characterMapper.insert(data);

      log.debug("插入角色成功 id={}", id);
    } else  {
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      characterMapper.update(data, updateQueryWrapper);
    }

    QueryWrapper queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("character_id", query.getId() == null ? id : query.getId());
    staffCharacterMapper.delete(queryWrapper);

    this.saveBatch(
      query.getStaffId().stream()
        .map(item -> {
          StaffCharacter staffCharacter = new StaffCharacter();
          staffCharacter.setCharacterId(data.getId());
          staffCharacter.setStaffId(item);
          return staffCharacter;
        })
        .collect(Collectors.toList())
    );
  }

  @Transactional
  public RestBean<Object> saveStaffCharacter(CharacterSaveQuery query) {
    Character data = new Character();

    data.setCover(query.getCover());
    data.setOriginalName(query.getOriginalName());
    data.setName(query.getName());
    data.setDescription(query.getDescription());

    // 添加的去重查询条件
    QueryWrapper<Character> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("name", query.getName());
    // 编辑的去重查询条件
    UpdateWrapper<Character> updateWrapper = new UpdateWrapper<>();
    updateWrapper.eq("id", query.getId());
    // 自定义验证方法
    ValidationFunction<Character> validationFunction = (old, newData) -> old.getName().equals(newData.getName());

    boolean result = genericService.validate(data, query.getId(), validationFunction, queryWrapper, characterMapper);

    if (query.getId() == null) {
      if (result) {
        saveCharacter(data, query);
        return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Error.REPEAT));
      }
    } else {
      if (result) {
        data.setId(query.getId());
        saveCharacter(data, query);
        return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Error.REPEAT));
      }
    }
  }
}




