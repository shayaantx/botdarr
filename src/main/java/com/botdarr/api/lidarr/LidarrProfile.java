package com.botdarr.api.lidarr;

public class LidarrProfile {
  public String getName() {
    return name;
  }

  public Integer getId() {
    return id;
  }

  private String name;
  private Boolean upgradeAllowed;
  private Integer cutoff;
  private Integer id;
}
