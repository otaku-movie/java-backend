package com.example.backend.response.reRelease;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ReReleaseListResponse {
  Integer id;
  Integer movieId;
  String name;
  String cover;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+9")
  Date startDate;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+9")
  Date endDate;
}
