package com.example.backend.utils;

import java.util.List;
import java.util.Objects;

public final class ManualFieldLockUtils {
  private ManualFieldLockUtils() {
  }

  public static void addIfChanged(List<String> fields, String fieldName, Object oldValue, Object newValue) {
    if (!Objects.equals(normalize(oldValue), normalize(newValue))) {
      fields.add(fieldName);
    }
  }

  private static Object normalize(Object value) {
    if (value instanceof String text) {
      String trimmed = text.trim();
      return trimmed.isEmpty() ? null : trimmed;
    }
    return value;
  }
}
