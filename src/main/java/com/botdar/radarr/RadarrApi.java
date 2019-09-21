package com.botdar.radarr;

import com.botdar.Api;
import com.botdar.Config;
import com.botdar.commands.CommandResponse;
import com.botdar.connections.ConnectionHelper;
import com.botdar.discord.EmbedHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RadarrApi implements Api {
  private RadarrApi() {}

  public static RadarrApi get() {
    if (instance == null) {
      synchronized (RadarrApi .class) {
        if (instance == null) {
          instance = new RadarrApi();
        }
      }
    }
    return instance;
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
        RadarrMovie existingMovie = existingTmdbIdsToMovies.get(radarrMovie.getTmdbId());
        boolean isExistingMovie = existingMovie != null;
        boolean skip = findNew ? isExistingMovie : !isExistingMovie;
        if (skip) {
          continue;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(radarrMovie.getTitle());
        embedBuilder.addField("TmdbId", "" + radarrMovie.getTmdbId(), false);
        if (findNew) {
          embedBuilder.addField("Add movie command", "movie add " + radarrMovie.getTitle() + " " + radarrMovie.getTmdbId(), false);
        } else {
          embedBuilder.addField("Id", "" + existingMovie.getId(), false);
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
    return ConnectionHelper.makeGetRequest(this, "queue", new ConnectionHelper.SimpleMessageEmbedResponseHandler() {
      @Override
      public List<MessageEmbed> onSuccess(String response) throws Exception {
        List<MessageEmbed> messageEmbeds = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          RadarrQueue radarrQueue = new Gson().fromJson(json.get(i), RadarrQueue.class);
          EmbedBuilder embedBuilder = new EmbedBuilder();
          embedBuilder.setTitle(radarrQueue.getRadarrQueueMovie().getTitle());
          embedBuilder.addField("Quality", radarrQueue.getQuality().getQuality().getName(), true);
          embedBuilder.addField("Status", radarrQueue.getStatus(), true);
          embedBuilder.addField("Time Left", radarrQueue.getTimeleft(), true);
          if (radarrQueue.getStatusMessages() != null) {
            for (String message : radarrQueue.getStatusMessages()) {
              embedBuilder.addField("Download message", message, true);
            }
          }
          embedBuilder.addField("Cancel download command", "movie cancel download " + radarrQueue.getId(), true);
          messageEmbeds.add(embedBuilder.build());
        }
        if (messageEmbeds == null || messageEmbeds.size() == 0) {
          return Arrays.asList(EmbedHelper.createInfoMessage("No downloads currently"));
        }
        return messageEmbeds;
      }
    });
  }

  @Override
  public List<MessageEmbed> addTitle(String searchText) {
    try {
      List<RadarrMovie> movies = lookupMovies(searchText);
      if (movies.size() == 0) {
        return Arrays.asList(EmbedHelper.createErrorMessage("No movies found"));
      }

      if (movies.size() == 1) {
        RadarrMovie radarrMovie = movies.get(0);
        if (existingMovieTitlesToIds.containsKey(radarrMovie.getTitle().toLowerCase())) {
          return Arrays.asList(EmbedHelper.createErrorMessage("Movie already exists"));
        }
        return Arrays.asList(addMovie(movies.get(0)));
      }
      List<MessageEmbed> restOfMovies = new ArrayList<>();
      restOfMovies.add(EmbedHelper.createInfoMessage("Too many movies found, please narrow search"));
      for (RadarrMovie radarrMovie : movies) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(radarrMovie.getTitle());
        embedBuilder.addField("TmdbId", "" + radarrMovie.getTmdbId(), false);
        embedBuilder.addField("Add movie command", "movie add " + radarrMovie.getTitle() + " " + radarrMovie.getTmdbId(), false);
        embedBuilder.setImage(radarrMovie.getRemotePoster());
        restOfMovies.add(embedBuilder.build());
      }
      return restOfMovies;
    } catch (Exception e) {
      return Arrays.asList(EmbedHelper.createErrorMessage("Error trying to add movie " + searchText + ", e=" + e.getMessage()));
    }
  }

  @Override
  public MessageEmbed add(String searchText, String id) {
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
    Collection<RadarrProfile> profiles = existingProfiles.values();
    if (profiles == null || profiles.isEmpty()) {
      return Arrays.asList(EmbedHelper.createErrorMessage("Found 0 profiles, please setup Radarr with at least one profile"));
    }

    List<MessageEmbed> profileMessages = new ArrayList<>();
    for (RadarrProfile radarrProfile : profiles) {
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

  @Override
  public List<MessageEmbed> lookupTorrents(String command, boolean showRejected) {
    Long id = existingMovieTitlesToIds.get(command.toLowerCase());
    if (id == null) {
      return Arrays.asList(EmbedHelper.createErrorMessage("Could not find id for " + command));
    }
    return ConnectionHelper.makeGetRequest(this, "release", "&movieId=" + id + "&sort_by=releaseWeight&order=asc", new ConnectionHelper.SimpleMessageEmbedResponseHandler() {
      @Override
      public List<MessageEmbed> onSuccess(String response) throws Exception {
        if (response == null || response.isEmpty() || response.equalsIgnoreCase("[]")) {
          return Arrays.asList(EmbedHelper.createErrorMessage("No downloads available for " + command));
        }
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();

        List<MessageEmbed> messageEmbeds = new ArrayList<>();
        for (int i = 0; i < json.size(); i++) {
          RadarrTorrent radarrTorrent = new Gson().fromJson(json.get(i), RadarrTorrent.class);
          if (!showRejected && radarrTorrent.isRejected()) {
            //dont show rejected torrents
            continue;
          }
          EmbedBuilder embedBuilder = new EmbedBuilder();
          embedBuilder.addBlankField(false);
          embedBuilder.addField("Torrent", radarrTorrent.getGuid(), false);
          embedBuilder.addField("Quality", radarrTorrent.getQuality().getQuality().getName(), true);
          embedBuilder.addField("Indexer", radarrTorrent.getIndexer(), true);
          embedBuilder.addField("Seeders", "" + radarrTorrent.getSeeders(), true);
          embedBuilder.addField("Leechers", "" + radarrTorrent.getLeechers(), true);
          messageEmbeds.add(embedBuilder.build());
        }
        if (messageEmbeds.isEmpty()) {
          messageEmbeds.add(EmbedHelper.createErrorMessage("No downloads available for " + command));
        }
        return messageEmbeds;
      }
    });
  }

  @Override
  public List<MessageEmbed> cancelDownload(long id) {
    return ConnectionHelper.makeDeleteRequest(this, "queue/" + id, "&blacklist=true", new ConnectionHelper.SimpleMessageEmbedResponseHandler() {
      @Override
      public List<MessageEmbed> onSuccess(String response) throws Exception {
        //TODO: implement
        return null;
      }
    });
  }

  @Override
  public void sendNotifications(JDA jda) {
    for (TextChannel textChannel : jda.getTextChannels()) {
      List<MessageEmbed> downloads = downloads();
      if (downloads == null || downloads.size() == 0) {
        new CommandResponse(EmbedHelper.createInfoMessage("No downloads running currently")).send(textChannel);
      } else {
        new CommandResponse(downloads).send(textChannel);
      }
    }
  }

  @Override
  public void cacheData(JDA jda) {
    ConnectionHelper.makeGetRequest(this, "/movie", new ConnectionHelper.SimpleEntityResponseHandler<RadarrMovie>() {
      @Override
      public List<RadarrMovie> onSuccess(String response) throws Exception {
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          RadarrMovie radarrMovie = new Gson().fromJson(json.get(i), RadarrMovie.class);
          existingTmdbIdsToMovies.put(radarrMovie.getTmdbId(), radarrMovie);
          existingMovieTitlesToIds.put(radarrMovie.getTitle().toLowerCase(), radarrMovie.getId());
        }
        return null;
      }
    });

    List<RadarrProfile> radarrProfiles = getRadarrProfiles();
    for (RadarrProfile radarrProfile : radarrProfiles) {
      existingProfiles.put(radarrProfile.getName().toLowerCase(), radarrProfile);
    }
  }

  @Override
  public String getApiToken() {
    return Config.Constants.RADARR_TOKEN;
  }

  private MessageEmbed addMovie(RadarrMovie radarrMovie) {
    //make sure we specify where the movie should get downloaded
    radarrMovie.setPath(Config.getProperty(Config.Constants.RADARR_PATH) + "/" + radarrMovie.getTitle() + "(" + radarrMovie.getYear() + ")");
    //make sure the movie is monitored
    radarrMovie.setMonitored(true);

    String radarrProfileName = Config.getProperty(Config.Constants.RADARR_DEFAULT_PROFILE);
    RadarrProfile radarrProfile = existingProfiles.get(radarrProfileName.toLowerCase());
    if (radarrProfile == null) {
      return EmbedHelper.createErrorMessage("Could not find radarr profile for default " + radarrProfileName);
    }
    radarrMovie.setQualityProfileId(radarrProfile.getId());

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

  private Map<String, RadarrProfile> existingProfiles = new ConcurrentHashMap<>();
  private Map<String, Long> existingMovieTitlesToIds = new ConcurrentHashMap<>();
  private Map<Long, RadarrMovie> existingTmdbIdsToMovies = new ConcurrentHashMap<>();
  private static volatile RadarrApi instance;
}
