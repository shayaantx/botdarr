package com.botdar.radarr;

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

  public RadarrProfileQualityItem getQuality() {
    return quality;
  }

  public void setQuality(RadarrProfileQualityItem quality) {
    this.quality = quality;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public RadarrQueueStatusMessages getStatusMessages() {
    return statusMessages;
  }

  public void setStatusMessages(RadarrQueueStatusMessages statusMessages) {
    this.statusMessages = statusMessages;
  }

  private String title;
  private String status;
  private String timeleft;
  private RadarrProfileQualityItem quality;
  private long id;
  private RadarrQueueStatusMessages statusMessages;
}
