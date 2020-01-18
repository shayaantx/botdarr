package com.botdar;

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
      if (Strings.isBlank(properties.getProperty(Constants.TOKEN))) {
        throw new RuntimeException("Discord token not set");
      }
      //TODO: add validation to make sure all properties are set
    } catch (Exception ex) {
      LOGGER.error("Error loading properties file", ex);
      throw new RuntimeException(ex);
    }
  }

  public static String getProperty(String key) {
    return getConfig().properties.getProperty(key);
  }

  public static final class Constants {
    /**
     * The discord bot token
     */
    public static final String TOKEN = "token";

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

    public static final String LIDARR_URL = "lidar-url";

    /**
     * The discord channel(s) to send notifications to
     */
    public static final String DISCORD_CHANNELS = "discord-channels";
  }

  private static String propertiesPath = "config/properties";
  private final Properties properties;
  private static final Logger LOGGER = LogManager.getLogger();
}