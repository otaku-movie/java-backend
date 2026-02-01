package com.example.backend.response.app;

import java.util.List;

public class AppRootMovieShowTimeResponse {
  private String date;
  private List<AppMovieShowTimeResponse> data;

  public String getDate() { return date; }
  public void setDate(String date) { this.date = date; }
  public List<AppMovieShowTimeResponse> getData() { return data; }
  public void setData(List<AppMovieShowTimeResponse> data) { this.data = data; }
}
