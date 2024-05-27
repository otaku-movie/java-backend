package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Service;



@Service
public class GenericService<T> {

  public boolean save(T data, QueryWrapper<T> queryWrapper, UpdateWrapper<T> updateWrapper, Integer id, ValidationFunction<T> validationFunction, BaseMapper<T> mapper) {
    if (!validate(data, id, validationFunction, queryWrapper, mapper)) {
      return false;
    }
    if (id == null) {
      // 新增操作
      mapper.insert(data);
    } else {
      // 更新操作
      updateWrapper.eq("id", id);
      mapper.update(data, updateWrapper);
    }
    return true;
  }

  public boolean validate(T data, Integer id, ValidationFunction<T> validationFunction, QueryWrapper<T> queryWrapper, BaseMapper<T> mapper) {
    if (id != null) {
      T old = mapper.selectById(id);
      if (old == null) {
        return false; // 表示角色不存在
      }

      if (!validationFunction.validate(old, data)) {
        // 名称发生变化，需要检查新名称是否存在
        if (mapper.selectCount(queryWrapper) > 0) {
          return false; // 表示角色已经存在
        }
      }
    } else {
      // 新增操作
      if (mapper.selectCount(queryWrapper) > 0) {
        return false; // 表示角色已经存在
      }
    }
    return true;
  }
}