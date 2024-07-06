package com.example.backend.response;

import lombok.Data;

import java.util.List;

@Data
public class MovieShowTimeList {
    private Integer id;
    private Boolean open;
    private String start_time;
    private String end_time;
    private Integer status;
    private Integer movie_id;
    private String movie_name;
    private String movie_cover;
    private Integer cinema_id;
    private String cinema_name;
    private Integer theater_hall_id;
    private String theater_hall_name;
    private String theater_hall_spec;
    private long selected_seat_count;
    private List<Spec> spec;
    private  Integer seat_count;
    private Integer subtitle_id;
    private String subtitle_name;
    private Integer show_time_tag_id;
    private String show_time_tag_name;
}
