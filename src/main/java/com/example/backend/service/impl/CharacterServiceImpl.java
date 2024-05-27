package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Character;
import com.example.backend.mapper.CharacterMapper;
import com.example.backend.service.CharacterService;
import org.springframework.stereotype.Service;

/**
* @author last order
* @description 针对表【button】的数据库操作Service实现
* @createDate 2024-05-24 17:37:24
*/
@Service
public class CharacterServiceImpl extends ServiceImpl<CharacterMapper, Character>
    implements CharacterService {

}




