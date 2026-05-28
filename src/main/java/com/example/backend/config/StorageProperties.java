package com.example.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
  /**
   * S3 兼容 endpoint。
   * R2 示例：https://<account_id>.r2.cloudflarestorage.com
   * MinIO 示例：http://localhost:9000
   */
  private String endpoint;

  private String accessKey;
  private String secretKey;

  /**
   * R2 必须使用 region=auto（AWS SDK v2 可用 Region.of("auto")）。
   * 对于 MinIO 可随便填一个（如 us-east-1）。
   */
  private String region = "auto";

  private String bucket;

  /**
   * 对外访问的 base URL（r2.dev 或自定义域名）。
   * 示例：https://pub-xxxx.r2.dev 或 https://cdn.example.com
   */
  private String publicUrl;

  /**
   * 是否使用 path-style（http://endpoint/bucket/key）。
   * R2 推荐 false；MinIO 通常 true（取决于部署）。
   */
  private boolean pathStyleAccess = false;
}

