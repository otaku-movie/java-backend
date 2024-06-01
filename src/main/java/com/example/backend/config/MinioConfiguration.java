package com.example.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "minio")
public  class MinioConfiguration {
  String accessKey;
  String secretKey;
  String endpoint;
  String previewURL;
  String bucket;
}
