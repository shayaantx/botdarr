package com.botdarr.api.sonarr;

import java.util.ArrayList;
import java.util.List;

public class SonarrProfileQualityItem {
  public SonarrProfileQuality getQuality() {
    return quality;
  }

  public void setQuality(SonarrProfileQuality quality) {
    this.quality = quality;
  }

  public boolean isAllowed() {
    return allowed;
  }

  public void setAllowed(boolean allowed) {
    this.allowed = allowed;
  }

  public List<SonarrProfileQualityItem> getItems() {
    return items;
  }

  public void setItems(List<SonarrProfileQualityItem> items) {
    this.items = items;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  private int id;
  private List<SonarrProfileQualityItem> items = new ArrayList<>();
  private SonarrProfileQuality quality;
  private boolean allowed;
}
