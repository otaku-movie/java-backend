package com.example.backend.service.storage;

import java.io.File;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.backend.config.StorageProperties;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3CompatibleObjectStorageService implements ObjectStorageService {

  private final StorageProperties props;
  private final ObjectProvider<S3Client> s3ClientProvider;

  @Override
  public UploadResult uploadFile(File file, String key, String contentType) {
    S3Client s3Client = s3ClientProvider.getIfAvailable();
    if (s3Client == null || !StringUtils.hasText(props.getBucket())) {
      throw new IllegalStateException("Storage 未配置（endpoint/bucket/access-key/secret-key）");
    }

    final var put = PutObjectRequest.builder()
      .bucket(props.getBucket())
      .key(key)
      .contentType(StringUtils.hasText(contentType) ? contentType : "application/octet-stream")
      // 让浏览器/Cloudflare 边缘缓存 30 天（与 Cache Rules 配合）
      .cacheControl("public, max-age=2592000, immutable")
      .build();

    s3Client.putObject(put, RequestBody.fromFile(file));

    return new UploadResult(key, publicUrl(key));
  }

  @Override
  public void delete(String key) {
    S3Client s3Client = s3ClientProvider.getIfAvailable();
    if (s3Client == null || !StringUtils.hasText(props.getBucket())) {
      throw new IllegalStateException("Storage 未配置（endpoint/bucket/access-key/secret-key）");
    }

    final var del = DeleteObjectRequest.builder()
      .bucket(props.getBucket())
      .key(key)
      .build();
    s3Client.deleteObject(del);
  }

  @Override
  public String publicUrl(String key) {
    final var base = StringUtils.trimTrailingCharacter(
      StringUtils.hasText(props.getPublicUrl()) ? props.getPublicUrl() : "",
      '/'
    );
    if (!StringUtils.hasText(base)) {
      // 没配置 publicUrl 时至少返回 key，避免 NPE
      return "/" + key;
    }
    return base + "/" + key;
  }
}

