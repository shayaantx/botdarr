package com.botdar;

import com.botdar.radarr.RadarrApi;
import gnu.trove.map.hash.TLongObjectHashMap;
import mockit.*;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.TextChannelImpl;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

public class ApiTests {
  private static SnowflakeCacheView<TextChannel> snowflakeCacheView = new SnowflakeCacheViewImpl<>(TextChannel.class, GuildChannel::getName);

  @BeforeClass
  public static void beforeAllTests() throws Exception {
    //set fake properties file so Config doesn't fail to load initially
    Deencapsulation.setField(Config.class, "propertiesPath", "test/config/properties");
    FileUtils.write(new File("test" + File.separator + "config", "properties"), "token=KG345ng");

    new MockUp<JDAImpl>() {
      @Mock
      public SnowflakeCacheView<TextChannel> getTextChannelCache() {
        return snowflakeCacheView;
      }
    };
  }

  @After
  public void afterEachTest() {
    TLongObjectHashMap objectMap = Deencapsulation.getField(snowflakeCacheView, "elements");
    objectMap.clear();
  }

  @Test
  public void sendDownloadUpdates_noDiscordChannelsPropertySet() {
    JDA jda = getDefaultJdaImpl();
    addTextChannels(createTextChannel(jda, 0, "channel1"));

    Api api = new RadarrApi();
    api.sendDownloadUpdates(jda);
    Logger logger = LogManager.getLogger();
    new Verifications() {{
      logger.warn("No discord channels set in properties file, cannot send notifications");
    }};
  }

  @Test
  public void sendDownloadUpdates_emptyStringSetForDiscordChannels() {
    JDA jda = getDefaultJdaImpl();
    addTextChannels(createTextChannel(jda, 0, "channel1"));

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
  public void sendDownloadUpdates_discordChannelNotAvailableForNotifications() {
    JDA jda = getDefaultJdaImpl();
    addTextChannels(createTextChannel(jda, 0, "channel1"));

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

  @Test
  public void sendDownloadUpdates_discordChannelPropertyWithoutSpacesNotIgnoredForNotifications() {
    JDA jda = getDefaultJdaImpl();
    addTextChannels(createTextChannel(jda, 0, "channel121"));
    addTextChannels(createTextChannel(jda, 1, "channel123"));

    Properties properties = new Properties();
    properties.setProperty("discord-channels", "channel121,channel123");
    setNewProperties(properties);

    new MockUp<RadarrApi>()  {
      @Mock
      List<MessageEmbed> downloads() {
        return Collections.emptyList();
      }
    };

    Api api = new RadarrApi();
    api.sendDownloadUpdates(jda);

    //verify downloads was called twice
    new Expectations() {{
      api.downloads(); times = 2;
    }};
  }

  private void setNewProperties(Properties properties) {
    //init singleton before trying to get it
    Config.getProperty("");
    Config config = Deencapsulation.getField(Config.class, "instance");
    Deencapsulation.setField(config, "properties", properties);
  }

  private void addTextChannels(TextChannel... textChannels) {
    TLongObjectHashMap objectMap = Deencapsulation.getField(snowflakeCacheView, "elements");
    for (TextChannel textChannel : textChannels) {
      objectMap.put(textChannel.getIdLong(), textChannel);
    }
  }

  private TextChannel createTextChannel(JDA jda, long id, String name) {
    return new TextChannelImpl(id, new GuildImpl((JDAImpl)jda, 0)).setName(name);
  }

  private JDA getDefaultJdaImpl() {
    return new JDAImpl(new AuthorizationConfig(AccountType.BOT, "x"));
  }
}
