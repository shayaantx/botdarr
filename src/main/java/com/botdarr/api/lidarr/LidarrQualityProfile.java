package com.botdarr.api.lidarr;

import com.botdarr.api.KeyBased;

public class LidarrQualityProfile implements KeyBased<String> {
  @Override
  public String getKey() {
    return name.toLowerCase();
  }

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
