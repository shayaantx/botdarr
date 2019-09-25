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

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public RadarrQueueStatusMessages[] getStatusMessages() {
    return statusMessages;
  }

  public void setStatusMessages(RadarrQueueStatusMessages[] statusMessages) {
    this.statusMessages = statusMessages;
  }

  public RadarrProfileQualityItem getQuality() {
    return quality;
  }

  public void setQuality(RadarrProfileQualityItem quality) {
    this.quality = quality;
  }

  public RadarrQueueMovie getRadarrQueueMovie() {
    return movie;
  }

  public void setRadarrQueueMovie(RadarrQueueMovie radarrQueueMovie) {
    this.movie = radarrQueueMovie;
  }

  private String status;
  private String timeleft;
  private RadarrProfileQualityItem quality;
  private long id;
  private RadarrQueueStatusMessages[] statusMessages;
  private RadarrQueueMovie movie;
}
