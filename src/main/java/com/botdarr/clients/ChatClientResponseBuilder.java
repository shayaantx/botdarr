package com.botdarr.clients;

import com.botdarr.commands.responses.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

public interface ChatClientResponseBuilder<T extends ChatClientResponse> {
  T build(HelpResponse helpResponse);
  T build(MusicHelpResponse musicHelpResponse);
  T build(MoviesHelpResponse moviesHelpResponse);
  T build(ShowsHelpResponse showsHelpResponse);
  T build(ShowResponse showResponse);
  T build(MusicArtistResponse musicArtistResponse);
  T build(MovieResponse movieResponse);
  T build(ShowDownloadResponse showDownloadResponse);
  T build(MovieDownloadResponse movieDownloadResponse);
  T build(MusicArtistDownloadResponse musicArtistDownloadResponse);
  T build(ErrorResponse errorResponse);
  T build(InfoResponse infoResponse);
  T build(SuccessResponse successResponse);
  T build(ShowProfileResponse showProfileResponse);
  T build(MovieProfileResponse movieProfileResponse);
  T build(NewShowResponse newShowResponse);
  T build(ExistingShowResponse existingShowResponse);
  T build(NewMovieResponse newMovieResponse);
  T build(ExistingMovieResponse existingMovieResponse);
  T build(NewMusicArtistResponse newMusicArtistResponse);
  T build(ExistingMusicArtistResponse exitingMusicArtistResponse);
  T build(DiscoverMovieResponse discoverMovieResponse);
  T build(StatusCommandResponse statusCommandResponse);

  static String getVersion() throws IOException {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream is = classloader.getResourceAsStream("version.txt");
    if (is != null) {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()))) {
        return br.lines().collect(Collectors.joining(System.lineSeparator()));
      }
    }
    return "local";
  }
}
