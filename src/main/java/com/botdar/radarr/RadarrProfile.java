package com.botdar.radarr;

import java.util.List;

public class RadarrProfile {
  public String getName() {
    return name;
  }

  public RadarrProfileCutoff getCutoff() {
    return cutoff;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCutoff(RadarrProfileCutoff cutoff) {
    this.cutoff = cutoff;
  }

  public List<RadarrProfileQualityItem> getItems() {
    return items;
  }

  public void setItems(List<RadarrProfileQualityItem> items) {
    this.items = items;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  private String name;
  private RadarrProfileCutoff cutoff;
  private List<RadarrProfileQualityItem> items;
  private long id;
}
