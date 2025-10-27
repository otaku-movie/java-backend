package com.example.backend.query.order;

import com.example.backend.query.PaginationQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MyTicketsQuery extends PaginationQuery {
    private Integer userId;
    // 可以添加其他筛选条件，比如电影名称、影院名称等
    private String movieName;
    private String cinemaName;
}
