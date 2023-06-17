package com.botdarr.api;

import com.botdarr.Config;
import com.botdarr.database.DatabaseBootstrap;
import com.botdarr.database.DatabaseHelper;
import com.botdarr.database.MockedDatabase;
import mockit.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ApiRequestsTests {
  @Before
  public void beforeEachTest() {
    //reset config singleton
    Deencapsulation.setField(Config.class, "instance", null);
  }

  @Test
  public void checkRequestLimits_noLimitsSet() {
    ApiRequests apiRequests = new ApiRequests();
    writeFakePropertiesFile(getDefaultProperties());
    Assert.assertFalse(apiRequests.checkRequestLimits(ApiRequestType.SHOW));
  }

  @Test
  public void checkRequestLimits_maxRequestCountSetButThresholdMissing() {
    ApiRequests apiRequests = new ApiRequests();
    Properties properties = getDefaultProperties();
    properties.setProperty("max-show-requests-per-user", "5");
    writeFakePropertiesFile(properties);
    Assert.assertFalse(apiRequests.checkRequestLimits(ApiRequestType.SHOW));
  }

  @Test
  public void checkRequestLimits_thresholdSetButMaxRequestCountMissing() {
    ApiRequests apiRequests = new ApiRequests();
    Properties properties = getDefaultProperties();
    properties.setProperty("max-requests-threshold", "WEEK");
    writeFakePropertiesFile(properties);
    Assert.assertFalse(apiRequests.checkRequestLimits(ApiRequestType.SHOW));
  }

  @Test
  public void checkRequestLimits_invalidRequestThresholdSet() {
    MockedLogger mockedLogger = new MockedLogger(
      "Invalid threshold option, valid options: day, month, week",
      new IllegalArgumentException("No enum constant com.botdarr.api.ApiRequestThreshold.WEEKx"));
    ApiRequests apiRequests = new ApiRequests();
    Properties properties = getDefaultProperties();
    properties.setProperty("max-show-requests-per-user", "5");
    properties.setProperty("max-requests-threshold", "WEEKx");
    writeFakePropertiesFile(properties);
    Assert.assertFalse(apiRequests.checkRequestLimits(ApiRequestType.SHOW));
    mockedLogger.validate();
  }

  @Test
  public void checkRequestLimits_invalidMaxRequestCountSet() {
    MockedLogger mockedLogger = new MockedLogger(
      "Invalid max requests per SHOW per user configuration",
      new NumberFormatException("For input string: \"5x\""));
    ApiRequests apiRequests = new ApiRequests();
    Properties properties = getDefaultProperties();
    properties.setProperty("max-show-requests-per-user", "5x");
    properties.setProperty("max-requests-threshold", "WEEK");
    writeFakePropertiesFile(properties);
    Assert.assertFalse(apiRequests.checkRequestLimits(ApiRequestType.SHOW));
    mockedLogger.validate();
  }

  @Test
  public void canMakeRequests_exceededRequestsForDay() throws Exception {
    testRequestThreshold(2, 2, "DAY", true);
  }

  @Test
  public void canMakeRequests_didNotExceedRequestsForDay() throws Exception {
    testRequestThreshold(1, 2, "DAY", false);
  }

  @Test
  public void canMakeRequests_exceededRequestsForWeek() throws Exception {
    testRequestThreshold(2, 2, "WEEK", true);
  }

  @Test
  public void canMakeRequests_didNotExceedRequestsForWeek() throws Exception {
    testRequestThreshold(1, 2, "WEEK", false);
  }

  @Test
  public void canMakeRequests_exceededRequestsForMonth() throws Exception {
    testRequestThreshold(2, 2, "MONTH", true);
  }

  @Test
  public void canMakeRequests_didNotExceedRequestsForMonth() throws Exception {
    testRequestThreshold(1, 2, "MONTH", false);
  }

  @Test
  public void getMaxDownloadsToShow_notConfiguredDefaultReturned() {
    writeFakePropertiesFile(getDefaultProperties());
    //20 is the default when max-downloads-to-show is not configured
    Assert.assertEquals(20, new ApiRequests().getMaxDownloadsToShow());
  }

  @Test
  public void getMaxDownloadsToShow_configuredValueNotANumber() {
    MockedLogger mockedLogger = new MockedLogger(
      "Invalid max downloads to show configuration",
      new NumberFormatException("For input string: \"25x\""));
    Properties properties = getDefaultProperties();
    properties.put("max-downloads-to-show", "25x");
    writeFakePropertiesFile(properties);

    //even when the configured max-downloads-to-show incorrectly, the default is 20
    Assert.assertEquals(20, new ApiRequests().getMaxDownloadsToShow());
    mockedLogger.validate();
  }

  @Test
  public void getMaxDownloadsToShow_configuredValueReturned() {
    Properties properties = getDefaultProperties();
    properties.put("max-downloads-to-show", "25");
    writeFakePropertiesFile(properties);

    Assert.assertEquals(25, new ApiRequests().getMaxDownloadsToShow());
  }


  @Test
  public void getMaxResultsToShow_notConfiguredDefaultReturned() {
    writeFakePropertiesFile(getDefaultProperties());
    //20 is the default when max-results-to-show is not configured
    Assert.assertEquals(20, new ApiRequests().getMaxResultsToShow());
  }

  @Test
  public void getMaxResultsToShow_configuredValueNotANumber() {
    MockedLogger mockedLogger = new MockedLogger(
      "Invalid max results to show configuration",
      new NumberFormatException("For input string: \"25x\""));
    Properties properties = getDefaultProperties();
    properties.put("max-results-to-show", "25x");
    writeFakePropertiesFile(properties);

    //even when the configured max-results-to-show incorrectly, the default is 20
    Assert.assertEquals(20, new ApiRequests().getMaxResultsToShow());
    mockedLogger.validate();
  }

  @Test
  public void getMaxResultsToShow_configuredValueReturned() {
    Properties properties = getDefaultProperties();
    properties.put("max-results-to-show", "25");
    writeFakePropertiesFile(properties);

    Assert.assertEquals(25, new ApiRequests().getMaxResultsToShow());
  }

  private void testRequestThreshold(int maxRequestsToAdd, int maxRequestsAllowed, String threshold, boolean expectExceedsThreshold) throws Exception {
    //create temporary database
    new MockedDatabase(temporaryFolder.newFile());
    DatabaseBootstrap.init();

    ApiRequests apiRequests = new ApiRequests();
    String username = "user1";
    //add existing requests
    for (int i = 0; i < maxRequestsToAdd; i++) {
      apiRequests.auditRequest(ApiRequestType.SHOW, username, "title" + i);
    }

    //setup max requests for the given max and threshold type
    Properties properties = getDefaultProperties();
    properties.setProperty("max-show-requests-per-user", "" + maxRequestsAllowed);
    properties.setProperty("max-requests-threshold", threshold);
    writeFakePropertiesFile(properties);

    if (expectExceedsThreshold) {
      //shouldn't be able to make requests
      Assert.assertFalse(apiRequests.canMakeRequests(ApiRequestType.SHOW, username));
    } else {
      //should be able to make requests
      Assert.assertTrue(apiRequests.canMakeRequests(ApiRequestType.SHOW, username));
    }
  }

  private void writeFakePropertiesFile(Properties properties) {
    File propertiesFile = null;
    try {
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

  private Properties getDefaultProperties() {
    Properties properties = new Properties();
    properties.setProperty("discord-token", "G$K$GK");
    properties.setProperty("discord-channels", "plex-testing2");
    properties.setProperty("radarr-url", "http://localhost:444");
    properties.setProperty("radarr-token", "FSJDkjmf#$Kf3");
    properties.setProperty("radarr-path", "/movies");
    properties.setProperty("radarr-default-profile", "any");
    return properties;
  }

  private static class MockedLogger extends MockUp<org.apache.logging.log4j.core.Logger> {
    private MockedLogger(String expectedMessage, Throwable expectedThrowable) {
      this.expectedMessage = expectedMessage;
      this.expectedThrowable = expectedThrowable;
    }

    @Mock
    public void error(String actualMessage, Throwable actualThrowable) {
      this.actualMessage = actualMessage;
      this.actualThrowable = actualThrowable;
    }

    public void validate() {
      Assert.assertEquals(expectedMessage, actualMessage);
      Assert.assertEquals(expectedThrowable.getClass(), actualThrowable.getClass());
      Assert.assertEquals(expectedThrowable.getMessage(), actualThrowable.getMessage());
    }

    private String actualMessage;
    private final String expectedMessage;
    private Throwable actualThrowable;
    private final Throwable expectedThrowable;
  }

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
}
