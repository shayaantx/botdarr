package com.botdarr.api.lidarr;

import java.util.List;

public class LidarrArtist {
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Boolean getEnded() {
    return ended;
  }

  public void setEnded(Boolean ended) {
    this.ended = ended;
  }

  public String getArtistName() {
    return artistName;
  }

  public void setArtistName(String artistName) {
    this.artistName = artistName;
  }

  public String getForeignArtistId() {
    return foreignArtistId;
  }

  public void setForeignArtistId(String foreignArtistId) {
    this.foreignArtistId = foreignArtistId;
  }

  public Integer getTadbId() {
    return tadbId;
  }

  public void setTadbId(Integer tadbId) {
    this.tadbId = tadbId;
  }

  public Integer getDiscogsId() {
    return discogsId;
  }

  public void setDiscogsId(Integer discogsId) {
    this.discogsId = discogsId;
  }

  public String getOverview() {
    return overview;
  }

  public void setOverview(String overview) {
    this.overview = overview;
  }

  public String getArtistType() {
    return artistType;
  }

  public void setArtistType(String artistType) {
    this.artistType = artistType;
  }

  public String getDisambiguation() {
    return disambiguation;
  }

  public void setDisambiguation(String disambiguation) {
    this.disambiguation = disambiguation;
  }

  public List<LidarrLink> getLinks() {
    return links;
  }

  public void setLinks(List<LidarrLink> links) {
    this.links = links;
  }

  public List<LidarrImage> getImages() {
    return images;
  }

  public void setImages(List<LidarrImage> images) {
    this.images = images;
  }

  public String getRemotePoster() {
    return remotePoster;
  }

  public void setRemotePoster(String remotePoster) {
    this.remotePoster = remotePoster;
  }

  public Integer getQualityProfileId() {
    return qualityProfileId;
  }

  public void setQualityProfileId(Integer qualityProfileId) {
    this.qualityProfileId = qualityProfileId;
  }

  public Integer getMetadataProfileId() {
    return metadataProfileId;
  }

  public void setMetadataProfileId(Integer metadataProfileId) {
    this.metadataProfileId = metadataProfileId;
  }

  public Boolean getAlbumFolder() {
    return albumFolder;
  }

  public void setAlbumFolder(Boolean albumFolder) {
    this.albumFolder = albumFolder;
  }

  public Boolean getMonitored() {
    return monitored;
  }

  public void setMonitored(Boolean monitored) {
    this.monitored = monitored;
  }

  public String getCleanName() {
    return cleanName;
  }

  public void setCleanName(String cleanName) {
    this.cleanName = cleanName;
  }

  public String getSortName() {
    return sortName;
  }

  public void setSortName(String sortName) {
    this.sortName = sortName;
  }

  public List<Object> getTags() {
    return tags;
  }

  public void setTags(List<Object> tags) {
    this.tags = tags;
  }

  public String getAdded() {
    return added;
  }

  public void setAdded(String added) {
    this.added = added;
  }

  public LidarrRating getRatings() {
    return ratings;
  }

  public void setRatings(LidarrRating ratings) {
    this.ratings = ratings;
  }

  public LidarrStatistics getStatistics() {
    return statistics;
  }

  public void setStatistics(LidarrStatistics statistics) {
    this.statistics = statistics;
  }

  private String status;
  private Boolean ended;
  private String artistName;
  private String foreignArtistId;
  private Integer tadbId;
  private Integer discogsId;
  private String overview;
  private String artistType;
  private String disambiguation;
  private List<LidarrLink> links = null;
  private List<LidarrImage> images = null;
  private String remotePoster;
  private Integer qualityProfileId;
  private Integer metadataProfileId;
  private Boolean albumFolder;
  private Boolean monitored;
  private String cleanName;
  private String sortName;
  private List<Object> tags = null;
  private String added;
  private LidarrRating ratings = new LidarrRating();
  private LidarrStatistics statistics = new LidarrStatistics();
  private LidarrAddOptions addOptions = new LidarrAddOptions();
}
