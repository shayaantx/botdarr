package com.botdarr.api.sonarr;

import com.botdarr.Config;
import com.botdarr.api.ArrRequestBuilder;
import org.apache.logging.log4j.util.Strings;

public class SonarrUrls {
  public static class SonarrRequestBuilder extends ArrRequestBuilder {
    public SonarrRequestBuilder() {
      super(Config.Constants.SONARR_URL, Config.Constants.SONARR_URL_BASE, Config.Constants.SONARR_TOKEN);
    }

    //TODO: do i even need this?
    private boolean isV4Enabled() {
      String isSonarrV4 = Config.getProperty(Config.Constants.SONARR_V4);
      return !Strings.isEmpty(isSonarrV4) && Boolean.parseBoolean(isSonarrV4);
    }

    @Override
    public String getApiSuffix() {
      if (isV4Enabled()) {
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
