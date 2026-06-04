package com.example.backend.response.app;

import com.example.backend.entity.Language;
import com.example.backend.entity.MovieShowTimeTag;

import java.util.List;

public class ShowTimeInfo {
  private Integer id;
  private Integer theaterHallId;
  private String theaterHallName;
  private String startTime;
  private String endTime;
  /** 上映规格名称，多个 */
  private List<String> specNames;
  /** 放映类型：dict_item.id，2D/3D */
  private Integer dimensionType;
  private Integer totalSeats;
  private Integer selectedSeats;
  private Integer availableSeats;
  private List<Integer> subtitleId;
  private List<Integer> showTimeTagId;
  private List<Language> subtitle;
  private List<MovieShowTimeTag> showTimeTags;
  private Integer movieVersionId;
  private Integer versionCode;
  private Integer reReleaseId;
  private String reReleaseVersionInfo;
  /** 该场次是否有入场者特典（设计 3.2） */
  private Boolean hasBenefits;
  /**
   * 影院官方购票页 URL。app 侧点击「购票」时优先打开这个外部链接，
   * NULL 时回退到自家选座流程（或显示「该场次暂不支持线上购票」）。
   */
  private String reservationUrl;
  /**
   * 爬虫透传的售票/座位状态：on_sale / few / sold_out / pre_sale / sale_ended / closed / unknown。
   * 当 seat 表查不到（外部预约场次），app 端用这个字段来渲染座位状态图标。
   */
  private String saleStatus;
  /**
   * 字幕语言名称列表（来自 language.name，按 subtitle_id 原顺序）。
   * app 列表卡片用它显示"字幕：English"之类的 chip + tooltip，提示该场次为字幕版（原音 + 字幕）。
   * 注意与 {@link #subtitle} 的区别：subtitle 是完整 Language 对象（带 id/code），
   * subtitleNames 只携带显示名，便于列表场景免去逐场 join 的成本。
   */
  private List<String> subtitleNames;

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
  public List<String> getSpecNames() { return specNames; }
  public void setSpecNames(List<String> specNames) { this.specNames = specNames; }
  public Integer getDimensionType() { return dimensionType; }
  public void setDimensionType(Integer dimensionType) { this.dimensionType = dimensionType; }
  public Integer getTotalSeats() { return totalSeats; }
  public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
  public Integer getSelectedSeats() { return selectedSeats; }
  public void setSelectedSeats(Integer selectedSeats) { this.selectedSeats = selectedSeats; }
  public Integer getAvailableSeats() { return availableSeats; }
  public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
  public List<Integer> getSubtitleId() { return subtitleId; }
  public void setSubtitleId(List<Integer> subtitleId) { this.subtitleId = subtitleId; }
  public List<Integer> getShowTimeTagId() { return showTimeTagId; }
  public void setShowTimeTagId(List<Integer> showTimeTagId) { this.showTimeTagId = showTimeTagId; }
  public List<Language> getSubtitle() { return subtitle; }
  public void setSubtitle(List<Language> subtitle) { this.subtitle = subtitle; }
  public List<MovieShowTimeTag> getShowTimeTags() { return showTimeTags; }
  public void setShowTimeTags(List<MovieShowTimeTag> showTimeTags) { this.showTimeTags = showTimeTags; }
  public Integer getMovieVersionId() { return movieVersionId; }
  public void setMovieVersionId(Integer movieVersionId) { this.movieVersionId = movieVersionId; }
  public Integer getVersionCode() { return versionCode; }
  public void setVersionCode(Integer versionCode) { this.versionCode = versionCode; }
  public Integer getReReleaseId() { return reReleaseId; }
  public void setReReleaseId(Integer reReleaseId) { this.reReleaseId = reReleaseId; }
  public String getReReleaseVersionInfo() { return reReleaseVersionInfo; }
  public void setReReleaseVersionInfo(String reReleaseVersionInfo) { this.reReleaseVersionInfo = reReleaseVersionInfo; }
  public Boolean getHasBenefits() { return hasBenefits; }
  public void setHasBenefits(Boolean hasBenefits) { this.hasBenefits = hasBenefits; }
  public String getReservationUrl() { return reservationUrl; }
  public void setReservationUrl(String reservationUrl) { this.reservationUrl = reservationUrl; }
  public String getSaleStatus() { return saleStatus; }
  public void setSaleStatus(String saleStatus) { this.saleStatus = saleStatus; }
  public List<String> getSubtitleNames() { return subtitleNames; }
  public void setSubtitleNames(List<String> subtitleNames) { this.subtitleNames = subtitleNames; }
}
