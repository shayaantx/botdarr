package com.botdar.slack;

import com.botdar.Config;
import com.botdar.api.radarr.RadarrMovie;
import com.botdar.api.radarr.RadarrProfile;
import com.botdar.api.radarr.RadarrQueue;
import com.botdar.api.radarr.RadarrTorrent;
import com.botdar.api.sonarr.SonarrProfile;
import com.botdar.api.sonarr.SonarrQueue;
import com.botdar.api.sonarr.SonarrShow;
import com.botdar.clients.ChatClientResponseBuilder;
import com.botdar.commands.Command;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.model.block.composition.PlainTextObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SlackResponseBuilder implements ChatClientResponseBuilder<SlackResponse> {
  @Override
  public SlackResponse getHelpResponse() {
    SlackResponse slackResponse = new SlackResponse();
    try {
      slackResponse.addBlock(SectionBlock.builder()
        .fields(Arrays.asList(PlainTextObject.builder().emoji(true).text("Version: " + ChatClientResponseBuilder.getVersion()).build()))
        .text(MarkdownTextObject.builder().text("*Commands*").build())
        .build());

      boolean radarrEnabled = Config.isRadarrEnabled();
      boolean sonarrEnabled = Config.isSonarrEnabled();
      if (radarrEnabled) {
        slackResponse.addBlock(SectionBlock.builder()
          .text(MarkdownTextObject.builder().text("*movies help* - Shows all the commands for movies").build())
          .build());
      }

      if (sonarrEnabled) {
        slackResponse.addBlock(SectionBlock.builder()
          .text(MarkdownTextObject.builder().text("*shows help* - Shows all the commands for shows").build())
          .build());
      }

      if (!radarrEnabled && !sonarrEnabled) {
        slackResponse.addBlock(SectionBlock.builder()
          .text(MarkdownTextObject.builder().text("*No radarr or sonarr commands configured, check your properties file and logs*").build())
          .build());
      }
    } catch (IOException e) {
      throw new RuntimeException("Error getting botdarr version", e);
    }
    return slackResponse;
  }

  @Override
  public SlackResponse getMoviesHelpResponse(List<Command> radarrCommands) {
    return getListOfCommands(radarrCommands);
  }

  @Override
  public SlackResponse getShowsHelpResponse(List<Command> sonarrCommands) {
    return getListOfCommands(sonarrCommands);
  }

  @Override
  public SlackResponse getShowResponse(SonarrShow show) {
    return null;
  }

  @Override
  public SlackResponse getShowDownloadResponses(SonarrQueue sonarrShow) {
    return null;
  }

  @Override
  public SlackResponse getMovieDownloadResponses(RadarrQueue radarrQueue) {
    return null;
  }

  @Override
  public SlackResponse createErrorMessage(String message) {
    return null;
  }

  @Override
  public SlackResponse createInfoMessage(String message) {
    return null;
  }

  @Override
  public SlackResponse createSuccessMessage(String message) {
    return null;
  }

  @Override
  public SlackResponse getTorrentResponses(RadarrTorrent radarrTorrent, String movieTitle) {
    return null;
  }

  @Override
  public SlackResponse getShowProfile(SonarrProfile sonarrProfile) {
    return null;
  }

  @Override
  public SlackResponse getMovieProfile(RadarrProfile radarrProfile) {
    return null;
  }

  @Override
  public SlackResponse getNewOrExistingShow(SonarrShow sonarrShow, SonarrShow existingShow, boolean findNew) {
    return null;
  }

  @Override
  public SlackResponse getNewOrExistingMovie(RadarrMovie lookupMovie, RadarrMovie existingMovie, boolean findNew) {
    return null;
  }

  @Override
  public SlackResponse getMovie(RadarrMovie radarrMovie) {
    return null;
  }

  @Override
  public SlackResponse getDiscoverableMovies(RadarrMovie radarrMovie) {
    return null;
  }

  private SlackResponse getListOfCommands(List<Command> commands) {
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Commands*").build())
      .build());
    for (Command command : commands) {
      slackResponse.addBlock(SectionBlock.builder()
        .text(MarkdownTextObject.builder().text("*" + command.getCommandText() + "* - " + command.getDescription()).build())
        .build());
    }
    return slackResponse;
  }
}
