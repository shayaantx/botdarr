package com.botdar.sonarr;

public class SonarrSeasonStatistics {
  public int getEpisodeCount() {
    return episodeCount;
  }

  public void setEpisodeCount(int episodeCount) {
    this.episodeCount = episodeCount;
  }

  public int getTotalEpisodeCount() {
    return totalEpisodeCount;
  }

  public void setTotalEpisodeCount(int totalEpisodeCount) {
    this.totalEpisodeCount = totalEpisodeCount;
  }

  public int getPercentOfEpisodes() {
    return percentOfEpisodes;
  }

  public void setPercentOfEpisodes(int percentOfEpisodes) {
    this.percentOfEpisodes = percentOfEpisodes;
  }

  private int episodeCount;
  private int totalEpisodeCount;
  private int percentOfEpisodes;
}
