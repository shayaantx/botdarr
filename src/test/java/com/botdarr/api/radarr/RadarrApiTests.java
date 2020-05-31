package com.botdarr.api.radarr;

import com.botdarr.Config;
import com.botdarr.TestResponse;
import com.botdarr.TestResponseBuilder;
import com.botdarr.api.radarr.*;
import com.botdarr.commands.CommandResponse;
import com.google.gson.Gson;
import mockit.Deencapsulation;
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
  public <T extends TestResponse> void discover_existingMoviesNotReturned() {
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/movies/discover/recommendations")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3");

    RadarrMovie expectedRadarrMovie = new RadarrMovie();
    expectedRadarrMovie.setTitle("movie1");

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(new RadarrMovie[] {expectedRadarrMovie}), MediaType.APPLICATION_JSON));

    //trigger api
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.discover());

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    Assert.assertEquals(testResponses.size(), 1);
    Assert.assertEquals(expectedRadarrMovie.getTitle(), testResponses.get(0).getRadarrMovie().getTitle());
  }

  @Test
  public <T extends TestResponse> void discover_maxResults() {
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/movies/discover/recommendations")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3");

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
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.discover());

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    //verify the max (20) even though the mock server returned 40 (see above)
    Assert.assertEquals(20, testResponses.size());
  }

  @Test
  public <T extends TestResponse> void lookup_maxResults() {
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/movie/lookup")
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
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.lookup("searchTerm", true));

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    //verify the max (20) even though the mock server returned 40 (see above)
    Assert.assertEquals(21, testResponses.size());
    //verify the first message is a message about the fact too many movies were returned by the server
    Assert.assertEquals(testResponses.get(0).getResponseMessage(), "Too many movies found, limiting results to 20");
  }

  @Test
  public <T extends TestResponse> void lookup_existingMoviesNotReturned() {
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/movie/lookup")
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
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.lookup("searchTerm", true));

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    //there should only be 1 response stating no new movies could be found
    Assert.assertEquals(1, testResponses.size());
    Assert.assertEquals("Could not find any new movies for search term=searchTerm", testResponses.get(0).getResponseMessage());
  }

  @Test
  public <T extends TestResponse> void lookup_existingMoviesReturned() {
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/movie/lookup")
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
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.lookup("searchTerm", false));

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    //since we looking up not new films, existing movies can be returned, and should be the only result
    Assert.assertEquals(1, testResponses.size());
    Assert.assertEquals(expectedRadarrMovie.getTitle(), testResponses.get(0).getRadarrMovie().getTitle());
    Assert.assertEquals(expectedRadarrMovie.getTmdbId(), testResponses.get(0).getRadarrMovie().getTmdbId());
  }

  @Test
  public <T extends TestResponse> void downloads_noDownloadsFound() {
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/queue")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3");

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(new RadarrQueue[] {}), MediaType.APPLICATION_JSON));

    //trigger api
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.downloads());

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    //since nothing is downloading we should only get back 1 response with a message about no downloads
    Assert.assertEquals(1, testResponses.size());
    Assert.assertEquals("No movies downloading", testResponses.get(0).getResponseMessage());
  }

  @Test
  public <T extends TestResponse> void downloads_downloadsFound() {
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/queue")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3");

    RadarrQueue radarrQueue = new RadarrQueue();
    radarrQueue.setId(1);
    radarrQueue.setTimeleft("05:00");
    radarrQueue.setStatus("DOWNLOADING");

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(new RadarrQueue[] {radarrQueue}), MediaType.APPLICATION_JSON));

    //trigger api
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.downloads());

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    //only movie is downloading, verify all properties
    Assert.assertEquals(1, testResponses.size());
    Assert.assertEquals(1, testResponses.get(0).getRadarrQueue().getId());
    Assert.assertEquals("05:00", testResponses.get(0).getRadarrQueue().getTimeleft());
    Assert.assertEquals("DOWNLOADING", testResponses.get(0).getRadarrQueue().getStatus());
  }

  @Test
  public <T extends TestResponse> void addWithTitle_noMoviesFound() {
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/movie/lookup")
      .withQueryStringParameter("apiKey", "FSJDkjmf#$Kf3")
      .withQueryStringParameter("term", "searchTerm");

    //setup expected response in mock server
    mockServerRule.getClient()
      .when(request)
      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(new Gson().toJson(new RadarrMovie[] {}), MediaType.APPLICATION_JSON));

    //trigger api
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.addWithTitle("searchTerm"));

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    //no movies should be found, the only response should be a message
    Assert.assertEquals(1, testResponses.size());
    Assert.assertEquals("No movies found", testResponses.get(0).getResponseMessage());
  }

  @Test
  public <T extends TestResponse> void addWithTitle_foundExistingMovieWithSingleResult() {
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/movie/lookup")
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
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.addWithTitle("searchTerm"));

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    //no movies should be found, the only response should be a message
    Assert.assertEquals(1, testResponses.size());
    Assert.assertEquals("movie already exists", testResponses.get(0).getResponseMessage());
  }

  @Test
  public <T extends TestResponse> void addWithTitle_foundExistingMovieWithMultipleResults() {
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/movie/lookup")
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
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.addWithTitle("movie1"));

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    //no movies should be found, the only response should be a message
    Assert.assertEquals(1, testResponses.size());
    Assert.assertEquals("No new movies found, check existing movies", testResponses.get(0).getResponseMessage());
  }

  @Test
  public <T extends TestResponse> void addWithTitle_foundManyResultsLimitedToMax() {
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    HttpRequest request = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/movie/lookup")
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
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.addWithTitle("movie"));

    //verify request was sent
    mockServerRule.getClient().verify(request);

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    //even though we sent 30 movies, the default api limit is 20
    //with the first message being a message about too many movies found added to the list (making the length 21)
    Assert.assertEquals(21, testResponses.size());
    Assert.assertEquals("Too many movies found, please narrow search or increase max results to show", testResponses.get(0).getResponseMessage());
  }

  @Test
  public <T extends TestResponse> void downloads_downloadsFoundButConfiguredToShowNone() {
    Properties properties = getDefaultProperties();
    properties.put("max-downloads-to-show", "0");
    writeFakePropertiesFile(properties);
    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());

    //trigger api
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.downloads());

    //verify response data
    List<TestResponse> testResponses = commandResponse.getMultipleChatClientResponses();
    //only movie is downloading, but our config explicitly states no downloads should be shown
    Assert.assertEquals(0, testResponses.size());
  }

  @Test
  public <T extends TestResponse> void addWithId_movieAlreadyExists() {
    RadarrMovie existingMovie = getRadarrMovie(1, 2, "movie1");
    List<RadarrMovie> radarrMovies = new ArrayList<>();
    radarrMovies.add(existingMovie);

    RadarrApi radarrApi = new RadarrApi(new TestResponseBuilder());
    RadarrCache radarrCache = Deencapsulation.getField(radarrApi, "RADARR_CACHE");
    radarrCache.add(existingMovie);

    HttpRequest titleRequest = HttpRequest.request()
      .withMethod("GET")
      .withPath("/api/movie/lookup")
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
    CommandResponse<TestResponse> commandResponse = new CommandResponse(radarrApi.addWithId("movie1", "2"));

    //verify response data
    TestResponse testResponses = commandResponse.getSingleChatClientResponse();
    Assert.assertEquals("movie already exists", testResponses.getResponseMessage());
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
