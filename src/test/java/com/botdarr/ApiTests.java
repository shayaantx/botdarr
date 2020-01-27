package com.botdarr;

import gnu.trove.map.hash.TLongObjectHashMap;
import mockit.*;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.TextChannelImpl;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;

import java.io.File;
import java.util.Properties;

public class ApiTests {
  private static SnowflakeCacheView<TextChannel> snowflakeCacheView = new SnowflakeCacheViewImpl<>(TextChannel.class, GuildChannel::getName);

  @BeforeClass
  public static void beforeAllTests() throws Exception {
    //set fake properties file so Config doesn't fail to load initially
    Deencapsulation.setField(Config.class, "propertiesPath", "test/config/properties");
    FileUtils.write(new File("test" + File.separator + "config", "properties"),
      "discord-token=KG345ng" + System.lineSeparator() + "discord-channels=test1");

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
