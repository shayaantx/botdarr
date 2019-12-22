package com.botdar;

import com.botdar.radarr.RadarrApi;
import mockit.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.TextChannelImpl;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

public class ApiTests {
  @BeforeClass
  public static void setup() throws Exception {
    //set fake properties file so Config doesn't fail to load initially
    Deencapsulation.setField(Config.class, "propertiesPath", "test/config/properties");
    FileUtils.write(new File("test" + File.separator + "config", "properties"), "token=KG345ng");
  }

  @Test
  public void sendDownloadUpdates_noDiscordChannelsPropertySet(@Mocked JDAImpl jda) {
    new MockUp<JDA>() {
      @Mock
      public List<TextChannel> getTextChannels() {
        return Arrays.asList(new TextChannelImpl(0, new GuildImpl(jda, 0)).setName("channel1"));
      }
    };
    Api api = new RadarrApi();
    api.sendDownloadUpdates(jda);
    Logger logger = LogManager.getLogger();
    new Verifications() {{
      logger.warn("No discord channels set in properties file, cannot send notifications");
    }};
  }

  @Test
  public void sendDownloadUpdates_emptyStringSetForDiscordChannels(@Mocked JDAImpl jda) {
    new MockUp<JDA>() {
      @Mock
      public List<TextChannel> getTextChannels() {
        return Arrays.asList(new TextChannelImpl(0, new GuildImpl(jda, 0)).setName("channel1"));
      }
    };

    //set the empty string for the property
    Properties properties = new Properties();
    properties.setProperty("discord-channels", "");
    setNewProperties(properties);
    
    Api api = new RadarrApi();
    api.sendDownloadUpdates(jda);
    Logger logger = LogManager.getLogger();
    new Verifications() {{
      logger.warn("No discord channels set in properties file, cannot send notifications");
    }};
  }

  @Test
  public void sendDownloadUpdates_discordChannelNotAvailableForNotifications(@Mocked JDAImpl jda) {
    new MockUp<JDA>() {
      @Mock
      public List<TextChannel> getTextChannels() {
        return Arrays.asList(new TextChannelImpl(0, new GuildImpl(jda, 0)).setName("channel1"));
      }
    };

    Properties properties = new Properties();
    properties.setProperty("discord-channels", "channel121");
    setNewProperties(properties);

    Api api = new RadarrApi();
    api.sendDownloadUpdates(jda);

    Logger logger = LogManager.getLogger();
    new Verifications() {{
      logger.debug("Channel channel1 is not configured for notifications");
    }};
  }

  private void setNewProperties(Properties properties) {
    //init singleton before trying to get it
    Config.getProperty("");
    Config config = Deencapsulation.getField(Config.class, "instance");
    Deencapsulation.setField(config, "properties", properties);
  }
}
