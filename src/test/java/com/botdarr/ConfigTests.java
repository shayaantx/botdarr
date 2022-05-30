package com.botdarr;

import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ConfigTests {
  @Before
  public void beforeEachTest() {
    Deencapsulation.setField(Config.class, "instance", null);
  }

  @Test
  public void getCommaDelimitedList_emptyItemsReturnEmptyList() {
    Assert.assertEquals(Collections.emptyList(), Config.getCommaDelimitedList(""));
  }

  @Test
  public void getCommaDelimitedList_nullItemsReturnEmptyList() {
    Assert.assertEquals(Collections.emptyList(), Config.getCommaDelimitedList(null));
  }

  @Test
  public void getCommaDelimitedList_singleItemReturnListWithItem() {
    List<String> items = Config.getCommaDelimitedList("item1");
    Assert.assertEquals(1, items.size());
    Assert.assertEquals("item1", items.get(0));
  }

  @Test
  public void getCommaDelimitedList_multipleItemsSplitByCommaReturnItemsInList() {
    List<String> items = Config.getCommaDelimitedList("item1,item2");
    Assert.assertEquals(2, items.size());
    Assert.assertEquals("item1", items.get(0));
    Assert.assertEquals("item2", items.get(1));
  }

  @Test
  public void getConfig_invalidCommandPrefixConfigured() throws Exception {
    Properties properties = new Properties();
    properties.put("discord-token", "#$F#$#");
    properties.put("discord-channels", "channel1");
    properties.put("command-prefix", "//");
    writeFakePropertiesFile(properties);
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("Command prefix must be a single character");
    Config.getProperty("");
  }

  @Test
  public void getConfig_noChatClientsConfigured() throws Exception {
    writeFakePropertiesFile(new Properties());
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("You don't have any chat clients configured, please configure one");
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

  @Test
  public void getConfig_telegramGroupsAndChannelsConfigured() throws Exception {
    Properties properties = new Properties();
    properties.put("telegram-token", "%H$$54j45i");
    properties.put("telegram-private-channels", "channel1:459349");
    properties.put("telegram-private-groups", "group1:459349");
    writeFakePropertiesFile(properties);
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("Cannot configure telegram for private channels and groups, you must pick one or the other");
    Config.getProperty("");
  }

  @Test
  public void getConfig_telegramChannelIdsContainNegativeOneHundred() throws Exception {
    Properties properties = new Properties();
    properties.put("telegram-token", "%H$$54j45i");
    properties.put("telegram-private-channels", "channel1:-100459349");
    writeFakePropertiesFile(properties);
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("Telegram channel or group contains -100, which is not necessary. We automatically add this to your channel at runtime, you can remove it.");
    Config.getProperty("");
  }

  @Test
  public void getConfig_telegramGroupIdsContainNegativeOneHundred() throws Exception {
    Properties properties = new Properties();
    properties.put("telegram-token", "%H$$54j45i");
    properties.put("telegram-private-groups", "group1:-100459349");
    writeFakePropertiesFile(properties);
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("Telegram channel or group contains -100, which is not necessary. We automatically add this to your channel at runtime, you can remove it.");
    Config.getProperty("");
  }

  @Test
  public void getPrefix_returnsDefaultPrefix() throws Exception {
    Properties properties = new Properties();
    properties.put("telegram-token", "%H$$54j45i");
    properties.put("telegram-private-groups", "group1:100459349");
    writeFakePropertiesFile(properties);
    Assert.assertEquals("!", Config.getPrefix());
  }

  @Test
  public void getPrefix_returnsConfiguredPrefix() throws Exception {
    Properties properties = new Properties();
    properties.put("telegram-token", "%H$$54j45i");
    properties.put("telegram-private-groups", "group1:100459349");
    properties.put("command-prefix", "$");
    writeFakePropertiesFile(properties);
    Assert.assertEquals("$", Config.getPrefix());
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
