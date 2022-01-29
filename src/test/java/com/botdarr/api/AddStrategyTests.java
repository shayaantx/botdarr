package com.botdarr.api;

import com.botdarr.TestCommandResponse;
import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.ErrorResponse;
import com.botdarr.commands.responses.InfoResponse;
import mockit.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
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
    }};
    CommandResponse commandResponse = mockAddStrategy.addWithSearchId(searchText, searchId);
    Assert.assertNotNull(commandResponse);
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new ErrorResponse("No movies found"),
                    commandResponse));
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
    }};
    CommandResponse commandResponse = mockAddStrategy.addWithSearchId(searchText, searchId);
    Assert.assertNotNull(commandResponse);
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new ErrorResponse("movie already exists"),
                    commandResponse));
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
    }};
    CommandResponse commandResponse = mockAddStrategy.addWithSearchId(searchText, searchId);
    Assert.assertNotNull(commandResponse);
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new ErrorResponse("Could not find movie with search text=" + searchText + " and id=" + searchId),
                    commandResponse));
  }

  @Test
  public void addWithSearchId_moviesFound_addMovieResponseReturned() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    String searchId = "searchId1";
    Object movie = new Object();
    TestCommandResponse expectedResponse = new TestCommandResponse();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(movie);
      mockAddStrategy.getItemId(movie); times = 1; result = searchId;
      mockAddStrategy.doesItemExist(movie); times = 1; result = false;
      mockAddStrategy.addContent(movie); times = 1; result = expectedResponse;
    }};
    CommandResponse commandResponse = mockAddStrategy.addWithSearchId(searchText, searchId);
    Assert.assertNotNull(commandResponse);
    Assert.assertEquals(expectedResponse, commandResponse);
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
    }};
    CommandResponse commandResponse = mockAddStrategy.addWithSearchId(searchText, searchId);
    Assert.assertNotNull(commandResponse);
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new ErrorResponse("Error adding content, e=expected error"),
                    commandResponse));
  }

  @Test
  public void addWithSearchTitle_noMoviesFound_errorResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Collections.emptyList();
    }};
    List<CommandResponse> commandResponses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(commandResponses);
    Assert.assertEquals(1, commandResponses.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new InfoResponse("No movies found"),
                    commandResponses.get(0)));
  }

  @Test
  public void addWithSearchTitle_oneMovieFound_alreadyExists_errorResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    Object foundMovie = new Object();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Collections.singletonList(foundMovie);
      mockAddStrategy.doesItemExist(foundMovie); times = 1; result = true;
    }};
    List<CommandResponse> commandResponses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(commandResponses);
    Assert.assertEquals(1, commandResponses.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new ErrorResponse("movie already exists"),
                    commandResponses.get(0)));
  }

  @Test
  public void addWithSearchTitle_oneMovieFound_doesntExist_addMovieResponseReturned() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    Object foundMovie = new Object();
    TestCommandResponse expectedResponse = new TestCommandResponse();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Collections.singletonList(foundMovie);
      mockAddStrategy.doesItemExist(foundMovie); times = 1; result = false;
      mockAddStrategy.addContent(foundMovie); times = 1; result = expectedResponse;
    }};
    List<CommandResponse> commandResponses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(commandResponses);
    Assert.assertEquals(1, commandResponses.size());
    Assert.assertEquals(expectedResponse, commandResponses.get(0));
  }

  @Test
  public void addWithSearchTitle_multipleMoviesFound_lessThanMaxResults_multipleAddMovieResponsesReturned() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";

    Object foundMovie = new Object();
    TestCommandResponse foundMovieResponse = new TestCommandResponse();

    Object foundMovie2 = new Object();
    TestCommandResponse foundMovieResponse2 = new TestCommandResponse();

    TestCommandResponse expectedResponseInfoMessage = new TestCommandResponse();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(foundMovie, foundMovie2);
      mockAddStrategy.doesItemExist(foundMovie); times = 1; result = false;
      mockAddStrategy.getResponse(foundMovie); times = 1; result = foundMovieResponse;
      mockAddStrategy.doesItemExist(foundMovie2); times = 1; result = false;
      mockAddStrategy.getResponse(foundMovie2); times = 1; result = foundMovieResponse2;
    }};
    List<CommandResponse> responses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(responses);
    Assert.assertEquals(3, responses.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new InfoResponse("Too many movies found, please narrow search or increase max results to show"),
                    responses.get(0)));
    Assert.assertEquals(foundMovieResponse, responses.get(1));
    Assert.assertEquals(foundMovieResponse2, responses.get(2));
  }

  @Test
  public void addWithSearchTitle_multipleMoviesFound_moreThanMaxResults_multipleAddMovieResponsesReturned() throws Exception {
    //only allow one max result to show
    MockAddStrategy mockAddStrategy = getMockAddStrategy(1);
    String searchText = "searchText1";

    Object foundMovie = new Object();
    TestCommandResponse foundMovieResponse = new TestCommandResponse();

    Object foundMovie2 = new Object();
    TestCommandResponse foundMovieResponse2 = new TestCommandResponse();

    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(foundMovie, foundMovie2);
      mockAddStrategy.doesItemExist(foundMovie); times = 1; result = false;
      mockAddStrategy.getResponse(foundMovie); times = 1; result = foundMovieResponse;
      mockAddStrategy.doesItemExist(foundMovie2); times = 1; result = false;
      mockAddStrategy.getResponse(foundMovie2); times = 1; result = foundMovieResponse2;
    }};
    List<CommandResponse> responses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(responses);
    Assert.assertEquals(2, responses.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new InfoResponse("Too many movies found, please narrow search or increase max results to show"),
                    responses.get(0)));
    Assert.assertEquals(foundMovieResponse, responses.get(1));
  }

  @Test
  public void addWithSearchTitle_multipleMoviesFound_allPreviouslyExist_infoResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    Object foundMovie = new Object();
    Object foundMovie2 = new Object();
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = Arrays.asList(foundMovie, foundMovie2);
      mockAddStrategy.doesItemExist(any); times = 2; result = true;
    }};
    List<CommandResponse> responses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(responses);
    Assert.assertEquals(1, responses.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new InfoResponse("No new movies found, check existing movies"),
                    responses.get(0)));
  }

  @Test
  public void addWithSearchTitle_exceptionThrown_errorResponse() throws Exception {
    MockAddStrategy mockAddStrategy = getMockAddStrategy();
    String searchText = "searchText1";
    Exception expectedException = new Exception("expected error");
    new Expectations(mockAddStrategy) {{
      mockAddStrategy.lookupContent(searchText); times = 1; result = expectedException;
      logger.error("Error trying to add movie", expectedException); times = 1;
    }};
    List<CommandResponse> responses = mockAddStrategy.addWithSearchTitle(searchText);
    Assert.assertNotNull(responses);
    Assert.assertEquals(1, responses.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
              new ErrorResponse("Error trying to add movie, for search text=searchText1, e=expected error"),
              responses.get(0)));
  }

  private MockAddStrategy getMockAddStrategy(int maxResultsToShow) {
    MockAddStrategy mockAddStrategy = new MockAddStrategy(ContentType.MOVIE);
    Deencapsulation.setField(mockAddStrategy, "LOGGER", logger);
    Deencapsulation.setField(mockAddStrategy, "MAX_RESULTS_TO_SHOW", maxResultsToShow);
    return mockAddStrategy;
  }

  private MockAddStrategy getMockAddStrategy() {
    return getMockAddStrategy(5);
  }

  @Mocked
  private Logger logger;

  private static class MockAddStrategy extends AddStrategy<Object> {
    public MockAddStrategy(ContentType contentType) {
      super(contentType);
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
    public CommandResponse addContent(Object content) {
      return null;
    }

    @Override
    public CommandResponse getResponse(Object item) {
      return null;
    }
  }
}
