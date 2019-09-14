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

  public long getTmdbId() {
    return tmdbId;
  }

  public void setTmdbId(long tmdbId) {
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

  public RadarrOptions getAddOptions() {
    return addOptions;
  }

  public void setAddOptions(RadarrOptions addOptions) {
    this.addOptions = addOptions;
  }

  public boolean isHasFile() {
    return hasFile;
  }

  public void setHasFile(boolean hasFile) {
    this.hasFile = hasFile;
  }

  public boolean isDownloaded() {
    return downloaded;
  }

  public void setDownloaded(boolean downloaded) {
    this.downloaded = downloaded;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  private String title;
  private int qualityProfileId;
  private String titleSlug;
  private List<RadarrImage> images;
  private String remotePoster;
  private long tmdbId;
  private int year;
  private String path;
  private boolean monitored = true;
  private boolean hasFile;
  private boolean downloaded;
  private RadarrOptions addOptions = new RadarrOptions();
  private long id;
}
