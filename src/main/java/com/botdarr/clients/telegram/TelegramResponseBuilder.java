package com.botdarr.clients.telegram;

import com.botdarr.Config;
import com.botdarr.api.lidarr.LidarrArtist;
import com.botdarr.api.lidarr.LidarrQueueRecord;
import com.botdarr.api.lidarr.LidarrQueueStatusMessage;
import com.botdarr.api.radarr.*;
import com.botdarr.api.sonarr.*;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.*;
import com.botdarr.utilities.ListUtils;
import j2html.tags.DomContent;
import org.apache.logging.log4j.util.Strings;

import static com.botdarr.api.lidarr.LidarrApi.ADD_ARTIST_COMMAND_FIELD_PREFIX;
import static com.botdarr.api.radarr.RadarrApi.ADD_MOVIE_COMMAND_FIELD_PREFIX;
import static com.botdarr.api.sonarr.SonarrApi.ADD_SHOW_COMMAND_FIELD_PREFIX;
import static j2html.TagCreator.*;
import static net.dv8tion.jda.api.entities.MessageEmbed.VALUE_MAX_LENGTH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TelegramResponseBuilder implements ChatClientResponseBuilder<TelegramResponse> {
  @Override
  public TelegramResponse getHelpResponse() {
    try {
      List<DomContent> domContents = new ArrayList<>();
      domContents.add(code("Version: " + ChatClientResponseBuilder.getVersion()));
      domContents.add(u(b("*Commands*")));
      boolean radarrEnabled = Config.isRadarrEnabled();
      boolean sonarrEnabled = Config.isSonarrEnabled();
      boolean lidarrEnabled = Config.isLidarrEnabled();
      if (radarrEnabled) {
        domContents.add(text(RadarrCommands.getHelpMovieCommandStr() + " - Shows all the commands for movies"));
      }
      if (sonarrEnabled) {
        domContents.add(text(SonarrCommands.getHelpShowCommandStr() + " - Shows all the commands for shows"));
      }
      if (lidarrEnabled) {
        domContents.add(text(LidarrCommands.getHelpCommandStr() + " - Shows all the commands for music"));
      }
      if (!radarrEnabled && !sonarrEnabled) {
        domContents.add(b("*No radarr or sonarr or lidarr commands configured, check your properties file and logs*"));
      }
      return new TelegramResponse(domContents);
    } catch (IOException e) {
      throw new RuntimeException("Error getting help response", e);
    }
  }

  @Override
  public TelegramResponse getMusicHelpResponse(List<Command> lidarCommands) {
    return new TelegramResponse(getListOfCommands(lidarCommands));
  }

  @Override
  public TelegramResponse getMoviesHelpResponse(List<Command> radarrCommands) {
    return new TelegramResponse(getListOfCommands(radarrCommands));
  }

  @Override
  public TelegramResponse getShowsHelpResponse(List<Command> sonarrCommands) {
    return new TelegramResponse(getListOfCommands(sonarrCommands));
  }

  @Override
  public TelegramResponse getShowResponse(SonarrShow show) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b("*Title* - " + show.getTitle()));
    domContents.add(code("TvdbId - " + show.getTvdbId()));
    domContents.add(u(ADD_SHOW_COMMAND_FIELD_PREFIX + " - " +  SonarrCommands.getAddShowCommandStr(show.getTitle(), show.getTvdbId())));
    domContents.add(a(show.getRemotePoster()));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getArtistResponse(LidarrArtist lidarrArtist) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b("*Artist Name* - " + lidarrArtist.getArtistName()));
    domContents.add(code("Id - " + lidarrArtist.getForeignArtistId()));
    domContents.add(u(ADD_ARTIST_COMMAND_FIELD_PREFIX + " - " +  LidarrCommands.getAddArtistCommandStr(lidarrArtist.getArtistName(), lidarrArtist.getForeignArtistId())));
    domContents.add(a(lidarrArtist.getRemotePoster()));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getShowDownloadResponses(SonarrQueue showQueue) {
    SonarQueueEpisode episode = showQueue.getEpisode();

    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b(showQueue.getSonarrQueueShow().getTitle()));
    StringBuilder queueDetails = new StringBuilder();
    queueDetails.append("Season/Episode - " + "S" + episode.getSeasonNumber() + "E" + episode.getEpisodeNumber() + "\n");
    queueDetails.append("Quality - " + showQueue.getQuality().getQuality().getName() + "\n");
    queueDetails.append("Status - " + showQueue.getStatus() + "\n");
    queueDetails.append("Time Left - " + (showQueue.getTimeleft() == null ? "unknown" : showQueue.getTimeleft()) + "\n");
    domContents.add(code(queueDetails.toString()));

    String overview = episode.getTitle() + ": " + episode.getOverview();
    if (overview.length() > VALUE_MAX_LENGTH) {
      overview = overview.substring(0, VALUE_MAX_LENGTH);
    }
    domContents.add(b("Overview - " + overview));
    if (showQueue.getStatusMessages() != null) {
      StringBuilder statusMessageBuilder = new StringBuilder();
      for (SonarrQueueStatusMessages statusMessage : showQueue.getStatusMessages()) {
        for (String message : statusMessage.getMessages()) {
          statusMessageBuilder.append("Download Message - " + message + "\n");
        }
      }
      domContents.add(code(statusMessageBuilder.toString()));
    }
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getMovieDownloadResponses(RadarrQueue radarrQueue) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b(radarrQueue.getRadarrQueueMovie().getTitle()));
    StringBuilder details = new StringBuilder();
    details.append("Quality - " + radarrQueue.getQuality().getQuality().getName() + "\n");
    details.append("Status - " + radarrQueue.getStatus() + "\n");
    details.append("Time Left - " + radarrQueue.getTimeleft() == null ? "unknown" : radarrQueue.getTimeleft() + "\n");
    if (radarrQueue.getStatusMessages() != null) {
      for (RadarrQueueStatusMessages statusMessage : radarrQueue.getStatusMessages()) {
        for (String message : statusMessage.getMessages()) {
          details.append("Download message - " + message + "\n");
        }
      }
    }
    domContents.add(code(details.toString()));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getArtistDownloadResponses(LidarrQueueRecord lidarrQueueRecord) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b(lidarrQueueRecord.getTitle()));
    StringBuilder details = new StringBuilder();
    details.append("Status - " + lidarrQueueRecord.getStatus() + "\n");
    details.append("Time Left - " + lidarrQueueRecord.getTimeleft() == null ? "unknown" : lidarrQueueRecord.getTimeleft() + "\n");
    if (lidarrQueueRecord.getStatusMessages() != null) {
      //limit messages to 5, since lidarr can really throw a ton of status messages
      for (LidarrQueueStatusMessage statusMessage : ListUtils.subList(lidarrQueueRecord.getStatusMessages(), 5)) {
        for (String message : statusMessage.getMessages()) {
          details.append("Download message - " + message + "\n");
        }
      }
    }
    domContents.add(code(details.toString()));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse createErrorMessage(String message) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b("Error! - " + message));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse createInfoMessage(String message) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b("Info! - " + message));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse createSuccessMessage(String message) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(u(b("Success! - " + message)));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getShowProfile(SonarrProfile sonarrProfile) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b("Profile"));
    domContents.add(text("Name - " + sonarrProfile.getName()));
    domContents.add(text("Cutoff - " + sonarrProfile.getCutoff().getName()));
    if (sonarrProfile.getItems() != null) {
      StringBuilder qualityItems = new StringBuilder();
      for (int k = 0; k < sonarrProfile.getItems().size(); k++) {
        SonarrProfileQualityItem sonarrProfileQualityItem = sonarrProfile.getItems().get(k);
        if (sonarrProfileQualityItem.isAllowed()) {
          qualityItems.append("Quality - name=" + sonarrProfileQualityItem.getQuality().getName() + ", resolution=" + sonarrProfileQualityItem.getQuality().getResolution() + "\n");
        }
      }
      domContents.add(code(qualityItems.toString()));
    }
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getMovieProfile(RadarrProfile radarrProfile) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b("Profile"));
    domContents.add(text("Name - " + radarrProfile.getName()));
    domContents.add(text("Cutoff - " + radarrProfile.getCutoff().getName()));
    if (radarrProfile.getItems() != null) {
      StringBuilder qualityItems = new StringBuilder();
      for (int k = 0; k < radarrProfile.getItems().size(); k++) {
        RadarrProfileQualityItem radarrProfileQualityItem = radarrProfile.getItems().get(k);
        if (radarrProfileQualityItem.isAllowed()) {
          qualityItems.append("Quality - name=" + radarrProfileQualityItem.getQuality().getName() + ", resolution=" + radarrProfileQualityItem.getQuality().getResolution() + "\n");
        }
      }
      domContents.add(code(qualityItems.toString()));
    }
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getNewOrExistingShow(SonarrShow sonarrShow, SonarrShow existingShow, boolean findNew) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b("*Title* - " + sonarrShow.getTitle()));
    domContents.add(code("TvdbId - " + sonarrShow.getTvdbId()));
    if (findNew) {
      domContents.add(u(ADD_SHOW_COMMAND_FIELD_PREFIX + " - " + SonarrCommands.getAddShowCommandStr(sonarrShow.getTitle(), sonarrShow.getTvdbId())));
    } else {
      StringBuilder existingShowDetails = new StringBuilder();
      existingShowDetails.append("Number of seasons - " + existingShow.getSeasons().size() + "\n");
      for (SonarrSeason sonarrSeason : existingShow.getSeasons()) {
        existingShowDetails.append("Season#" + sonarrSeason.getSeasonNumber() +
          ",Available Epsiodes=" + sonarrSeason.getStatistics().getEpisodeCount() + ",Total Epsiodes=" + sonarrSeason.getStatistics().getTotalEpisodeCount() + "\n");
      }
      domContents.add(code(existingShowDetails.toString()));
    }
    domContents.add(a(sonarrShow.getRemotePoster()));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getNewOrExistingMovie(RadarrMovie lookupMovie, RadarrMovie existingMovie, boolean findNew) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b(lookupMovie.getTitle()));
    if (findNew) {
      domContents.add(u(ADD_MOVIE_COMMAND_FIELD_PREFIX + " - " + RadarrCommands.getAddMovieCommandStr(lookupMovie.getTitle(), lookupMovie.getTmdbId())));
    } else {
      StringBuilder existingDetails = new StringBuilder();
      existingDetails.append("Id - " + existingMovie.getId() + "\n");
      existingDetails.append("Downloaded - " + existingMovie.isDownloaded() + "\n");
      existingDetails.append("Has File - " + existingMovie.isHasFile() + "\n");
      domContents.add(code(existingDetails.toString()));
    }
    domContents.add(a(lookupMovie.getRemotePoster()));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getNewOrExistingArtist(LidarrArtist lookupArtist, LidarrArtist existingArtist, boolean findNew) {
    List<DomContent> domContents = new ArrayList<>();
    String artistDetail = " (" + lookupArtist.getDisambiguation() + ")";
    domContents.add(b(lookupArtist.getArtistName() + (Strings.isEmpty(lookupArtist.getDisambiguation()) ? "" :  artistDetail)));
    if (findNew) {
      domContents.add(u(ADD_ARTIST_COMMAND_FIELD_PREFIX + " - " + LidarrCommands.getAddArtistCommandStr(lookupArtist.getArtistName(), lookupArtist.getForeignArtistId())));
    }
    domContents.add(a(lookupArtist.getRemotePoster()));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getMovieResponse(RadarrMovie radarrMovie) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b(radarrMovie.getTitle()));
    domContents.add(text("TmdbId - " + radarrMovie.getTmdbId()));
    domContents.add(u(b(ADD_MOVIE_COMMAND_FIELD_PREFIX + " - " + RadarrCommands.getAddMovieCommandStr(radarrMovie.getTitle(), radarrMovie.getTmdbId()))));
    domContents.add(a(radarrMovie.getRemotePoster()));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getDiscoverableMovies(RadarrMovie radarrMovie) {
    return getMovieResponse(radarrMovie);
  }

  private List<DomContent> getListOfCommands(List<Command> commands) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(u(b("*Commands*")));
    for (Command command : commands) {
      domContents.add(b(text(new CommandProcessor().getPrefix() + command.getCommandUsage())));
      domContents.add(text(command.getDescription()));
      domContents.add(text(" "));
    }
    return domContents;
  }
}
