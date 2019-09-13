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

  private String name;
  private RadarrProfileCutoff cutoff;
  private List<RadarrProfileQualityItem> items;
}
