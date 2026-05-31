package com.example.backend.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3CompatibleStorageConfiguration {

  @Bean
  public S3Client s3Client(StorageProperties props) {
    if (!StringUtils.hasText(props.getEndpoint())
      || !StringUtils.hasText(props.getBucket())
      || !StringUtils.hasText(props.getAccessKey())
      || !StringUtils.hasText(props.getSecretKey())) {
      // 允许应用在未完整配置存储时启动；调用上传接口时再返回错误
      return null;
    }

    final var s3Config = S3Configuration.builder()
      .pathStyleAccessEnabled(props.isPathStyleAccess())
      .build();

    return S3Client.builder()
      .region(Region.of(StringUtils.hasText(props.getRegion()) ? props.getRegion() : "auto"))
      .endpointOverride(URI.create(props.getEndpoint()))
      .serviceConfiguration(s3Config)
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
        )
      )
      .build();
  }
}

