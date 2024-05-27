package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.StaffCharacter;
import com.example.backend.query.CharacterSaveQuery;

/**
* @author last order
* @description 针对表【character】的数据库操作Service
* @createDate 2024-05-27 10:28:55
*/
public interface StaffCharacterService extends IService<StaffCharacter> {

  RestBean<Object> saveStaffCharacter(CharacterSaveQuery query);
}
