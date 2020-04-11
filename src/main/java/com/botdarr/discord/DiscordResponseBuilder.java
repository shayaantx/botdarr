package com.botdarr.discord;

import com.botdarr.Config;
import com.botdarr.api.sonarr.*;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.api.radarr.*;
import com.botdarr.commands.Command;
import com.botdarr.commands.RadarrCommands;
import com.botdarr.commands.SonarrCommands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static com.botdarr.api.RadarrApi.ADD_MOVIE_COMMAND_FIELD_PREFIX;
import static com.botdarr.api.SonarrApi.ADD_SHOW_COMMAND_FIELD_PREFIX;
import static net.dv8tion.jda.api.entities.MessageEmbed.VALUE_MAX_LENGTH;

public class DiscordResponseBuilder implements ChatClientResponseBuilder<DiscordResponse> {
  @Override
  public DiscordResponse getHelpResponse() {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Commands");
    try {
      embedBuilder.setDescription(ChatClientResponseBuilder.getVersion());
    } catch (IOException e) {
      throw new RuntimeException("Error getting botdarr version", e);
    }
    boolean radarrEnabled = Config.isRadarrEnabled();
    boolean sonarrEnabled = Config.isSonarrEnabled();
    if (radarrEnabled) {
      embedBuilder.addField("movies help", "Shows all the commands for movies", false);
    }

    if (sonarrEnabled) {
      embedBuilder.addField("shows help", "Shows all the commands for shows", false);
    }

    if (!radarrEnabled && !sonarrEnabled) {
      embedBuilder.appendDescription("No radarr or sonarr commands configured, check your properties file and logs");
    }
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse getMoviesHelpResponse(List<Command> radarrCommands) {
    return getListOfCommands(radarrCommands);
  }

  @Override
  public DiscordResponse getShowsHelpResponse(List<Command> sonarrCommands) {
    return getListOfCommands(sonarrCommands);
  }

  @Override
  public DiscordResponse getShowResponse(SonarrShow show) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(show.getTitle());
    embedBuilder.addField("TvdbId", "" + show.getTvdbId(), false);
    embedBuilder.addField(ADD_SHOW_COMMAND_FIELD_PREFIX, SonarrCommands.getAddShowCommandStr(show.getTitle(), show.getTvdbId()), false);
    embedBuilder.setImage(show.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse getShowDownloadResponses(SonarrQueue showQueue) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    SonarQueueEpisode episode = showQueue.getEpisode();
    embedBuilder.setTitle(showQueue.getSonarrQueueShow().getTitle());
    embedBuilder.addField("Season/Episode", "S" + episode.getSeasonNumber() + "E" + episode.getEpisodeNumber(), true);
    embedBuilder.addField("Quality", showQueue.getQuality().getQuality().getName(), true);
    embedBuilder.addField("Status", showQueue.getStatus(), true);
    embedBuilder.addField("Time Left", showQueue.getTimeleft() == null ? "unknown" : showQueue.getTimeleft(), true);
    String overview = episode.getTitle() + ": " + episode.getOverview();
    if (overview.length() > VALUE_MAX_LENGTH) {
      overview = overview.substring(0, VALUE_MAX_LENGTH);
    }
    embedBuilder.addField("Overview", overview, false);
    if (showQueue.getStatusMessages() != null) {
      for (SonarrQueueStatusMessages statusMessage : showQueue.getStatusMessages()) {
        for (String message : statusMessage.getMessages()) {
          embedBuilder.addField("Download message", message, true);
        }
      }
    }
    embedBuilder.addField("Cancel download command", "show cancel download " + showQueue.getId(), true);
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse getMovieDownloadResponses(RadarrQueue radarrQueue) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(radarrQueue.getRadarrQueueMovie().getTitle());
    embedBuilder.addField("Quality", radarrQueue.getQuality().getQuality().getName(), true);
    embedBuilder.addField("Status", radarrQueue.getStatus(), true);
    embedBuilder.addField("Time Left", radarrQueue.getTimeleft() == null ? "unknown" : radarrQueue.getTimeleft(), true);
    if (radarrQueue.getStatusMessages() != null) {
      for (RadarrQueueStatusMessages statusMessage : radarrQueue.getStatusMessages()) {
        for (String message : statusMessage.getMessages()) {
          embedBuilder.addField("Download message", message, true);
        }
      }
    }
    embedBuilder.addField("Cancel download command", "movie cancel download " + radarrQueue.getId(), true);
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse getTorrentResponses(RadarrTorrent radarrTorrent, String movieTitle) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.addField("Title", radarrTorrent.getTitle(), false);
    embedBuilder.addField("Torrent", radarrTorrent.getGuid(), false);
    embedBuilder.addField("Quality", radarrTorrent.getQuality().getQuality().getName(), true);
    embedBuilder.addField("Indexer", radarrTorrent.getIndexer(), true);
    embedBuilder.addField("Seeders", "" + radarrTorrent.getSeeders(), true);
    embedBuilder.addField("Leechers", "" + radarrTorrent.getLeechers(), true);
    embedBuilder.addField("Size", "" + FileUtils.byteCountToDisplaySize(radarrTorrent.getSize()), true);
    String[] rejections = radarrTorrent.getRejections();
    if (rejections != null) {
      embedBuilder.addBlankField(false);
      for (String rejection : rejections) {
        embedBuilder.addField("Rejection Reason", rejection, false);
      }
    }
    String key = radarrTorrent.getGuid() + ":title=" + movieTitle;
    byte[] encodedBytes = Base64.getEncoder().encode(key.getBytes());
    embedBuilder.addField("Download hash command", "movie hash download " + new String(encodedBytes), true);
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse getShowProfile(SonarrProfile sonarrProfile) {
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
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse getMovieProfile(RadarrProfile radarrProfile) {
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
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse getNewOrExistingShow(SonarrShow sonarrShow, SonarrShow existingShow, boolean findNew) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(sonarrShow.getTitle());
    embedBuilder.addField("TvdbId", "" + sonarrShow.getTvdbId(), true);
    if (findNew) {
      embedBuilder.addField(ADD_SHOW_COMMAND_FIELD_PREFIX, SonarrCommands.getAddShowCommandStr(sonarrShow.getTitle(), sonarrShow.getTvdbId()), false);
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
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse getNewOrExistingMovie(RadarrMovie radarrMovie, RadarrMovie existingMovie, boolean findNew) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(radarrMovie.getTitle());
    embedBuilder.addField("TmdbId", "" + radarrMovie.getTmdbId(), false);
    if (findNew) {
      embedBuilder.addField(ADD_MOVIE_COMMAND_FIELD_PREFIX, RadarrCommands.getAddMovieCommandStr(radarrMovie.getTitle(), radarrMovie.getTmdbId()), false);
    } else {
      embedBuilder.addField("Id", "" + existingMovie.getId(), false);
      embedBuilder.addField("Downloaded", existingMovie.isDownloaded() + "", false);
      embedBuilder.addField("Has File", existingMovie.isHasFile() + "", false);
    }
    embedBuilder.setImage(radarrMovie.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse getMovie(RadarrMovie radarrMovie) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(radarrMovie.getTitle());
    embedBuilder.addField("TmdbId", "" + radarrMovie.getTmdbId(), false);
    embedBuilder.addField(ADD_MOVIE_COMMAND_FIELD_PREFIX, RadarrCommands.getAddMovieCommandStr(radarrMovie.getTitle(), radarrMovie.getTmdbId()), false);
    embedBuilder.setImage(radarrMovie.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse getDiscoverableMovies(RadarrMovie radarrMovie) {
    return getMovie(radarrMovie);
  }

  @Override
  public DiscordResponse createErrorMessage(String message) {
    return new DiscordResponse(createErrorMessageEmbed(message));
  }

  @Override
  public DiscordResponse createInfoMessage(String message) {
    return new DiscordResponse(createInfoMessageEmbed(message));
  }

  @Override
  public DiscordResponse createSuccessMessage(String message) {
    return new DiscordResponse(createSuccessMessageEmbed(message));
  }

  private MessageEmbed createInfoMessageEmbed(String message) {
    return createMessageEmbed("Info", Color.WHITE, message);
  }

  private MessageEmbed createSuccessMessageEmbed(String message) {
    return createMessageEmbed("Success!", Color.GREEN, message);
  }

  private MessageEmbed createErrorMessageEmbed(String message) {
    return createMessageEmbed("Error!", Color.RED, message);
  }

  private static MessageEmbed createMessageEmbed(String title, Color color, String message) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(title);
    embedBuilder.setDescription(message);
    embedBuilder.setColor(color);
    return embedBuilder.build();
  }

  private DiscordResponse getListOfCommands(List<Command> commands) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Commands");
    for (Command com : commands) {
      embedBuilder.addField(com.getCommandText(), com.getDescription(), false);
    }
    return new DiscordResponse(embedBuilder.build());
  }

  private static final Logger LOGGER = LogManager.getLogger("DiscordLog");
}
