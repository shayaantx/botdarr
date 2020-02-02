package com.botdarr.api;

import com.botdarr.Config;
import com.botdarr.clients.ChatClient;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.util.List;

public interface Api {
  /**
   * The url base for this api (can be null/empty)
   */
  String getUrlBase();

  /**
   * Get this api's url endpoint
   */
  String getApiUrl(String path);

  /**
   * Gets all the in-progress downloads
   */
  List<ChatClientResponse> downloads();

  /**
   * Notifications that are sent every 10 minutes
   */
  void sendPeriodicNotifications(ChatClient chatClient);

  /**
   * Data cached from jda directly in the api
   */
  void cacheData();

  /**
   * Gets the auth token for this api
   */
  String getApiToken();

  default String getApiUrl(String apiUrlKey, String apiTokenKey, String path) {
    String urlBase = Strings.isBlank(getUrlBase()) ? "" : getUrlBase();
    return Config.getProperty(apiUrlKey) + "/" + urlBase + "/api/" + path + "?apikey=" + Config.getProperty(apiTokenKey);
  }

  static final Logger LOGGER = LogManager.getLogger();
}
