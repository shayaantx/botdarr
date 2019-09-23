package com.botdar.radarr;

public class RadarrTorrent {
  public String getGuid() {
    return guid;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public RadarrProfileQualityItem getQuality() {
    return quality;
  }

  public void setQuality(RadarrProfileQualityItem quality) {
    this.quality = quality;
  }

  public String getIndexer() {
    return indexer;
  }

  public void setIndexer(String indexer) {
    this.indexer = indexer;
  }

  public boolean isRejected() {
    return rejected;
  }

  public void setRejected(boolean rejected) {
    this.rejected = rejected;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  public int getSeeders() {
    return seeders;
  }

  public void setSeeders(int seeders) {
    this.seeders = seeders;
  }

  public int getLeechers() {
    return leechers;
  }

  public void setLeechers(int leechers) {
    this.leechers = leechers;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String[] getRejections() {
    return rejections;
  }

  public void setRejections(String[] rejections) {
    this.rejections = rejections;
  }

  private String[] rejections;
  private String guid;
  private RadarrProfileQualityItem quality;
  private String indexer;
  private boolean rejected;
  private String downloadUrl;
  private int seeders;
  private int leechers;
  private String title;
}
