package com.botdarr.api;

import com.botdarr.Config;
import com.botdarr.TestCommandResponse;
import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.InfoResponse;
import com.google.gson.JsonElement;
import mockit.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.mockserver.junit.MockServerRule;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

  @Before
  public void beforeEachTest() throws Exception {
    Properties properties = new Properties();
    properties.put("telegram-token", "%H$$54j45i");
    properties.put("telegram-private-channels", "channel1:459349");
    writeFakePropertiesFile(properties);
  }

  @Test
  public void downloads_maxDownloadsToConfiguredToZero_noDownloadsReturned() {
    DownloadsStrategy mockDownloadsStrategy = getMockDownloadsStrategy(0);
    List<CommandResponse> responses = mockDownloadsStrategy.downloads();
    Assert.assertNotNull(responses);
    Assert.assertEquals(0, responses.size());
  }

  @Test
  public void downloads_noDownloadsFound_infoMessageReturned() {
    DownloadsStrategy mockDownloadsStrategy = getMockDownloadsStrategy(1);
    List<CommandResponse> responses = mockDownloadsStrategy.downloads();
    Assert.assertNotNull(responses);
    Assert.assertEquals(0, responses.size());
  }

  @Test
  public void parseContent_tooManyDownloads_infoMessageIncluded() {
    DownloadsStrategy mockDownloadsStrategy = getMockDownloadsStrategy(5);
    new Expectations(mockDownloadsStrategy) {{
      mockDownloadsStrategy.getResponse((JsonElement)any); times = 6; result = new TestCommandResponse();
    }};
    //6 items is greater than the configured value above (5)
    List<CommandResponse> responses = mockDownloadsStrategy.parseContent("[{}, {}, {}, {}, {}, {}]");
    Assert.assertNotNull(responses);
    //even though the max is 6, the first response is an info message
    Assert.assertEquals(6, responses.size());
    Assert.assertTrue(
            EqualsBuilder.reflectionEquals(
                    new InfoResponse("Too many downloads, limiting results to 5"),
                    responses.get(0)));
  }

  @Test
  public void getContentDownloads_endpointUnavailable_emptyResponse() {
    DownloadsStrategy downloadsStrategy = new DownloadsStrategy(api, "") {
      @Override
      public CommandResponse getResponse(JsonElement rawElement) {
        return null;
      }
    };
    Deencapsulation.setField(downloadsStrategy, "LOGGER", logger);
    new Expectations(downloadsStrategy) {{
      api.getApiUrl(""); result = "http://localhost"; times = 2;
      api.getApiToken(); result = "token1"; times = 1;
      logger.error("Error trying to connect to http://localhost"); times = 1;
    }};

    //confirm even tho we failed to connect we don't return error notifications that could flood chat clients
    Assert.assertTrue(downloadsStrategy.getContentDownloads().isEmpty());
  }

  private DownloadsStrategy getMockDownloadsStrategy(int maxDownloadsToShow) {
    DownloadsStrategy mockDownloadsStrategy = new MockDownloadsStrategy(api, "");
    Deencapsulation.setField(mockDownloadsStrategy, "MAX_DOWNLOADS_TO_SHOW", maxDownloadsToShow);
    return  mockDownloadsStrategy;
  }

  private void writeFakePropertiesFile(Properties properties) throws Exception {
    File propertiesFile = new File(temporaryFolder.getRoot(), "properties");
    Deencapsulation.setField(Config.class, "propertiesPath", propertiesFile.getPath());
    try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
      properties.store(fos, "");
    }
  }

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  @Mocked
  private Logger logger;

  @Injectable
  private Api api;

  private static class MockDownloadsStrategy extends DownloadsStrategy {

    public MockDownloadsStrategy(Api api, String url) {
      super(api, url);
    }

    @Override
    public CommandResponse getResponse(JsonElement rawElement) {
      return null;
    }

    @Override
    public List<CommandResponse> getContentDownloads() {
      return new ArrayList<>();
    }
  }
}
