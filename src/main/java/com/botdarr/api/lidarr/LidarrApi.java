package com.botdarr.api.lidarr;

import com.botdarr.Config;
import com.botdarr.api.*;
import com.botdarr.api.sonarr.SonarrUrls;
import com.botdarr.commands.CommandContext;
import com.botdarr.commands.responses.*;
import com.botdarr.connections.ConnectionHelper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LidarrApi implements Api {
  @Override
  public String getUrlBase() {
    return Config.getProperty(Config.Constants.LIDARR_URL_BASE);
  }

  @Override
  public String getApiUrl(String path) {
    return getApiUrl(Config.Constants.LIDARR_URL, Config.Constants.LIDARR_TOKEN, path);
  }

  @Override
  public List<CommandResponse> downloads() {
    return getDownloadsStrategy().downloads();
  }

  @Override
  public void cacheData() {
    new CacheProfileStrategy<LidarrQualityProfile, String>() {

      @Override
      public void deleteFromCache(List<String> profilesAddUpdated) {
        LIDARR_CACHE.removeDeletedQualityProfiles(profilesAddUpdated);
      }

      @Override
      public List<LidarrQualityProfile> getProfiles() {
        return ConnectionHelper.makeGetRequest(LidarrApi.this, LidarrUrls.PROFILE, new ConnectionHelper.SimpleEntityResponseHandler<List<LidarrQualityProfile>>() {
          @Override
          public List<LidarrQualityProfile> onSuccess(String response) {
            List<LidarrQualityProfile> lidarrQualityProfiles = new ArrayList<>();
            JsonParser parser = new JsonParser();
            JsonArray json = parser.parse(response).getAsJsonArray();
            for (int i = 0; i < json.size(); i++) {
              LidarrQualityProfile lidarrQualityProfile = new Gson().fromJson(json.get(i), LidarrQualityProfile.class);
              lidarrQualityProfiles.add(lidarrQualityProfile);
            }
            return lidarrQualityProfiles;
          }
        });
      }

      @Override
      public void addProfile(LidarrQualityProfile profile) {
        LIDARR_CACHE.addQualityProfile(profile);
      }
    }.cacheData();

    new CacheProfileStrategy<LidarrMetadataProfile, String>() {

      @Override
      public void deleteFromCache(List<String> profilesAddUpdated) {
        LIDARR_CACHE.removeDeletedMetadataProfiles(profilesAddUpdated);
      }

      @Override
      public List<LidarrMetadataProfile> getProfiles() {
        return ConnectionHelper.makeGetRequest(LidarrApi.this, LidarrUrls.METADATA_PROFILE, new ConnectionHelper.SimpleEntityResponseHandler<List<LidarrMetadataProfile>>() {
          @Override
          public List<LidarrMetadataProfile> onSuccess(String response) {
            List<LidarrMetadataProfile> lidarrMetadataProfiles = new ArrayList<>();
            JsonParser parser = new JsonParser();
            JsonArray json = parser.parse(response).getAsJsonArray();
            for (int i = 0; i < json.size(); i++) {
              LidarrMetadataProfile lidarrMetadataProfile = new Gson().fromJson(json.get(i), LidarrMetadataProfile.class);
              lidarrMetadataProfiles.add(lidarrMetadataProfile);
            }
            return lidarrMetadataProfiles;
          }
        });
      }

      @Override
      public void addProfile(LidarrMetadataProfile profile) {
        LIDARR_CACHE.addMetadataProfile(profile);
      }
    }.cacheData();

    new CacheContentStrategy<LidarrArtist, String>(this, LidarrUrls.ALL_ARTISTS) {

      @Override
      public void deleteFromCache(List<String> itemsToRetain) {
        LIDARR_CACHE.removeDeletedArtists(itemsToRetain);
      }

      @Override
      public String addToCache(JsonElement cacheItem) {
        LidarrArtist lidarrArtist = new Gson().fromJson(cacheItem, LidarrArtist.class);
        LIDARR_CACHE.addArtist(new Gson().fromJson(cacheItem, LidarrArtist.class));
        return lidarrArtist.getKey();
      }
    }.cacheData();

    //TODO: add album cache
  }

  @Override
  public String getApiToken() {
    return Config.Constants.LIDARR_TOKEN;
  }

  public CommandResponse addArtistWithId(String id, String artistName) {
    return getArtistAddStrategy().addWithSearchId(artistName, id);
  }

  public List<CommandResponse> addArtist(String artistToSearch) {
    return getArtistAddStrategy().addWithSearchTitle(artistToSearch);
  }

  public List<CommandResponse> lookupArtists(String search, boolean findNew) {
    return new LookupStrategy<LidarrArtist>(ContentType.ARTIST) {

      @Override
      public LidarrArtist lookupExistingItem(LidarrArtist lookupItem) {
        return LIDARR_CACHE.getExistingArtist(lookupItem);
      }

      @Override
      public List<LidarrArtist> lookup(String searchTerm) throws Exception {
        return lookupArtists(searchTerm);
      }

      @Override
      public CommandResponse getExistingItem(LidarrArtist existingItem) {
        return new ExistingMusicArtistResponse(existingItem);
      }

      @Override
      public CommandResponse getNewItem(LidarrArtist lookupItem) {
        return new NewMusicArtistResponse(lookupItem);
      }

      @Override
      public boolean isPathBlacklisted(LidarrArtist item) {
        for (String path : Config.getExistingItemBlacklistPaths()) {
          if (item.getPath() != null && item.getPath().startsWith(path)) {
            return true;
          }
        }
        return false;
      }
    }.lookup(search, findNew);
  }

  private AddStrategy<LidarrArtist> getArtistAddStrategy() {
    return new AddStrategy<LidarrArtist>(ContentType.ARTIST) {
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
      public CommandResponse addContent(LidarrArtist content) {
        return addArtist(content);
      }

      @Override
      public CommandResponse getResponse(LidarrArtist item) {
        return new MusicArtistResponse(item);
      }
    };
  }

  private DownloadsStrategy getDownloadsStrategy() {
    return new DownloadsStrategy(this, LidarrUrls.DOWNLOAD_BASE) {
      @Override
      public CommandResponse getResponse(JsonElement rawElement) {
        LidarrQueueRecord lidarrQueueRecord = new Gson().fromJson(rawElement, LidarrQueueRecord.class);
        return new MusicArtistDownloadResponse(lidarrQueueRecord);
      }
      @Override
      public List<CommandResponse> getContentDownloads() {
        return ConnectionHelper.makeGetRequest(LidarrApi.this, LidarrUrls.DOWNLOAD_BASE, new ConnectionHelper.SimpleMessageEmbedResponseHandler() {
          @Override
          public List<CommandResponse> onSuccess(String response) {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(response).getAsJsonObject();
            return parseContent(json.get("records").toString());
          }
        });
      }
    };
  }

  private List<LidarrArtist> lookupArtists(String search) throws Exception {
    return ConnectionHelper.makeGetRequest(this, LidarrUrls.LOOKUP_ARTISTS, "&term=" + URLEncoder.encode(search, "UTF-8"),
      new ConnectionHelper.SimpleEntityResponseHandler<List<LidarrArtist>>() {
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

  private CommandResponse addArtist(LidarrArtist lidarrArtist) {
    lidarrArtist.setMonitored(true);
    lidarrArtist.setRootFolderPath(Config.getProperty(Config.Constants.LIDARR_PATH) + "/");

    String lidarrProfileName = Config.getProperty(Config.Constants.LIDARR_DEFAULT_QUALITY_PROFILE);
    LidarrQualityProfile lidarrQualityProfile = LIDARR_CACHE.getQualityProfile(lidarrProfileName.toLowerCase());
    if (lidarrQualityProfile == null) {
      return new ErrorResponse("Could not find lidarr profile for default " + lidarrProfileName);
    }

    String lidarrMetadataProfileName = Config.getProperty(Config.Constants.LIDARR_DEFAULT_METADATA_PROFILE);
    LidarrMetadataProfile lidarrMetadataProfile = LIDARR_CACHE.getMetadataProfile(lidarrMetadataProfileName.toLowerCase());
    if (lidarrMetadataProfile == null) {
      return new ErrorResponse("Could not find lidarr metadata profile for default " + lidarrProfileName);
    }
    lidarrArtist.setMetadataProfileId(lidarrMetadataProfile.getId());
    lidarrArtist.setQualityProfileId(lidarrQualityProfile.getId());
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(getApiUrl(SonarrUrls.ARTIST_BASE));
      post.addHeader("content-type", "application/json");

      ObjectMapper mapper = new ObjectMapper();
      //lidarr for some reason doesn't support raw unicode characters in json parsing (since they should be allowed), so we escape them here
      mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
      String json = mapper.writeValueAsString(lidarrArtist);
      post.setEntity(new StringEntity(json, Charset.forName("UTF-8")));

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Client request=" + post);
        LOGGER.debug("Client data=" + (json));
      }

      String username = CommandContext.getConfig().getUsername();

      ApiRequests apiRequests = new ApiRequests();
      ApiRequestType apiRequestType = ApiRequestType.ARTIST;
      if (apiRequests.checkRequestLimits(apiRequestType) && !apiRequests.canMakeRequests(apiRequestType, username)) {
        ApiRequestThreshold requestThreshold = ApiRequestThreshold.valueOf(Config.getProperty(Config.Constants.MAX_REQUESTS_THRESHOLD));
        return new ErrorResponse("Could not add artist, user " + username + " has exceeded max artist requests for " + requestThreshold.getReadableName());
      }
      try (CloseableHttpResponse response = client.execute(post)) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Respone=" + response.toString());
          LOGGER.debug("Response content=" + IOUtils.toString(response.getEntity().getContent()));
          LOGGER.debug("Reason=" + response.getStatusLine().toString());
        }
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
          return new ErrorResponse("Could not add artist, status-code=" + statusCode + ", reason=" + response.getStatusLine().getReasonPhrase());
        }
        //cache artist after successful response
        LIDARR_CACHE.addArtist(lidarrArtist);
        LogManager.getLogger("AuditLog").info("User " + username + " added " + lidarrArtist.getArtistName());
        apiRequests.auditRequest(apiRequestType, username, lidarrArtist.getArtistName());
        return new SuccessResponse("Artist " + lidarrArtist.getArtistName() + " added, lidarr-detail=" + response.getStatusLine().getReasonPhrase());
      }
    } catch (IOException e) {
      LOGGER.error("Error trying to add artist", e);
      return new ErrorResponse("Error adding artist, error=" + e.getMessage());
    }
  }

  private static final LidarrCache LIDARR_CACHE = new LidarrCache();
  public static final String ADD_ARTIST_COMMAND_FIELD_PREFIX = "Add artist command";
}
