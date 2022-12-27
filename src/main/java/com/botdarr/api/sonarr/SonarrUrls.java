package com.botdarr.api.sonarr;

import com.botdarr.Config;
import com.botdarr.api.ArrRequestBuilder;

import static com.botdarr.Config.isSonarrV4Enabled;

public class SonarrUrls {
  public static class SonarrRequestBuilder extends ArrRequestBuilder {
    public SonarrRequestBuilder() {
      super(Config.Constants.SONARR_URL, Config.Constants.SONARR_URL_BASE, Config.Constants.SONARR_TOKEN);
    }

    @Override
    public String getApiSuffix() {
      if (isSonarrV4Enabled()) {
        return "/api/v3/";
      }
      return super.getApiSuffix();
    }
  }
  public static final String DOWNLOAD_BASE = "queue";
  public static final String LOOKUP_SERIES = "series/lookup";
  public static final String SERIES_BASE = "series";
  public static final String PROFILE = "qualityprofile";
  public static final String EPISODES_LOOKUP = "episode";
}
