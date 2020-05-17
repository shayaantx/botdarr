package com.botdarr.api.lidarr;

import com.botdarr.Config;
import com.botdarr.api.*;
import com.botdarr.clients.ChatClient;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.connections.ConnectionHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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
    if (MAX_DOWNLOADS_TO_SHOW <= 0) {
      LOGGER.debug("Bot configured to show no downloads");
      return;
    }
    List<ChatClientResponse> downloads = getDownloadsStrategy().getContentDownloads();
    if (downloads != null && !downloads.isEmpty()) {
      chatClient.sendToConfiguredChannels(downloads);
    } else {
      LOGGER.debug("No music downloads available for sending");
    }
  }

  @Override
  public void cacheData() {
    LIDARR_CACHE.reset();

    ConnectionHelper.makeGetRequest(this, LidarrUrls.ALL_ARTISTS, new ConnectionHelper.SimpleEntityResponseHandler<LidarrArtist>() {
      @Override
      public List<LidarrArtist> onSuccess(String response) throws Exception {
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          LidarrArtist artist = new Gson().fromJson(json.get(i), LidarrArtist.class);
          LIDARR_CACHE.addArtist(artist);
        }
        return null;
      }
    });

    //TODO: add profiles
    LOGGER.debug("Finished caching lidarr data");
  }

  @Override
  public String getApiToken() {
    return null;
  }

  public ChatClientResponse addArtist(String artist) {
    //TODO: lookup existing artist
    return null;
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

  private DownloadsStrategy getDownloadsStrategy() {
    return new DownloadsStrategy(this, LidarrUrls.DOWNLOAD_BASE, chatClientResponseBuilder, ContentType.ARTIST) {
      @Override
      public ChatClientResponse getResponse(JsonElement rawElement) {
        LidarrArtist lidarrArtist = new Gson().fromJson(rawElement, LidarrArtist.class);
        return chatClientResponseBuilder.getArtistDownloadResponses(lidarrArtist);
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

  private final ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  private static final LidarrCache LIDARR_CACHE = new LidarrCache();
  private final int MAX_DOWNLOADS_TO_SHOW = new ApiRequests().getMaxDownloadsToShow();
  private final int MAX_RESULTS_TO_SHOW = new ApiRequests().getMaxResultsToShow();
  public static final String ADD_ARTIST_COMMAND_FIELD_PREFIX = "Add artist command";
}
