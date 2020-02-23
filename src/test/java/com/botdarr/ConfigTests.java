package com.botdarr;

import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class ConfigTests {
  @Before
  public void beforeEachTest() {
    Deencapsulation.setField(Config.class, "instance", null);
  }

  @Test
  public void getConfig_noChatClientsConfigured() throws Exception {
    writeFakePropertiesFile(new Properties());
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("You don't have Discord, Slack, Telegram configured, please configure one");
    Config.getProperty("");
  }

  @Test
  public void getConfig_moreThanOneChatClientsConfigured() throws Exception {
    Properties properties = new Properties();
    properties.put("discord-token", "#$F#$#");
    properties.put("discord-channels", "channel1");
    properties.put("telegram-token", "%H$$54j45i");
    properties.put("telegram-private-channels", "channel1:459349");
    writeFakePropertiesFile(properties);
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("You cannot configure more than one chat client");
    Config.getProperty("");
  }

  private void writeFakePropertiesFile(Properties properties) throws Exception {
    File propertiesFile = new File(temporaryFolder.getRoot(), "properties");
    Deencapsulation.setField(Config.class, "propertiesPath", propertiesFile.getPath());
    try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
      properties.store(fos, "");
    }
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
}
