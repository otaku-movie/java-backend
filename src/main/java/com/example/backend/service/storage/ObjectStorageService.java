package com.example.backend.service.storage;

import java.io.File;

public interface ObjectStorageService {
  UploadResult uploadFile(File file, String key, String contentType);

  void delete(String key);

  /**
   * 返回可直接访问的 URL（基于 storage.public-url 拼接）。
   */
  String publicUrl(String key);
}

