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

public class LookupStrategyTests {
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
  public void lookup_newMovie_noMoviesFound_errorResponseReturned() throws Exception {
    String searchText = "searchText";
    MockLookupStrategy mockLookupStrategy = getMockLookupStrategy(1);
    new Expectations(mockLookupStrategy) {{
      mockLookupStrategy.lookup(searchText); times = 1; result = Collections.emptyList();
    }};
    List<CommandResponse> responses = mockLookupStrategy.lookup(searchText, true);
    Assert.assertNotNull(responses);
    Assert.assertEquals(1, responses.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(new ErrorResponse("Could not find any new movies for search term=" + searchText),
            responses.get(0)));
  }

  @Test
  public void lookup_newMovie_noMoviesFound_allExisting_errorResponseReturned() throws Exception {
    String searchText = "searchText";
    MockLookupStrategy mockLookupStrategy = getMockLookupStrategy(1);
    Object expectedObject = new Object();
    new Expectations(mockLookupStrategy) {{
      mockLookupStrategy.lookup(searchText); times = 1; result = Collections.singletonList(expectedObject);
      mockLookupStrategy.lookupExistingItem(expectedObject); times = 1; result = new Object();
    }};
    List<CommandResponse> responses = mockLookupStrategy.lookup(searchText, true);
    Assert.assertNotNull(responses);
    Assert.assertEquals(1, responses.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(new ErrorResponse("Could not find any new movies for search term=" + searchText),
                    responses.get(0)));
  }

  @Test
  public void lookup_existingMovie_noMoviesFound_noneExist_errorResponseReturned() throws Exception {
    String searchText = "searchText";
    MockLookupStrategy mockLookupStrategy = getMockLookupStrategy(1);
    Object expectedObject = new Object();
    new Expectations(mockLookupStrategy) {{
      mockLookupStrategy.lookup(searchText); times = 1; result = Collections.singletonList(expectedObject);
      mockLookupStrategy.lookupExistingItem(expectedObject); times = 1; result = null;
    }};
    List<CommandResponse> responses = mockLookupStrategy.lookup(searchText, false);
    Assert.assertNotNull(responses);
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new ErrorResponse("Could not find any existing movies for search term=" + searchText),
                    responses.get(0)));
  }

  @Test
  public void lookup_newMovie_moviesFound_tooManyResultsLimited_infoResponseIncluded() throws Exception {
    String searchText = "searchText";
    MockLookupStrategy mockLookupStrategy = getMockLookupStrategy(2);
    new Expectations(mockLookupStrategy) {{
      mockLookupStrategy.lookup(searchText); times = 1; result = Arrays.asList(new Object(), new Object(), new Object());
      mockLookupStrategy.lookupExistingItem(any); times = 3; result = null;
      mockLookupStrategy.getNewItem(any); times = 3; result = new TestCommandResponse();
    }};
    List<CommandResponse> responses = mockLookupStrategy.lookup(searchText, true);
    Assert.assertNotNull(responses);
    Assert.assertEquals(3, responses.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new InfoResponse("Too many movies found, limiting results to 2"),
                    responses.get(0)));
  }

  @Test
  public void lookup_exceptionThrown_errorResponse() throws Exception {
    String searchText = "searchText";
    MockLookupStrategy mockLookupStrategy = getMockLookupStrategy(2);
    Exception expectedException = new Exception("expected error");
    new Expectations(mockLookupStrategy) {{
      mockLookupStrategy.lookup(searchText); times = 1; result = expectedException;
      logger.error("Error trying to lookup movie, searchText=searchText", expectedException); times = 1;
    }};
    List<CommandResponse> responses = mockLookupStrategy.lookup(searchText, true);
    Assert.assertNotNull(responses);
    Assert.assertEquals(1, responses.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new ErrorResponse("Error looking up movie, e=expected error"),
                    responses.get(0)));
  }

  private MockLookupStrategy getMockLookupStrategy(int maxResultsToShow) {
    MockLookupStrategy mockLookupStrategy = new MockLookupStrategy(ContentType.MOVIE);
    Deencapsulation.setField(mockLookupStrategy, "MAX_RESULTS_TO_SHOW", maxResultsToShow);
    Deencapsulation.setField(mockLookupStrategy, "LOGGER", logger);
    return mockLookupStrategy;
  }

  @Mocked
  private Logger logger;

  private static class MockLookupStrategy extends LookupStrategy<Object> {

    public MockLookupStrategy(ContentType contentType) {
      super(contentType);
    }

    @Override
    public Object lookupExistingItem(Object lookupItem) {
      return null;
    }

    @Override
    public List<Object> lookup(String searchTerm) throws Exception {
      return null;
    }

    @Override
    public CommandResponse getExistingItem(Object existingItem) {
      return null;
    }

    @Override
    public CommandResponse getNewItem(Object lookupItem) {
      return null;
    }

    @Override
    public boolean isPathBlacklisted(Object item) {
      return false;
    }
  }
}
