package com.example.backend.controller;


import com.amazonaws.services.s3.internal.eventstreaming.Message;
import com.example.backend.config.MinioConfiguration;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.utils.MessageUtils;
import com.example.backend.utils.Utils;
import com.fasterxml.uuid.Generators;
import io.minio.MinioClient;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

@Data
class UploadResponse {
  String url;
  String path;
}

@RestController
public class UploadController {

  @Autowired
  private MinioConfiguration minioConfiguration;

  private static ByteBuffer getRandomByteBuffer(int size) throws IOException {
    byte[] b = new byte[size];
    new Random().nextBytes(b);
    return ByteBuffer.wrap(b);
  }
  @PostMapping(value = "/api/upload", consumes = "multipart/form-data")
  public RestBean<UploadResponse> upload(MultipartFile file) throws IOException {
    System.out.println(minioConfiguration);

    S3Client s3Client = S3Client.builder()
      .region(Region.US_EAST_1)  // 指定区域
      .endpointOverride(URI.create(minioConfiguration.getEndpoint()))
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(
          minioConfiguration.getAccessKey(),
          minioConfiguration.getSecretKey()
        )
      ))
      .build();
    try {
//      Magic magic = new Magic();
//      MagicMatch match = magic.getMagicMatch(f, false);
      String uuid = Generators.timeBasedEpochGenerator().generate().toString().replace("-", "");
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      File f = Utils.MultipartToFile(file);
      String filename = f.getName();
      String date = format.format(new Date());
      String ext = filename.substring(filename.lastIndexOf("."));
      String path = date + "/image/" + uuid + ext;
      PutObjectRequest objectRequest = PutObjectRequest.builder()
        .bucket(minioConfiguration.getBucket())
//        .contentType()
        .key(path)
        .build();
      s3Client.putObject(objectRequest, RequestBody.fromFile(f));

      String url = new StringBuilder()
        .append(minioConfiguration.getPreviewURL())
        .append(minioConfiguration.getBucket())
        .append("/") + path;

      UploadResponse map = new UploadResponse();

      map.setPath(path);
      map.setUrl(url);

      return RestBean.success(map, MessageUtils.getMessage("successs.uploadSuccess"));
    } catch (IOException e) {
      e.printStackTrace();
      return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage("error.uploadError"));
    }
  }

  @DeleteMapping("/api/deleteFile")
  public RestBean<Null> delete(@RequestParam String path) {
    S3Client s3Client = S3Client.builder()
      .region(Region.US_EAST_1)  // 指定区域
      .endpointOverride(URI.create(minioConfiguration.getEndpoint()))
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(
          minioConfiguration.getAccessKey(),
          minioConfiguration.getSecretKey()
        )
      ))
      .build();

    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
      .bucket(minioConfiguration.getBucket())
      .key(path)
      .build();

    s3Client.deleteObject(deleteObjectRequest);

    return RestBean.success(null, MessageUtils.getMessage("success.save"));
  }
}
