package com.botdarr.api;

import com.botdarr.Config;
import com.botdarr.api.radarr.*;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.CommandContext;
import com.botdarr.clients.ChatClient;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.connections.ConnectionHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    try {
      List<ChatClientResponse> responses = new ArrayList<>();
      List<RadarrMovie> movies = lookupMovies(search);
      for (RadarrMovie lookupMovie : movies) {
        RadarrMovie existingMovie = RADARR_CACHE.getExistingMovie(lookupMovie.getTmdbId());
        boolean isExistingMovie = existingMovie != null;
        boolean skip = findNew ? isExistingMovie : !isExistingMovie;
        if (skip) {
          continue;
        }
        responses.add(chatClientResponseBuilder.getNewOrExistingMovie(lookupMovie, existingMovie, findNew));
      }
      if (responses.size() > MAX_RESULTS_TO_SHOW) {
        responses = responses.subList(0, MAX_RESULTS_TO_SHOW - 1);
        responses.add(0, chatClientResponseBuilder.createInfoMessage("Too many movies found, please narrow search"));
      }
      if (responses.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Could not find any " + (findNew ? "new" : "existing") + " movies for search term=" + search));
      }
      return responses;
    } catch (Exception e) {
      LOGGER.error("Error trying to lookup movie", e);
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Error looking up content, e=" + e.getMessage()));
    }
  }

  @Override
  public List<ChatClientResponse> downloads() {
    List<ChatClientResponse> chatClientResponses = getMovieDownloads();
    if (chatClientResponses.isEmpty()) {
      chatClientResponses.add(chatClientResponseBuilder.createInfoMessage("No movies downloading"));
    }
    return chatClientResponses;
  }

  public List<ChatClientResponse> addWithTitle(String searchText) {
    try {
      List<RadarrMovie> movies = lookupMovies(searchText);
      if (movies.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createInfoMessage("No movies found"));
      }

      if (movies.size() == 1) {
        RadarrMovie radarrMovie = movies.get(0);
        if (RADARR_CACHE.doesMovieExist(radarrMovie.getTitle())) {
          return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Movie already exists"));
        }
        return Arrays.asList(addMovie(movies.get(0)));
      }
      List<ChatClientResponse> restOfMovies = new ArrayList<>();
      for (RadarrMovie radarrMovie : movies) {
        if (RADARR_CACHE.doesMovieExist(radarrMovie.getTitle())) {
          //skip existing movies
          continue;
        }
        restOfMovies.add(chatClientResponseBuilder.getMovie(radarrMovie));
      }
      if (restOfMovies.size() > 1) {
        restOfMovies = subList(restOfMovies);
        restOfMovies.add(0, chatClientResponseBuilder.createInfoMessage("Too many movies found, please narrow search"));
      }
      if (restOfMovies.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createInfoMessage("No new movies found, check existing movies"));
      }
      return restOfMovies;
    } catch (Exception e) {
      LOGGER.error("Error trying to add movie", e);
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Error trying to add movie " + searchText + ", e=" + e.getMessage()));
    }
  }

  public ChatClientResponse addWithId(String searchText, String id) {
    try {
      List<RadarrMovie> movies = lookupMovies(searchText);
      if (movies.isEmpty()) {
        LOGGER.warn("Search text " + searchText + "yielded no movies, trying id");
      }
      movies = lookupMovieById(id);
      if (movies.isEmpty()) {
        LOGGER.warn("Search id " + id + "yielded no movies, stopping");
        return chatClientResponseBuilder.createErrorMessage("No movies found");
      }
      for (RadarrMovie radarrMovie : movies) {
        if (radarrMovie.getTmdbId() == Integer.valueOf(id)) {
          return addMovie(radarrMovie);
        }
      }
      return chatClientResponseBuilder.createErrorMessage("Could not find movie with search text=" + searchText + " and id=" + id);
    } catch (Exception e) {
      LOGGER.error("Error trying to add movie", e);
      return chatClientResponseBuilder.createErrorMessage("Error adding content, e=" + e.getMessage());
    }
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
        return ConnectionHelper.makePostRequest(this, "release", radarrTorrent, new ConnectionHelper.SimpleMessageEmbedResponseHandler(chatClientResponseBuilder) {
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
      return ConnectionHelper.makeDeleteRequest(this, "queue/" + id, "&blacklist=true", new ConnectionHelper.SimpleMessageEmbedResponseHandler(chatClientResponseBuilder) {
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
    List<ChatClientResponse> downloads = getMovieDownloads();
    if (downloads != null && !downloads.isEmpty()) {
      chatClient.sendMessage(downloads, null);
    } else {
      LOGGER.debug("No movie downloads available for sending");
    }
  }

  @Override
  public void cacheData() {
    ConnectionHelper.makeGetRequest(this, "/movie", new ConnectionHelper.SimpleEntityResponseHandler<RadarrMovie>() {
      @Override
      public List<RadarrMovie> onSuccess(String response) throws Exception {
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          RadarrMovie radarrMovie = new Gson().fromJson(json.get(i), RadarrMovie.class);
          RADARR_CACHE.add(radarrMovie);
        }
        return null;
      }
    });

    List<RadarrProfile> radarrProfiles = getRadarrProfiles();
    for (RadarrProfile radarrProfile : radarrProfiles) {
      RADARR_CACHE.addProfile(radarrProfile);
    }
    LOGGER.info("Finished caching radarr data");
  }

  @Override
  public String getApiToken() {
    return Config.Constants.RADARR_TOKEN;
  }

  public List<ChatClientResponse> discover() {
    return ConnectionHelper.makeGetRequest(this, "movies/discover/recommendations", new ConnectionHelper.SimpleEntityResponseHandler<ChatClientResponse>() {
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
          if (i == MAX_RESULTS_TO_SHOW) {
            //don't show more than MAX_RESULTS_TO_SHOW
            break;
          }
          RadarrMovie radarrMovie = new Gson().fromJson(json.get(i), RadarrMovie.class);
          recommendedMovies.add(chatClientResponseBuilder.getDiscoverableMovies(radarrMovie));
        }
        return recommendedMovies;
      }
    });
  }

  private List<ChatClientResponse> getMovieDownloads() {
    return ConnectionHelper.makeGetRequest(this, "queue", new ConnectionHelper.SimpleMessageEmbedResponseHandler(chatClientResponseBuilder) {
      @Override
      public List<ChatClientResponse> onSuccess(String response) throws Exception {
        List<ChatClientResponse> chatClientResponses = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        boolean tooManyDownloads = json.size() >= MAX_RESULTS_TO_SHOW;
        for (int i = 0; i < json.size(); i++) {
          RadarrQueue radarrQueue = new Gson().fromJson(json.get(i), RadarrQueue.class);
          chatClientResponses.add(chatClientResponseBuilder.getMovieDownloadResponses(radarrQueue));
        }
        if (tooManyDownloads) {
          chatClientResponses = subList(chatClientResponses);
          chatClientResponses.add(0, chatClientResponseBuilder.createInfoMessage("Too many downloads, limiting results to " + MAX_RESULTS_TO_SHOW));
        }
        return chatClientResponses;
      }
    });
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
      HttpPost post = new HttpPost(getApiUrl("movie"));

      post.addHeader("content-type", "application/x-www-form-urlencoded");
      String json = new Gson().toJson(radarrMovie, RadarrMovie.class);
      post.setEntity(new StringEntity(json));

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Client request=" + post.toString());
        LOGGER.debug("Client data=" + (json));
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
        LogManager.getLogger("AuditLog").info("User " + CommandContext.getConfig().getUsername() + " added " + radarrMovie.getTitle());
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
    return ConnectionHelper.makeGetRequest(this, "release", "&movieId=" + id + "&sort_by=releaseWeight&order=asc", new ConnectionHelper.SimpleEntityResponseHandler<RadarrTorrent>() {
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
    return ConnectionHelper.makeGetRequest(this, "movie/lookup", "&term=" + URLEncoder.encode(search, "UTF-8"),
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

  private List<RadarrMovie> lookupMovieById(String tmdbid) throws Exception {
    return ConnectionHelper.makeGetRequest(this, "movie/lookup/tmdb", "&tmdbId=" + URLEncoder.encode(tmdbid, "UTF-8"),
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

  private List<RadarrProfile> getRadarrProfiles() {
    return ConnectionHelper.makeGetRequest(this, "profile", new ConnectionHelper.SimpleEntityResponseHandler<RadarrProfile>() {
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

  private List<ChatClientResponse> subList(List<ChatClientResponse> responses) {
    return responses.subList(0, responses.size() > MAX_RESULTS_TO_SHOW ? MAX_RESULTS_TO_SHOW - 1 : responses.size());
  }

  private final ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  private static final RadarrCache RADARR_CACHE = new RadarrCache();
  private static final int MAX_RESULTS_TO_SHOW = 20;
}
