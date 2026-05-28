package com.example.backend.service.storage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResult {
  private String key;
  private String url;
}

