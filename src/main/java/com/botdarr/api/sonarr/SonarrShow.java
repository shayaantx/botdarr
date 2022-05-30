package com.botdarr.api.sonarr;

import com.botdarr.api.KeyBased;
import com.botdarr.api.radarr.RadarrImage;
import org.apache.logging.log4j.util.Strings;

import java.util.List;

public class SonarrShow implements KeyBased<Long> {
  @Override
  public Long getKey() {
    return tvdbId;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSortTitle() {
    return sortTitle;
  }

  public void setSortTitle(String sortTitle) {
    this.sortTitle = sortTitle;
  }

  public int getSeasonCount() {
    return seasonCount;
  }

  public void setSeasonCount(int seasonCount) {
    this.seasonCount = seasonCount;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getOverview() {
    return overview;
  }

  public void setOverview(String overview) {
    this.overview = overview;
  }

  public String getNetwork() {
    return network;
  }

  public void setNetwork(String network) {
    this.network = network;
  }

  public String getAirTime() {
    return airTime;
  }

  public void setAirTime(String airTime) {
    this.airTime = airTime;
  }

  public List<SonarrImage> getImages() {
    return images;
  }

  public void setImages(List<SonarrImage> images) {
    this.images = images;
  }

  public String getRemotePoster() {
    return remotePoster;
  }

  public String getRemoteImage() {
    if (Strings.isEmpty(remotePoster)) {
      for(SonarrImage sonarrImage : images) {
        if (sonarrImage.getCoverType().equals("poster") && !Strings.isEmpty(sonarrImage.getRemoteUrl())) {
          return sonarrImage.getRemoteUrl();
        }
      }
    }
    return remotePoster;
  }

  public void setRemotePoster(String remotePoster) {
    this.remotePoster = remotePoster;
  }

  public List<SonarrSeason> getSeasons() {
    return seasons;
  }

  public void setSeasons(List<SonarrSeason> seasons) {
    this.seasons = seasons;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getProfileId() {
    return profileId;
  }

  public void setProfileId(int profileId) {
    this.profileId = profileId;
  }

  public boolean isSeasonFolder() {
    return seasonFolder;
  }

  public void setSeasonFolder(boolean seasonFolder) {
    this.seasonFolder = seasonFolder;
  }

  public boolean isMonitored() {
    return monitored;
  }

  public void setMonitored(boolean monitored) {
    this.monitored = monitored;
  }

  public boolean isUseSceneNumbering() {
    return useSceneNumbering;
  }

  public void setUseSceneNumbering(boolean useSceneNumbering) {
    this.useSceneNumbering = useSceneNumbering;
  }

  public long getRuntime() {
    return runtime;
  }

  public void setRuntime(long runtime) {
    this.runtime = runtime;
  }

  public void setTvdbId(long tvdbId) {
    this.tvdbId = tvdbId;
  }

  public long getTvRageId() {
    return tvRageId;
  }

  public void setTvRageId(long tvRageId) {
    this.tvRageId = tvRageId;
  }

  public long getTvMazeId() {
    return tvMazeId;
  }

  public void setTvMazeId(long tvMazeId) {
    this.tvMazeId = tvMazeId;
  }

  public String getFirstAired() {
    return firstAired;
  }

  public void setFirstAired(String firstAired) {
    this.firstAired = firstAired;
  }

  public String getSeriesType() {
    return seriesType;
  }

  public void setSeriesType(String seriesType) {
    this.seriesType = seriesType;
  }

  public int getQualityProfileId() {
    return qualityProfileId;
  }

  public void setQualityProfileId(int qualityProfileId) {
    this.qualityProfileId = qualityProfileId;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getTvdbId() {
    return tvdbId;
  }

  public String getTitle() {
    return title;
  }

  public long getId() {
    return id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getTitleSlug() {
    return titleSlug;
  }

  public void setTitleSlug(String titleSlug) {
    this.titleSlug = titleSlug;
  }

  private int qualityProfileId;
  private long id;
  private String title;
  private String sortTitle;
  private int seasonCount;
  private String status;
  private String overview;
  private String network;
  private String airTime;
  private List<SonarrImage> images;
  private String remotePoster;
  private List<SonarrSeason> seasons;
  private int year;
  private int profileId;
  private boolean seasonFolder;
  private boolean monitored;
  private boolean useSceneNumbering;
  private long runtime;
  private long tvdbId;
  private long tvRageId;
  private long tvMazeId;
  private String firstAired;
  private String seriesType;
  private String path;
  private String titleSlug;
  private SonarrOptions addOptions = new SonarrOptions();
}
