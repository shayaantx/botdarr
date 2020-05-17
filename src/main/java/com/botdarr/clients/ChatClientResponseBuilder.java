package com.botdarr.clients;

import com.botdarr.api.lidarr.LidarrArtist;
import com.botdarr.api.radarr.RadarrMovie;
import com.botdarr.api.radarr.RadarrProfile;
import com.botdarr.api.radarr.RadarrQueue;
import com.botdarr.api.radarr.RadarrTorrent;
import com.botdarr.api.sonarr.SonarrProfile;
import com.botdarr.api.sonarr.SonarrQueue;
import com.botdarr.api.sonarr.SonarrShow;
import com.botdarr.commands.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public interface ChatClientResponseBuilder<T extends ChatClientResponse> {
  T getHelpResponse();
  T getMusicHelpResponse(List<Command> lidarCommands);
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
  T getNewOrExistingArtist(LidarrArtist lookupArtist, LidarrArtist existingArtist, boolean findNew);
  T getMovie(RadarrMovie radarrMovie);
  T getDiscoverableMovies(RadarrMovie radarrMovie);

  static String getVersion() throws IOException {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream is = classloader.getResourceAsStream("version.txt");
    if (is != null) {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()))) {
        String versionTxt = br.lines().collect(Collectors.joining(System.lineSeparator()));
        return versionTxt;
      }
    }
    return "local";
  }
}
