package com.botdarr.api.sonarr;

import com.botdarr.Config;
import com.botdarr.api.*;
import com.botdarr.api.radarr.RadarrMovie;
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
import java.util.*;

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
    return getDownloadsStrategy().downloads();
  }

  public ChatClientResponse addWithId(String searchText, String id) {
    return getAddStrategy().addWithSearchId(searchText, id);
  }

  public List<ChatClientResponse> addWithTitle(String searchText) {
    return getAddStrategy().addWithSearchTitle(searchText);
  }

  public List<ChatClientResponse> lookup(String search, boolean findNew) {
    return new LookupStrategy<SonarrShow>(chatClientResponseBuilder, ContentType.SHOW) {

      @Override
      public SonarrShow lookupExistingItem(SonarrShow lookupItem) {
        return SONARR_CACHE.getExistingShowFromTvdbId(lookupItem.getTvdbId());
      }

      @Override
      public List<SonarrShow> lookup(String searchTerm) throws Exception {
        return lookupShows(searchTerm);
      }

      @Override
      public ChatClientResponse getNewOrExistingItem(SonarrShow lookupItem, SonarrShow existingItem, boolean findNew) {
        return chatClientResponseBuilder.getNewOrExistingShow(lookupItem, existingItem, findNew);
      }

      @Override
      public boolean isPathBlacklisted(SonarrShow item) {
        for (String path : Config.getExistingItemBlacklistPaths()) {
          if (item.getPath() != null && item.getPath().startsWith(path)) {
            return true;
          }
        }
        return false;
      }
    }.lookup(search, findNew);
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

  @Override
  public void sendPeriodicNotifications(ChatClient chatClient) {
    new PeriodicNotificationStrategy(ContentType.SHOW, getDownloadsStrategy()) {
      @Override
      public void sendToConfiguredChannels(List downloads) {
        chatClient.sendToConfiguredChannels(downloads);
      }
    }.sendPeriodicNotifications();
  }

  @Override
  public void cacheData() {
    new CacheProfileStrategy<SonarrProfile, String>() {
      @Override
      public void deleteFromCache(List<String> profilesAddUpdated) {
        SONARR_CACHE.removeDeletedProfiles(profilesAddUpdated);
      }

      @Override
      public List<SonarrProfile> getProfiles() {
        return ConnectionHelper.makeGetRequest(SonarrApi.this, SonarrUrls.PROFILE, new ConnectionHelper.SimpleEntityResponseHandler<List<SonarrProfile>>() {
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

      @Override
      public void addProfile(SonarrProfile profile) {
        SONARR_CACHE.addProfile(profile);
      }
    }.cacheData();

    new CacheContentStrategy<SonarrShow, Long>(this, SonarrUrls.SERIES_BASE) {
      @Override
      public void deleteFromCache(List<Long> itemsAddedUpdated) {
        SONARR_CACHE.removeDeletedShows(itemsAddedUpdated);
      }

      @Override
      public Long addToCache(JsonElement cacheItem) {
        SonarrShow sonarrShow = new Gson().fromJson(cacheItem, SonarrShow.class);
        SONARR_CACHE.add(sonarrShow);
        return sonarrShow.getKey();
      }
    }.cacheData();
  }

  @Override
  public String getApiToken() {
    return Config.Constants.SONARR_TOKEN;
  }

  private AddStrategy<SonarrShow> getAddStrategy() {
    return new AddStrategy<SonarrShow>(chatClientResponseBuilder, ContentType.SHOW) {
      @Override
      public List<SonarrShow> lookupContent(String search) throws Exception {
        return lookupShows(search);
      }

      @Override
      public List<SonarrShow> lookupItemById(String id) {
        //TODO: if sonarr has a lookup by id, implement
        return Collections.emptyList();
      }

      @Override
      public boolean doesItemExist(SonarrShow content) {
        return SONARR_CACHE.doesShowExist(content.getTitle());
      }

      @Override
      public String getItemId(SonarrShow item) {
        return String.valueOf(item.getTvdbId());
      }

      @Override
      public ChatClientResponse addContent(SonarrShow content) {
        return addShow(content);
      }

      @Override
      public ChatClientResponse getResponse(SonarrShow item) {
        return chatClientResponseBuilder.getShowResponse(item);
      }
    };
  }

  private DownloadsStrategy getDownloadsStrategy() {
    return new DownloadsStrategy(this, SonarrUrls.DOWNLOAD_BASE, chatClientResponseBuilder, ContentType.SHOW) {
      @Override
      public ChatClientResponse getResponse(JsonElement rawElement) {
        SonarrQueue showQueue = new Gson().fromJson(rawElement, SonarrQueue.class);
        SonarQueueEpisode episode = showQueue.getEpisode();
        if (episode == null) {
          //something is wrong with the download, skip
          LOGGER.error("Series " + showQueue.getSonarrQueueShow().getTitle() + " missing episode info for id " + showQueue.getId());
          return null;
        }
        return chatClientResponseBuilder.getShowDownloadResponses(showQueue);
      }
    };
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
    String username = CommandContext.getConfig().getUsername();
    ApiRequests apiRequests = new ApiRequests();
    ApiRequestType apiRequestType = ApiRequestType.SHOW;
    if (apiRequests.checkRequestLimits(apiRequestType) && !apiRequests.canMakeRequests(apiRequestType, username)) {
      ApiRequestThreshold requestThreshold = ApiRequestThreshold.valueOf(Config.getProperty(Config.Constants.MAX_REQUESTS_THRESHOLD));
      return chatClientResponseBuilder.createErrorMessage("Could not add show, user " + username + " has exceeded max show requests for " + requestThreshold.getReadableName());
    }
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(getApiUrl(SonarrUrls.SERIES_BASE));

      post.addHeader("content-type", "application/x-www-form-urlencoded");
      post.setEntity(new StringEntity(new GsonBuilder().addSerializationExclusionStrategy(excludeUnnecessaryFields).create().toJson(sonarrShow, SonarrShow.class)));

      try (CloseableHttpResponse response = client.execute(post)) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
          return chatClientResponseBuilder.createErrorMessage("Could not add show, status-code=" + statusCode + ", reason=" + response.getStatusLine().getReasonPhrase());
        }
        //cache show after successful request
        SONARR_CACHE.add(sonarrShow);
        LogManager.getLogger("AuditLog").info("User " + username + " added " + title);
        apiRequests.auditRequest(apiRequestType, username, title);
        return chatClientResponseBuilder.createSuccessMessage("Show " + title + " added, sonarr-detail=" + response.getStatusLine().getReasonPhrase());
      }
    } catch (IOException e) {
      LOGGER.error("Error trying to add show=" + title, e);
      return chatClientResponseBuilder.createErrorMessage("Error adding show=" + title + ", error=" + e.getMessage());
    }
  }

  private List<SonarrShow> lookupShows(String search) throws Exception {
    return ConnectionHelper.makeGetRequest(this, SonarrUrls.LOOKUP_SERIES, "&term=" + URLEncoder.encode(search, "UTF-8"), new ConnectionHelper.SimpleEntityResponseHandler<List<SonarrShow>>() {
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

  private final ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  private static final SonarrCache SONARR_CACHE = new SonarrCache();
  public static final String ADD_SHOW_COMMAND_FIELD_PREFIX = "Add show command";
}
