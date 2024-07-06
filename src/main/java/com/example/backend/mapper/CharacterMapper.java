package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.query.CharacterListQuery;
import com.example.backend.entity.Character;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.response.CharacterList;

/**
* @author last order
* @description 针对表【character】的数据库操作Mapper
* @createDate 2024-05-27 10:28:55
* @Entity generator.domain.Character
*/
public interface CharacterMapper extends BaseMapper<Character> {
  IPage<CharacterList> characterList(CharacterListQuery query, IPage<CharacterList> page);
  CharacterList characterDetail(Integer id);
}




