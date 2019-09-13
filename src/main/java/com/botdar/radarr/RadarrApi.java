package com.botdar.radarr;

import com.botdar.Api;
import com.botdar.Config;
import com.botdar.connections.ConnectionHelper;
import com.botdar.discord.EmbedHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RadarrApi implements Api {
  public RadarrApi() {
    cacheRadarrInfo();
    Executors.newScheduledThreadPool(1).schedule(new Runnable() {
      @Override
      public void run() {
        cacheRadarrInfo();
      }
    }, 1, TimeUnit.MINUTES);
  }

  @Override
  public String getApiUrl(String path) {
    return Config.getProperty(Config.Constants.RADARR_URL) + "/api/" + path + "?apikey=" + Config.getProperty(Config.Constants.RADARR_TOKEN);
  }

  @Override
  public List<MessageEmbed> lookup(String search, boolean findNew) {
    try {
      List<MessageEmbed> messageEmbeds = new ArrayList<>();
      List<RadarrMovie> movies = lookupMovies(search);
      for (RadarrMovie radarrMovie : movies) {
        RadarrMovie existingMovie = existingMoviesTmdbIds.get(radarrMovie.getTmdbId());
        boolean isExistingMovie = existingMovie != null;
        boolean skip = findNew ? isExistingMovie : !isExistingMovie;
        if (skip) {
          continue;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(radarrMovie.getTitle());
        embedBuilder.addField("TmdbId", "" + radarrMovie.getTmdbId(), true);
        embedBuilder.addBlankField(false);
        if (findNew) {
          embedBuilder.addField("Add movie command", "movie add " + radarrMovie.getTitle() + " " + radarrMovie.getTmdbId(), false);
        } else {
          embedBuilder.addField("Downloaded", existingMovie.isDownloaded() + "", false);
          embedBuilder.addField("Has File", existingMovie.isHasFile() + "", false);
        }
        embedBuilder.setImage(radarrMovie.getRemotePoster());
        messageEmbeds.add(embedBuilder.build());
      }
      if (messageEmbeds.size() == 0) {
        return Arrays.asList(EmbedHelper.createErrorMessage("Could not find any " + (findNew ? "new" : "existing") + " movies for search term=" + search));
      }
      return messageEmbeds;
    } catch (Exception e) {
      //TODO: log to logger
      return Arrays.asList(EmbedHelper.createErrorMessage("Error looking up content, e=" + e.getMessage()));
    }
  }

  @Override
  public List<MessageEmbed> downloads() {
    return ConnectionHelper.makeGetRequest(this, "queue", new ConnectionHelper.SimpleEntityResponseHandler<MessageEmbed>() {
      @Override
      public List<MessageEmbed> onSuccess(String response) throws Exception {
        List<MessageEmbed> messageEmbeds = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          JsonObject jsonObject = json.get(i).getAsJsonObject();
          EmbedBuilder embedBuilder = new EmbedBuilder();
          embedBuilder.setTitle(jsonObject.get("title").getAsString());
          embedBuilder.addField("Quality", jsonObject.get("quality").getAsJsonObject().get("quality").getAsJsonObject().get("name").getAsString(), true);
          embedBuilder.addField("Status", jsonObject.get("status").getAsString(), true);
          embedBuilder.addField("Time Left", jsonObject.get("timeleft").getAsString(), true);
          messageEmbeds.add(embedBuilder.build());
        }
        return messageEmbeds;
      }
    });
  }

  @Override
  public MessageEmbed add2(String searchText, String id) {
    try {
      List<RadarrMovie> movies = lookupMovies(searchText);
      if (movies.size() == 0) {
        return EmbedHelper.createErrorMessage("No movies found");
      }
      for (RadarrMovie radarrMovie : movies) {
        if (radarrMovie.getTmdbId() == Integer.valueOf(id)) {
          return addMovie(radarrMovie);
        }
      }
      return EmbedHelper.createErrorMessage("Could not find movie with search text=" + searchText + " and id=" + id);
    } catch (Exception e) {
      //TODO: log to logger
      return EmbedHelper.createErrorMessage("Error adding content, e=" + e.getMessage());
    }
  }

  @Override
  public List<MessageEmbed> getProfiles() {
    return ConnectionHelper.makeGetRequest(this, "profile", new ConnectionHelper.SimpleMessageEmbedResponseHandler() {
      @Override
      public List<MessageEmbed> onSuccess(String response) {
        List<MessageEmbed> profileMessages = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          RadarrProfile radarrProfile = new Gson().fromJson(json.get(i), RadarrProfile.class);
          EmbedBuilder embedBuilder = new EmbedBuilder();
          embedBuilder.setTitle("Profile");
          embedBuilder.addField("Name", radarrProfile.getName(), false);
          embedBuilder.addField("Cutoff", radarrProfile.getCutoff().getName(), false);
          embedBuilder.addBlankField(false);
          for (int k = 0; k < radarrProfile.getItems().size(); k++) {
            RadarrProfileQualityItem radarrProfileQualityItem = radarrProfile.getItems().get(k);
            if (radarrProfileQualityItem.isAllowed()) {
              embedBuilder.addField(
                "Quality",
                "name=" + radarrProfileQualityItem.getQuality().getName() + ", resolution=" + radarrProfileQualityItem.getQuality().getResolution(),
                true);
            }
          }
          profileMessages.add(embedBuilder.build());
        }
        return profileMessages;
      }
    });
  }

  private MessageEmbed addMovie(RadarrMovie radarrMovie) {
    //make sure we specify where the movie should get downloaded
    radarrMovie.setPath(Config.getProperty(Config.Constants.RADARR_PATH) + "/" + radarrMovie.getTitle() + "(" + radarrMovie.getYear() + ")");
    //make sure the movie is monitored
    radarrMovie.setMonitored(true);

    //TODO: 1 = any on my radarr, but it could be any id, need a property for this
    radarrMovie.setQualityProfileId(1);

    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(getApiUrl("movie"));

      post.addHeader("content-type", "application/x-www-form-urlencoded");
      post.setEntity(new StringEntity(new Gson().toJson(radarrMovie, RadarrMovie.class)));

      try (CloseableHttpResponse response = client.execute(post)) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
          return EmbedHelper.createErrorMessage("Could not add movie, status-code=" + statusCode + ", reason=" + response.getStatusLine().getReasonPhrase());
        }
        return EmbedHelper.createSuccessMessage("Movie added, radarr-detail=" + response.getStatusLine().getReasonPhrase());
      }
    } catch (IOException e) {
      //TODO: print to something other than console log
      e.printStackTrace();
      return EmbedHelper.createErrorMessage("Error adding movie, error=" + e.getMessage());
    }
  }

  private List<RadarrMovie> lookupMovies(String search) throws Exception {
    return ConnectionHelper.makeGetRequest(this, "movie/lookup", "&term=" + URLEncoder.encode(search, "UTF-8"), new ConnectionHelper.SimpleEntityResponseHandler<RadarrMovie>() {
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

  private Map<Long, RadarrMovie> getExistingMoviesTmdbIds() {
    Map<Long, RadarrMovie> existingMovies = new HashMap<>();
    ConnectionHelper.makeGetRequest(this, "/movie", new ConnectionHelper.SimpleEntityResponseHandler<RadarrMovie>() {
      @Override
      public List<RadarrMovie> onSuccess(String response) throws Exception {
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          RadarrMovie radarrMovie = new Gson().fromJson(json.get(i), RadarrMovie.class);
          existingMovies.put(radarrMovie.getTmdbId(), radarrMovie);
        }
        return null;
      }
    });
    return existingMovies;
  }

  private void cacheRadarrInfo() {
    existingMoviesTmdbIds.putAll(getExistingMoviesTmdbIds());
    //TODO: cache profiles into set
  }

  private Map<Long, RadarrMovie> existingMoviesTmdbIds = new ConcurrentHashMap<>();
}
