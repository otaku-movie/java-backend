package com.example.backend.response.app;

public class AppBeforeMovieShowTimeResponse {
  private Integer cinemaId;
  private String cinemaName;
  private String cinemaAddress;
  private String cinemaTel;
  private Double cinemaLatitude;
  private Double cinemaLongitude;
  /** 当前登录用户是否已收藏该影院（未登录恒为 false） */
  private Boolean favorite;
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
  private Integer movieId;
  private java.util.List<Integer> specIds;
  private Integer reReleaseId;
  private String reReleaseVersionInfo;
  /** 影院官方购票链接（爬虫从源站抓取，可能为 null）。 */
  private String reservationUrl;
  /**
   * 爬虫透传的售票/座位状态（取自 movie_show_time.crawl_raw_json->>'status'）。
   * 取值：on_sale / few / sold_out / pre_sale / sale_ended / closed / unknown / null。
   * app 端在 seat 表无数据（外部预约场次）时优先用这个值渲染状态图标。
   */
  private String saleStatus;
  /**
   * 字幕语言名称，按 subtitle_id 原顺序用「、」拼接（例：'English、日本語'）。
   * 列表卡片需要直接展示"该场次带 X 字幕"提示，避免单看时间无法判断是字幕版还是吹替版。
   */
  private String subtitleNames;
  /** 特殊场次名/活动名（来自标题装饰，普通场次为空）。 */
  private String eventTitle;

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
  public Boolean getFavorite() { return favorite; }
  public void setFavorite(Boolean favorite) { this.favorite = favorite; }
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
  public Integer getMovieId() { return movieId; }
  public void setMovieId(Integer movieId) { this.movieId = movieId; }
  public java.util.List<Integer> getSpecIds() { return specIds; }
  public void setSpecIds(java.util.List<Integer> specIds) { this.specIds = specIds; }
  public Integer getReReleaseId() { return reReleaseId; }
  public void setReReleaseId(Integer reReleaseId) { this.reReleaseId = reReleaseId; }
  public String getReReleaseVersionInfo() { return reReleaseVersionInfo; }
  public void setReReleaseVersionInfo(String reReleaseVersionInfo) { this.reReleaseVersionInfo = reReleaseVersionInfo; }
  public String getReservationUrl() { return reservationUrl; }
  public void setReservationUrl(String reservationUrl) { this.reservationUrl = reservationUrl; }
  public String getSaleStatus() { return saleStatus; }
  public void setSaleStatus(String saleStatus) { this.saleStatus = saleStatus; }
  public String getSubtitleNames() { return subtitleNames; }
  public void setSubtitleNames(String subtitleNames) { this.subtitleNames = subtitleNames; }
  public String getEventTitle() { return eventTitle; }
  public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
}
