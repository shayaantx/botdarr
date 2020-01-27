package com.botdarr.api.radarr;

public class RadarrProfileQualityItem {
  public RadarrProfileQuality getQuality() {
    return quality;
  }

  public void setQuality(RadarrProfileQuality quality) {
    this.quality = quality;
  }

  public boolean isAllowed() {
    return allowed;
  }

  public void setAllowed(boolean allowed) {
    this.allowed = allowed;
  }

  private RadarrProfileQuality quality;
  private boolean allowed;
}
