package com.botdarr;

import com.botdarr.api.ApiRequests;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;


public class ApiRequestsTests {
  @Test
  public void checkRequestLimits_noLimitsSet() {
    ApiRequests apiRequests = new ApiRequests();
    writeFakePropertiesFile(getDefaultProperties());
    Assert.assertFalse(apiRequests.checkRequestLimits());
  }

  @Test
  public void checkRequestLimits_maxRequestCountSetButThresholdMissing() {

  }

  @Test
  public void checkRequestLimits_thresholdSetButMaxRequestCountMissing() {

  }

  @Test
  public void checkRequestLimits_invalidRequestThresholdSet() {

  }

  @Test
  public void checkRequestLimits_invalidMaxRequestCountSet() {

  }

  private void writeFakePropertiesFile(Properties properties) {
    File propertiesFile = new File(temporaryFolder.getRoot(), "properties");
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

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
}
