package com.botdarr;

import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.responses.*;

public class TestResponseBuilder implements ChatClientResponseBuilder<TestClientResponse> {

  @Override
  public TestClientResponse build(HelpResponse helpResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(MusicHelpResponse musicHelpResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(MoviesHelpResponse moviesHelpResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(ShowsHelpResponse showsHelpResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(ShowResponse showResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(MusicArtistResponse musicArtistResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(MovieResponse movieResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(ShowDownloadResponse showDownloadResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(MovieDownloadResponse movieDownloadResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(MusicArtistDownloadResponse musicArtistDownloadResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(ErrorResponse errorResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(InfoResponse infoResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(SuccessResponse successResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(ShowProfileResponse showProfileResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(MovieProfileResponse movieProfileResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(NewShowResponse newShowResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(ExistingShowResponse existingShowResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(NewMovieResponse newMovieResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(ExistingMovieResponse existingMovieResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(NewMusicArtistResponse newMusicArtistResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(ExistingMusicArtistResponse exitingMusicArtistResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(DiscoverMovieResponse discoverMovieResponse) {
    return new TestClientResponse();
  }

  @Override
  public TestClientResponse build(StatusCommandResponse statusCommandResponse) {
    return new TestClientResponse();
  }
}