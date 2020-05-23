package com.botdarr.api.lidarr;

import com.botdarr.Config;
import com.botdarr.api.*;
import com.botdarr.api.sonarr.SonarrUrls;
import com.botdarr.clients.ChatClient;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.CommandContext;
import com.botdarr.connections.ConnectionHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LidarrApi implements Api {
  public LidarrApi(ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder) {
    this.chatClientResponseBuilder = chatClientResponseBuilder;
  }

  @Override
  public String getUrlBase() {
    return Config.getProperty(Config.Constants.LIDARR_URL_BASE);
  }

  @Override
  public String getApiUrl(String path) {
    return getApiUrl(Config.Constants.LIDARR_URL, Config.Constants.LIDARR_TOKEN, path);
  }

  @Override
  public List<ChatClientResponse> downloads() {
    return getDownloadsStrategy().downloads();
  }

  @Override
  public void sendPeriodicNotifications(ChatClient chatClient) {
    new PeriodicNotificationStrategy(ContentType.ARTIST, getDownloadsStrategy()) {
      @Override
      public void sendToConfiguredChannels(List downloads) {
        chatClient.sendToConfiguredChannels(downloads);
      }
    }.sendPeriodicNotifications();
  }

  @Override
  public void cacheData() {
    new CacheProfileStrategy<LidarrProfile>() {

      @Override
      public void resetCache() {
        LIDARR_CACHE.resetProfiles();
      }

      @Override
      public List<LidarrProfile> getProfiles() {
        return ConnectionHelper.makeGetRequest(LidarrApi.this, LidarrUrls.PROFILE, new ConnectionHelper.SimpleEntityResponseHandler<LidarrProfile>() {
          @Override
          public List<LidarrProfile> onSuccess(String response) {
            List<LidarrProfile> lidarrProfiles = new ArrayList<>();
            JsonParser parser = new JsonParser();
            JsonArray json = parser.parse(response).getAsJsonArray();
            for (int i = 0; i < json.size(); i++) {
              LidarrProfile lidarrProfile = new Gson().fromJson(json.get(i), LidarrProfile.class);
              lidarrProfiles.add(lidarrProfile);
            }
            return lidarrProfiles;
          }
        });
      }

      @Override
      public void addProfile(LidarrProfile profile) {
        LIDARR_CACHE.addProfile(profile);
      }
    }.cacheData();

    new CacheContentStrategy<LidarrArtist>(this, LidarrUrls.ALL_ARTISTS) {

      @Override
      public void resetCache() {
        LIDARR_CACHE.resetArtists();
      }

      @Override
      public void addToCache(JsonElement cacheItem) {
        LIDARR_CACHE.addArtist(new Gson().fromJson(cacheItem, LidarrArtist.class));
      }
    }.cacheData();

    //TODO: add album cache
  }

  @Override
  public String getApiToken() {
    return null;
  }

  public List<ChatClientResponse> addArtist(String artistToSearch) {
    return getArtistAddStrategy().addWithSearchTitle(artistToSearch);
  }

  public List<ChatClientResponse> lookupArtists(String search, boolean findNew) {
    return new LookupStrategy<LidarrArtist>(chatClientResponseBuilder, ContentType.ARTIST) {

      @Override
      public LidarrArtist lookupExistingItem(LidarrArtist lookupItem) {
        return LIDARR_CACHE.getExistingArtist(lookupItem);
      }

      @Override
      public List<LidarrArtist> lookup(String searchTerm) throws Exception {
        return lookupArtists(searchTerm);
      }

      @Override
      public ChatClientResponse getNewOrExistingItem(LidarrArtist lookupItem, LidarrArtist existingItem, boolean findNew) {
        return chatClientResponseBuilder.getNewOrExistingArtist(lookupItem, existingItem, findNew);
      }
    }.lookup(search, findNew);
  }

  /*
  public ChatClientResponse addArtistWithId() {
    //http://192.168.1.196:8686/api/v1/artist
    //post

  }*/

  private AddStrategy<LidarrArtist> getArtistAddStrategy() {
    return new AddStrategy<LidarrArtist>(chatClientResponseBuilder, ContentType.ARTIST) {
      @Override
      public List<LidarrArtist> lookupContent(String search) throws Exception {
        return lookupArtists(search);
      }

      @Override
      public List<LidarrArtist> lookupItemById(String id) throws Exception {
        //TODO: if there is a way to search artist by id, implement
        return Collections.emptyList();
      }

      @Override
      public boolean doesItemExist(LidarrArtist content) {
        return LIDARR_CACHE.doesArtistExist(content);
      }

      @Override
      public String getItemId(LidarrArtist item) {
        return item.getForeignArtistId();
      }

      @Override
      public ChatClientResponse addContent(LidarrArtist content) {
        return addArtist(content);
      }

      @Override
      public ChatClientResponse getResponse(LidarrArtist item) {
        return null;
      }
    };
  }

  private DownloadsStrategy getDownloadsStrategy() {
    return new DownloadsStrategy(this, LidarrUrls.DOWNLOAD_BASE, chatClientResponseBuilder, ContentType.ARTIST) {
      @Override
      public ChatClientResponse getResponse(JsonElement rawElement) {
        LidarrQueue lidarrQueue = new Gson().fromJson(rawElement, LidarrQueue.class);
        return chatClientResponseBuilder.getArtistDownloadResponses(lidarrQueue);
      }
    };
  }

  private List<LidarrArtist> lookupArtists(String search) throws Exception {
    return ConnectionHelper.makeGetRequest(this, LidarrUrls.LOOKUP_ARTISTS, "&term=" + URLEncoder.encode(search, "UTF-8"),
      new ConnectionHelper.SimpleEntityResponseHandler<LidarrArtist>() {
        @Override
        public List<LidarrArtist> onSuccess(String response) {
          List<LidarrArtist> artists = new ArrayList<>();
          JsonParser parser = new JsonParser();
          JsonArray json = parser.parse(response).getAsJsonArray();
          for (int i = 0; i < json.size(); i++) {
            artists.add(new Gson().fromJson(json.get(i), LidarrArtist.class));
          }
          return artists;
        }
      }
    );
  }

  private ChatClientResponse addArtist(LidarrArtist lidarrArtist) {
    String artistName = lidarrArtist.getArtistName();
    lidarrArtist.setMonitored(true);
    lidarrArtist.setRootFolderrPath(Config.getProperty(Config.Constants.LIDARR_PATH) + "/" + artistName);

    String lidarrProfileName = Config.getProperty(Config.Constants.LIDARR_DEFAULT_PROFILE);
    LidarrProfile lidarrProfile = LIDARR_CACHE.getProfile(lidarrProfileName.toLowerCase());
    if (lidarrProfile == null) {
      return chatClientResponseBuilder.createErrorMessage("Could not find lidarr profile for default " + lidarrProfileName);
    }
    lidarrArtist.setQualityProfileId(lidarrProfile.getId());
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(getApiUrl(SonarrUrls.ARTIST_BASE));
      post.addHeader("content-type", "application/x-www-form-urlencoded");
      String json = new Gson().toJson(lidarrArtist, LidarrArtist.class);
      post.setEntity(new StringEntity(json));

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Client request=" + post.toString());
        LOGGER.debug("Client data=" + (json));
      }

      String username = CommandContext.getConfig().getUsername();

      ApiRequests apiRequests = new ApiRequests();
      ApiRequestType apiRequestType = ApiRequestType.ARTIST;
      if (apiRequests.checkRequestLimits(apiRequestType) && !apiRequests.canMakeRequests(apiRequestType, username)) {
        ApiRequestThreshold requestThreshold = ApiRequestThreshold.valueOf(Config.getProperty(Config.Constants.MAX_REQUESTS_THRESHOLD));
        return chatClientResponseBuilder.createErrorMessage("Could not add artist, user " + username + " has exceeded max artist requests for " + requestThreshold.getReadableName());
      }
      try (CloseableHttpResponse response = client.execute(post)) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Respone=" + response.toString());
          LOGGER.debug("Response content=" + IOUtils.toString(response.getEntity().getContent()));
          LOGGER.debug("Reason=" + response.getStatusLine().toString());
        }
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
          return chatClientResponseBuilder.createErrorMessage("Could not add artist, status-code=" + statusCode + ", reason=" + response.getStatusLine().getReasonPhrase());
        }
        LogManager.getLogger("AuditLog").info("User " + username + " added " + lidarrArtist.getArtistName());
        apiRequests.auditRequest(apiRequestType, username, lidarrArtist.getArtistName());
        return chatClientResponseBuilder.createSuccessMessage("Artist " + lidarrArtist.getArtistName() + " added, lidarr-detail=" + response.getStatusLine().getReasonPhrase());
      }
    } catch (IOException e) {
      LOGGER.error("Error trying to add artist", e);
      return chatClientResponseBuilder.createErrorMessage("Error adding artist, error=" + e.getMessage());
    }
  }

  private final ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  private static final LidarrCache LIDARR_CACHE = new LidarrCache();
  public static final String ADD_ARTIST_COMMAND_FIELD_PREFIX = "Add artist command";
}
