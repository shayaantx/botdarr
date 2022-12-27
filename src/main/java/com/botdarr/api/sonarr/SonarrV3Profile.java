package com.botdarr.api.sonarr;

import java.util.List;

public class SonarrV3Profile implements SonarrProfile {
  @Override
  public String getKey() {
    return name.toLowerCase();
  }

  public String getName() {
    return name;
  }

  public SonarrProfileCutoff getCutoff() {
    return cutoff;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCutoff(SonarrProfileCutoff cutoff) {
    this.cutoff = cutoff;
  }

  public List<SonarrProfileQualityItem> getItems() {
    return items;
  }

  public void setItems(List<SonarrProfileQualityItem> items) {
    this.items = items;
  }

  public long getId() {
    return id;
  }

  @Override
  public String getCutOffDisplayStr() {
    return cutoff != null ? "" + cutoff.getId() : "";
  }

  public void setId(long id) {
    this.id = id;
  }

  private String name;
  private SonarrProfileCutoff cutoff;
  private List<SonarrProfileQualityItem> items;
  private long id;
}
