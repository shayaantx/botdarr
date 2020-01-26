package com.botdar.api.radarr;

public class RadarrOptions {
  public boolean isSearchForMovie() {
    return searchForMovie;
  }

  public void setSearchForMovie(boolean searchForMovie) {
    this.searchForMovie = searchForMovie;
  }

  private boolean searchForMovie = true;
}
