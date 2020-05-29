package com.botdarr.api.radarr;

import com.botdarr.Config;
import com.botdarr.api.*;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.CommandContext;
import com.botdarr.clients.ChatClient;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.connections.ConnectionHelper;
import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

public class RadarrApi implements Api {
  public RadarrApi(ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder) {
    this.chatClientResponseBuilder = chatClientResponseBuilder;
  }

  @Override
  public String getUrlBase() {
    return Config.getProperty(Config.Constants.RADARR_URL_BASE);
  }

  @Override
  public String getApiUrl(String path) {
    return getApiUrl(Config.Constants.RADARR_URL, Config.Constants.RADARR_TOKEN, path);
  }

  public List<ChatClientResponse> lookup(String search, boolean findNew) {
    return new LookupStrategy<RadarrMovie>(chatClientResponseBuilder, ContentType.MOVIE) {

      @Override
      public RadarrMovie lookupExistingItem(RadarrMovie lookupItem) {
        return RADARR_CACHE.getExistingMovie(lookupItem.getTmdbId());
      }

      @Override
      public List<RadarrMovie> lookup(String searchTerm) throws Exception {
        return lookupMovies(searchTerm);
      }

      @Override
      public ChatClientResponse getNewOrExistingItem(RadarrMovie lookupItem, RadarrMovie existingItem, boolean findNew) {
        return chatClientResponseBuilder.getNewOrExistingMovie(lookupItem, existingItem, findNew);
      }
    }.lookup(search, findNew);
  }

  @Override
  public List<ChatClientResponse> downloads() {
    return getDownloadsStrategy().downloads();
  }

  public List<ChatClientResponse> addWithTitle(String searchText) {
    return getAddStrategy().addWithSearchTitle(searchText);
  }

  public ChatClientResponse addWithId(String searchText, String tmdbId) {
    return getAddStrategy().addWithSearchId(searchText, tmdbId);
  }

  public List<ChatClientResponse> getProfiles() {
    Collection<RadarrProfile> profiles = RADARR_CACHE.getQualityProfiles();
    if (profiles == null || profiles.isEmpty()) {
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Found 0 profiles, please setup Radarr with at least one profile"));
    }

    List<ChatClientResponse> profileMessages = new ArrayList<>();
    for (RadarrProfile radarrProfile : profiles) {
      profileMessages.add(chatClientResponseBuilder.getMovieProfile(radarrProfile));
    }
    return profileMessages;
  }

  public List<ChatClientResponse> forceDownload(String command) {
    String decodedKey = new String(Base64.getDecoder().decode(command.getBytes()));
    //the hash format is guid:title
    //title couldn't contain : so we find the first occurrence
    int titleIndex = decodedKey.indexOf("title=");
    String[] decodedKeyArray = {decodedKey.substring(0, titleIndex - 1), decodedKey.substring(titleIndex + 6)};
    if (decodedKeyArray.length != 2) {
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Invalid key=" + decodedKey));
    }

    String guid = decodedKeyArray[0];
    String title = decodedKeyArray[1];
    List<RadarrTorrent> radarrTorrents = lookupTorrents(title);

    if (radarrTorrents.isEmpty()) {
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Found no movies to force download, title=" + title));
    }

    for (RadarrTorrent radarrTorrent : radarrTorrents) {
      if (radarrTorrent.getGuid().equalsIgnoreCase(guid)) {
        return ConnectionHelper.makePostRequest(this, RadarrUrls.RELEASE_BASE, radarrTorrent, new ConnectionHelper.SimpleMessageEmbedResponseHandler(chatClientResponseBuilder) {
          @Override
          public List<ChatClientResponse> onSuccess(String response) throws Exception {
            return Arrays.asList(chatClientResponseBuilder.createSuccessMessage("Forced the download for " + title));
          }
        });
      }
    }
    return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Could not force download movie for title=" + title + ", guid=" + guid));
  }

  public List<ChatClientResponse> lookupTorrents(String movieTitle, boolean showRejected) {
    List<RadarrTorrent> radarrTorrents = lookupTorrents(movieTitle);
    if (radarrTorrents.isEmpty()) {
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("No downloads available for " + movieTitle + ", make sure you have exact film name."));
    }

    List<ChatClientResponse> responses = new ArrayList<>();
    for (RadarrTorrent radarrTorrent : radarrTorrents) {
      if (!showRejected && radarrTorrent.isRejected()) {
        //dont show rejected torrents
        continue;
      }
      responses.add(chatClientResponseBuilder.getTorrentResponses(radarrTorrent, movieTitle));
    }

    if (responses.isEmpty()) {
      responses.add(chatClientResponseBuilder.createErrorMessage("Torrents were found but all of them were rejected based on your profiles/indexer settings for movie " + movieTitle));
    }

    return responses;
  }

  public List<ChatClientResponse> cancelDownload(String command) {
    try {
      Long id = Long.valueOf(command);
      return ConnectionHelper.makeDeleteRequest(this, RadarrUrls.DOWNLOAD_BASE + "/" + id, "&blacklist=true", new ConnectionHelper.SimpleMessageEmbedResponseHandler(chatClientResponseBuilder) {
        @Override
        public List<ChatClientResponse> onSuccess(String response) throws Exception {
          //TODO: implement
          return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Not implemented yet"));
        }
      });
    } catch (NumberFormatException e) {
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Require an id value to cancel a download, e=" + e.getMessage()));
    }
  }

  @Override
  public void sendPeriodicNotifications(ChatClient chatClient) {
    new PeriodicNotificationStrategy(ContentType.MOVIE, getDownloadsStrategy()) {
      @Override
      public void sendToConfiguredChannels(List downloads) {
        chatClient.sendToConfiguredChannels(downloads);
      }
    }.sendPeriodicNotifications();
  }

  @Override
  public void cacheData() {
    new CacheProfileStrategy<RadarrProfile, String>() {
      @Override
      public void deleteFromCache(List<String> profilesAddUpdated) {
        RADARR_CACHE.removeDeletedProfiles(profilesAddUpdated);
      }

      @Override
      public List<RadarrProfile> getProfiles() {
        return ConnectionHelper.makeGetRequest(RadarrApi.this, RadarrUrls.PROFILE_BASE, new ConnectionHelper.SimpleEntityResponseHandler<RadarrProfile>() {
          @Override
          public List<RadarrProfile> onSuccess(String response) {
            List<RadarrProfile> radarrProfiles = new ArrayList<>();
            JsonParser parser = new JsonParser();
            JsonArray json = parser.parse(response).getAsJsonArray();
            for (int i = 0; i < json.size(); i++) {
              RadarrProfile radarrProfile = new Gson().fromJson(json.get(i), RadarrProfile.class);
              radarrProfiles.add(radarrProfile);
            }
            return radarrProfiles;
          }
        });
      }

      @Override
      public void addProfile(RadarrProfile profile) {
        RADARR_CACHE.addProfile(profile);
      }
    }.cacheData();

    new CacheContentStrategy<RadarrMovie, Long>(this, RadarrUrls.MOVIE_BASE) {
      @Override
      public void deleteFromCache(List<Long> itemsAddedUpdated) {
        RADARR_CACHE.removeDeletedMovies(itemsAddedUpdated);
      }

      @Override
      public Long addToCache(JsonElement cacheItem) {
        RadarrMovie radarrMovie = new Gson().fromJson(cacheItem, RadarrMovie.class);
        RADARR_CACHE.add(radarrMovie);
        return radarrMovie.getKey();
      }
    }.cacheData();
  }

  @Override
  public String getApiToken() {
    return Config.Constants.RADARR_TOKEN;
  }

  public List<ChatClientResponse> discover() {
    return ConnectionHelper.makeGetRequest(this, RadarrUrls.DISCOVER_MOVIES, new ConnectionHelper.SimpleEntityResponseHandler<ChatClientResponse>() {
      @Override
      public List<ChatClientResponse> onSuccess(String response) throws Exception {
        List<ChatClientResponse> recommendedMovies = new ArrayList<>();
        if (response == null || response.isEmpty() || response.equalsIgnoreCase("[]")) {
          LOGGER.warn("Found no response when looking for movie recommendations");
          return Collections.emptyList();
        }
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();

        for (int i = 0; i < json.size(); i++) {
          if (i == new ApiRequests().getMaxResultsToShow()) {
            //don't show more than configured max results to show
            break;
          }
          RadarrMovie radarrMovie = new Gson().fromJson(json.get(i), RadarrMovie.class);
          recommendedMovies.add(chatClientResponseBuilder.getDiscoverableMovies(radarrMovie));
        }
        return recommendedMovies;
      }
    });
  }

  private DownloadsStrategy getDownloadsStrategy() {
    return new DownloadsStrategy(this, RadarrUrls.DOWNLOAD_BASE, chatClientResponseBuilder, ContentType.MOVIE) {
      @Override
      public ChatClientResponse getResponse(JsonElement rawElement) {
        RadarrQueue radarrQueue = new Gson().fromJson(rawElement, RadarrQueue.class);
        return chatClientResponseBuilder.getMovieDownloadResponses(radarrQueue);
      }
    };
  }

  private AddStrategy<RadarrMovie> getAddStrategy() {
    return new AddStrategy<RadarrMovie>(chatClientResponseBuilder, ContentType.MOVIE) {
      @Override
      public List<RadarrMovie> lookupContent(String search)  throws Exception {
        return lookupMovies(search);
      }

      @Override
      public List<RadarrMovie> lookupItemById(String id) throws Exception {
        return lookupMoviesById(id);
      }

      @Override
      public boolean doesItemExist(RadarrMovie content) {
        return RADARR_CACHE.doesMovieExist(content.getTitle());
      }

      @Override
      public String getItemId(RadarrMovie item) {
        return String.valueOf(item.getTmdbId());
      }

      @Override
      public ChatClientResponse addContent(RadarrMovie content) {
        return addMovie(content);
      }

      @Override
      public ChatClientResponse getResponse(RadarrMovie item) {
        return chatClientResponseBuilder.getMovieResponse(item);
      }

      @Override
      protected void cacheContent(RadarrMovie addContent) {
        RADARR_CACHE.add(addContent);
      }
    };
  }

  private ChatClientResponse addMovie(RadarrMovie radarrMovie) {
    //make sure we specify where the movie should get downloaded
    radarrMovie.setPath(Config.getProperty(Config.Constants.RADARR_PATH) + File.separator + radarrMovie.getTitle() + "(" + radarrMovie.getYear() + ")");
    //make sure the movie is monitored
    radarrMovie.setMonitored(true);

    String radarrProfileName = Config.getProperty(Config.Constants.RADARR_DEFAULT_PROFILE);
    RadarrProfile radarrProfile = RADARR_CACHE.getProfile(radarrProfileName.toLowerCase());
    if (radarrProfile == null) {
      return chatClientResponseBuilder.createErrorMessage("Could not find radarr profile for default " + radarrProfileName);
    }
    radarrMovie.setQualityProfileId((int) radarrProfile.getId());

    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(getApiUrl(RadarrUrls.MOVIE_BASE));

      post.addHeader("content-type", "application/x-www-form-urlencoded");
      String json = new Gson().toJson(radarrMovie, RadarrMovie.class);
      post.setEntity(new StringEntity(json));

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Client request=" + post.toString());
        LOGGER.debug("Client data=" + (json));
      }

      String username = CommandContext.getConfig().getUsername();
      ApiRequests apiRequests = new ApiRequests();
      ApiRequestType apiRequestType = ApiRequestType.MOVIE;
      if (apiRequests.checkRequestLimits(apiRequestType) && !apiRequests.canMakeRequests(apiRequestType, username)) {
        ApiRequestThreshold requestThreshold = ApiRequestThreshold.valueOf(Config.getProperty(Config.Constants.MAX_REQUESTS_THRESHOLD));
        return chatClientResponseBuilder.createErrorMessage("Could not add movie, user " + username + " has exceeded max movie requests for " + requestThreshold.getReadableName());
      }
      try (CloseableHttpResponse response = client.execute(post)) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Respone=" + response.toString());
          LOGGER.debug("Response content=" + IOUtils.toString(response.getEntity().getContent()));
          LOGGER.debug("Reason=" + response.getStatusLine().toString());
        }
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
          return chatClientResponseBuilder.createErrorMessage("Could not add movie, status-code=" + statusCode + ", reason=" + response.getStatusLine().getReasonPhrase());
        }
        LogManager.getLogger("AuditLog").info("User " + username + " added " + radarrMovie.getTitle());
        apiRequests.auditRequest(apiRequestType, username, radarrMovie.getTitle());
        return chatClientResponseBuilder.createSuccessMessage("Movie " + radarrMovie.getTitle() + " added, radarr-detail=" + response.getStatusLine().getReasonPhrase());
      }
    } catch (IOException e) {
      LOGGER.error("Error trying to add movie", e);
      return chatClientResponseBuilder.createErrorMessage("Error adding movie, error=" + e.getMessage());
    }
  }

  private List<RadarrTorrent> lookupTorrents(String title) {
    Long id = RADARR_CACHE.getMovieSonarrId(title);
    if (id == null) {
      LOGGER.warn("Could not find title id for title " + title);
      return Collections.emptyList();
    }
    return ConnectionHelper.makeGetRequest(this, RadarrUrls.RELEASE_BASE, "&movieId=" + id + "&sort_by=releaseWeight&order=asc", new ConnectionHelper.SimpleEntityResponseHandler<RadarrTorrent>() {
      @Override
      public List<RadarrTorrent> onSuccess(String response) throws Exception {
        List<RadarrTorrent> radarrTorrents = new ArrayList<>();
        if (response == null || response.isEmpty() || response.equalsIgnoreCase("[]")) {
          LOGGER.warn("Found no response when looking for radarr torrents");
          return Collections.emptyList();
        }
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();

        for (int i = 0; i < json.size(); i++) {
          radarrTorrents.add(new Gson().fromJson(json.get(i), RadarrTorrent.class));
        }
        return radarrTorrents;
      }
    });
  }

  private List<RadarrMovie> lookupMovies(String search) throws Exception {
    return ConnectionHelper.makeGetRequest(this, RadarrUrls.MOVIE_LOOKUP, "&term=" + URLEncoder.encode(search, "UTF-8"),
      new ConnectionHelper.SimpleEntityResponseHandler<RadarrMovie>() {
      @Override
      public List<RadarrMovie> onSuccess(String response) {
        List<RadarrMovie> movies = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          movies.add(new Gson().fromJson(json.get(i), RadarrMovie.class));
        }
        return movies;
      }
    });
  }

  private List<RadarrMovie> lookupMoviesById(String tmdbid) throws Exception {
    return ConnectionHelper.makeGetRequest(this, RadarrUrls.MOVIE_LOOKUP_TMDB, "&tmdbId=" + URLEncoder.encode(tmdbid, "UTF-8"),
      new ConnectionHelper.SimpleEntityResponseHandler<RadarrMovie>() {
      @Override
      public List<RadarrMovie> onSuccess(String response) {
        List<RadarrMovie> movies = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(response).getAsJsonObject();
        movies.add(new Gson().fromJson(json, RadarrMovie.class));
        return movies;
      }
    });
  }

  private final ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  private static final RadarrCache RADARR_CACHE = new RadarrCache();
  public static final String ADD_MOVIE_COMMAND_FIELD_PREFIX = "Add movie command";
}
