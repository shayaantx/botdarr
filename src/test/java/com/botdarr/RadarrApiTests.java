package com.botdarr;

import com.botdarr.api.RadarrApi;
import com.botdarr.api.radarr.RadarrMovie;
import com.botdarr.api.radarr.RadarrProfile;
import com.botdarr.api.radarr.RadarrQueue;
import com.botdarr.api.radarr.RadarrTorrent;
import com.botdarr.api.sonarr.SonarrProfile;
import com.botdarr.api.sonarr.SonarrQueue;
import com.botdarr.api.sonarr.SonarrShow;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.Command;
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
import java.util.List;
import java.util.Properties;

public class RadarrApiTests {
  @Before
  public void beforeEachTest() throws Exception {
    File propertiesFile = new File(temporaryFolder.getRoot(), "properties");
    Deencapsulation.setField(Config.class, "propertiesPath", propertiesFile.getPath());
    Properties properties = new Properties();
    properties.setProperty("discord-token", "G$K$GK");
    properties.setProperty("discord-channels", "plex-testing2");
    properties.setProperty("radarr-url", "http://localhost:" + mockServerRule.getPort());
    properties.setProperty("radarr-token", "FSJDkjmf#$Kf3");
    properties.setProperty("radarr-path", "/movies");
    properties.setProperty("radarr-default-profile", "any");
    try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
      properties.store(fos, "");
    }
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
    Assert.assertEquals(expectedRadarrMovie.getTitle(), testResponses.get(0).radarrMovie.getTitle());
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
    Assert.assertEquals(20, testResponses.size());
    //verify the first message is a message about the fact too many movies were returned by the server
    Assert.assertEquals(testResponses.get(0).responseMessage, "Too many movies found, please narrow search");
  }

  private static class TestResponse implements ChatClientResponse {
    private TestResponse() {}
    private TestResponse(RadarrMovie radarrMovie) {
      this.radarrMovie = radarrMovie;
    }
    private TestResponse(String responseMessage) {
      this.responseMessage = responseMessage;
    }
    private String responseMessage;
    private RadarrMovie radarrMovie;
  }

  private static class TestResponseBuilder implements ChatClientResponseBuilder<TestResponse> {

    @Override
    public TestResponse getHelpResponse() {
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
    public TestResponse getShowDownloadResponses(SonarrQueue sonarrShow) {
      return new TestResponse();
    }

    @Override
    public TestResponse getMovieDownloadResponses(RadarrQueue radarrQueue) {
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
    public TestResponse getTorrentResponses(RadarrTorrent radarrTorrent, String movieTitle) {
      return new TestResponse();
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
    public TestResponse getMovie(RadarrMovie radarrMovie) {
      return new TestResponse(radarrMovie);
    }

    @Override
    public TestResponse getDiscoverableMovies(RadarrMovie radarrMovie) {
      return new TestResponse(radarrMovie);
    }
  }

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
}
