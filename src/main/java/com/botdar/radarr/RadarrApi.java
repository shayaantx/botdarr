package com.botdar.radarr;

import com.botdar.Api;
import com.botdar.Config;
import com.botdar.commands.CommandResponse;
import com.botdar.connections.ConnectionHelper;
import com.botdar.discord.EmbedHelper;
import com.google.gson.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

public class RadarrApi implements Api {
  public RadarrApi() {}

  @Override
  public String getApiUrl(String path) {
    return getApiUrl(Config.Constants.RADARR_URL, Config.Constants.RADARR_TOKEN, path);
  }

  @Override
  public List<MessageEmbed> lookup(String search, boolean findNew) {
    try {
      List<MessageEmbed> messageEmbeds = new ArrayList<>();
      List<RadarrMovie> movies = lookupMovies(search);
      for (RadarrMovie radarrMovie : movies) {
        RadarrMovie existingMovie = RADARR_CACHE.getExistingMovie(radarrMovie.getTmdbId());
        boolean isExistingMovie = existingMovie != null;
        boolean skip = findNew ? isExistingMovie : !isExistingMovie;
        if (skip) {
          continue;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(radarrMovie.getTitle());
        embedBuilder.addField("TmdbId", "" + radarrMovie.getTmdbId(), false);
        if (findNew) {
          embedBuilder.addField("Add movie command", "movie id add " + radarrMovie.getTitle() + " " + radarrMovie.getTmdbId(), false);
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
      LOGGER.error("Error trying to lookup movie", e);
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
            for (RadarrQueueStatusMessages statusMessage : radarrQueue.getStatusMessages()) {
              for (String message : statusMessage.getMessages()) {
                embedBuilder.addField("Download message", message, true);
              }
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
  public List<MessageEmbed> addWithTitle(String searchText) {
    try {
      List<RadarrMovie> movies = lookupMovies(searchText);
      if (movies.size() == 0) {
        return Arrays.asList(EmbedHelper.createErrorMessage("No movies found"));
      }

      if (movies.size() == 1) {
        RadarrMovie radarrMovie = movies.get(0);
        if (RADARR_CACHE.doesMovieOrShowExist(radarrMovie.getTitle())) {
          return Arrays.asList(EmbedHelper.createErrorMessage("Movie already exists"));
        }
        return Arrays.asList(addMovie(movies.get(0)));
      }
      List<MessageEmbed> restOfMovies = new ArrayList<>();
      restOfMovies.add(EmbedHelper.createInfoMessage("Too many movies found, please narrow search"));
      for (RadarrMovie radarrMovie : movies) {
        if (RADARR_CACHE.doesMovieOrShowExist(radarrMovie.getTitle())) {
          //skip existing movies
          continue;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(radarrMovie.getTitle());
        embedBuilder.addField("TmdbId", "" + radarrMovie.getTmdbId(), false);
        embedBuilder.addField("Add movie command", "movie id add " + radarrMovie.getTitle() + " " + radarrMovie.getTmdbId(), false);
        embedBuilder.setImage(radarrMovie.getRemotePoster());
        restOfMovies.add(embedBuilder.build());
      }
      return restOfMovies;
    } catch (Exception e) {
      return Arrays.asList(EmbedHelper.createErrorMessage("Error trying to add movie " + searchText + ", e=" + e.getMessage()));
    }
  }

  @Override
  public MessageEmbed addWithId(String searchText, String id) {
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
      LOGGER.error("Error trying to add movie", e);
      return EmbedHelper.createErrorMessage("Error adding content, e=" + e.getMessage());
    }
  }

  @Override
  public List<MessageEmbed> getProfiles() {
    Collection<RadarrProfile> profiles = RADARR_CACHE.getQualityProfiles();
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
  public List<MessageEmbed> forceDownload(String command) {
    String decodedKey = new String(Base64.getDecoder().decode(command.getBytes()));
    int lastColonCharacter = decodedKey.lastIndexOf(':');
    String[] decodedKeyArray =  {decodedKey.substring(0, lastColonCharacter), decodedKey.substring(lastColonCharacter + 1)};
    if (decodedKeyArray.length != 2) {
      return Arrays.asList(EmbedHelper.createErrorMessage("Invalid key=" + decodedKey));
    }

    String guid = decodedKeyArray[0];
    String title = decodedKeyArray[1];
    List<RadarrTorrent> radarrTorrents = lookupTorrents(title);

    if (radarrTorrents.isEmpty()) {
      return Arrays.asList(EmbedHelper.createErrorMessage("Found no movies to force download, title=" + title));
    }

    for (RadarrTorrent radarrTorrent : radarrTorrents) {
      if (radarrTorrent.getGuid().equalsIgnoreCase(guid)) {
        return ConnectionHelper.makePostRequest(this, "release", radarrTorrent, new ConnectionHelper.SimpleMessageEmbedResponseHandler() {
          @Override
          public List<MessageEmbed> onSuccess(String response) throws Exception {
            return Arrays.asList(EmbedHelper.createSuccessMessage("Forced the download"));
          }
        });
      }
    }
    return Arrays.asList(EmbedHelper.createErrorMessage("Could not force download movie for title=" + title + ", guid=" + guid));
  }

  @Override
  public List<MessageEmbed> lookupTorrents(String command, boolean showRejected) {
    List<RadarrTorrent> radarrTorrents = lookupTorrents(command);
    if (radarrTorrents.isEmpty()) {
      return Arrays.asList(EmbedHelper.createErrorMessage("No downloads available for " + command));
    }

    List<MessageEmbed> messageEmbeds = new ArrayList<>();
    for (RadarrTorrent radarrTorrent : radarrTorrents) {
      if (!showRejected && radarrTorrent.isRejected()) {
        //dont show rejected torrents
        continue;
      }
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.addField("Title", radarrTorrent.getTitle(), false);
      embedBuilder.addField("Torrent", radarrTorrent.getGuid(), false);
      embedBuilder.addField("Quality", radarrTorrent.getQuality().getQuality().getName(), true);
      embedBuilder.addField("Indexer", radarrTorrent.getIndexer(), true);
      embedBuilder.addField("Seeders", "" + radarrTorrent.getSeeders(), true);
      embedBuilder.addField("Leechers", "" + radarrTorrent.getLeechers(), true);
      String[] rejections = radarrTorrent.getRejections();
      if (rejections != null) {
        embedBuilder.addBlankField(false);
        for (String rejection : rejections) {
          embedBuilder.addField("Rejection Reason", rejection, false);
        }
      }
      String key = radarrTorrent.getGuid() + ":" + radarrTorrent.getMovieTitle();
      byte[] encodedBytes = Base64.getEncoder().encode(key.getBytes());
      embedBuilder.addField("Download hash command", "movie hash download " + new String(encodedBytes), true);
      messageEmbeds.add(embedBuilder.build());
    }

    if (messageEmbeds.isEmpty()) {
      messageEmbeds.add(EmbedHelper.createErrorMessage("No downloads available for " + command));
    }

    return messageEmbeds;
  }

  @Override
  public List<MessageEmbed> cancelDownload(long id) {
    return ConnectionHelper.makeDeleteRequest(this, "queue/" + id, "&blacklist=true", new ConnectionHelper.SimpleMessageEmbedResponseHandler() {
      @Override
      public List<MessageEmbed> onSuccess(String response) throws Exception {
        //TODO: implement
        return Arrays.asList(EmbedHelper.createErrorMessage("Not implemented yet"));
      }
    });
  }

  @Override
  public void sendPeriodicNotifications(JDA jda) {
    sendDownloadUpdates(jda);
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
          RADARR_CACHE.add(radarrMovie);
        }
        return null;
      }
    });

    List<RadarrProfile> radarrProfiles = getRadarrProfiles();
    for (RadarrProfile radarrProfile : radarrProfiles) {
      RADARR_CACHE.addProfile(radarrProfile);
    }
  }

  @Override
  public String getApiToken() {
    return Config.Constants.RADARR_TOKEN;
  }

  public List<MessageEmbed> discover() {
    return ConnectionHelper.makeGetRequest(this, "movies/discover/recommendations", new ConnectionHelper.SimpleEntityResponseHandler<MessageEmbed>() {
      @Override
      public List<MessageEmbed> onSuccess(String response) throws Exception {
        List<MessageEmbed> recommendedMovies = new ArrayList<>();
        if (response == null || response.isEmpty() || response.equalsIgnoreCase("[]")) {
          LOGGER.warn("Found no response when looking for movie recommendations");
          return Collections.emptyList();
        }
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();

        for (int i = 0; i < json.size(); i++) {
          if (i == 20) {
            //don't show more than 20
            break;
          }
          RadarrMovie radarrMovie = new Gson().fromJson(json.get(i), RadarrMovie.class);
          EmbedBuilder embedBuilder = new EmbedBuilder();
          embedBuilder.setTitle(radarrMovie.getTitle());
          embedBuilder.addField("TmdbId", "" + radarrMovie.getTmdbId(), false);
          embedBuilder.addField("Add movie command", "movie id add " + radarrMovie.getTitle() + " " + radarrMovie.getTmdbId(), false);
          embedBuilder.setImage(radarrMovie.getRemotePoster());
          recommendedMovies.add(embedBuilder.build());
        }
        return recommendedMovies;
      }
    });
  }

  private MessageEmbed addMovie(RadarrMovie radarrMovie) {
    //make sure we specify where the movie should get downloaded
    radarrMovie.setPath(Config.getProperty(Config.Constants.RADARR_PATH) + "/" + radarrMovie.getTitle() + "(" + radarrMovie.getYear() + ")");
    //make sure the movie is monitored
    radarrMovie.setMonitored(true);

    String radarrProfileName = Config.getProperty(Config.Constants.RADARR_DEFAULT_PROFILE);
    RadarrProfile radarrProfile = RADARR_CACHE.getProfile(radarrProfileName.toLowerCase());
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
      LOGGER.error("Error trying to add movie", e);
      return EmbedHelper.createErrorMessage("Error adding movie, error=" + e.getMessage());
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
  private static final RadarrCache RADARR_CACHE = new RadarrCache();
  private static final Logger LOGGER = LogManager.getLogger();
}
