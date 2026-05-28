package com.example.backend.controller;

import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.service.storage.ObjectStorageService;
import com.example.backend.service.storage.UploadResult;
import com.example.backend.utils.MessageUtils;
import com.example.backend.utils.Utils;
import com.fasterxml.uuid.Generators;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
class UploadResponse {
  String url;
  String path;
}

@Slf4j
@RestController
public class UploadController {

  private final ObjectStorageService storage;

  public UploadController(ObjectStorageService storage) {
    this.storage = storage;
  }

  @PostMapping(value = ApiPaths.Upload.UPLOAD, consumes = "multipart/form-data")
  public RestBean<UploadResponse> upload(MultipartFile file) throws IOException {
    try {
      String uuid = Generators.timeBasedEpochGenerator().generate().toString().replace("-", "");
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      File f = Utils.MultipartToFile(file);
      String filename = f.getName();
      String date = format.format(new Date());
      String ext = filename.substring(filename.lastIndexOf("."));
      String key = date + "/image/" + uuid + ext;

      UploadResult result = storage.uploadFile(f, key, file.getContentType());

      UploadResponse map = new UploadResponse();

      map.setPath(result.getKey());
      map.setUrl(result.getUrl());

      return RestBean.success(map, MessageUtils.getMessage(MessageKeys.Upload.SUCCESS));
    } catch (IOException e) {
      log.error("Upload failed", e);
      return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Upload.ERROR));
    } catch (Exception e) {
      log.error("Upload failed", e);
      return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Upload.ERROR));
    }
  }


  @DeleteMapping(ApiPaths.Upload.DELETE)
  public RestBean<Null> delete(@Validated @RequestParam @NotEmpty(message = "path 不能为空") String path ) {
    if (!StringUtils.hasText(path)) {
      return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Upload.ERROR));
    }
    storage.delete(path);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
  }
}
