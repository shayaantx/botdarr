package com.botdar;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Config {
  private static volatile Config instance;
  private static Config getConfig() {
    if (instance == null) {
      synchronized (Config .class) {
        if (instance == null) {
          instance = new Config();
        }
      }
    }
    return instance;
  }

  private Config() {
    try (InputStream input = new FileInputStream("properties")) {
      properties = new Properties();
      properties.load(input);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static String getProperty(String key) {
    return getConfig().properties.getProperty(key);
  }

  public static final class Constants {
    public static final String TOKEN = "token";
    public static final String RADARR_URL = "radarr-url";
    public static final String RADARR_TOKEN = "radarr-token";
    public static final String RADARR_PATH = "radarr-path";
    public static final String SONARR_URL = "sonarr-url";
    public static final String LIDARR_URL = "lidar-url";
    public static final String JACKETT_URL = "jackett-url";
  }

  private final Properties properties;
}