package com.botdarr.api;

import com.botdarr.Config;
import com.botdarr.api.sonarr.*;
import com.botdarr.clients.ChatClient;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.CommandContext;
import com.botdarr.connections.ConnectionHelper;
import com.google.gson.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SonarrApi implements Api {
  public SonarrApi(ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder) {
    this.chatClientResponseBuilder = chatClientResponseBuilder;
  }

  @Override
  public String getUrlBase() {
    return Config.getProperty(Config.Constants.SONARR_URL_BASE);
  }

  @Override
  public String getApiUrl(String path) {
    return getApiUrl(Config.Constants.SONARR_URL, Config.Constants.SONARR_TOKEN, path);
  }

  @Override
  public List<ChatClientResponse> downloads() {
    List<ChatClientResponse> chatClientResponses = getShowDownloads();
    if (chatClientResponses.isEmpty()) {
      chatClientResponses.add(chatClientResponseBuilder.createInfoMessage("No shows downloading"));
    }
    return chatClientResponses;
  }

  public ChatClientResponse addWithId(String searchText, String id) {
    try {
      List<SonarrShow> shows = lookupShows(searchText);
      if (shows.size() == 0) {
        return chatClientResponseBuilder.createErrorMessage("No shows found");
      }
      for (SonarrShow sonarrShow : shows) {
        if (sonarrShow.getTvdbId() == Integer.valueOf(id)) {
          return addShow(sonarrShow);
        }
      }
      return chatClientResponseBuilder.createErrorMessage("Could not find show with search text=" + searchText + " and id=" + id);
    } catch (Exception e) {
      LOGGER.error("Error trying to add show=" + searchText, e);
      return chatClientResponseBuilder.createErrorMessage("Error adding content, e=" + e.getMessage());
    }
  }

  public List<ChatClientResponse> addWithTitle(String searchText) {
    try {
      List<SonarrShow> shows = lookupShows(searchText);
      if (shows.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createErrorMessage("No shows found"));
      }
      if (shows.size() == 1) {
        SonarrShow sonarrShow = shows.get(0);
        if (SONARR_CACHE.doesShowExist(sonarrShow.getTitle())) {
          return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Show already exists"));
        }
        return Arrays.asList(addShow(shows.get(0)));
      }
      List<ChatClientResponse> restOfShows = new ArrayList<>();
      for (SonarrShow sonarrShow : shows) {
        if (SONARR_CACHE.doesShowExist(sonarrShow.getTitle())) {
          //skip existing movies
          continue;
        }
        restOfShows.add(chatClientResponseBuilder.getShowResponse(sonarrShow));
      }
      if (restOfShows.size() > 1) {
        restOfShows.add(0, chatClientResponseBuilder.createInfoMessage("Too many shows found, please narrow search"));
      }
      if (restOfShows.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createInfoMessage("No new shows found, check existing movies"));
      }
      return subList(restOfShows);
    } catch (Exception e) {
      LOGGER.error("Error found trying to add show=" + searchText, e);
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Error trying to add show " + searchText + ", e=" + e.getMessage()));
    }
  }

  public List<ChatClientResponse> lookup(String search, boolean findNew) {
    try {
      List<ChatClientResponse> responses = new ArrayList<>();
      List<SonarrShow> shows = lookupShows(search);
      for (SonarrShow sonarrShow : shows) {
        //TODO: should we try to lookup shows with rage/maze id's as well?
        SonarrShow existingShow = SONARR_CACHE.getExistingShowFromTvdbId(sonarrShow.getTvdbId());
        boolean isExistingMovie = existingShow != null;
        boolean skip = findNew ? isExistingMovie : !isExistingMovie;
        if (skip) {
          continue;
        }
        responses.add(chatClientResponseBuilder.getNewOrExistingShow(sonarrShow, existingShow, findNew));
      }
      if (responses.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Could not find any " + (findNew ? "new" : "existing") + " shows for search term=" + search));
      }
      if (responses.size() > MAX_RESULTS_TO_SHOW) {
        responses = subList(responses);
        responses.add(0, chatClientResponseBuilder.createInfoMessage("Too many shows found, limiting results to " + MAX_RESULTS_TO_SHOW));
      }
      return responses;
    } catch (Exception e) {
      LOGGER.error("Error trying to lookup show, searchText=" + search, e);
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Error looking up content, e=" + e.getMessage()));
    }
  }

  public List<ChatClientResponse> lookupTorrents(String command, boolean showRejected) {
    //TODO: implement
    return null;
  }

  public List<ChatClientResponse> cancelDownload(long id) {
    //TODO: implement
    return null;
  }

  public List<ChatClientResponse> getProfiles() {
    Collection<SonarrProfile> profiles = SONARR_CACHE.getQualityProfiles();
    if (profiles == null || profiles.isEmpty()) {
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Found 0 profiles, please setup Sonarr with at least one profile"));
    }

    List<ChatClientResponse> profileMessages = new ArrayList<>();
    for (SonarrProfile sonarrProfile : profiles) {
      profileMessages.add(chatClientResponseBuilder.getShowProfile(sonarrProfile));
    }
    return profileMessages;
  }

  public List<ChatClientResponse> forceDownload(String command) {
    //TODO: implement
    return null;
  }

  @Override
  public void sendPeriodicNotifications(ChatClient chatClient) {
    List<ChatClientResponse> downloads = getShowDownloads();
    if (downloads != null && !downloads.isEmpty()) {
      chatClient.sendMessage(downloads, null);
    } else {
      LOGGER.debug("No show downloads available for sending");
    }
  }

  @Override
  public void cacheData() {
    ConnectionHelper.makeGetRequest(this, "/series", new ConnectionHelper.SimpleEntityResponseHandler<SonarrShow>() {
      @Override
      public List<SonarrShow> onSuccess(String response) throws Exception {
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          SonarrShow sonarrShow = new Gson().fromJson(json.get(i), SonarrShow.class);
          SONARR_CACHE.add(sonarrShow);
        }
        return null;
      }
    });
    List<SonarrProfile> sonarrProfiles = getSonarrProfiles();
    for (SonarrProfile sonarrProfile : sonarrProfiles) {
      SONARR_CACHE.addProfile(sonarrProfile);
    }
    LOGGER.info("Finished caching sonarr data");
  }

  @Override
  public String getApiToken() {
    return Config.Constants.SONARR_TOKEN;
  }

  private List<ChatClientResponse> getShowDownloads() {
    return ConnectionHelper.makeGetRequest(this, "queue", new ConnectionHelper.SimpleMessageEmbedResponseHandler(chatClientResponseBuilder) {
      @Override
      public List<ChatClientResponse> onSuccess(String response) throws Exception {
        List<ChatClientResponse> responses = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          SonarrQueue showQueue = new Gson().fromJson(json.get(i), SonarrQueue.class);
          SonarQueueEpisode episode = showQueue.getEpisode();
          if (episode == null) {
            //something is wrong with the download, skip
            LOGGER.error("Series " + showQueue.getSonarrQueueShow().getTitle() + " missing episode info for id " + showQueue.getId());
            continue;
          }
          responses.add(chatClientResponseBuilder.getShowDownloadResponses(showQueue));
        }
        if (json.size() >= MAX_RESULTS_TO_SHOW) {
          responses = subList(responses);
          responses.add(0, chatClientResponseBuilder.createInfoMessage("Too many downloads, limiting results to " + MAX_RESULTS_TO_SHOW));
        }
        return responses;
      }
    });
  }

  private ChatClientResponse addShow(SonarrShow sonarrShow) {
    String title = sonarrShow.getTitle();
    //make sure we specify where the show should get downloaded
    sonarrShow.setPath(Config.getProperty(Config.Constants.SONARR_PATH) + "/" + title);
    //make sure the show is monitored
    sonarrShow.setMonitored(true);
    //make sure to have seasons stored in separate folders
    sonarrShow.setSeasonFolder(true);

    String sonarrProfileName = Config.getProperty(Config.Constants.SONARR_DEFAULT_PROFILE);
    SonarrProfile sonarrProfile = SONARR_CACHE.getProfile(sonarrProfileName.toLowerCase());
    if (sonarrProfile == null) {
      return chatClientResponseBuilder.createErrorMessage("Could not find sonarr profile for default " + sonarrProfile);
    }
    sonarrShow.setQualityProfileId((int) sonarrProfile.getId());
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(getApiUrl("series"));

      post.addHeader("content-type", "application/x-www-form-urlencoded");
      post.setEntity(new StringEntity(new GsonBuilder().addSerializationExclusionStrategy(excludeUnnecessaryFields).create().toJson(sonarrShow, SonarrShow.class)));

      try (CloseableHttpResponse response = client.execute(post)) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
          return chatClientResponseBuilder.createErrorMessage("Could not add show, status-code=" + statusCode + ", reason=" + response.getStatusLine().getReasonPhrase());
        }
        LogManager.getLogger("AuditLog").info("User " + CommandContext.getConfig().getUsername() + " added " + title);
        return chatClientResponseBuilder.createSuccessMessage("Show " + title + " added, sonarr-detail=" + response.getStatusLine().getReasonPhrase());
      }
    } catch (IOException e) {
      LOGGER.error("Error trying to add show=" + title, e);
      return chatClientResponseBuilder.createErrorMessage("Error adding show=" + title + ", error=" + e.getMessage());
    }
  }

  private List<SonarrShow> lookupShows(String search) throws Exception {
    return ConnectionHelper.makeGetRequest(this, "series/lookup", "&term=" + URLEncoder.encode(search, "UTF-8"), new ConnectionHelper.SimpleEntityResponseHandler<SonarrShow>() {
      @Override
      public List<SonarrShow> onSuccess(String response) {
        List<SonarrShow> movies = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          movies.add(new Gson().fromJson(json.get(i), SonarrShow.class));
        }
        return movies;
      }
    });
  }

  private List<SonarrProfile> getSonarrProfiles() {
    return ConnectionHelper.makeGetRequest(this, "profile", new ConnectionHelper.SimpleEntityResponseHandler<SonarrProfile>() {
      @Override
      public List<SonarrProfile> onSuccess(String response) {
        List<SonarrProfile> sonarrProfiles = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          SonarrProfile sonarrProfile = new Gson().fromJson(json.get(i), SonarrProfile.class);
          sonarrProfiles.add(sonarrProfile);
        }
        return sonarrProfiles;
      }
    });
  }

  private ExclusionStrategy excludeUnnecessaryFields = new ExclusionStrategy() {
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
      //profileId breaks the post request to /series for some reason and I don't believe its a required field
      return fieldAttributes.getName().equalsIgnoreCase("profileId");
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
      return false;
    }
  };

  private List<ChatClientResponse> subList(List<ChatClientResponse> responses) {
    return responses.subList(0, responses.size() > MAX_RESULTS_TO_SHOW ? MAX_RESULTS_TO_SHOW - 1 : responses.size());
  }

  private final ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  private static final SonarrCache SONARR_CACHE = new SonarrCache();
  private static final int MAX_RESULTS_TO_SHOW = 20;
  public static final String ADD_SHOW_COMMAND_FIELD_PREFIX = "Add show command";
}
