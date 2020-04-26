package com.botdarr.api.lidarr;

import com.botdarr.Config;
import com.botdarr.api.Api;
import com.botdarr.api.ApiRequests;
import com.botdarr.clients.ChatClient;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.connections.ConnectionHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
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
    if (MAX_DOWNLOADS_TO_SHOW <= 0) {
      return Collections.emptyList();
    }
    //TODO: implement
    /*
    List<ChatClientResponse> chatClientResponses = getMusicDownloads();
    if (chatClientResponses.isEmpty()) {
      chatClientResponses.add(chatClientResponseBuilder.createInfoMessage("No music downloading"));
    }
    return chatClientResponses;
    */
    return null;
  }

  @Override
  public void sendPeriodicNotifications(ChatClient chatClient) {
    if (MAX_DOWNLOADS_TO_SHOW <= 0) {
      LOGGER.debug("Bot configured to show no downloads");
      return;
    }
    //TODO: implement
    /*List<ChatClientResponse> downloads = getMusicDownloads();
    if (downloads != null && !downloads.isEmpty()) {
      chatClient.sendToConfiguredChannels(downloads);
    } else {
      LOGGER.debug("No music downloads available for sending");
    }*/
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
    try {
      List<ChatClientResponse> responses = new ArrayList<>();
      List<LidarrArtist> artists = lookupArtists(search);
      for (LidarrArtist lookupArtist : artists) {
        LidarrArtist existingArtist = LIDARR_CACHE.getExistingArtist(lookupArtist);
        boolean isExistingArtist = existingArtist != null;
        boolean skip = findNew ? isExistingArtist : !isExistingArtist;
        if (skip) {
          continue;
        }
        responses.add(chatClientResponseBuilder.getNewOrExistingArtist(lookupArtist, existingArtist, findNew));
      }
      if (responses.size() > MAX_RESULTS_TO_SHOW) {
        responses = responses.subList(0, MAX_RESULTS_TO_SHOW - 1);
        responses.add(0, chatClientResponseBuilder.createInfoMessage("Too many artists found, please narrow search"));
      }
      if (responses.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Could not find any " + (findNew ? "new" : "existing") + " artists for search term=" + search));
      }
      return responses;

    } catch (Exception e) {
      LOGGER.error("Error trying to lookup artists", e);
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Error looking up content, e=" + e.getMessage()));
    }
  }

  /*
  public ChatClientResponse addArtistWithId() {
    //http://192.168.1.196:8686/api/v1/artist
    //post

  }*/

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
