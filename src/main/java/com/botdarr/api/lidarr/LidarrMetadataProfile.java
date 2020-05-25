package com.botdarr.api.lidarr;

import com.botdarr.api.KeyBased;

public class LidarrMetadataProfile implements KeyBased<String> {
  @Override
  public String getKey() {
    return name.toLowerCase();
  }

  public Integer getId() {
    return id;
  }
  private String name;
  private Integer id;
}
