package com.botdarr;

import com.botdarr.clients.ChatClientType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Collectors;

public class Config {
  private static volatile Config instance;

  private static Config getConfig() {
    if (instance == null) {
      synchronized (Config.class) {
        if (instance == null) {
          instance = new Config();
        }
      }
    }
    return instance;
  }

  private Config() {
    try (InputStream input = new FileInputStream(propertiesPath)) {
      properties = new Properties();
      properties.load(input);

      for (ChatClientType possibleChatClientType : ChatClientType.values()) {
        if (possibleChatClientType.isConfigured(properties) && chatClientType != null) {
          throw new RuntimeException("You cannot configure more than one chat client");
        }
        if (possibleChatClientType.isConfigured(properties)) {
          chatClientType = possibleChatClientType;
        }
      }

      if (chatClientType == null) {
        String allChatClientTypes = Arrays.asList(ChatClientType.values())
          .stream()
          .sorted(Comparator.comparing(ChatClientType::getReadableName))
          .map(ChatClientType::getReadableName)
          .collect(Collectors.joining(", "));
        throw new RuntimeException("You don't have " + allChatClientTypes + " configured, please configure one");
      }

      this.isRaddarrEnabled =
        !Strings.isBlank(properties.getProperty(Constants.RADARR_URL)) &&
        !Strings.isBlank(properties.getProperty(Constants.RADARR_PATH)) &&
        !Strings.isBlank(properties.getProperty(Constants.RADARR_TOKEN)) &&
        !Strings.isBlank(properties.getProperty(Constants.RADARR_DEFAULT_PROFILE));

      if (!this.isRaddarrEnabled) {
        LOGGER.warn("Radarr commands are not enabled, make sure you set the radarr url, path, token, default profile");
      }

      this.isSonarrEnabled =
        !Strings.isBlank(properties.getProperty(Constants.SONARR_URL)) &&
        !Strings.isBlank(properties.getProperty(Constants.SONARR_PATH)) &&
        !Strings.isBlank(properties.getProperty(Constants.SONARR_TOKEN)) &&
        !Strings.isBlank(properties.getProperty(Constants.SONARR_DEFAULT_PROFILE));

      if (!this.isSonarrEnabled) {
        LOGGER.warn("Sonarr commands are not enabled, make sure you set the sonarr url, path, token, default profile");
      }

      this.isLidarrEnabled =
        !Strings.isBlank(properties.getProperty(Constants.LIDARR_URL)) &&
        !Strings.isBlank(properties.getProperty(Constants.LIDARR_PATH)) &&
        !Strings.isBlank(properties.getProperty(Constants.LIDARR_TOKEN)) &&
        !Strings.isBlank(properties.getProperty(Constants.LIDARR_DEFAULT_PROFILE));\

      if (!this.isLidarrEnabled) {
        LOGGER.warn("Lidarr commands are not enabled, make sure you set the lidarr url, path, token, default profile");
      }

      String configuredPrefix = properties.getProperty(Config.Constants.COMMAND_PREFIX);
      if (!Strings.isEmpty(configuredPrefix)  && configuredPrefix.length() > 1) {
        throw new RuntimeException("Command prefix must be a single character");
      }
    } catch (Exception ex) {
      LOGGER.error("Error loading properties file", ex);
      throw new RuntimeException(ex);
    }
  }

  public static String getProperty(String key) {
    return getConfig().properties.getProperty(key);
  }

  public static boolean isRadarrEnabled() {
    return getConfig().isRaddarrEnabled;
  }

  public static boolean isSonarrEnabled() {
    return getConfig().isSonarrEnabled;
  }

  public static boolean isLidarrEnabled() {
    return getConfig().isLidarrEnabled;
  }

  public static ChatClientType getChatClientType() {
    return getConfig().chatClientType;
  }

  public static final class Constants {
    /**
     * The telegram auth token
     */
    public static final String TELEGRAM_TOKEN = "telegram-token";

    /**
     * The telegram private channel(s) to send notifications to
     */
    public static final String TELEGRAM_PRIVATE_CHANNELS = "telegram-private-channels";

    /**
     * The discord bot token
     */
    public static final String DISCORD_TOKEN = "discord-token";

    /**
     * The discord channel(s) to send notifications to
     */
    public static final String DISCORD_CHANNELS = "discord-channels";

    /**
     * The slack bot oauth token
     */
    public static final String SLACK_BOT_TOKEN = "slack-bot-token";

    /**
     * The slack user oauth token
     */
    public static final String SLACK_USER_TOKEN = "slack-user-token";

    /**
     * The slack channel(s) to send notifications to
     */
    public static final String SLACK_CHANNELS = "slack-channels";

    /**
     * The url to your radarr instance
     * (i.e., http://SOME_IP:PORT)
     */
    public static final String RADARR_URL = "radarr-url";

    /**
     * The api token for radarr
     */
    public static final String RADARR_TOKEN = "radarr-token";

    /**
     * The root file path to where you want radarr to download your movies
     */
    public static final String RADARR_PATH = "radarr-path";

    /**
     * The default quality profile you want radarr to use when adding movies
     */
    public static final String RADARR_DEFAULT_PROFILE = "radarr-default-profile";

    /**
     * The url base for radarr
     */
    public static final String RADARR_URL_BASE = "radarr-url-base";

    /**
     * The url to your sonarr instance
     * (i.e., http://SOME_IP:PORT)
     */
    public static final String SONARR_URL = "sonarr-url";

    /**
     * The api token for sonarr
     */
    public static final String SONARR_TOKEN = "sonarr-token";

    /**
     * The root file path to where you want sonarr to download your movies
     */
    public static final String SONARR_PATH = "sonarr-path";

    /**
     * The default quality profile you want sonarr to use when adding movies
     */
    public static final String SONARR_DEFAULT_PROFILE = "sonarr-default-profile";

    /**
     * The url base for sonarr
     */
    public static final String SONARR_URL_BASE = "sonarr-url-base";

    /**
     * The url to your lidarr instance
     */
    public static final String LIDARR_URL = "lidarr-url";

    /**
     * The path to set on all added music in lidarr
     */
    public static final String LIDARR_PATH = "lidarr-path";

    /**
     * The api token for accessing lidarr
     */
    public static final String LIDARR_TOKEN = "lidarr-token";

    /**
     * The default profile you want lidarr to use when adding artists
     */
    public static final String LIDARR_DEFAULT_PROFILE = "lidarr-default-profile";

    /**
     * The url base for lidarr
     */
    public static final String LIDARR_URL_BASE = "lidarr-url-base";

    /**
     * The database name
     */
    public static final String DATABASE_NAME = "botdarr.db";

    /**
     * The max amount of movie requests per user per configured threshold
     */
    public static final String MAX_MOVIE_REQUESTS_PER_USER = "max-movie-requests-per-user";

    /**
     * The max amount of show requests per user per configured threshold
     */
    public static final String MAX_SHOW_REQUESTS_PER_USER = "max-show-requests-per-user";

    /**
     * The type of threshold to enforce around request maximums (i.e., {@link com.botdarr.api.ApiRequestThreshold}
     */
    public static final String MAX_REQUESTS_THRESHOLD = "max-requests-threshold";

    /**
     * The max number of downloads to show to the user(s)
     */
    public static final String MAX_DOWNLOADS_TO_SHOW = "max-downloads-to-show";

    /**
     * The max number of results to show per search command
     */
    public static final String MAX_RESULTS_TO_SHOW = "max-results-to-show";

    /**
     * The prefix for all commands
     */
    public static final String COMMAND_PREFIX = "command-prefix";
  }

  private static String propertiesPath = "config/properties";
  private final Properties properties;
  private final boolean isRaddarrEnabled;
  private final boolean isSonarrEnabled;
  private final boolean isLidarrEnabled;
  private ChatClientType chatClientType = null;
  private static final Logger LOGGER = LogManager.getLogger();
}