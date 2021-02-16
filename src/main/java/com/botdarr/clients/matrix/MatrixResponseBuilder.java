package com.botdarr.clients.matrix;

import com.botdarr.Config;
import com.botdarr.api.lidarr.LidarrArtist;
import com.botdarr.api.lidarr.LidarrQueueRecord;
import com.botdarr.api.lidarr.LidarrQueueStatusMessage;
import com.botdarr.api.radarr.*;
import com.botdarr.api.sonarr.*;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.*;
import com.botdarr.utilities.ListUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.Map;

import static com.botdarr.api.lidarr.LidarrApi.ADD_ARTIST_COMMAND_FIELD_PREFIX;
import static com.botdarr.api.radarr.RadarrApi.ADD_MOVIE_COMMAND_FIELD_PREFIX;
import static com.botdarr.api.sonarr.SonarrApi.ADD_SHOW_COMMAND_FIELD_PREFIX;
import static com.botdarr.commands.StatusCommand.STATUS_COMMAND;
import static com.botdarr.commands.StatusCommand.STATUS_COMMAND_DESCRIPTION;

public class MatrixResponseBuilder implements ChatClientResponseBuilder<MatrixResponse> {
  @Override
  public MatrixResponse getHelpResponse() {
    try {
      MatrixResponse matrixResponse = new MatrixResponse();
      matrixResponse.addContent("<b><u>Version: " + ChatClientResponseBuilder.getVersion() + "</u></b>");
      boolean radarrEnabled = Config.isRadarrEnabled();
      boolean sonarrEnabled = Config.isSonarrEnabled();
      boolean lidarrEnabled = Config.isLidarrEnabled();
      if (radarrEnabled) {
        matrixResponse.addContent("<b>" + RadarrCommands.getHelpMovieCommandStr() + "</b> - Shows all the commands for movies");
      }

      if (sonarrEnabled) {
        matrixResponse.addContent("<b>" + SonarrCommands.getHelpShowCommandStr() + "</b> - Shows all the commands for shows");
      }

      if (lidarrEnabled) {
        matrixResponse.addContent("<b>" + LidarrCommands.getHelpCommandStr() + "</b> - Shows all the commands for music");
      }

      if (!radarrEnabled && !sonarrEnabled && !lidarrEnabled) {
        matrixResponse.addContent("<b>No radarr or sonarr or lidarr commands configured, check your properties file and logs</b>");
      }

      if (!Config.getStatusEndpoints().isEmpty()) {
        matrixResponse.addContent("<b>" + new CommandProcessor().getPrefix() + STATUS_COMMAND + "</b> - " + STATUS_COMMAND_DESCRIPTION);
      }
      return matrixResponse;
    } catch (Exception e) {
      throw new RuntimeException("Error getting botdarr version", e);
    }
  }

  @Override
  public MatrixResponse getMusicHelpResponse(List<Command> lidarCommands) {
    return getListOfCommands(lidarCommands);
  }

  @Override
  public MatrixResponse getMoviesHelpResponse(List<Command> radarrCommands) {
    return getListOfCommands(radarrCommands);
  }

  @Override
  public MatrixResponse getShowsHelpResponse(List<Command> sonarrCommands) {
    return getListOfCommands(sonarrCommands);
  }

  @Override
  public MatrixResponse getShowResponse(SonarrShow show) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<b>Title</b> - " + show.getTitle());
    matrixResponse.addContent("<b>TvdbId</b> - " + String.valueOf(show.getTvdbId()));
    matrixResponse.addContent("<b>" + ADD_SHOW_COMMAND_FIELD_PREFIX + "</b> - " + SonarrCommands.getAddShowCommandStr(show.getTitle(), show.getTvdbId()));
    matrixResponse.addImage(show.getRemotePoster());
    return matrixResponse;
  }

  @Override
  public MatrixResponse getArtistResponse(LidarrArtist lidarrArtist) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<b>Artist Name</b> - " + lidarrArtist.getArtistName());
    matrixResponse.addContent("<b>Id</b> - " + String.valueOf(lidarrArtist.getForeignArtistId()));
    matrixResponse.addContent("<b><u>" + ADD_ARTIST_COMMAND_FIELD_PREFIX + "</u></b> - " + LidarrCommands.getAddArtistCommandStr(lidarrArtist.getArtistName(), lidarrArtist.getForeignArtistId()));
    return matrixResponse;
  }

  @Override
  public MatrixResponse getMovieResponse(RadarrMovie radarrMovie) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<b>Movie Title</b> - " + radarrMovie.getTitle());
    matrixResponse.addContent("<b>TmdbId</b> - " + radarrMovie.getTmdbId());
    matrixResponse.addContent("<b><u>" + ADD_MOVIE_COMMAND_FIELD_PREFIX + "</u></b> - " + RadarrCommands.getAddMovieCommandStr(radarrMovie.getTitle(), radarrMovie.getTmdbId()));
    return matrixResponse;
  }

  @Override
  public MatrixResponse getShowDownloadResponses(SonarrQueue sonarrShow) {
    MatrixResponse matrixResponse = new MatrixResponse();
    SonarQueueEpisode episode = sonarrShow.getEpisode();
    matrixResponse.addContent("<b>Title</b> - " + sonarrShow.getSonarrQueueShow().getTitle());
    matrixResponse.addContent("<b>Season/Episode</b> - " + "S" + episode.getSeasonNumber() + "E" + episode.getEpisodeNumber());
    matrixResponse.addContent("<b>Quality</b> - " + sonarrShow.getQuality().getQuality().getName());
    matrixResponse.addContent("<b>Status</b> - " + sonarrShow.getStatus());
    matrixResponse.addContent("<b>Time Left</b> - <i>" + (sonarrShow.getTimeleft() == null ? "unknown" : sonarrShow.getTimeleft()) + "</i>");
    String overview = episode.getTitle() + ": " + episode.getOverview();
    if (overview.length() > 1024) {
      overview = overview.substring(0, 1024);
    }
    matrixResponse.addContent("<b>Overview</b> - "  + overview);
    if (sonarrShow.getStatusMessages() != null) {
      for (SonarrQueueStatusMessages statusMessage : sonarrShow.getStatusMessages()) {
        for (String message : statusMessage.getMessages()) {
          matrixResponse.addContent("<b>Download message</b> - " + message);
        }
      }
    }
    return matrixResponse;
  }

  @Override
  public MatrixResponse getMovieDownloadResponses(RadarrQueue radarrQueue) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<b>Title</b> - " + radarrQueue.getTitle());
    matrixResponse.addContent("<b>Quality</b> - " + radarrQueue.getQuality().getQuality().getName());
    matrixResponse.addContent("<b>Status</b> - " + radarrQueue.getStatus());
    matrixResponse.addContent("<b>Time Left</b> - <i>" + (radarrQueue.getTimeleft() == null ? "unknown" : radarrQueue.getTimeleft()) + "</i>");
    if (radarrQueue.getStatusMessages() != null) {
      for (RadarrQueueStatusMessages statusMessage : radarrQueue.getStatusMessages()) {
        for (String message : statusMessage.getMessages()) {
          matrixResponse.addContent("<b>Download message</b> - " + message);
        }
      }
    }
    return matrixResponse;
  }

  @Override
  public MatrixResponse getArtistDownloadResponses(LidarrQueueRecord lidarrQueue) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<b>Title</b> - " + lidarrQueue.getTitle());
    matrixResponse.addContent("<b>Time Left</b> - <i>" + (lidarrQueue.getTimeleft() == null ? "unknown" : lidarrQueue.getTimeleft()) + "</b>");
    matrixResponse.addContent("<b>Status</b> - " + lidarrQueue.getStatus());
    if (lidarrQueue.getStatusMessages() != null) {
      for (LidarrQueueStatusMessage statusMessage : ListUtils.subList(lidarrQueue.getStatusMessages(), 5)) {
        for (String message : statusMessage.getMessages()) {
          matrixResponse.addContent("<b>Download message</b> - " + message);
        }
      }
    }
    return matrixResponse;
  }

  @Override
  public MatrixResponse createErrorMessage(String message) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<span data-mx-color=\"#ff0000\">" + message + "</span>");
    return matrixResponse;
  }

  @Override
  public MatrixResponse createInfoMessage(String message) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<span>" + message + "</span>");
    return matrixResponse;
  }

  @Override
  public MatrixResponse createSuccessMessage(String message) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<span data-mx-color=\"#3aeb34\">" + message + "</span>");
    return matrixResponse;
  }

  @Override
  public MatrixResponse getShowProfile(SonarrProfile sonarrProfile) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<u><b>Profile</b></u>");
    matrixResponse.addContent("<b>Name</b> - " + sonarrProfile.getName());
    matrixResponse.addContent("<b>Cutoff</b> - " + sonarrProfile.getCutoff().getName());
    for (int k = 0; k < sonarrProfile.getItems().size(); k++) {
      SonarrProfileQualityItem sonarrProfileQualityItem = sonarrProfile.getItems().get(k);
      if (sonarrProfileQualityItem.isAllowed()) {
        matrixResponse.addContent("<b>Quality</b> - Name: " + sonarrProfileQualityItem.getQuality().getName() + ", Resolution: " + sonarrProfileQualityItem.getQuality().getResolution());
      }
    }
    return matrixResponse;
  }

  @Override
  public MatrixResponse getMovieProfile(RadarrProfile radarrProfile) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<u><b>Profile</b></u>");
    matrixResponse.addContent("<b>Name</b> - " + radarrProfile.getName());
    matrixResponse.addContent("<b>Cutoff</b> - " + radarrProfile.getCutoff().getName());
    for (int k = 0; k < radarrProfile.getItems().size(); k++) {
      RadarrProfileQualityItem sonarrProfileQualityItem = radarrProfile.getItems().get(k);
      if (sonarrProfileQualityItem.isAllowed()) {
        matrixResponse.addContent("<b>Quality</b> - Name: " + sonarrProfileQualityItem.getQuality().getName() + ", Resolution: " + sonarrProfileQualityItem.getQuality().getResolution());
      }
    }
    return matrixResponse;
  }

  @Override
  public MatrixResponse getNewOrExistingShow(SonarrShow sonarrShow, SonarrShow existingShow, boolean findNew) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<b>Title</b> - " + sonarrShow.getTitle());
    matrixResponse.addContent("<b>TvdbId</b> - " + sonarrShow.getTvdbId());
    if (findNew) {
      matrixResponse.addContent("<b>" + ADD_SHOW_COMMAND_FIELD_PREFIX + "</b> - " + SonarrCommands.getAddShowCommandStr(sonarrShow.getTitle(), sonarrShow.getTvdbId()));
    } else {
      matrixResponse.addContent("<b>Id</b> - " + existingShow.getId());
      if (existingShow.getSeasons() != null) {
        matrixResponse.addContent("Number of seasons - " + existingShow.getSeasons().size());
        for (SonarrSeason sonarrSeason : existingShow.getSeasons()) {
          matrixResponse.addContent(
            "Season#" + sonarrSeason.getSeasonNumber() +
            ",Available Epsiodes=" + sonarrSeason.getStatistics().getEpisodeCount() +
            ",Total Epsiodes=" + sonarrSeason.getStatistics().getTotalEpisodeCount());
        }
      }
    }
    matrixResponse.addImage(sonarrShow.getRemotePoster());
    return matrixResponse;
  }

  @Override
  public MatrixResponse getNewOrExistingMovie(RadarrMovie lookupMovie, RadarrMovie existingMovie, boolean findNew) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<b>Title</b> - " + lookupMovie.getTitle());
    matrixResponse.addContent("<b>TmdbId</b> - " + String.valueOf(lookupMovie.getTmdbId()));
    if (findNew) {
      matrixResponse.addContent("<b>" + ADD_MOVIE_COMMAND_FIELD_PREFIX + "</b> - " + RadarrCommands.getAddMovieCommandStr(lookupMovie.getTitle(), lookupMovie.getTmdbId()));
    } else {
      matrixResponse.addContent("<b>Id</b> - " + existingMovie.getId());
      matrixResponse.addContent("<b>Downloaded</b> - " + String.valueOf(existingMovie.isDownloaded()));
      matrixResponse.addContent("<b>Has File</b> - " + String.valueOf(existingMovie.isHasFile()));
    }
    matrixResponse.addImage(lookupMovie.getRemotePoster());
    return matrixResponse;
  }

  @Override
  public MatrixResponse getNewOrExistingArtist(LidarrArtist lookupArtist, LidarrArtist existingArtist, boolean findNew) {
    MatrixResponse matrixResponse = new MatrixResponse();
    String artistDetail = " (" + lookupArtist.getDisambiguation() + ")";
    matrixResponse.addContent("<b>Title</b> - " + (lookupArtist.getArtistName() + (Strings.isEmpty(lookupArtist.getDisambiguation()) ? "" :  artistDetail)));
    if (findNew) {
      matrixResponse.addContent("<b>" + ADD_ARTIST_COMMAND_FIELD_PREFIX + "</b> - " + LidarrCommands.getAddArtistCommandStr(lookupArtist.getArtistName(), lookupArtist.getForeignArtistId()));
    }
    matrixResponse.addImage(lookupArtist.getRemotePoster());
    return matrixResponse;
  }

  @Override
  public MatrixResponse getDiscoverableMovies(RadarrMovie radarrMovie) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<b>Title</b> - " + radarrMovie.getTitle());
    matrixResponse.addContent("<b>TmdbId</b> - " + radarrMovie.getTmdbId());
    matrixResponse.addContent("<b>" + ADD_MOVIE_COMMAND_FIELD_PREFIX + "</b> - " + RadarrCommands.getAddMovieCommandStr(radarrMovie.getTitle(), radarrMovie.getTmdbId()));
    if (radarrMovie.getRemotePoster() != null && !radarrMovie.getRemotePoster().isEmpty()) {
      matrixResponse.addImage(radarrMovie.getRemotePoster());
    }
    return matrixResponse;
  }

  @Override
  public MatrixResponse getStatusCommandResponse(Map<String, Boolean> endpointStatuses) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addRawContent("<table>");
    matrixResponse.addRawContent("<thead><tr><td>Endpoint</td><td>Statuses</td></tr></thead>");
    for (Map.Entry<String, Boolean> endpointStatusEntry : endpointStatuses.entrySet()) {
      matrixResponse.addRawContent("<tr>");
      matrixResponse.addRawContent("<td>");
      matrixResponse.addRawContent(endpointStatusEntry.getKey());
      matrixResponse.addRawContent("</td>");
      matrixResponse.addRawContent("<td>");
      matrixResponse.addRawContent(endpointStatusEntry.getValue() ? "\uD83D\uDFE2" : "\uD83D\uDD34");
      matrixResponse.addRawContent("</td>");
      matrixResponse.addRawContent("</tr>");
    }
    matrixResponse.addRawContent("</table>");
    return matrixResponse;
  }

  private MatrixResponse getListOfCommands(List<Command> commands) {
    MatrixResponse matrixResponse = new MatrixResponse();
    matrixResponse.addContent("<b><u>Commands</u></b>");
    for (Command command : commands) {
      matrixResponse.addContent("<b>" + new CommandProcessor().getPrefix() + command.getCommandUsage() + "</b>" + " - " + command.getDescription());
    }
    return matrixResponse;
  }
}
