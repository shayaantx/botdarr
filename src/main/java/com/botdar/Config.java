package com.botdar;

import com.botdar.clients.ChatClientType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

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

      boolean configuredDiscord =
        !Strings.isBlank(properties.getProperty(Constants.DISCORD_TOKEN)) &&
        !Strings.isBlank(properties.getProperty(Constants.DISCORD_CHANNELS));

      boolean configuredSlack =
        !Strings.isBlank(properties.getProperty(Constants.SLACK_TOKEN)) &&
        !Strings.isBlank(properties.getProperty(Constants.SLACK_CHANNELS));

      if (!configuredDiscord && !configuredSlack) {
        throw new RuntimeException("You don't have discord or slack configured, please configure one");
      }

      if (configuredDiscord && configuredSlack) {
        throw new RuntimeException("You have both discord and slack configured, you can only use one at a time");
      }

      if (configuredDiscord) {
        chatClientType = ChatClientType.DISCORD;
      } else {
        chatClientType = ChatClientType.SLACK;
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

  public static ChatClientType getChatClientType() {
    return getConfig().chatClientType;
  }

  public static final class Constants {
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
    public static final String SLACK_TOKEN = "slack-token";

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

    //TODO: implement
    public static final String LIDARR_URL = "lidar-url";
  }

  private static String propertiesPath = "config/properties";
  private final Properties properties;
  private final boolean isRaddarrEnabled;
  private final boolean isSonarrEnabled;
  private final ChatClientType chatClientType;
  private static final Logger LOGGER = LogManager.getLogger();
}