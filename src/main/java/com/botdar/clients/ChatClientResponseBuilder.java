package com.botdar.clients;

import com.botdar.api.radarr.RadarrMovie;
import com.botdar.api.radarr.RadarrProfile;
import com.botdar.api.radarr.RadarrQueue;
import com.botdar.api.radarr.RadarrTorrent;
import com.botdar.api.sonarr.SonarrProfile;
import com.botdar.api.sonarr.SonarrQueue;
import com.botdar.api.sonarr.SonarrShow;
import com.botdar.commands.Command;

import java.util.List;

public interface ChatClientResponseBuilder<T extends ChatClientResponse> {
  T getHelpResponse();
  T getMoviesHelpResponse(List<Command> radarrCommands);
  T getShowsHelpResponse(List<Command> sonarrCommands);
  T getShowResponse(SonarrShow show);
  T getShowDownloadResponses(SonarrQueue sonarrShow);
  T getMovieDownloadResponses(RadarrQueue radarrQueue);
  T createErrorMessage(String message);
  T createInfoMessage(String message);
  T createSuccessMessage(String message);
  T getTorrentResponses(RadarrTorrent radarrTorrent, String movieTitle);
  T getShowProfile(SonarrProfile sonarrProfile);
  T getMovieProfile(RadarrProfile radarrProfile);
  T getNewOrExistingShow(SonarrShow sonarrShow, SonarrShow existingShow, boolean findNew);
  T getNewOrExistingMovie(RadarrMovie lookupMovie, RadarrMovie existingMovie, boolean findNew);
  T getMovie(RadarrMovie radarrMovie);
  T getDiscoverableMovies(RadarrMovie radarrMovie);
}
