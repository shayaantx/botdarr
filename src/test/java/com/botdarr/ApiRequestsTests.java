package com.botdarr;

import com.botdarr.api.ApiRequests;
import com.botdarr.database.Bootstrap;
import com.botdarr.database.DatabaseHelper;
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
    Deencapsulation.setField(Config.class, "instance", null);
  }

  @Test
  public void checkRequestLimits_noLimitsSet() {
    ApiRequests apiRequests = new ApiRequests();
    writeFakePropertiesFile(getDefaultProperties());
    Assert.assertFalse(apiRequests.checkRequestLimits());
  }

  @Test
  public void checkRequestLimits_maxRequestCountSetButThresholdMissing() {
    ApiRequests apiRequests = new ApiRequests();
    Properties properties = getDefaultProperties();
    properties.setProperty("max-requests-per-user", "5");
    writeFakePropertiesFile(properties);
    Assert.assertFalse(apiRequests.checkRequestLimits());
  }

  @Test
  public void checkRequestLimits_thresholdSetButMaxRequestCountMissing() {
    ApiRequests apiRequests = new ApiRequests();
    Properties properties = getDefaultProperties();
    properties.setProperty("max-requests-threshold", "WEEK");
    writeFakePropertiesFile(properties);
    Assert.assertFalse(apiRequests.checkRequestLimits());
  }

  @Test
  public void checkRequestLimits_invalidRequestThresholdSet() {
    MockedLogger mockedLogger = new MockedLogger(
      "Invalid threshold option, valid options: day, month, week",
      new IllegalArgumentException("No enum constant com.botdarr.api.ApiRequestThreshold.WEEKx"));
    ApiRequests apiRequests = new ApiRequests();
    Properties properties = getDefaultProperties();
    properties.setProperty("max-requests-per-user", "5");
    properties.setProperty("max-requests-threshold", "WEEKx");
    writeFakePropertiesFile(properties);
    Assert.assertFalse(apiRequests.checkRequestLimits());
    mockedLogger.validate();
  }

  @Test
  public void checkRequestLimits_invalidMaxRequestCountSet() {
    MockedLogger mockedLogger = new MockedLogger(
      "Invalid max requests per user configuration",
      new NumberFormatException("For input string: \"5x\""));
    ApiRequests apiRequests = new ApiRequests();
    Properties properties = getDefaultProperties();
    properties.setProperty("max-requests-per-user", "5x");
    properties.setProperty("max-requests-threshold", "WEEK");
    writeFakePropertiesFile(properties);
    Assert.assertFalse(apiRequests.checkRequestLimits());
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

  private void testRequestThreshold(int maxRequestsToAdd, int maxRequestsAllowed, String threshold, boolean expectExceedsThreshold) throws Exception {
    //create temporary database
    new MockedDatabase(temporaryFolder.newFile());
    Bootstrap.init();

    ApiRequests apiRequests = new ApiRequests();
    String username = "user1";
    //add existing requests
    for (int i = 0; i < maxRequestsToAdd; i++) {
      apiRequests.auditRequest(username, "title" + i);
    }

    //setup max requests for the given max and threshold type
    Properties properties = getDefaultProperties();
    properties.setProperty("max-requests-per-user", "" + maxRequestsAllowed);
    properties.setProperty("max-requests-threshold", threshold);
    writeFakePropertiesFile(properties);

    if (expectExceedsThreshold) {
      //shouldn't be able to make requests
      Assert.assertFalse(apiRequests.canMakeRequests(username));
    } else {
      //should be able to make requests
      Assert.assertTrue(apiRequests.canMakeRequests(username));
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

  private static class MockedDatabase extends MockUp<DatabaseHelper> {
    private MockedDatabase(File temporaryDatabase) {
      this.temporaryDatabase = temporaryDatabase;
    }

    @Mock
    public File getDatabaseFile() {
      return temporaryDatabase;
    }

    private final File temporaryDatabase;
  }

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
}
