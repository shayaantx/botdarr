package com.botdarr.api.radarr;

import com.botdarr.Config;
import com.botdarr.api.ArrRequestBuilder;

public class RadarrUrls {
  public static class RadarrV3RequestBuilder extends ArrRequestBuilder {
    public RadarrV3RequestBuilder() {
      super(Config.Constants.RADARR_URL, Config.Constants.RADARR_URL_BASE, Config.Constants.RADARR_TOKEN);
    }

    @Override
    public String getApiSuffix() {
      return "/api/v3/";
    }
  }

  /**
   * The base download(s) url for get, put, delete requests (which each do different things in radarr)
   * See <a href="https://radarr.video/docs/api/#/Queue/get_api_v3_queue">...</a>
   */
  public static final String DOWNLOAD_BASE = "queue";

  /**
   * The base movie url for getting and adding movies (get, post requests)
   * See <a href="https://radarr.video/docs/api/#/Movie/post_api_v3_movie">...</a>
   */
  public static final String MOVIE_BASE = "movie";

  /**
   * See <a href="https://radarr.video/docs/api/#/MovieLookup/get_api_v3_movie_lookup">...</a>
   */
  public static final String MOVIE_LOOKUP = "movie/lookup";

  /**
   * See <a href="https://radarr.video/docs/api/#/MovieLookup/get_api_v3_movie_lookup_tmdb">...</a>
   */
  public static final String MOVIE_LOOKUP_TMDB = "movie/lookup/tmdb";

  /**
   * The url for triggering gets requests in radarr to discover new movies
   * See <a href="https://radarr.video/docs/api/#/ImportListMovies/get_api_v3_importlist_movie">...</a>
   */
  public static final String DISCOVER_MOVIES = "importlist/movie";

  /**
   * The url base for adding, getting, deleting movie profiles
   */
  public static final String PROFILE_BASE = "/qualityProfile";
}
