package com.example.backend.config;

import io.minio.MinioClient;

public class MinIOConfig {
  public String bucket = "test-comic-and-novel";
  public MinioClient getMinioClient () {
    return MinioClient.builder()
      .endpoint("http://43.154.172.94:9000/")
      .credentials("X1V3A40fyPQWLgsuWBvc", "w8eyeUL3RIU3p5fYzdXvD4oEG7WjQABysKILqf5M")
      .build();
  }
}
