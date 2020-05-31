package com.botdarr.api;

import com.botdarr.TestResponse;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import mockit.*;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AddStrategyTests {
  @BeforeClass
  public static void beforeAllTests() {
    new MockUp<ApiRequests>() {
      @Mock
      public int getMaxResultsToShow() {
        //mock out the max results to show which we will override in each test
        return 0;
      }
    };
  }

  @Test
  public void addWithSearchId_noMoviesFound_errorResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    String searchId = "searchId1";
    new Expectations(mockAddStrategy) {{
      logger.warn("Search text " + searchText + "yielded no movies, trying id");
      mockAddStrategy.lookupContent(searchText); times = 1; result = Collections.emptyList();
      logger.warn("Search id " + searchId + "yielded no movies, stopping");
      mockAddStrategy.lookupItemById(searchId); times = 1; result = Collections.emptyList();
      chatClientResponseBuilder.createErrorMessage("No movies found"); times = 1; result = new TestResponse("");
    }};
    Assert.assertNotNull(mockAddStrategy.addWithSearchId(searchText, searchId));
  }

  @Test
  public void addWithSearchId_moviesFound_movieExistsAlready_errorResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    String searchId = "searchId1";
    Object movie = new Object();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(movie);
      mockAddStrategy.getItemId(movie); times = 1; result = searchId;
      mockAddStrategy.doesItemExist(movie); times = 1; result = true;
      chatClientResponseBuilder.createErrorMessage("movie already exists");
    }};
    Assert.assertNotNull(mockAddStrategy.addWithSearchId(searchText, searchId));
  }

  @Test
  public void addWithSearchId_moviesFound_itemIdsDontMatch_errorResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    String searchId = "searchId1";
    Object movie = new Object();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(movie);
      mockAddStrategy.getItemId(movie); times = 1; result = "unknownId";
      chatClientResponseBuilder.createErrorMessage("Could not find movie with search text=" + searchText + " and id=" + searchId);
    }};
    Assert.assertNotNull(mockAddStrategy.addWithSearchId(searchText, searchId));
  }

  @Test
  public void addWithSearchId_moviesFound_addMovieResponseReturned() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    String searchId = "searchId1";
    Object movie = new Object();
    TestResponse expectedResponse = new TestResponse();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(movie);
      mockAddStrategy.getItemId(movie); times = 1; result = searchId;
      mockAddStrategy.doesItemExist(movie); times = 1; result = false;
      mockAddStrategy.addContent(movie); times = 1; result = expectedResponse;
      mockAddStrategy.cacheContent(movie); times = 1;
    }};
    ChatClientResponse chatClientResponse = mockAddStrategy.addWithSearchId(searchText, searchId);
    Assert.assertNotNull(chatClientResponse);
    Assert.assertEquals(expectedResponse, chatClientResponse);
  }

  @Test
  public void addWithSearchId_exceptionThrown_errorResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    String searchId = "searchId1";
    Exception expectedException = new Exception("expected error");
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = expectedException;
      logger.error("Error trying to add movie", expectedException); times = 1;
      chatClientResponseBuilder.createErrorMessage("Error adding content, e=expected error"); times = 1; result = new TestResponse();
    }};
    Assert.assertNotNull(mockAddStrategy.addWithSearchId(searchText, searchId));
  }

  @Test
  public void addWithSearchTitle_noMoviesFound_errorResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Collections.emptyList();
      chatClientResponseBuilder.createInfoMessage("No movies found"); times = 1; result = new TestResponse();
    }};
    Assert.assertNotNull(mockAddStrategy.addWithSearchTitle(searchText));
  }

  @Test
  public void addWithSearchTitle_oneMovieFound_alreadyExists_errorResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    Object foundMovie = new Object();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(foundMovie);
      mockAddStrategy.doesItemExist(foundMovie); times = 1; result = true;
      chatClientResponseBuilder.createErrorMessage("movie already exists"); times = 1; result = new TestResponse();
    }};
    List<ChatClientResponse> responses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(responses);
    Assert.assertEquals(1, responses.size());
  }

  @Test
  public void addWithSearchTitle_oneMovieFound_doesntExist_addMovieResponseReturned() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    Object foundMovie = new Object();
    TestResponse expectedResponse = new TestResponse();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(foundMovie);
      mockAddStrategy.doesItemExist(foundMovie); times = 1; result = false;
      mockAddStrategy.addContent(foundMovie); times = 1; result = expectedResponse;
    }};
    List<ChatClientResponse> responses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(responses);
    Assert.assertEquals(1, responses.size());
    Assert.assertEquals(expectedResponse, responses.get(0));
  }

  @Test
  public void addWithSearchTitle_multipleMoviesFound_lessThanMaxResults_multipleAddMovieResponsesReturned() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";

    Object foundMovie = new Object();
    TestResponse foundMovieResponse = new TestResponse();

    Object foundMovie2 = new Object();
    TestResponse foundMovieResponse2 = new TestResponse();

    TestResponse expectedResponseInfoMessage = new TestResponse();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(foundMovie, foundMovie2);
      mockAddStrategy.doesItemExist(foundMovie); times = 1; result = false;
      mockAddStrategy.getResponse(foundMovie); times = 1; result = foundMovieResponse;
      mockAddStrategy.doesItemExist(foundMovie2); times = 1; result = false;
      mockAddStrategy.getResponse(foundMovie2); times = 1; result = foundMovieResponse2;
      chatClientResponseBuilder.createInfoMessage("Too many movies found, please narrow search or increase max results to show"); times = 1; result = expectedResponseInfoMessage;
    }};
    List<ChatClientResponse> responses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(responses);
    Assert.assertEquals(3, responses.size());
    Assert.assertEquals(expectedResponseInfoMessage, responses.get(0));
    Assert.assertEquals(foundMovieResponse, responses.get(1));
    Assert.assertEquals(foundMovieResponse2, responses.get(2));
  }

  @Test
  public void addWithSearchTitle_multipleMoviesFound_moreThanMaxResults_multipleAddMovieResponsesReturned() throws Exception {
    //only allow one max result to show
    MockAddStrategy mockAddStrategy = getMockAddStrategy(1);
    String searchText = "searchText1";

    Object foundMovie = new Object();
    TestResponse foundMovieResponse = new TestResponse();

    Object foundMovie2 = new Object();
    TestResponse foundMovieResponse2 = new TestResponse();

    TestResponse expectedResponseInfoMessage = new TestResponse();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(foundMovie, foundMovie2);
      mockAddStrategy.doesItemExist(foundMovie); times = 1; result = false;
      mockAddStrategy.getResponse(foundMovie); times = 1; result = foundMovieResponse;
      mockAddStrategy.doesItemExist(foundMovie2); times = 1; result = false;
      mockAddStrategy.getResponse(foundMovie2); times = 1; result = foundMovieResponse2;
      chatClientResponseBuilder.createInfoMessage("Too many movies found, please narrow search or increase max results to show"); times = 1; result = expectedResponseInfoMessage;
    }};
    List<ChatClientResponse> responses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(responses);
    Assert.assertEquals(2, responses.size());
    Assert.assertEquals(expectedResponseInfoMessage, responses.get(0));
    Assert.assertEquals(foundMovieResponse, responses.get(1));
  }

  @Test
  public void addWithSearchTitle_multipleMoviesFound_allPreviouslyExist_infoResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    Object foundMovie = new Object();
    Object foundMovie2 = new Object();
    TestResponse expectedResponseInfoMessage = new TestResponse();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(foundMovie, foundMovie2);
      mockAddStrategy.doesItemExist(any); times = 2; result = true;
      chatClientResponseBuilder.createInfoMessage("No new movies found, check existing movies"); times = 1; result = expectedResponseInfoMessage;
    }};
    List<ChatClientResponse> responses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(responses);
    Assert.assertEquals(1, responses.size());
    Assert.assertEquals(expectedResponseInfoMessage, responses.get(0));
  }

  @Test
  public void addWithSearchTitle_exceptionThrown_errorResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    Exception expectedException = new Exception("expected error");
    TestResponse expectedResponseErrorMessage = new TestResponse();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = expectedException;
      logger.error("Error trying to add movie", expectedException); times = 1;
      chatClientResponseBuilder.createErrorMessage("Error trying to add movie, for search text=" + searchText + ", e=expected error"); times = 1; result = expectedResponseErrorMessage;
    }};
    List<ChatClientResponse> responses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(responses);
    Assert.assertEquals(1, responses.size());
    Assert.assertEquals(expectedResponseErrorMessage, responses.get(0));
  }

  private MockAddStrategy getMockAddStrategy(int maxResultsToShow) {
    MockAddStrategy mockAddStrategy = new MockAddStrategy(chatClientResponseBuilder, ContentType.MOVIE);
    Deencapsulation.setField(mockAddStrategy, "LOGGER", logger);
    Deencapsulation.setField(mockAddStrategy, "MAX_RESULTS_TO_SHOW", maxResultsToShow);
    return mockAddStrategy;
  }

  private MockAddStrategy getMockAddStrategy() {
    return getMockAddStrategy(5);
  }

  @Mocked
  private Logger logger;

  @Injectable
  private ChatClientResponseBuilder<TestResponse> chatClientResponseBuilder;

  private static class MockAddStrategy extends AddStrategy<Object> {
    public MockAddStrategy(ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder, ContentType contentType) {
      super(chatClientResponseBuilder, contentType);
    }

    @Override
    public List<Object> lookupContent(String search) throws Exception {
      return null;
    }

    @Override
    public List<Object> lookupItemById(String id) throws Exception {
      return null;
    }

    @Override
    public boolean doesItemExist(Object content) {
      return false;
    }

    @Override
    public String getItemId(Object item) {
      return null;
    }

    @Override
    public ChatClientResponse addContent(Object content) {
      return null;
    }

    @Override
    public ChatClientResponse getResponse(Object item) {
      return null;
    }

    @Override
    protected void cacheContent(Object addContent) {

    }
  }
}
