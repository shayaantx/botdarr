package com.botdarr.api.radarr;

public class RadarrQueue {
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getTimeleft() {
    return timeleft;
  }

  public void setTimeleft(String timeleft) {
    this.timeleft = timeleft;
  }

  public RadarrQueueStatusMessages[] getStatusMessages() {
    return statusMessages;
  }

  public RadarrProfileQualityItem getQuality() {
    return quality;
  }

  public void setQuality(RadarrProfileQualityItem quality) {
    this.quality = quality;
  }

  public long getMovieId() {
    return movieId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  private String status;
  private String timeleft;
  private RadarrProfileQualityItem quality;
  private RadarrQueueStatusMessages[] statusMessages;
  private long movieId;
  private String title;
}
