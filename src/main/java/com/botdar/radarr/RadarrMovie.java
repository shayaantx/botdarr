package com.botdar.radarr;

import java.util.List;

public class RadarrMovie {
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getQualityProfileId() {
    return qualityProfileId;
  }

  public void setQualityProfileId(int qualityProfileId) {
    this.qualityProfileId = qualityProfileId;
  }

  public String getTitleSlug() {
    return titleSlug;
  }

  public void setTitleSlug(String titleSlug) {
    this.titleSlug = titleSlug;
  }

  public List<RadarrImage> getImages() {
    return images;
  }

  public void setImages(List<RadarrImage> images) {
    this.images = images;
  }

  public String getRemotePoster() {
    return remotePoster;
  }

  public void setRemotePoster(String remotePoster) {
    this.remotePoster = remotePoster;
  }

  public int getTmdbId() {
    return tmdbId;
  }

  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public boolean isMonitored() {
    return monitored;
  }

  public void setMonitored(boolean monitored) {
    this.monitored = monitored;
  }

  public boolean isSearchForMovie() {
    return searchForMovie;
  }

  public void setSearchForMovie(boolean searchForMovie) {
    this.searchForMovie = searchForMovie;
  }

  private String title;
  private int qualityProfileId;
  private String titleSlug;
  private List<RadarrImage> images;
  private String remotePoster;
  private int tmdbId;
  private int year;
  private String path;
  private boolean monitored = true;
  private boolean searchForMovie = true;
}
