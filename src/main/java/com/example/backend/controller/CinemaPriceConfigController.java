package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Dict;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.CinemaPriceConfig;
import com.example.backend.entity.DictItem;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.CinemaPriceConfigMapper;
import com.example.backend.mapper.DictItemMapper;
import com.example.backend.mapper.DictMapper;
import com.example.backend.response.cinema.CinemaPriceConfigResponse;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
class CinemaPriceConfigListQuery {
    @NotNull(message = "cinemaId 不能为空")
    private Integer cinemaId;
}

@Data
class CinemaPriceConfigSaveQuery {
    private Integer id;
    @NotNull(message = "cinemaId 不能为空")
    private Integer cinemaId;
    @NotNull(message = "dimensionType 不能为空")
    private Integer dimensionType;
    @NotNull(message = "surcharge 不能为空")
    private BigDecimal surcharge;
}

/**
 * 影院票价配置（3D 加价等）
 */
@RestController
public class CinemaPriceConfigController {

    @Autowired
    private CinemaPriceConfigMapper cinemaPriceConfigMapper;
    @Autowired
    private DictItemMapper dictItemMapper;
    @Autowired
    private DictMapper dictMapper;
    @Autowired
    private MessageUtils messageUtils;

    @PostMapping(ApiPaths.Admin.Cinema.PRICE_CONFIG_LIST)
    public RestBean<List<CinemaPriceConfigResponse>> list(@RequestBody @Validated CinemaPriceConfigListQuery query) {
        QueryWrapper<CinemaPriceConfig> qw = new QueryWrapper<>();
        qw.eq("cinema_id", query.getCinemaId()).orderByAsc("dimension_type");
        List<CinemaPriceConfig> configs = cinemaPriceConfigMapper.selectList(qw);

        List<Integer> dimensionTypeCodes = configs.stream()
                .map(CinemaPriceConfig::getDimensionType)
                .distinct()
                .collect(Collectors.toList());

        final Map<Integer, String> nameMap;
        if (dimensionTypeCodes.isEmpty()) {
            nameMap = Map.of();
        } else {
            Dict dict = dictMapper.selectOne(new QueryWrapper<Dict>().eq("code", "dimensionType").last("LIMIT 1"));
            List<DictItem> items = dict != null
                    ? dictItemMapper.selectList(new QueryWrapper<DictItem>().eq("dict_id", dict.getId()).in("code", dimensionTypeCodes))
                    : List.of();
            nameMap = items.stream().collect(Collectors.toMap(DictItem::getCode, DictItem::getName, (a, b) -> a));
        }

        List<CinemaPriceConfigResponse> result = configs.stream().map(c -> {
            CinemaPriceConfigResponse r = new CinemaPriceConfigResponse();
            r.setId(c.getId());
            r.setCinemaId(c.getCinemaId());
            r.setDimensionType(c.getDimensionType());
            r.setDimensionTypeName(nameMap.getOrDefault(c.getDimensionType(), ""));
            r.setSurcharge(c.getSurcharge());
            return r;
        }).collect(Collectors.toList());

        return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
    }

    @SaCheckLogin
    @CheckPermission(code = "cinema.save")
    @PostMapping(ApiPaths.Admin.Cinema.PRICE_CONFIG_SAVE)
    public RestBean<Null> save(@RequestBody @Validated CinemaPriceConfigSaveQuery query) {
        CinemaPriceConfig entity = new CinemaPriceConfig();
        entity.setCinemaId(query.getCinemaId());
        entity.setDimensionType(query.getDimensionType());
        entity.setSurcharge(query.getSurcharge());

        if (query.getId() != null) {
            entity.setId(query.getId());
            cinemaPriceConfigMapper.updateById(entity);
        } else {
            QueryWrapper<CinemaPriceConfig> qw = new QueryWrapper<>();
            qw.eq("cinema_id", query.getCinemaId()).eq("dimension_type", query.getDimensionType());
            CinemaPriceConfig existing = cinemaPriceConfigMapper.selectOne(qw);
            if (existing != null) {
                existing.setSurcharge(query.getSurcharge());
                cinemaPriceConfigMapper.updateById(existing);
            } else {
                cinemaPriceConfigMapper.insert(entity);
            }
        }
        return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
    }

    @SaCheckLogin
    @CheckPermission(code = "cinema.remove")
    @DeleteMapping(ApiPaths.Admin.Cinema.PRICE_CONFIG_REMOVE)
    public RestBean<Null> remove(@RequestParam Integer id) {
        if (id == null) {
            return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(),
                    MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
        }
        cinemaPriceConfigMapper.deleteById(id);
        return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.REMOVE_SUCCESS));
    }
}
