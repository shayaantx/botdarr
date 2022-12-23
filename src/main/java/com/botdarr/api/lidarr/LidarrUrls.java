package com.botdarr.api.lidarr;

import com.botdarr.Config;
import com.botdarr.api.ArrRequestBuilder;

public class LidarrUrls {
  public static class LidarrRequestBuilder extends ArrRequestBuilder {
    public LidarrRequestBuilder() {
      super(Config.Constants.LIDARR_URL, Config.Constants.LIDARR_URL_BASE, Config.Constants.LIDARR_TOKEN);
    }
  }

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
  public static final String ARTIST_BASE = "v1/artist";
}
