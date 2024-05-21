package com.example.backend.response;

import lombok.Data;

@Data
class SelectSeatListResponse {
    private Integer id;
    private String name;
    private Integer seatType;
    private Integer xAxis;
    private Integer yAxis;
    private Integer zAxis;
    private String xName;
    private Boolean selected;
}
