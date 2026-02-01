package com.example.backend.response.app;

public class AppBeforeMovieShowTimeResponse {
  private Integer cinemaId;
  private String cinemaName;
  private String cinemaAddress;
  private String cinemaTel;
  private Double cinemaLatitude;
  private Double cinemaLongitude;
  private Integer id;
  private Integer theaterHallId;
  private String theaterHallName;
  private String startTime;
  private String endTime;
  private String specName;
  /** 放映类型：dict_item.id，2D/3D */
  private Integer dimensionType;
  private Integer totalSeats;
  private Integer selectedSeats;
  private Double distance;
  private Integer movieVersionId;
  private Integer versionCode;

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
  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public Integer getTheaterHallId() { return theaterHallId; }
  public void setTheaterHallId(Integer theaterHallId) { this.theaterHallId = theaterHallId; }
  public String getTheaterHallName() { return theaterHallName; }
  public void setTheaterHallName(String theaterHallName) { this.theaterHallName = theaterHallName; }
  public String getStartTime() { return startTime; }
  public void setStartTime(String startTime) { this.startTime = startTime; }
  public String getEndTime() { return endTime; }
  public void setEndTime(String endTime) { this.endTime = endTime; }
  public String getSpecName() { return specName; }
  public void setSpecName(String specName) { this.specName = specName; }
  public Integer getDimensionType() { return dimensionType; }
  public void setDimensionType(Integer dimensionType) { this.dimensionType = dimensionType; }
  public Integer getTotalSeats() { return totalSeats; }
  public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
  public Integer getSelectedSeats() { return selectedSeats; }
  public void setSelectedSeats(Integer selectedSeats) { this.selectedSeats = selectedSeats; }
  public Double getDistance() { return distance; }
  public void setDistance(Double distance) { this.distance = distance; }
  public Integer getMovieVersionId() { return movieVersionId; }
  public void setMovieVersionId(Integer movieVersionId) { this.movieVersionId = movieVersionId; }
  public Integer getVersionCode() { return versionCode; }
  public void setVersionCode(Integer versionCode) { this.versionCode = versionCode; }
}
