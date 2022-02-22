package com.botdarr.api.radarr;

import com.botdarr.api.KeyBased;

import java.util.Date;
import java.util.List;

public class RadarrMovie implements KeyBased<Long> {
  @Override
  public Long getKey() {
    return Long.valueOf(tmdbId);
  }
  public String getOriginalTitle() {
    return originalTitle;
  }

  public void setOriginalTitle(String originalTitle) {
    this.originalTitle = originalTitle;
  }

  public List<RadarrAlternateTitle> getAlternateTitles() {
    return alternateTitles;
  }

  public void setAlternateTitles(List<RadarrAlternateTitle> alternateTitles) {
    this.alternateTitles = alternateTitles;
  }

  public int getSecondaryYearSourceId() {
    return secondaryYearSourceId;
  }

  public void setSecondaryYearSourceId(int secondaryYearSourceId) {
    this.secondaryYearSourceId = secondaryYearSourceId;
  }

  public String getSortTitle() {
    return sortTitle;
  }

  public void setSortTitle(String sortTitle) {
    this.sortTitle = sortTitle;
  }

  public int getSizeOnDisk() {
    return sizeOnDisk;
  }

  public void setSizeOnDisk(int sizeOnDisk) {
    this.sizeOnDisk = sizeOnDisk;
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

  public Date getDigitalRelease() {
    return digitalRelease;
  }

  public void setDigitalRelease(Date digitalRelease) {
    this.digitalRelease = digitalRelease;
  }

  public List<RadarrImage> getImages() {
    return images;
  }

  public void setImages(List<RadarrImage> images) {
    this.images = images;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getRemotePoster() {
    return remotePoster;
  }

  public void setRemotePoster(String remotePoster) {
    this.remotePoster = remotePoster;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public boolean isHasFile() {
    return hasFile;
  }

  public void setHasFile(boolean hasFile) {
    this.hasFile = hasFile;
  }

  public String getYouTubeTrailerId() {
    return youTubeTrailerId;
  }

  public void setYouTubeTrailerId(String youTubeTrailerId) {
    this.youTubeTrailerId = youTubeTrailerId;
  }

  public String getStudio() {
    return studio;
  }

  public void setStudio(String studio) {
    this.studio = studio;
  }

  public int getQualityProfileId() {
    return qualityProfileId;
  }

  public void setQualityProfileId(int qualityProfileId) {
    this.qualityProfileId = qualityProfileId;
  }

  public boolean isMonitored() {
    return monitored;
  }

  public void setMonitored(boolean monitored) {
    this.monitored = monitored;
  }

  public String getMinimumAvailability() {
    return minimumAvailability;
  }

  public void setMinimumAvailability(String minimumAvailability) {
    this.minimumAvailability = minimumAvailability;
  }

  public boolean isAvailable() {
    return isAvailable;
  }

  public void setAvailable(boolean available) {
    isAvailable = available;
  }

  public String getFolderName() {
    return folderName;
  }

  public void setFolderName(String folderName) {
    this.folderName = folderName;
  }

  public int getRuntime() {
    return runtime;
  }

  public void setRuntime(int runtime) {
    this.runtime = runtime;
  }

  public String getCleanTitle() {
    return cleanTitle;
  }

  public void setCleanTitle(String cleanTitle) {
    this.cleanTitle = cleanTitle;
  }

  public String getImdbId() {
    return imdbId;
  }

  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }

  public long getTmdbId() {
    return tmdbId;
  }

  public void setTmdbId(long tmdbId) {
    this.tmdbId = tmdbId;
  }

  public String getTitleSlug() {
    return titleSlug;
  }

  public void setTitleSlug(String titleSlug) {
    this.titleSlug = titleSlug;
  }

  public String getFolder() {
    return folder;
  }

  public void setFolder(String folder) {
    this.folder = folder;
  }

  public List<String> getGenres() {
    return genres;
  }

  public void setGenres(List<String> genres) {
    this.genres = genres;
  }

  public List<Object> getTags() {
    return tags;
  }

  public void setTags(List<Object> tags) {
    this.tags = tags;
  }

  public Date getAdded() {
    return added;
  }

  public void setAdded(Date added) {
    this.added = added;
  }

  public RadarrRatings getRatings() {
    return ratings;
  }

  public void setRatings(RadarrRatings ratings) {
    this.ratings = ratings;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getRootFolderPath() {
    return rootFolderPath;
  }

  public void setRootFolderPath(String rootFolderPath) {
    this.rootFolderPath = rootFolderPath;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public RadarrOptions getAddOptions() {
    return addOptions;
  }

  public void setAddOptions(RadarrOptions addOptions) {
    this.addOptions = addOptions;
  }

  public long id;
  public String path;
  public String rootFolderPath;
  private String title;
  private String originalTitle;
  private transient List<RadarrAlternateTitle> alternateTitles;
  private int secondaryYearSourceId;
  private String sortTitle;
  private int sizeOnDisk;
  private String status;
  private String overview;
  private Date digitalRelease;
  private List<RadarrImage> images;
  private String website;
  private String remotePoster;
  private int year;
  private boolean hasFile;
  private String youTubeTrailerId;
  private String studio;
  private int qualityProfileId;
  private boolean monitored;
  private String minimumAvailability;
  private boolean isAvailable;
  private String folderName;
  private int runtime;
  private String cleanTitle;
  private String imdbId;
  private long tmdbId;
  private String titleSlug;
  private String folder;
  private List<String> genres;
  private List<Object> tags;
  private Date added;
  private RadarrRatings ratings;
  private RadarrOptions addOptions = new RadarrOptions();
}
