package com.example.backend.response;

import lombok.Data;

import java.util.List;

@Data
public class MovieShowTimeList {
    private Integer id;
    private Boolean open;
    private String startTime;
    private String endTime;
    private Integer status;
    private Integer movieId;
    private String movieName;
    private String movieCover;
    private Integer cinemaId;
    private String cinemaName;
    private Integer theaterHallId;
    private String theaterHallName;
    private String theaterHallSpec;
    private long selectedSeatCount;
    private List<Spec> spec;
    private  Integer seatCount;
    private Integer subtitleId;
    private String subtitleName;
    private Integer showTimeTagId;
    private String showTimeTagName;
}
