package com.botdar.sonarr;

public class SonarrSeason {
  public int getSeasonNumber() {
    return seasonNumber;
  }

  public void setSeasonNumber(int seasonNumber) {
    this.seasonNumber = seasonNumber;
  }

  public boolean isMonitored() {
    return monitored;
  }

  public void setMonitored(boolean monitored) {
    this.monitored = monitored;
  }

  public SonarrSeasonStatistics getStatistics() {
    return statistics;
  }

  public void setStatistics(SonarrSeasonStatistics statistics) {
    this.statistics = statistics;
  }
  private int seasonNumber;
  private boolean monitored;
  private SonarrSeasonStatistics statistics;
}
