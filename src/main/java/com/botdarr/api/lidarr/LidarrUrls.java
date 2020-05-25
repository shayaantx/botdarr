package com.botdarr.api.lidarr;

public class LidarrUrls {
  private static final String VERSION = "v1/";
  /**
   * See https://github.com/lidarr/Lidarr/wiki/Artist
   */
  public static final String ALL_ARTISTS = VERSION + "artist";

  /**
   * See https://github.com/lidarr/Lidarr/wiki/Artist-Lookup
   */
  public static final String LOOKUP_ARTISTS = VERSION + "Artist/lookup";
  public static final String DOWNLOAD_BASE = VERSION + "queue";

  public static final String PROFILE = VERSION + "qualityprofile";

  public static final String METADATA_PROFILE = VERSION + "metadataprofile";
}
