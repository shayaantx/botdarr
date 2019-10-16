package com.botdar.sonarr;

import com.botdar.Api;
import com.botdar.Config;
import com.botdar.connections.ConnectionHelper;
import com.botdar.discord.EmbedHelper;
import com.botdar.radarr.RadarrMovie;
import com.google.gson.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SonarrApi implements Api {
  @Override
  public String getApiUrl(String path) {
    return getApiUrl(Config.Constants.SONARR_URL, Config.Constants.SONARR_TOKEN, path);
  }

  @Override
  public MessageEmbed addWithId(String searchText, String id) {
    try {
      List<SonarrShow> shows = lookupShows(searchText);
      if (shows.size() == 0) {
        return EmbedHelper.createErrorMessage("No shows found");
      }
      for (SonarrShow sonarrShow : shows) {
        if (sonarrShow.getTvdbId() == Integer.valueOf(id)) {
          return addShow(sonarrShow);
        }
      }
      return EmbedHelper.createErrorMessage("Could not find show with search text=" + searchText + " and id=" + id);
    } catch (Exception e) {
      LOGGER.error("Error trying to add show=" + searchText, e);
      return EmbedHelper.createErrorMessage("Error adding content, e=" + e.getMessage());
    }
  }

  @Override
  public List<MessageEmbed> addWithTitle(String searchText) {
    try {
      List<SonarrShow> shows = lookupShows(searchText);
      if (shows.size() == 0) {
        return Arrays.asList(EmbedHelper.createErrorMessage("No shows found"));
      }
      if (shows.size() == 1) {
        SonarrShow sonarrShow = shows.get(0);
        if (SONARR_CACHE.doesShowExist(sonarrShow.getTitle())) {
          return Arrays.asList(EmbedHelper.createErrorMessage("Show already exists"));
        }
        return Arrays.asList(addShow(shows.get(0)));
      }
      List<MessageEmbed> restOfShows = new ArrayList<>();
      restOfShows.add(EmbedHelper.createInfoMessage("Too many shows found, please narrow search"));
      for (SonarrShow sonarrShow : shows) {
        if (SONARR_CACHE.doesShowExist(sonarrShow.getTitle())) {
          //skip existing movies
          continue;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(sonarrShow.getTitle());
        embedBuilder.addField("TvdbId", "" + sonarrShow.getTvdbId(), false);
        embedBuilder.addField("Add show command", "show id add " + sonarrShow.getTitle() + " " + sonarrShow.getTvdbId(), false);
        embedBuilder.setImage(sonarrShow.getRemotePoster());
        restOfShows.add(embedBuilder.build());
      }
      return restOfShows;
    } catch (Exception e) {
      LOGGER.error("Error found trying to add show=" + searchText, e);
      return Arrays.asList(EmbedHelper.createErrorMessage("Error trying to add show " + searchText + ", e=" + e.getMessage()));
    }
  }

  @Override
  public List<MessageEmbed> lookup(String search, boolean findNew) {
    try {
      List<MessageEmbed> messageEmbeds = new ArrayList<>();
      List<SonarrShow> shows = lookupShows(search);
      for (SonarrShow sonarrShow : shows) {
        //TODO: should we try to lookup shows with rage/maze id's as well?
        SonarrShow existingShow = SONARR_CACHE.getExistingShowFromTvdbId(sonarrShow.getTvdbId());
        boolean isExistingMovie = existingShow != null;
        boolean skip = findNew ? isExistingMovie : !isExistingMovie;
        if (skip) {
          continue;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(sonarrShow.getTitle());
        embedBuilder.addField("TmdbId", "" + sonarrShow.getTvdbId(), true);
        if (findNew) {
          embedBuilder.addField("Add movie command", "show id add " + sonarrShow.getTitle() + " " + sonarrShow.getTvdbId(), false);
        } else {
          embedBuilder.addField("Id", "" + existingShow.getId(), true);
          if (existingShow.getSeasons() != null) {
            embedBuilder.addField("Number of seasons", "" + existingShow.getSeasons().size(), true);
            for (SonarrSeason sonarrSeason : existingShow.getSeasons()) {
              embedBuilder.addField("",
                "Season#" + sonarrSeason.getSeasonNumber() +
                ",Available Epsiodes=" + sonarrSeason.getStatistics().getEpisodeCount() + ",Total Epsiodes=" + sonarrSeason.getStatistics().getTotalEpisodeCount(), false);
            }
          }
        }
        embedBuilder.setImage(sonarrShow.getRemotePoster());
        messageEmbeds.add(embedBuilder.build());
      }
      if (messageEmbeds.size() == 0) {
        return Arrays.asList(EmbedHelper.createErrorMessage("Could not find any " + (findNew ? "new" : "existing") + " shows for search term=" + search));
      }
      return messageEmbeds;
    } catch (Exception e) {
      LOGGER.error("Error trying to lookup show, searchText=" + search, e);
      return Arrays.asList(EmbedHelper.createErrorMessage("Error looking up content, e=" + e.getMessage()));
    }
  }

  @Override
  public List<MessageEmbed> lookupTorrents(String command, boolean showRejected) {
    return null;
  }

  @Override
  public List<MessageEmbed> downloads() {
    return ConnectionHelper.makeGetRequest(this, "queue", new ConnectionHelper.SimpleMessageEmbedResponseHandler() {
      @Override
      public List<MessageEmbed> onSuccess(String response) throws Exception {
        List<MessageEmbed> messageEmbeds = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        //only show a max of 20 episodes
        int size = json.size() >= 20 ? 20 : json.size();
        for (int i = 0; i < size; i++) {
          SonarrQueue showQueue = new Gson().fromJson(json.get(i), SonarrQueue.class);
          EmbedBuilder embedBuilder = new EmbedBuilder();
          embedBuilder.setTitle(showQueue.getSonarrQueueShow().getTitle());
          embedBuilder.addField("Quality", showQueue.getQuality().getQuality().getName(), true);
          embedBuilder.addField("Status", showQueue.getStatus(), true);
          embedBuilder.addField("Time Left", showQueue.getTimeleft(), true);
          if (showQueue.getStatusMessages() != null) {
            for (SonarrQueueStatusMessages statusMessage : showQueue.getStatusMessages()) {
              for (String message : statusMessage.getMessages()) {
                embedBuilder.addField("Download message", message, true);
              }
            }
          }
          embedBuilder.addField("Cancel download command", "show cancel download " + showQueue.getId(), true);
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
  public List<MessageEmbed> cancelDownload(long id) {
    return null;
  }

  @Override
  public List<MessageEmbed> getProfiles() {
    Collection<SonarrProfile> profiles = SONARR_CACHE.getQualityProfiles();
    if (profiles == null || profiles.isEmpty()) {
      return Arrays.asList(EmbedHelper.createErrorMessage("Found 0 profiles, please setup Sonarr with at least one profile"));
    }

    List<MessageEmbed> profileMessages = new ArrayList<>();
    for (SonarrProfile sonarrProfile : profiles) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setTitle("Profile");
      embedBuilder.addField("Name", sonarrProfile.getName(), false);
      embedBuilder.addField("Cutoff", sonarrProfile.getCutoff().getName(), false);
      embedBuilder.addBlankField(false);
      for (int k = 0; k < sonarrProfile.getItems().size(); k++) {
        SonarrProfileQualityItem sonarrProfileQualityItem = sonarrProfile.getItems().get(k);
        if (sonarrProfileQualityItem.isAllowed()) {
          embedBuilder.addField(
            "Quality",
            "name=" + sonarrProfileQualityItem.getQuality().getName() + ", resolution=" + sonarrProfileQualityItem.getQuality().getResolution(),
            true);
        }
      }
      profileMessages.add(embedBuilder.build());
    }
    return profileMessages;
  }

  @Override
  public List<MessageEmbed> forceDownload(String command) {
    return null;
  }

  @Override
  public void sendPeriodicNotifications(JDA jda) {
    sendDownloadUpdates(jda);
  }

  @Override
  public void cacheData(JDA jda) {
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

  private MessageEmbed addShow(SonarrShow sonarrShow) {
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
      return EmbedHelper.createErrorMessage("Could not find sonarr profile for default " + sonarrProfile);
    }
    sonarrShow.setQualityProfileId((int)sonarrProfile.getId());
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(getApiUrl("series"));

      post.addHeader("content-type", "application/x-www-form-urlencoded");
      post.setEntity(new StringEntity(new GsonBuilder().addSerializationExclusionStrategy(excludeUnnecessaryFields).create().toJson(sonarrShow, SonarrShow.class)));

      try (CloseableHttpResponse response = client.execute(post)) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
          return EmbedHelper.createErrorMessage("Could not add show, status-code=" + statusCode + ", reason=" + response.getStatusLine().getReasonPhrase());
        }
        return EmbedHelper.createSuccessMessage("Show " + title + " added, sonarr-detail=" + response.getStatusLine().getReasonPhrase());
      }
    } catch (IOException e) {
      LOGGER.error("Error trying to add show=" + title, e);
      return EmbedHelper.createErrorMessage("Error adding show=" + title + ", error=" + e.getMessage());
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

  private static final SonarrCache SONARR_CACHE = new SonarrCache();
  private static final Logger LOGGER = LogManager.getLogger();
}
