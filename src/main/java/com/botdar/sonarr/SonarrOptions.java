package com.botdar.sonarr;

public class SonarrOptions {
  public boolean isIgnoreEpisodesWithFiles() {
    return ignoreEpisodesWithFiles;
  }

  public void setIgnoreEpisodesWithFiles(boolean ignoreEpisodesWithFiles) {
    this.ignoreEpisodesWithFiles = ignoreEpisodesWithFiles;
  }

  public boolean isIgnoreEpisodesWithoutFiles() {
    return ignoreEpisodesWithoutFiles;
  }

  public void setIgnoreEpisodesWithoutFiles(boolean ignoreEpisodesWithoutFiles) {
    this.ignoreEpisodesWithoutFiles = ignoreEpisodesWithoutFiles;
  }

  public boolean isSearchForMissingEpisodes() {
    return searchForMissingEpisodes;
  }

  public void setSearchForMissingEpisodes(boolean searchForMissingEpisodes) {
    this.searchForMissingEpisodes = searchForMissingEpisodes;
  }

  private boolean ignoreEpisodesWithFiles = false;
  private boolean ignoreEpisodesWithoutFiles = false;
  private boolean searchForMissingEpisodes = true;
}
