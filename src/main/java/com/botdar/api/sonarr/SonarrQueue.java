package com.botdar.api.sonarr;

public class SonarrQueue {

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

  public SonarrQueueStatusMessages[] getStatusMessages() {
    return statusMessages;
  }

  public void setStatusMessages(SonarrQueueStatusMessages[] statusMessages) {
    this.statusMessages = statusMessages;
  }

  public SonarrProfileQualityItem getQuality() {
    return quality;
  }

  public void setQuality(SonarrProfileQualityItem quality) {
    this.quality = quality;
  }

  public SonarrQueueShow getSonarrQueueShow() {
    return series;
  }

  public void setRadarrQueueMovie(SonarrQueueShow radarrQueueMovie) {
    this.series = radarrQueueMovie;
  }

  public SonarQueueEpisode getEpisode() {
    return episode;
  }

  public void setEpisode(SonarQueueEpisode episode) {
    this.episode = episode;
  }

  private String status;
  private String timeleft;
  private SonarrProfileQualityItem quality;
  private long id;
  private SonarrQueueStatusMessages[] statusMessages;
  private SonarrQueueShow series;
  private SonarQueueEpisode episode;
}
