package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Character;
import com.example.backend.entity.MovieCharacter;
import com.example.backend.mapper.CharacterMapper;
import com.example.backend.mapper.MovieCharacterMapper;
import com.example.backend.service.CharacterService;
import com.example.backend.service.MovieCharacterService;
import org.springframework.stereotype.Service;

/**
* @author last order
* @description 针对表【button】的数据库操作Service实现
* @createDate 2024-05-24 17:37:24
*/
@Service
public class MovieCharacterServiceImpl extends ServiceImpl<MovieCharacterMapper, MovieCharacter>
    implements MovieCharacterService {

}




