package com.example.backend.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.Language;
import com.example.backend.entity.MovieShowTimeTag;
import com.example.backend.response.showTime.MovieShowTimeDetail;
import com.example.backend.typeHandler.IntegerArrayTypeHandler;
import lombok.Data;

import java.util.List;

@Data
public class MovieShowTimeList extends MovieShowTimeDetail {
//    Integer id;
    Boolean open;
//    String startTime;
//    String endTime;
//    Integer status;
//    Integer movieId;
//    String movieName;
//    String moviePoster;
//    Integer cinemaId;
//    String cinemaName;
//    Integer theaterHallId;
//    String theaterHallName;
//    String theaterHallSpec;
      String theaterHallSpec;
     Integer seatCount;

}
