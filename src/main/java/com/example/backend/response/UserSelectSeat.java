package com.example.backend.response;

import lombok.Data;

import java.util.List;

@Data
public class UserSelectSeat {
  private Integer movieShowTimeId;
  private Integer movieId;


  private String movieName;


  private String moviePoster;


  private String date;


  private String startTime;


  private String endTime;


  private String specName;


  private Integer cinemaId;


  private String cinemaName;


  private Integer theaterHallId;

  private String theaterHallName;

  List<UserSelectSeatList> seat;
}
