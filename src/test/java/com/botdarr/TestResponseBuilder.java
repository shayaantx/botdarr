package com.botdarr;

import com.botdarr.api.lidarr.LidarrArtist;
import com.botdarr.api.lidarr.LidarrQueueRecord;
import com.botdarr.api.radarr.RadarrMovie;
import com.botdarr.api.radarr.RadarrProfile;
import com.botdarr.api.radarr.RadarrQueue;
import com.botdarr.api.sonarr.SonarrProfile;
import com.botdarr.api.sonarr.SonarrQueue;
import com.botdarr.api.sonarr.SonarrShow;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.Command;

import java.util.List;

public class TestResponseBuilder implements ChatClientResponseBuilder<TestResponse> {

  @Override
  public TestResponse getHelpResponse() {
    return new TestResponse();
  }

  @Override
  public TestResponse getMusicHelpResponse(List<Command> lidarCommands) {
    return new TestResponse();
  }

  @Override
  public TestResponse getMoviesHelpResponse(List<Command> radarrCommands) {
    return new TestResponse();
  }

  @Override
  public TestResponse getShowsHelpResponse(List<Command> sonarrCommands) {
    return new TestResponse();
  }

  @Override
  public TestResponse getShowResponse(SonarrShow show) {
    return new TestResponse();
  }

  @Override
  public TestResponse getArtistResponse(LidarrArtist lidarrArtist) {
    return new TestResponse();
  }

  @Override
  public TestResponse getShowDownloadResponses(SonarrQueue sonarrShow) {
    return new TestResponse();
  }

  @Override
  public TestResponse getMovieDownloadResponses(RadarrQueue radarrQueue) {
    return new TestResponse(radarrQueue);
  }

  @Override
  public TestResponse getArtistDownloadResponses(LidarrQueueRecord lidarrQueueRecord) {
    return new TestResponse();
  }

  @Override
  public TestResponse createErrorMessage(String message) {
    return new TestResponse(message);
  }

  @Override
  public TestResponse createInfoMessage(String message) {
    return new TestResponse(message);
  }

  @Override
  public TestResponse createSuccessMessage(String message) {
    return new TestResponse(message);
  }

  @Override
  public TestResponse getShowProfile(SonarrProfile sonarrProfile) {
    return new TestResponse();
  }

  @Override
  public TestResponse getMovieProfile(RadarrProfile radarrProfile) {
    return new TestResponse();
  }

  @Override
  public TestResponse getNewOrExistingShow(SonarrShow sonarrShow, SonarrShow existingShow, boolean findNew) {
    return new TestResponse();
  }

  @Override
  public TestResponse getNewOrExistingMovie(RadarrMovie lookupMovie, RadarrMovie existingMovie, boolean findNew) {
    return new TestResponse(lookupMovie);
  }

  @Override
  public TestResponse getNewOrExistingArtist(LidarrArtist lookupArtist, LidarrArtist existingArtist, boolean findNew) {
    return new TestResponse();
  }

  @Override
  public TestResponse getMovieResponse(RadarrMovie radarrMovie) {
    return new TestResponse(radarrMovie);
  }

  @Override
  public TestResponse getDiscoverableMovies(RadarrMovie radarrMovie) {
    return new TestResponse(radarrMovie);
  }
}