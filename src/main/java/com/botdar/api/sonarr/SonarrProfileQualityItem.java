package com.botdar.api.sonarr;

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

  private SonarrProfileQuality quality;
  private boolean allowed;
}
