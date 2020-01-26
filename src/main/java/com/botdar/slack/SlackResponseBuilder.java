package com.botdar.slack;

import com.botdar.api.radarr.RadarrMovie;
import com.botdar.api.radarr.RadarrProfile;
import com.botdar.api.radarr.RadarrQueue;
import com.botdar.api.radarr.RadarrTorrent;
import com.botdar.api.sonarr.SonarrProfile;
import com.botdar.api.sonarr.SonarrQueue;
import com.botdar.api.sonarr.SonarrShow;
import com.botdar.clients.ChatClientResponseBuilder;
import com.botdar.commands.Command;

import java.util.List;

public class SlackResponseBuilder implements ChatClientResponseBuilder<SlackResponse> {
  @Override
  public SlackResponse getHelpResponse() {
    return null;
  }

  @Override
  public SlackResponse getMoviesHelpResponse(List<Command> radarrCommands) {
    return null;
  }

  @Override
  public SlackResponse getShowsHelpResponse(List<Command> sonarrCommands) {
    return null;
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
}
