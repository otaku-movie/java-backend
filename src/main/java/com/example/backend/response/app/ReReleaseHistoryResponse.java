package com.example.backend.response.app;

import lombok.Data;

/**
 * App：电影详情页的重映历史条目
 */
@Data
public class ReReleaseHistoryResponse {
    private Integer id;
    private String startDate;
    private String endDate;
    private Integer status;
    private String versionInfo;
    private String displayNameOverride;
    private String posterOverride;
    private Integer timeOverride;
}

