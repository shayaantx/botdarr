package com.botdarr.api;

public enum ContentType {
  MOVIE,
  SHOW,
  ARTIST;

  public String getDisplayName() {
    return this.name().toLowerCase();
  }
}
