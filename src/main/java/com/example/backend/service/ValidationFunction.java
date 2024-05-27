package com.example.backend.service;

@FunctionalInterface
public interface ValidationFunction<T> {
  boolean validate(T old, T data);
}
