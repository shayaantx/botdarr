package com.botdar.radarr;

public class RadarrTorrent {
  public String getGuid() {
    return guid;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public RadarrProfileQualityItem getQuality() {
    return quality;
  }

  public void setQuality(RadarrProfileQualityItem quality) {
    this.quality = quality;
  }

  public String getIndexer() {
    return indexer;
  }

  public void setIndexer(String indexer) {
    this.indexer = indexer;
  }

  public boolean isRejected() {
    return rejected;
  }

  public void setRejected(boolean rejected) {
    this.rejected = rejected;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  public int getSeeders() {
    return seeders;
  }

  public void setSeeders(int seeders) {
    this.seeders = seeders;
  }

  public int getLeechers() {
    return leechers;
  }

  public void setLeechers(int leechers) {
    this.leechers = leechers;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String[] getRejections() {
    return rejections;
  }

  public void setRejections(String[] rejections) {
    this.rejections = rejections;
  }

  public long getAge() {
    return age;
  }

  public void setAge(long age) {
    this.age = age;
  }

  public double getAgeHours() {
    return ageHours;
  }

  public void setAgeHours(double ageHours) {
    this.ageHours = ageHours;
  }

  public double getAgeMinutes() {
    return ageMinutes;
  }

  public void setAgeMinutes(double ageMinutes) {
    this.ageMinutes = ageMinutes;
  }

  public boolean isApproved() {
    return approved;
  }

  public void setApproved(boolean approved) {
    this.approved = approved;
  }

  public String getCommentUrl() {
    return commentUrl;
  }

  public void setCommentUrl(String commentUrl) {
    this.commentUrl = commentUrl;
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition(String edition) {
    this.edition = edition;
  }

  public boolean isFullSeason() {
    return fullSeason;
  }

  public void setFullSeason(boolean fullSeason) {
    this.fullSeason = fullSeason;
  }

  public String[] getIndexerFlags() {
    return indexerFlags;
  }

  public void setIndexerFlags(String[] indexerFlags) {
    this.indexerFlags = indexerFlags;
  }

  public int getIndexerId() {
    return indexerId;
  }

  public void setIndexerId(int indexerId) {
    this.indexerId = indexerId;
  }

  public String getInfoHash() {
    return infoHash;
  }

  public void setInfoHash(String infoHash) {
    this.infoHash = infoHash;
  }

  public String getInfoUrl() {
    return infoUrl;
  }

  public void setInfoUrl(String infoUrl) {
    this.infoUrl = infoUrl;
  }

  public boolean isAbsoluteNumbering() {
    return isAbsoluteNumbering;
  }

  public void setAbsoluteNumbering(boolean absoluteNumbering) {
    isAbsoluteNumbering = absoluteNumbering;
  }

  public boolean isDaily() {
    return isDaily;
  }

  public void setDaily(boolean daily) {
    isDaily = daily;
  }

  public boolean isPossibleSpecialEpisode() {
    return isPossibleSpecialEpisode;
  }

  public void setPossibleSpecialEpisode(boolean possibleSpecialEpisode) {
    isPossibleSpecialEpisode = possibleSpecialEpisode;
  }

  public String[] getLanguages() {
    return languages;
  }

  public void setLanguages(String[] languages) {
    this.languages = languages;
  }

  public String getMagnetUrl() {
    return magnetUrl;
  }

  public void setMagnetUrl(String magnetUrl) {
    this.magnetUrl = magnetUrl;
  }

  public String getMappingResult() {
    return mappingResult;
  }

  public void setMappingResult(String mappingResult) {
    this.mappingResult = mappingResult;
  }

  public String getMovieTitle() {
    return movieTitle;
  }

  public void setMovieTitle(String movieTitle) {
    this.movieTitle = movieTitle;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getPublishDate() {
    return publishDate;
  }

  public void setPublishDate(String publishDate) {
    this.publishDate = publishDate;
  }

  public int getQualityWeight() {
    return qualityWeight;
  }

  public void setQualityWeight(int qualityWeight) {
    this.qualityWeight = qualityWeight;
  }

  public String getReleaseGroup() {
    return releaseGroup;
  }

  public void setReleaseGroup(String releaseGroup) {
    this.releaseGroup = releaseGroup;
  }

  public int getReleaseWeight() {
    return releaseWeight;
  }

  public void setReleaseWeight(int releaseWeight) {
    this.releaseWeight = releaseWeight;
  }

  public int getSeasonNumber() {
    return seasonNumber;
  }

  public void setSeasonNumber(int seasonNumber) {
    this.seasonNumber = seasonNumber;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public boolean isSpecial() {
    return special;
  }

  public void setSpecial(boolean special) {
    this.special = special;
  }

  public long getSuspectedMovieId() {
    return suspectedMovieId;
  }

  public void setSuspectedMovieId(long suspectedMovieId) {
    this.suspectedMovieId = suspectedMovieId;
  }

  public boolean isTemporarilyRejected() {
    return temporarilyRejected;
  }

  public void setTemporarilyRejected(boolean temporarilyRejected) {
    this.temporarilyRejected = temporarilyRejected;
  }

  public int getTvRageId() {
    return tvRageId;
  }

  public void setTvRageId(int tvRageId) {
    this.tvRageId = tvRageId;
  }

  public long getTvdbId() {
    return tvdbId;
  }

  public void setTvdbId(long tvdbId) {
    this.tvdbId = tvdbId;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  private String[] rejections;
  private String guid;
  private RadarrProfileQualityItem quality;
  private String indexer;
  private boolean rejected;
  private String downloadUrl;
  private int seeders;
  private int leechers;
  private String title;
  private long age;
  private double ageHours;
  private double ageMinutes;
  private boolean approved;
  private String commentUrl;
  private String edition;
  private boolean fullSeason;
  private String[] indexerFlags;
  private int indexerId;
  private String infoHash;
  private String infoUrl;
  private boolean isAbsoluteNumbering;
  private boolean isDaily;
  private boolean isPossibleSpecialEpisode;
  private String[] languages;
  private String magnetUrl;
  private String mappingResult;
  private String movieTitle;
  private String protocol;
  private String publishDate;
  private int qualityWeight;
  private String releaseGroup;
  private int releaseWeight;
  private int seasonNumber;
  private long size;
  private boolean special;
  private long suspectedMovieId;
  private boolean temporarilyRejected;
  private int tvRageId;
  private long tvdbId;
  private int year;
}
