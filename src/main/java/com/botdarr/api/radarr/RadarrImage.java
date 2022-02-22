package com.botdarr.api.radarr;

public class RadarrImage {
  public String getCoverType() {
    return coverType;
  }

  public void setCoverType(String coverType) {
    this.coverType = coverType;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getRemoteUrl() {
    return remoteUrl;
  }

  public void setRemoteUrl(String remoteUrl) {
    this.remoteUrl = remoteUrl;
  }

  private String coverType;
  private String url;
  public String remoteUrl;
}
