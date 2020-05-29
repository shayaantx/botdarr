package com.botdarr.api.lidarr;

public class LidarrUrls {
  /**
   * The base endpoint prefix for lidarr v1 api, https://github.com/lidarr/Lidarr/wiki/API
   */
  private static final String VERSION = "v1/";

  /**
   * See https://github.com/lidarr/Lidarr/wiki/Artist
   */
  public static final String ALL_ARTISTS = VERSION + "artist";

  /**
   * See https://github.com/lidarr/Lidarr/wiki/Artist-Lookup
   */
  public static final String LOOKUP_ARTISTS = VERSION + "Artist/lookup";

  /**
   * See https://github.com/lidarr/Lidarr/wiki/Queue
   */
  public static final String DOWNLOAD_BASE = VERSION + "queue";

  /**
   * TODO: doc
   */
  public static final String PROFILE = VERSION + "qualityprofile";

  /**
   * TODO: doc
   */
  public static final String METADATA_PROFILE = VERSION + "metadataprofile";
}
