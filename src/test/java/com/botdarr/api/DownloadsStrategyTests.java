package com.botdarr.api;

import com.botdarr.TestResponse;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.google.gson.JsonElement;
import mockit.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DownloadsStrategyTests {
  @BeforeClass
  public static void beforeAllTests() {
    new MockUp<ApiRequests>() {
      @Mock
      public int getMaxDownloadsToShow() {
        //mock out the max downloads to show which we will override in each test
        return 0;
      }
    };
  }

  @Test
  public void downloads_maxDownloadsToConfiguredToZero_noDownloadsReturned() {
    DownloadsStrategy mockDownloadsStrategy = getMockDownloadsStrategy(0);
    List<ChatClientResponse> responses = mockDownloadsStrategy.downloads();
    Assert.assertNotNull(responses);
    Assert.assertEquals(0, responses.size());
  }

  @Test
  public void downloads_noDownloadsFound_infoMessageReturned() {
    DownloadsStrategy mockDownloadsStrategy = getMockDownloadsStrategy(1);
    TestResponse expectedResponse = new TestResponse();
    new Expectations(mockDownloadsStrategy) {{
      chatClientResponseBuilder.createInfoMessage("No movies downloading"); times = 1; result = expectedResponse;
    }};
    List<ChatClientResponse> responses = mockDownloadsStrategy.downloads();
    Assert.assertNotNull(responses);
    Assert.assertEquals(1, responses.size());
    Assert.assertEquals(expectedResponse, responses.get(0));
  }

  @Test
  public void parseContent_tooManyDownloads_infoMessageIncluded() {
    DownloadsStrategy mockDownloadsStrategy = getMockDownloadsStrategy(5);
    TestResponse expectedInfoResponse = new TestResponse();
    new Expectations(mockDownloadsStrategy) {{
      mockDownloadsStrategy.getResponse((JsonElement)any); times = 6; result = new TestResponse();
      chatClientResponseBuilder.createInfoMessage("Too many downloads, limiting results to 5"); times = 1; result = expectedInfoResponse;
    }};
    //6 items is greater than the configured value above (5)
    List<ChatClientResponse> responses = mockDownloadsStrategy.parseContent("[{}, {}, {}, {}, {}, {}]");
    Assert.assertNotNull(responses);
    //even tho the max is 6, the first response is an info message
    Assert.assertEquals(6, responses.size());
    Assert.assertEquals(expectedInfoResponse, responses.get(0));
  }

  private DownloadsStrategy getMockDownloadsStrategy(int maxDownloadsToShow) {
    DownloadsStrategy mockDownloadsStrategy = new MockDownloadsStrategy(api, "", chatClientResponseBuilder, ContentType.MOVIE);
    Deencapsulation.setField(mockDownloadsStrategy, "MAX_DOWNLOADS_TO_SHOW", maxDownloadsToShow);
    return  mockDownloadsStrategy;
  }

  @Injectable
  private Api api;

  @Injectable
  private ChatClientResponseBuilder<TestResponse> chatClientResponseBuilder;

  private static class MockDownloadsStrategy extends DownloadsStrategy {

    public MockDownloadsStrategy(Api api, String url, ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder, ContentType contentType) {
      super(api, url, chatClientResponseBuilder, contentType);
    }

    @Override
    public ChatClientResponse getResponse(JsonElement rawElement) {
      return null;
    }

    @Override
    public List<ChatClientResponse> getContentDownloads() {
      return new ArrayList<>();
    }
  }
}
