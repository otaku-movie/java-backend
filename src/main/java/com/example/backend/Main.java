package com.example.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Main {
  @GetMapping("/api/test")
  public String main() {
    return "hello world";
  }
}