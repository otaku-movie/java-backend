package com.example.backend.response;

import lombok.Data;

import java.util.List;

@Data
public class MovieShowTimeList {
    private Integer id;
    private String startTime;
    private String endTime;
    private Integer status;
    private Integer movieId;
    private String movieName;
    private Integer cinemaId;
    private String cinemaName;
    private Integer theaterHallId;
    private String theaterHallName;
    private Integer theaterHallSpec;
    private Long seatTotal;
    private long selectedSeatCount;
    private List<Spec> spec;
}
