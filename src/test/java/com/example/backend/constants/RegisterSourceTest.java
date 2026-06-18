package com.example.backend.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterSourceTest {

  @Test
  void normalize_mapsClientAliases() {
    assertEquals(RegisterSource.H5, RegisterSource.normalize("movie-h5"));
    assertEquals(RegisterSource.IOS, RegisterSource.normalize("iOS"));
    assertEquals(RegisterSource.ANDROID, RegisterSource.normalize("android"));
    assertEquals(RegisterSource.UNKNOWN, RegisterSource.normalize(null));
    assertEquals(RegisterSource.UNKNOWN, RegisterSource.normalize(""));
  }
}
