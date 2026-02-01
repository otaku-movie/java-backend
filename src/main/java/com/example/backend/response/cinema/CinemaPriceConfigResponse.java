package com.example.backend.response.cinema;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 影院票价配置（3D 加价等）响应
 */
@Data
public class CinemaPriceConfigResponse {
    private Integer id;
    private Integer cinemaId;
    private Integer dimensionType;
    private String dimensionTypeName;
    private BigDecimal surcharge;
}
