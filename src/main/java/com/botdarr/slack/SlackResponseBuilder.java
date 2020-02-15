package com.botdarr.slack;

import com.botdarr.Config;
import com.botdarr.api.radarr.*;
import com.botdarr.api.sonarr.*;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.Command;
import com.github.seratch.jslack.api.model.block.ContextBlock;
import com.github.seratch.jslack.api.model.block.ContextBlockElement;
import com.github.seratch.jslack.api.model.block.ImageBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.model.block.composition.PlainTextObject;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static com.botdarr.api.RadarrApi.ADD_MOVIE_COMMAND_FIELD_PREFIX;
import static com.botdarr.api.SonarrApi.ADD_SHOW_COMMAND_FIELD_PREFIX;
import static net.dv8tion.jda.api.entities.MessageEmbed.VALUE_MAX_LENGTH;

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
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Title* - " + show.getTitle()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(PlainTextObject.builder().text("TvdbId - " + show.getTvdbId()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(PlainTextObject.builder().text(ADD_SHOW_COMMAND_FIELD_PREFIX + " - " + "show id add " + show.getTitle() + " " + show.getTvdbId()).build())
      .build());
    if (!Strings.isBlank(show.getRemotePoster())) {
      //if there is no poster to display, slack will fail to render all the blocks
      //so make sure there is one before trying to render
      slackResponse.addBlock(ImageBlock.builder()
        .imageUrl(show.getRemotePoster())
        .altText(show.getTitle() + " poster")
        .build());
    }
    return slackResponse;
  }

  @Override
  public SlackResponse getShowDownloadResponses(SonarrQueue showQueue) {
    SonarQueueEpisode episode = showQueue.getEpisode();
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Title* - " + showQueue.getSonarrQueueShow().getTitle()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Season/Episode - " + "S" + episode.getSeasonNumber() + "E" + episode.getEpisodeNumber()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Quality - " + showQueue.getQuality().getQuality().getName()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Status - " + showQueue.getStatus()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Time Left - *" + (showQueue.getTimeleft() == null ? "unknown" : showQueue.getTimeleft()) + "*").build())
      .build());
    String overview = episode.getTitle() + ": " + episode.getOverview();
    if (overview.length() > VALUE_MAX_LENGTH) {
      overview = overview.substring(0, VALUE_MAX_LENGTH);
    }
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Overview - " + overview).build())
      .build());
    if (showQueue.getStatusMessages() != null) {
      List<ContextBlockElement> contextBlockElements = new ArrayList<>();
      for (SonarrQueueStatusMessages statusMessage : showQueue.getStatusMessages()) {
        for (String message : statusMessage.getMessages()) {
          contextBlockElements.add(PlainTextObject.builder().text(message).build());
        }
      }
      if (showQueue.getStatusMessages() != null && showQueue.getStatusMessages().length > 0) {
        slackResponse.addBlock(ContextBlock.builder()
          .elements(contextBlockElements)
          .build());
      }
    }
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Cancel download command - " + "show cancel download " + showQueue.getId()).build())
      .build());
    return slackResponse;
  }

  @Override
  public SlackResponse getMovieDownloadResponses(RadarrQueue radarrQueue) {
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Title* - " + radarrQueue.getRadarrQueueMovie().getTitle()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Quality - " + radarrQueue.getQuality().getQuality().getName()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Status - " + radarrQueue.getStatus()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Time Left - *" + (radarrQueue.getTimeleft() == null ? "unknown" : radarrQueue.getTimeleft()) + "*").build())
      .build());
    if (radarrQueue.getStatusMessages() != null) {
      List<ContextBlockElement> contextBlockElements = new ArrayList<>();
      for (RadarrQueueStatusMessages statusMessage : radarrQueue.getStatusMessages()) {
        for (String message : statusMessage.getMessages()) {
          contextBlockElements.add(PlainTextObject.builder().text(message).build());
        }
      }
      if (radarrQueue.getStatusMessages() != null && radarrQueue.getStatusMessages().length > 0) {
        slackResponse.addBlock(ContextBlock.builder()
          .elements(contextBlockElements)
          .build());
      }
    }
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Cancel download command - " + "movie cancel download " + radarrQueue.getId()).build())
      .build());
    return slackResponse;
  }

  @Override
  public SlackResponse createErrorMessage(String message) {
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Error* - " + message).build())
      .build());
    return slackResponse;
  }

  @Override
  public SlackResponse createInfoMessage(String message) {
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Info* - " + message).build())
      .build());
    return slackResponse;
  }

  @Override
  public SlackResponse createSuccessMessage(String message) {
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Success* - " + message).build())
      .build());
    return slackResponse;
  }

  @Override
  public SlackResponse getTorrentResponses(RadarrTorrent radarrTorrent, String movieTitle) {
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Title* - " + radarrTorrent.getTitle()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Torrent - " + radarrTorrent.getGuid()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Quality - " + radarrTorrent.getQuality().getQuality().getName()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Indexer - " + radarrTorrent.getIndexer()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Seeders - " + radarrTorrent.getSeeders()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Leechers - " + radarrTorrent.getLeechers()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Size - " + FileUtils.byteCountToDisplaySize(radarrTorrent.getSize())).build())
      .build());

    String[] rejections = radarrTorrent.getRejections();
    if (rejections != null) {
      List<ContextBlockElement> contextBlockElements = new ArrayList<>();
      for (String rejection : rejections) {
        contextBlockElements.add(PlainTextObject.builder().text("Rejection Reason - " + rejection).build());
      }
      slackResponse.addBlock(ContextBlock.builder()
        .elements(contextBlockElements)
        .build());
    }
    String key = radarrTorrent.getGuid() + ":title=" + movieTitle;
    byte[] encodedBytes = Base64.getEncoder().encode(key.getBytes());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Download hash command - " + "movie hash download " + new String(encodedBytes)).build())
      .build());
    return slackResponse;
  }

  @Override
  public SlackResponse getShowProfile(SonarrProfile sonarrProfile) {
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Profile*").build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Name - " + sonarrProfile.getName()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Cutoff - " + sonarrProfile.getCutoff().getName()).build())
      .build());

    List<ContextBlockElement> contextBlockElements = new ArrayList<>();
    for (int k = 0; k < sonarrProfile.getItems().size(); k++) {
      SonarrProfileQualityItem sonarrProfileQualityItem = sonarrProfile.getItems().get(k);
      if (sonarrProfileQualityItem.isAllowed()) {
        contextBlockElements.add(PlainTextObject.builder()
          .text("Quality - name=" + sonarrProfileQualityItem.getQuality().getName() + ", resolution=" + sonarrProfileQualityItem.getQuality().getResolution())
          .build());
      }
    }
    slackResponse.addBlock(ContextBlock.builder()
      .elements(contextBlockElements)
      .build());
    return slackResponse;
  }

  @Override
  public SlackResponse getMovieProfile(RadarrProfile radarrProfile) {
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Profile*").build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Name - " + radarrProfile.getName()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("Cutoff - " + radarrProfile.getCutoff().getName()).build())
      .build());

    List<ContextBlockElement> contextBlockElements = new ArrayList<>();
    for (int k = 0; k < radarrProfile.getItems().size(); k++) {
      RadarrProfileQualityItem radarrProfileQualityItem = radarrProfile.getItems().get(k);
      if (radarrProfileQualityItem.isAllowed()) {
        contextBlockElements.add(PlainTextObject.builder()
          .text("Quality - name=" + radarrProfileQualityItem.getQuality().getName() + ", resolution=" + radarrProfileQualityItem.getQuality().getResolution())
          .build());
      }
    }
    slackResponse.addBlock(ContextBlock.builder()
      .elements(contextBlockElements)
      .build());
    return slackResponse;
  }

  @Override
  public SlackResponse getNewOrExistingShow(SonarrShow sonarrShow, SonarrShow existingShow, boolean findNew) {
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Title* - " + sonarrShow.getTitle()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("TvdbId - " + sonarrShow.getTvdbId()).build())
      .build());
    if (findNew) {
      slackResponse.addBlock(SectionBlock.builder()
        .text(MarkdownTextObject.builder().text(ADD_SHOW_COMMAND_FIELD_PREFIX + " - " + "show id add " + sonarrShow.getTitle() + " " + sonarrShow.getTvdbId()).build())
        .build());
    } else {
      slackResponse.addBlock(SectionBlock.builder()
        .text(MarkdownTextObject.builder().text("Id - " + existingShow.getId()).build())
        .build());
      if (existingShow.getSeasons() != null) {
        List<ContextBlockElement> contextBlockElements = new ArrayList<>();
        contextBlockElements.add(PlainTextObject.builder()
          .text("Number of seasons - name=" + existingShow.getSeasons().size())
          .build());
        for (SonarrSeason sonarrSeason : existingShow.getSeasons()) {
          contextBlockElements.add(PlainTextObject.builder()
            .text("Season#" + sonarrSeason.getSeasonNumber() +
              ",Available Epsiodes=" + sonarrSeason.getStatistics().getEpisodeCount() + ",Total Epsiodes=" + sonarrSeason.getStatistics().getTotalEpisodeCount())
            .build());
        }

        slackResponse.addBlock(ContextBlock.builder()
          .elements(contextBlockElements)
          .build());
      }
    }
    slackResponse.addBlock(ImageBlock.builder()
      .imageUrl(sonarrShow.getRemotePoster())
      .altText(sonarrShow.getTitle() + " poster")
      .build());
    return slackResponse;
  }

  @Override
  public SlackResponse getNewOrExistingMovie(RadarrMovie lookupMovie, RadarrMovie existingMovie, boolean findNew) {
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Title* - " + lookupMovie.getTitle()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("TmdbId - " + lookupMovie.getTmdbId()).build())
      .build());
    if (findNew) {
      slackResponse.addBlock(SectionBlock.builder()
        .text(MarkdownTextObject.builder().text(ADD_MOVIE_COMMAND_FIELD_PREFIX + " - " + "movie id add " + lookupMovie.getTitle() + " " + lookupMovie.getTmdbId()).build())
        .build());
    } else {
      slackResponse.addBlock(SectionBlock.builder()
        .text(MarkdownTextObject.builder().text("Id - " + existingMovie.getId()).build())
        .build());
      slackResponse.addBlock(SectionBlock.builder()
        .text(MarkdownTextObject.builder().text("Downloaded - " + existingMovie.isDownloaded()).build())
        .build());
      slackResponse.addBlock(SectionBlock.builder()
        .text(MarkdownTextObject.builder().text("Has File - " + existingMovie.isHasFile()).build())
        .build());
    }
    slackResponse.addBlock(ImageBlock.builder()
      .imageUrl(lookupMovie.getRemotePoster())
      .altText(lookupMovie.getTitle() + " poster")
      .build());
    return slackResponse;
  }

  @Override
  public SlackResponse getMovie(RadarrMovie radarrMovie) {
    SlackResponse slackResponse = new SlackResponse();
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("*Title* - " + radarrMovie.getTitle()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text("TmdbId - " + radarrMovie.getTmdbId()).build())
      .build());
    slackResponse.addBlock(SectionBlock.builder()
      .text(MarkdownTextObject.builder().text(ADD_MOVIE_COMMAND_FIELD_PREFIX + " - " + "movie id add " + radarrMovie.getTitle() + " " + radarrMovie.getTmdbId()).build())
      .build());
    if (!Strings.isBlank(radarrMovie.getRemotePoster())) {
      //if there is no poster to display, slack will fail to render all the blocks
      //so make sure there is one before trying to render
      slackResponse.addBlock(ImageBlock.builder()
        .imageUrl(radarrMovie.getRemotePoster())
        .altText(radarrMovie.getTitle() + " poster")
        .build());
    }
    return slackResponse;
  }

  @Override
  public SlackResponse getDiscoverableMovies(RadarrMovie radarrMovie) {
    return getMovie(radarrMovie);
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
