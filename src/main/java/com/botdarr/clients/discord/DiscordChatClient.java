package com.botdarr.clients.discord;

import com.botdarr.Config;
import com.botdarr.clients.ChatClient;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.LogManager;

import java.util.*;
import java.util.List;

public class DiscordChatClient implements ChatClient<DiscordResponse> {
  public DiscordChatClient(JDA readyEventJda) {
    this.jda = readyEventJda;
  }

  @Override
  public void sendToConfiguredChannels(List<DiscordResponse> chatClientResponses) {
    sendMessages(channel -> {
      for (DiscordResponse discordResponse : chatClientResponses) {
        channel.sendMessage(discordResponse.getMessage()).queue();
      }
    }, null);
  }

  public void sendMessage(DiscordResponse chatClientResponse, String channelName) {
    sendMessages(channel -> channel.sendMessage(chatClientResponse.getMessage()).queue(), channelName);
  }

  private void sendMessages(MessageSender messageSender, String channelName) {
    Set<String> supportedDiscordChannels = Sets.newHashSet(Splitter.on(',').trimResults().split(Config.getProperty(Config.Constants.DISCORD_CHANNELS)));
    for (TextChannel textChannel : jda.getTextChannels()) {
      if (!supportedDiscordChannels.contains(textChannel.getName())) {
        LogManager.getLogger("com.botdarr.clients.discord").warn("Channel " + textChannel.getName() + " is not configured to receive messages");
        continue;
      }
      if (channelName != null && !channelName.equalsIgnoreCase(textChannel.getName())) {
        LogManager.getLogger("com.botdarr.clients.discord").debug("Channel " + textChannel.getName() + " doesn't match target channel " + channelName);
        continue;
      }
      messageSender.send(textChannel);
    }
  }

  private interface MessageSender {
    void send(TextChannel channel);
  }
  private final JDA jda;
}
