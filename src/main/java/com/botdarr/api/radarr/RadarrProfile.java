package com.botdarr.api.radarr;

import com.botdarr.api.KeyBased;

import java.util.List;

public class RadarrProfile implements KeyBased<String> {
  @Override
  public String getKey() {
    return name.toLowerCase();
  }

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
