package com.botdarr.api.radarr;

import com.botdarr.Config;
import com.botdarr.TestCommandResponse;
import com.botdarr.commands.responses.*;
import com.google.gson.Gson;
import mockit.Deencapsulation;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RadarrApiTests {
  @Before
  public void beforeEachTest() {
    writeFakePropertiesFile(getDefaultProperties());
  }

  @After
  public void afterEachTest() {
    mockServerRule.getClient().reset();
  }

  @Test
  public <T extends TestCommandResponse> void discover_existingMoviesNotReturned() {
    RadarrApi radarrApi = new RadarrApi();
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/importlist/movie")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3")
      .withQueryStringParameter("includeRecommendations", "true");

    RadarrMovie expectedRadarrMovie = new RadarrMovie();
    expectedRadarrMovie.setTitle("movie1");

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(new RadarrMovie[] {expectedRadarrMovie}), MediaType.APPLICATION_JSON));

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.discover();

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    Assert.assertEquals(commandResponse.size(), 1);
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new DiscoverMovieResponse(expectedRadarrMovie),
                    commandResponse.get(0), false, null, true));
  }

  @Test
  public <T extends TestCommandResponse> void discover_maxResults() {
    RadarrApi radarrApi = new RadarrApi();
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/importlist/movie")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3")
      .withQueryStringParameter("includeRecommendations", "true");

    RadarrMovie[] radarrMovies = new RadarrMovie[40];
    for (int i = 0; i < radarrMovies.length; i++) {
      RadarrMovie expectedRadarrMovie = new RadarrMovie();
      expectedRadarrMovie.setTitle("movie1");
      radarrMovies[i] = expectedRadarrMovie;
    }

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(radarrMovies), MediaType.APPLICATION_JSON));

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.discover();

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    //verify the max (20) even though the mock server returned 40 (see above)
    Assert.assertEquals(20, commandResponse.size());
  }

  @Test
  public <T extends TestCommandResponse> void lookup_maxResults() {
    RadarrApi radarrApi = new RadarrApi();
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/movie/lookup")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3")
      .withQueryStringParameter("term", "searchTerm");

    RadarrMovie[] radarrMovies = new RadarrMovie[40];
    for (int i = 0; i < radarrMovies.length; i++) {
      RadarrMovie expectedRadarrMovie = new RadarrMovie();
      expectedRadarrMovie.setTitle("movie1");
      radarrMovies[i] = expectedRadarrMovie;
    }

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(radarrMovies), MediaType.APPLICATION_JSON));

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.lookup("searchTerm", true);

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    //verify the max (20) even though the mock server returned 40 (see above)
    Assert.assertEquals(21, commandResponse.size());
    //verify the first message is a message about the fact too many movies were returned by the server
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new InfoResponse("Too many movies found, limiting results to 20"),
                    commandResponse.get(0), false, null, true));
  }

  @Test
  public <T extends TestCommandResponse> void lookup_existingMoviesNotReturned() {
    RadarrApi radarrApi = new RadarrApi();
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/movie/lookup")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3")
      .withQueryStringParameter("term", "searchTerm");

    RadarrMovie expectedRadarrMovie = new RadarrMovie();
    expectedRadarrMovie.setTitle("movie1");
    expectedRadarrMovie.setTmdbId(1);

    RadarrCache radarrCache = Deencapsulation.getField(radarrApi, "RADARR_CACHE");
    radarrCache.add(expectedRadarrMovie);

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(new RadarrMovie[] {expectedRadarrMovie}), MediaType.APPLICATION_JSON));

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.lookup("searchTerm", true);

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    //there should only be 1 response stating no new movies could be found
    Assert.assertEquals(1, commandResponse.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new ErrorResponse("Could not find any new movies for search term=searchTerm"),
                    commandResponse.get(0), false, null, true));
  }

  @Test
  public <T extends TestCommandResponse> void lookup_existingMoviesReturned() {
    RadarrApi radarrApi = new RadarrApi();
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/movie/lookup")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3")
      .withQueryStringParameter("term", "searchTerm");

    RadarrMovie expectedRadarrMovie = new RadarrMovie();
    expectedRadarrMovie.setTitle("movie1");
    expectedRadarrMovie.setTmdbId(1);

    RadarrCache radarrCache = Deencapsulation.getField(radarrApi, "RADARR_CACHE");
    radarrCache.add(expectedRadarrMovie);

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(new RadarrMovie[] {expectedRadarrMovie}), MediaType.APPLICATION_JSON));

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.lookup("searchTerm", false);

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    //since we looking up not new films, existing movies can be returned, and should be the only result
    Assert.assertEquals(1, commandResponse.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new ExistingMovieResponse(expectedRadarrMovie),
                    commandResponse.get(0), false, null, true));
  }

  @Test
  public <T extends TestCommandResponse> void downloads_noDownloadsFound() {
    RadarrApi radarrApi = new RadarrApi();
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/queue")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3");

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(new RadarrQueuePage()), MediaType.APPLICATION_JSON));

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.downloads();

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    //since nothing is downloading we should get back 0 responses
    Assert.assertEquals(0, commandResponse.size());
  }

  @Test
  public <T extends TestCommandResponse> void downloads_downloadsFound() {
    RadarrApi radarrApi = new RadarrApi();
    RadarrCache radarrCache = new RadarrCache();
    Deencapsulation.setField(radarrApi, "RADARR_CACHE", radarrCache);
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/queue")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3");

    long radarrMovieId = 1;
    String radarrMovieTitle = "title1";

    RadarrQueue radarrQueue = new RadarrQueue();
    radarrQueue.setTimeleft("05:00");
    radarrQueue.setStatus("DOWNLOADING");
    radarrQueue.setMovieId(radarrMovieId);
    radarrQueue.setTitle(radarrMovieTitle);

    RadarrMovie existingMovie = new RadarrMovie();
    existingMovie.setId(radarrMovieId);
    existingMovie.setTitle(radarrMovieTitle);
    radarrCache.add(existingMovie);

    //setup expected response in mock server
    RadarrQueuePage radarrQueuePage = new RadarrQueuePage();
    radarrQueuePage.setRecords(new ArrayList<RadarrQueue>() {{add(radarrQueue);}});
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(radarrQueuePage), MediaType.APPLICATION_JSON));

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.downloads();

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    //only movie is downloading, verify all properties
    Assert.assertEquals(1, commandResponse.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new MovieDownloadResponse(radarrQueue),
                    commandResponse.get(0), false, null, true));
  }

  @Test
  public <T extends TestCommandResponse> void addWithTitle_noMoviesFound() {
    RadarrApi radarrApi = new RadarrApi();
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/movie/lookup")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3")
      .withQueryStringParameter("term", "searchTerm");

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(new RadarrMovie[] {}), MediaType.APPLICATION_JSON));

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.addWithTitle("searchTerm");

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    //no movies should be found, the only response should be a message
    Assert.assertEquals(1, commandResponse.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new InfoResponse("No movies found"),
                    commandResponse.get(0), false, null, true));
  }

  @Test
  public <T extends TestCommandResponse> void addWithTitle_foundExistingMovieWithSingleResult() {
    RadarrApi radarrApi = new RadarrApi();
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/movie/lookup")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3")
      .withQueryStringParameter("term", "searchTerm");

    RadarrMovie expectedRadarrMovie = new RadarrMovie();
    expectedRadarrMovie.setTitle("movie1");
    expectedRadarrMovie.setTmdbId(1);

    RadarrCache radarrCache = Deencapsulation.getField(radarrApi, "RADARR_CACHE");
    radarrCache.add(expectedRadarrMovie);

    //setup expected response in mock serverx
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(new RadarrMovie[] {expectedRadarrMovie}), MediaType.APPLICATION_JSON));

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.addWithTitle("searchTerm");

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    //no movies should be found, the only response should be a message
    Assert.assertEquals(1, commandResponse.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new ErrorResponse("movie already exists"),
                    commandResponse.get(0), false, null, true));
  }

  @Test
  public <T extends TestCommandResponse> void addWithTitle_foundExistingMovieWithMultipleResults() {
    RadarrApi radarrApi = new RadarrApi();
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/movie/lookup")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3")
      .withQueryStringParameter("term", "movie1");

    RadarrMovie expectedRadarrMovie = new RadarrMovie();
    expectedRadarrMovie.setTitle("movie1");
    expectedRadarrMovie.setTmdbId(1);

    RadarrMovie expectedRadarrMovie2 = new RadarrMovie();
    expectedRadarrMovie2.setTitle("movie2");
    expectedRadarrMovie2.setTmdbId(2);

    RadarrCache radarrCache = Deencapsulation.getField(radarrApi, "RADARR_CACHE");
    radarrCache.add(expectedRadarrMovie);
    radarrCache.add(expectedRadarrMovie2);

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(new RadarrMovie[] {expectedRadarrMovie, expectedRadarrMovie2}), MediaType.APPLICATION_JSON));

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.addWithTitle("movie1");

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    //no movies should be found, the only response should be a message
    Assert.assertEquals(1, commandResponse.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new InfoResponse("No new movies found, check existing movies"),
                    commandResponse.get(0), false, null, true));
  }

  @Test
  public <T extends TestCommandResponse> void addWithTitle_foundManyResultsLimitedToMax() {
    RadarrApi radarrApi = new RadarrApi();
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/movie/lookup")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3")
      .withQueryStringParameter("term", "movie");
    List<RadarrMovie> radarrMovies = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
      RadarrMovie expectedRadarrMovie = new RadarrMovie();
      expectedRadarrMovie.setTitle("movie" + i);
      expectedRadarrMovie.setTmdbId(i);

      radarrMovies.add(expectedRadarrMovie);
    }

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(radarrMovies.toArray()), MediaType.APPLICATION_JSON));

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.addWithTitle("movie");

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    //even though we sent 30 movies, the default api limit is 20
    //with the first message being a message about too many movies found added to the list (making the length 21)
    Assert.assertEquals(21, commandResponse.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new InfoResponse("Too many movies found, please narrow search or increase max results to show"),
                    commandResponse.get(0), false, null, true));
  }

  @Test
  public <T extends TestCommandResponse> void downloads_downloadsFoundButConfiguredToShowNone() {
    Properties properties = getDefaultProperties();
    properties.put("max-downloads-to-show", "0");
    writeFakePropertiesFile(properties);
    RadarrApi radarrApi = new RadarrApi();

    //trigger api
    List<CommandResponse> commandResponse = radarrApi.downloads();

    //verify response data
    //only movie is downloading, but our config explicitly states no downloads should be shown
    Assert.assertEquals(0, commandResponse.size());
  }

  @Test
  public <T extends TestCommandResponse> void addWithId_movieAlreadyExists() {
    RadarrMovie existingMovie = getRadarrMovie(1, 2, "movie1");
    List<RadarrMovie> radarrMovies = new ArrayList<>();
    radarrMovies.add(existingMovie);

    RadarrApi radarrApi = new RadarrApi();
    RadarrCache radarrCache = Deencapsulation.getField(radarrApi, "RADARR_CACHE");
    radarrCache.add(existingMovie);

    HttpRequest titleRequest = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/v3/movie/lookup")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3")
      .withQueryStringParameter("term", "movie1");

    //setup expected response in mock server for both requests
    HttpResponse expectedResponse = HttpResponse.response()
      .withStatusCode(200)
      .withBody(new Gson().toJson(radarrMovies.toArray()), MediaType.APPLICATION_JSON);
    mockServerRule.getClient()
      .when(titleRequest)
      .respond(expectedResponse);

    //trigger api
    CommandResponse commandResponse = radarrApi.addWithId("movie1", "2");

    //verify response data
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new ErrorResponse("movie already exists"),
                    commandResponse, false, null, true));
  }

  private RadarrMovie getRadarrMovie(long id, long tmdbId, String title) {
    RadarrMovie radarrMovie = new RadarrMovie();
    radarrMovie.setId(id);
    radarrMovie.setTmdbId(tmdbId);
    radarrMovie.setTitle(title);
    return radarrMovie;
  }

  private Properties getDefaultProperties() {
    Properties properties = new Properties();
    properties.setProperty("discord-token", "G$K$GK");
    properties.setProperty("discord-channels", "plex-testing2");
    properties.setProperty("radarr-url", "http://localhost:" + mockServerRule.getPort());
    properties.setProperty("radarr-token", "FSJDkjmf#$Kf3");
    properties.setProperty("radarr-path", "/movies");
    properties.setProperty("radarr-default-profile", "any");
    return properties;
  }

  private void writeFakePropertiesFile(Properties properties) {
    Deencapsulation.setField(Config.class, "instance", null);
    File propertiesFile = null;
    try {
      new File(temporaryFolder.getRoot(), "properties").delete();
      propertiesFile = temporaryFolder.newFile("properties");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Deencapsulation.setField(Config.class, "propertiesPath", propertiesFile.getPath());
    try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
      properties.store(fos, "");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
}
