package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.backend.typeHandler.IntegerArrayTypeHandler;
import com.example.backend.typeHandler.StringArrayTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 电影票种。数组字段类型统一约定：
 * 数据库 integer[] / text[] → 后端 List&lt;Integer&gt; / List&lt;String&gt; → 接口 JSON number[] / string[]，前端用同一类型即可。
 */
@TableName(value ="movie_ticket_type", autoResultMap = true)
@Data
public class MovieTicketType implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    @TableField(value = "name")
    private String name;

    @TableField(value = "price")
    private BigDecimal price;

    /** 票种描述 */
    @TableField(value = "description")
    private String description;

    @TableField(value = "cinema_id")
    private Integer cinemaId;

    /** 是否启用，禁用后不在选座/购票中展示 */
    @TableField(value = "enabled")
    private Boolean enabled;

    /** 生效周期类型：票种专用 dict_item.code (dict.code=ticketTypeScheduleType)，1=周 2=月 3=每日 4=特定日期，在票种表单内直接配置，不关联规则 */
    @TableField(value = "schedule_type")
    private Integer scheduleType;

    /** 适用星期几，数组 1-7（1=周一…7=周日），空或 null 表示全周，仅 scheduleType=周 时使用 */
    @TableField(value = "applicable_weekdays", typeHandler = IntegerArrayTypeHandler.class)
    private List<Integer> applicableWeekdays;

    /** 每月几号生效，数组 1-31，仅 scheduleType=月 时使用 */
    @TableField(value = "applicable_month_days", typeHandler = IntegerArrayTypeHandler.class)
    private List<Integer> applicableMonthDays;

    /** 特定日期生效，格式 YYYY-MM-DD，仅 scheduleType=特定日期 时使用 */
    @TableField(value = "applicable_dates", typeHandler = StringArrayTypeHandler.class)
    private List<String> applicableDates;

    /** 每日生效开始时间 HH:mm，仅 scheduleType=每日 时使用 */
    @TableField(value = "daily_start_time")
    private String dailyStartTime;

    /** 每日生效结束时间 HH:mm，仅 scheduleType=每日 时使用 */
    @TableField(value = "daily_end_time")
    private String dailyEndTime;

    /** 排序号，越小越靠前 */
    @TableField(value = "order_num")
    private Integer orderNum;

    /**
     *
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}