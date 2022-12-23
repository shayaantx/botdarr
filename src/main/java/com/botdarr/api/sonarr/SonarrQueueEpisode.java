package com.botdarr.api.sonarr;

public class SonarrQueueEpisode {
  public int getSeasonNumber() {
    return seasonNumber;
  }

  public void setSeasonNumber(int seasonNumber) {
    this.seasonNumber = seasonNumber;
  }

  public int getEpisodeNumber() {
    return episodeNumber;
  }

  public void setEpisodeNumber(int episodeNumber) {
    this.episodeNumber = episodeNumber;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getOverview() {
    return overview;
  }

  public void setOverview(String overview) {
    this.overview = overview;
  }

  public long getSeriesId() {
    return seriesId;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  private int seasonNumber;
  private int episodeNumber;
  private String title;
  private String overview;
  private long seriesId;
  private long id;
}
