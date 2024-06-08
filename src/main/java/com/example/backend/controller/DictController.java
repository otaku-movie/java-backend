package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.Dict;
import com.example.backend.entity.DictItem;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.DictItemMapper;
import com.example.backend.mapper.DictMapper;
import com.example.backend.query.MovieListQuery;
import com.example.backend.utils.MessageUtils;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
class specifyQuery {
  private  List<String> code;
}

@Data
class DictItemEditQuery {
  private Integer dictId;
  private List<DictItem> dictItem;
}

@RestController
public class DictController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private DictMapper dictMapper;
  @Autowired
  private DictItemMapper dictItemMapper;

  @PostMapping("/api/dict/list")
  public RestBean<List<Object>> list(@RequestBody MovieListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<Dict> page = new Page<>(query.getPage(), query.getPageSize());

    IPage list = dictMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/dict/detail")
  public RestBean<List<Object>> dictItemlist(@RequestParam Integer id)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("dict_id", id);

    List list = dictItemMapper.selectList(wrapper);

    return RestBean.success(list, MessageUtils.getMessage("success.get"));
  }
  // 根据 name 获取字典
  @PostMapping("/api/dict/specify")
  public RestBean<Map<String, List<DictItem>>> specify(@RequestBody specifyQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.in("code", query.getCode());
    List<Dict> list = dictMapper.selectList(wrapper);

    Map<String, List<DictItem>> result = list.stream().collect(
      Collectors.toMap(Dict::getCode, dict -> {
        QueryWrapper<DictItem> dictItemQueryWrapper = new QueryWrapper<>();
        dictItemQueryWrapper.eq("dict_id", dict.getId()); // 设置查询条件
        return dictItemMapper.selectList(dictItemQueryWrapper);
      })
    );

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @SaCheckLogin
  @CheckPermission(code = "dict.item.save")
  @Transactional
  @PostMapping("/api/admin/dict/item/save")
  public RestBean<Null> list(@RequestBody DictItemEditQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("dict_id", query.getDictId());
    dictItemMapper.delete(wrapper);

    query.getDictItem().stream().forEach(item -> {
      DictItem dictItem = new DictItem();
      dictItem.setDictId(item.getDictId());
      dictItem.setCode(item.getCode());
      dictItem.setName(item.getName());
      dictItemMapper.insert(dictItem);
    });

    return RestBean.success(null, MessageUtils.getMessage("success.save"));
  }
  @SaCheckLogin
  @CheckPermission(code = "dict.item.remove")
  @Transactional
  @DeleteMapping("/api/admin/dict/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("dict_id", id);

    dictItemMapper.delete(wrapper);
    dictMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
}
