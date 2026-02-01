package com.example.backend.response.app;

import java.util.List;

public class AppMovieShowTimeResponse {
  private Integer cinemaId;
  private String cinemaName;
  private String cinemaAddress;
  private String cinemaTel;
  private Double cinemaLatitude;
  private Double cinemaLongitude;
  private Integer totalShowTimes;
  private Double distance;
  private List<ShowTimeInfo> showTimes;

  public Integer getCinemaId() { return cinemaId; }
  public void setCinemaId(Integer cinemaId) { this.cinemaId = cinemaId; }
  public String getCinemaName() { return cinemaName; }
  public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }
  public String getCinemaAddress() { return cinemaAddress; }
  public void setCinemaAddress(String cinemaAddress) { this.cinemaAddress = cinemaAddress; }
  public String getCinemaTel() { return cinemaTel; }
  public void setCinemaTel(String cinemaTel) { this.cinemaTel = cinemaTel; }
  public Double getCinemaLatitude() { return cinemaLatitude; }
  public void setCinemaLatitude(Double cinemaLatitude) { this.cinemaLatitude = cinemaLatitude; }
  public Double getCinemaLongitude() { return cinemaLongitude; }
  public void setCinemaLongitude(Double cinemaLongitude) { this.cinemaLongitude = cinemaLongitude; }
  public Integer getTotalShowTimes() { return totalShowTimes; }
  public void setTotalShowTimes(Integer totalShowTimes) { this.totalShowTimes = totalShowTimes; }
  public Double getDistance() { return distance; }
  public void setDistance(Double distance) { this.distance = distance; }
  public List<ShowTimeInfo> getShowTimes() { return showTimes; }
  public void setShowTimes(List<ShowTimeInfo> showTimes) { this.showTimes = showTimes; }
}
