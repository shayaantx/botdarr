package com.botdarr.telegram;

import com.botdarr.Config;
import com.botdarr.api.radarr.RadarrMovie;
import com.botdarr.api.radarr.RadarrProfile;
import com.botdarr.api.radarr.RadarrQueue;
import com.botdarr.api.radarr.RadarrTorrent;
import com.botdarr.api.sonarr.SonarrProfile;
import com.botdarr.api.sonarr.SonarrQueue;
import com.botdarr.api.sonarr.SonarrSeason;
import com.botdarr.api.sonarr.SonarrShow;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.Command;
import j2html.tags.DomContent;

import static com.botdarr.api.SonarrApi.ADD_SHOW_COMMAND_FIELD_PREFIX;
import static j2html.TagCreator.*;

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
      if (radarrEnabled) {
        domContents.add(text("*movies help* - Shows all the commands for movies"));
      }
      if (sonarrEnabled) {
        domContents.add(text("*shows help* - Shows all the commands for shows"));
      }
      if (!radarrEnabled && !sonarrEnabled) {
        domContents.add(b("*No radarr or sonarr commands configured, check your properties file and logs*"));
      }
      return new TelegramResponse(domContents);
    } catch (IOException e) {
      throw new RuntimeException("Error getting help response", e);
    }
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
    domContents.add(u(ADD_SHOW_COMMAND_FIELD_PREFIX + " - " + "show id add " + show.getTitle() + " " + show.getTvdbId()));
    domContents.add(a(show.getRemotePoster()));
    return new TelegramResponse(domContents);
  }

  @Override
  public TelegramResponse getShowDownloadResponses(SonarrQueue sonarrShow) {
    return null;
  }

  @Override
  public TelegramResponse getMovieDownloadResponses(RadarrQueue radarrQueue) {
    return null;
  }

  @Override
  public TelegramResponse createErrorMessage(String message) {
    return null;
  }

  @Override
  public TelegramResponse createInfoMessage(String message) {
    return null;
  }

  @Override
  public TelegramResponse createSuccessMessage(String message) {
    return null;
  }

  @Override
  public TelegramResponse getTorrentResponses(RadarrTorrent radarrTorrent, String movieTitle) {
    return null;
  }

  @Override
  public TelegramResponse getShowProfile(SonarrProfile sonarrProfile) {
    return null;
  }

  @Override
  public TelegramResponse getMovieProfile(RadarrProfile radarrProfile) {
    return null;
  }

  @Override
  public TelegramResponse getNewOrExistingShow(SonarrShow sonarrShow, SonarrShow existingShow, boolean findNew) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(b("*Title* - " + sonarrShow.getTitle()));
    domContents.add(code("TvdbId - " + sonarrShow.getTvdbId()));
    if (findNew) {
      domContents.add(u(ADD_SHOW_COMMAND_FIELD_PREFIX + " - " + "show id add " + sonarrShow.getTitle() + " " + sonarrShow.getTvdbId()));
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
    return null;
  }

  @Override
  public TelegramResponse getMovie(RadarrMovie radarrMovie) {
    return null;
  }

  @Override
  public TelegramResponse getDiscoverableMovies(RadarrMovie radarrMovie) {
    return null;
  }

  private List<DomContent> getListOfCommands(List<Command> commands) {
    List<DomContent> domContents = new ArrayList<>();
    domContents.add(u(b("*Commands*")));
    for (Command command : commands) {
      domContents.add(text("*" + command.getCommandText() + "* - " + command.getDescription()));
      domContents.add(text(" "));
    }
    return domContents;
  }
}
