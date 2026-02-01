package com.example.backend.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.Dict;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.SelectSeat;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.enumerate.SeatState;
import com.example.backend.entity.SeatArea;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.DictMapper;
import com.example.backend.mapper.CinemaPriceConfigMapper;
import com.example.backend.mapper.DictItemMapper;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.SeatAreaMapper;
import com.example.backend.mapper.SeatMapper;
import com.example.backend.entity.DictItem;
import com.example.backend.response.SeatListResponse;
import com.example.backend.response.SpecPriceItem;
import com.example.backend.response.UserSelectSeat;
import com.example.backend.response.UserSelectSeatList;
import com.example.backend.service.SelectSeatService;
import com.example.backend.utils.MessageUtils;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;

@RestController
public class SelectSeatController {

    @Autowired
    private MovieShowTimeMapper movieShowTimeMapper;

    @Autowired
    CinemaMapper cinemaMapper;

    @Autowired
    CinemaPriceConfigMapper cinemaPriceConfigMapper;

    @Autowired
    DictItemMapper dictItemMapper;
    @Autowired
    DictMapper dictMapper;

    @Autowired
    private SelectSeatService selectSeatService;
    
    @Autowired
    private SeatMapper seatMapper;
    
    @Autowired
    private SeatAreaMapper seatAreaMapper;


    @SaCheckLogin
    @PostMapping(ApiPaths.Common.ShowTime.SELECT_SEAT_SAVE)
    public RestBean<Object> save(@RequestBody @Validated SaveSelectSeatQuery query) {
        Integer userId = StpUtil.getLoginIdAsInt();
        
        try {
            // 调用 Service 方法保存选座
            selectSeatService.saveSeatSelection(
                userId,
                query.getMovieShowTimeId(),
                query.getTheaterHallId(),
                query.getSeatPosition()
            );
            
            return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
        } catch (RuntimeException e) {
            // 处理业务异常
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    @SaCheckLogin
    @PostMapping(ApiPaths.Common.ShowTime.SELECT_SEAT_CANCEL)
    public RestBean<Object> cancel(@RequestBody @Validated CancelSelectSeatQuery query) {
        Integer userId = StpUtil.getLoginIdAsInt();
        
        try {
            // 调用 Service 方法取消选座
            selectSeatService.cancelSeatSelection(
                userId,
                query.getMovieShowTimeId(),
                query.getTheaterHallId(),
                query.getSeatPosition()
            );
            
            return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
        } catch (RuntimeException e) {
            // 处理业务异常
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    @SaCheckLogin
    @GetMapping(ApiPaths.Common.ShowTime.USER_SELECT_SEAT)
    public RestBean<Object> list(
            @RequestParam("movieShowTimeId") Integer movieShowTimeId) {

        Integer userId = StpUtil.getLoginIdAsInt();

        // 先从数据库获取场次、影院等基础信息（不使用数据库的选座结果）
        UserSelectSeat result = movieShowTimeMapper.userSelectSeatWithoutSpec(
                userId,
                movieShowTimeId,
                SeatState.selected.getCode());

        Integer theaterHallId = (result != null ? result.getTheaterHallId() : null);

        // 仅从 Redis 获取当前用户已选座位
        List<SelectSeat> redisSeats = (theaterHallId != null)
                ? selectSeatService.getUserSelectedSeatsFromRedis(movieShowTimeId, theaterHallId, userId)
                : Collections.emptyList();

        // 如果数据库没有基础信息且 Redis 也没有记录，直接返回空
        if (result == null && redisSeats.isEmpty()) {
            return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
        }

        // 至少要有一个承载结果的对象
        if (result == null) {
            result = new UserSelectSeat();
            result.setMovieShowTimeId(movieShowTimeId);
        }

        // 用于补充区域价格 / 名称的信息（可选）
        Map<Integer, UserSelectSeatList> baseSeatMap = null;
        if (result.getSeat() != null && !result.getSeat().isEmpty()) {
            baseSeatMap = result.getSeat().stream()
                    .filter(s -> s.getSeatId() != null)
                    .collect(Collectors.toMap(UserSelectSeatList::getSeatId, Function.identity(), (a, b) -> a));
        }

        if (!redisSeats.isEmpty()) {
            List<UserSelectSeatList> mergedSeatList = new ArrayList<>();
            for (SelectSeat seat : redisSeats) {
                UserSelectSeatList dto = new UserSelectSeatList();
                dto.setSeatId(seat.getSeatId());
                dto.setSeatName(seat.getSeatName());
                dto.setX(seat.getX());
                dto.setY(seat.getY());
                dto.setMovieTicketTypeId(seat.getMovieTicketTypeId());

                // 如果数据库有对应座位的区域信息，则补上
                if (baseSeatMap != null && seat.getSeatId() != null) {
                    UserSelectSeatList base = baseSeatMap.get(seat.getSeatId());
                    if (base != null) {
                        dto.setAreaPrice(base.getAreaPrice());
                        dto.setAreaName(base.getAreaName());
                    }
                }
                mergedSeatList.add(dto);
            }
            // 按照 x, y 坐标排序（先按 x 排序，x 相同再按 y 排序）
            mergedSeatList.sort((a, b) -> {
                int xCompare = Integer.compare(a.getX() != null ? a.getX() : 0, b.getX() != null ? b.getX() : 0);
                return xCompare != 0 ? xCompare : Integer.compare(a.getY() != null ? a.getY() : 0, b.getY() != null ? b.getY() : 0);
            });
            result.setSeat(mergedSeatList);
        } else {
            // Redis 没有记录时，视为当前没有已选座位
            result.setSeat(new ArrayList<>());
        }

        // 获取座位信息（包含区域ID）
        Map<Integer, SeatListResponse> seatInfoMap = null;
        java.util.Set<Integer> areaIds = new java.util.HashSet<>();
        if (theaterHallId != null) {
            List<SeatListResponse> allSeats = seatMapper.seatList(theaterHallId);
            if (allSeats != null && !allSeats.isEmpty()) {
                seatInfoMap = allSeats.stream()
                        .filter(s -> s.getId() != null)
                        .collect(Collectors.toMap(SeatListResponse::getId, Function.identity(), (a, b) -> a));
                
                // 收集所有区域ID（直接从 seatAreaId 字段获取）
                for (SeatListResponse seatInfo : allSeats) {
                    if (seatInfo.getSeatAreaId() != null) {
                        areaIds.add(seatInfo.getSeatAreaId());
                    }
                }
            }
        }
        
        // 批量查询区域信息（通过区域ID）
        Map<Integer, SeatArea> areaMap = new java.util.HashMap<>();
        if (!areaIds.isEmpty()) {
            List<SeatArea> areas = seatAreaMapper.selectBatchIds(areaIds);
            if (areas != null) {
                areaMap = areas.stream()
                        .filter(a -> a.getId() != null)
                        .collect(Collectors.toMap(SeatArea::getId, Function.identity(), (a, b) -> a));
            }
        }
        
        // 规格名称+价格、放映类型名称+加价（后端填充）
        if (result.getCinemaId() != null) {
            List<com.example.backend.response.Spec> specs = cinemaMapper.getCinemaSpec(result.getCinemaId());
            List<Integer> specIds = result.getSpecIds();
            if (specIds != null && !specIds.isEmpty() && !specs.isEmpty()) {
                List<SpecPriceItem> specPriceList = specs.stream()
                        .filter(s -> s.getId() != null && specIds.contains(s.getId()))
                        .map(s -> new SpecPriceItem(
                                s.getId(),
                                s.getName(),
                                s.getPlusPrice() != null ? new BigDecimal(s.getPlusPrice()) : BigDecimal.ZERO))
                        .collect(Collectors.toList());
                result.setSpecPriceList(specPriceList);
                if (!specPriceList.isEmpty()) {
                    result.setSpecName(specPriceList.get(0).getName());
                    result.setPlusPrice(specPriceList.get(0).getPlusPrice());
                }
            } else {
                result.setSpecPriceList(Collections.emptyList());
                result.setPlusPrice(BigDecimal.ZERO);
            }
            Integer dimType = result.getDimensionType(); // dict_item.code: 1=2D, 2=3D
            if (dimType != null) {
                Dict dict = dictMapper.selectOne(new QueryWrapper<Dict>().eq("code", "dimensionType").last("LIMIT 1"));
                if (dict != null) {
                    DictItem dimItem = dictItemMapper.selectOne(new QueryWrapper<DictItem>().eq("dict_id", dict.getId()).eq("code", dimType).last("LIMIT 1"));
                    if (dimItem != null) {
                        result.setDisplayTypeName(dimItem.getName());
                    }
                }
                BigDecimal surcharge = cinemaPriceConfigMapper.getSurcharge(result.getCinemaId(), dimType);
                result.setDisplayTypeSurcharge(surcharge != null ? surcharge : BigDecimal.ZERO);
            }
        }
        
        // 为每个座位补充区域信息（areaPrice 和 areaName）
        if (result.getSeat() != null && !result.getSeat().isEmpty() && seatInfoMap != null) {
            for (UserSelectSeatList seat : result.getSeat()) {
                if (seat.getSeatId() == null) continue;
                
                SeatListResponse seatInfo = seatInfoMap.get(seat.getSeatId());
                if (seatInfo == null) continue;
                
                // 补充区域价格
                if (seat.getAreaPrice() == null && seatInfo.getAreaPrice() != null) {
                    seat.setAreaPrice(BigDecimal.valueOf(seatInfo.getAreaPrice()));
                }
                
                // 补充区域名称（通过区域ID从 areaMap 获取）
                if (seat.getAreaName() == null && seatInfo.getSeatAreaId() != null) {
                    SeatArea area = areaMap.get(seatInfo.getSeatAreaId());
                    if (area != null && area.getName() != null) {
                        seat.setAreaName(area.getName());
                    }
                }
            }
        }

        return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
    }

    @GetMapping(ApiPaths.Common.ShowTime.SELECT_SEAT_LIST)
    public RestBean<Object> selectSeatList(
            @RequestParam("movieShowTimeId") Integer movieShowTimeId,
            @RequestParam("theaterHallId") Integer theaterHallId) {
        Object result = selectSeatService.selectSeatList(theaterHallId, movieShowTimeId);

        return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
    }
}
