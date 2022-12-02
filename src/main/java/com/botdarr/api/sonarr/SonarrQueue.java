package com.botdarr.api.sonarr;

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

  public void setSeries(SonarrQueueShow series) {
    this.series = series;
  }

  public SonarrQueueEpisode getEpisode() {
    return episode;
  }

  public void setEpisode(SonarrQueueEpisode episode) {
    this.episode = episode;
  }

  public long getSeriesId() {
    return seriesId;
  }

  public void setSeriesId(long seriesId) {
    this.seriesId = seriesId;
  }

  public long getEpisodeId() {
    return episodeId;
  }

  public void setEpisodeId(long episodeId) {
    this.episodeId = episodeId;
  }

  private SonarrProfileQualityItem quality;
  private SonarrQueueStatusMessages[] statusMessages;
  private SonarrQueueShow series;
  private SonarrQueueEpisode episode;
  private String status;
  private String timeleft;
  private long seriesId;
  private long episodeId;
  private long id;
}
