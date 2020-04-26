package com.botdarr.api;

import com.botdarr.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

public enum ApiRequestType {
  MOVIE(Config.Constants.MAX_MOVIE_REQUESTS_PER_USER),
  SHOW(Config.Constants.MAX_SHOW_REQUESTS_PER_USER);

  private ApiRequestType(String configProperty) {
    this.maxRequestsPerUserProperty = configProperty;
  }

  public boolean isConfigured() {
    String maxRequestsValue = Config.getProperty(maxRequestsPerUserProperty);
    if (Strings.isEmpty(maxRequestsValue)) {
      return false;
    }
    try {
      Integer.valueOf(maxRequestsValue);
      return true;
    } catch (NumberFormatException e) {
      LOGGER.error("Invalid max requests per " + this.name() + " per user configuration", e);
    }
    return false;
  }

  public int getMaxRequestsAllowed() {
    return Integer.valueOf(Config.getProperty(maxRequestsPerUserProperty));
  }

  private final String maxRequestsPerUserProperty;
  private static final Logger LOGGER = LogManager.getLogger();
}
