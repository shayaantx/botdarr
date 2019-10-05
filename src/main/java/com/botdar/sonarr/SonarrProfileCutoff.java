package com.botdar.sonarr;

public class SonarrProfileCutoff {
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  private long id;
  private String name;
}
