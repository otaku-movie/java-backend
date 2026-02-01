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
}
