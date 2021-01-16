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
      chatClientResponseBuilder.createErrorMessage("Could not find any new movies for search term=" + searchText); times = 1; result = new TestResponse();
    }};
    Assert.assertNotNull(mockLookupStrategy.lookup(searchText, true));
  }

  @Test
  public void lookup_newMovie_noMoviesFound_allExisting_errorResponseReturned() throws Exception {
    String searchText = "searchText";
    MockLookupStrategy mockLookupStrategy = getMockLookupStrategy(1);
    Object expectedObject = new Object();
    new Expectations(mockLookupStrategy) {{
      mockLookupStrategy.lookup(searchText); times = 1; result = Arrays.asList(expectedObject);
      mockLookupStrategy.lookupExistingItem(expectedObject); times = 1; result = new Object();
      chatClientResponseBuilder.createErrorMessage("Could not find any new movies for search term=" + searchText); times = 1; result = new TestResponse();
    }};
    Assert.assertNotNull(mockLookupStrategy.lookup(searchText, true));
  }

  @Test
  public void lookup_existingMovie_noMoviesFound_noneExist_errorResponseReturned() throws Exception {
    String searchText = "searchText";
    MockLookupStrategy mockLookupStrategy = getMockLookupStrategy(1);
    Object expectedObject = new Object();
    new Expectations(mockLookupStrategy) {{
      mockLookupStrategy.lookup(searchText); times = 1; result = Arrays.asList(expectedObject);
      mockLookupStrategy.lookupExistingItem(expectedObject); times = 1; result = null;
      chatClientResponseBuilder.createErrorMessage("Could not find any existing movies for search term=" + searchText); times = 1; result = new TestResponse();
    }};
    Assert.assertNotNull(mockLookupStrategy.lookup(searchText, false));
  }

  @Test
  public void lookup_newMovie_moviesFound_tooManyResultsLimited_infoResponseIncluded() throws Exception {
    String searchText = "searchText";
    MockLookupStrategy mockLookupStrategy = getMockLookupStrategy(2);
    TestResponse expectedInfoResponse = new TestResponse();
    new Expectations(mockLookupStrategy) {{
      mockLookupStrategy.lookup(searchText); times = 1; result = Arrays.asList(new Object(), new Object(), new Object());
      mockLookupStrategy.lookupExistingItem(any); times = 3; result = null;
      mockLookupStrategy.getNewOrExistingItem(any, any, true); times = 3; result = new TestResponse();
      chatClientResponseBuilder.createInfoMessage("Too many movies found, limiting results to 2"); times = 1; result = expectedInfoResponse;
    }};
    List<ChatClientResponse> responses = mockLookupStrategy.lookup(searchText, true);
    Assert.assertNotNull(responses);
    Assert.assertEquals(3, responses.size());
    Assert.assertEquals(expectedInfoResponse, responses.get(0));
  }

  @Test
  public void lookup_exceptionThrown_errorResponse() throws Exception {
    String searchText = "searchText";
    MockLookupStrategy mockLookupStrategy = getMockLookupStrategy(2);
    Exception expectedException = new Exception("expected error");
    TestResponse expectedErrorResponse = new TestResponse();
    new Expectations(mockLookupStrategy) {{
      mockLookupStrategy.lookup(searchText); times = 1; result = expectedException;
      logger.error("Error trying to lookup movie, searchText=searchText", expectedException); times = 1;
      chatClientResponseBuilder.createErrorMessage("Error looking up movie, e=expected error"); times = 1; result = expectedErrorResponse;
    }};
    List<ChatClientResponse> responses = mockLookupStrategy.lookup(searchText, true);
    Assert.assertNotNull(responses);
    Assert.assertEquals(1, responses.size());
    Assert.assertEquals(expectedErrorResponse, responses.get(0));
  }

  private MockLookupStrategy getMockLookupStrategy(int maxResultsToShow) {
    MockLookupStrategy mockLookupStrategy = new MockLookupStrategy(chatClientResponseBuilder, ContentType.MOVIE);
    Deencapsulation.setField(mockLookupStrategy, "MAX_RESULTS_TO_SHOW", maxResultsToShow);
    Deencapsulation.setField(mockLookupStrategy, "LOGGER", logger);
    return mockLookupStrategy;
  }

  @Mocked
  private Logger logger;

  @Injectable
  private ChatClientResponseBuilder<TestResponse> chatClientResponseBuilder;

  private static class MockLookupStrategy extends LookupStrategy<Object> {

    public MockLookupStrategy(ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder, ContentType contentType) {
      super(chatClientResponseBuilder, contentType);
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
    public ChatClientResponse getNewOrExistingItem(Object lookupItem, Object existingItem, boolean findNew) {
      return null;
    }

    @Override
    public boolean isPathBlacklisted(Object item) {
      return false;
    }
  }
}
