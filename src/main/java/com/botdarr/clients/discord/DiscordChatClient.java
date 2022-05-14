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
    sendMessages(channel -> {
      LogManager.getLogger("com.botdarr.clients.discord").debug("Attempting to queue message to channel " + channelName);
      channel.sendMessage(chatClientResponse.getMessage()).queue();
      LogManager.getLogger("com.botdarr.clients.discord").debug("Finished queue message to channel " + channelName);
    }, channelName);
  }

  private void sendMessages(MessageSender messageSender, String channelName) {
    LogManager.getLogger("com.botdarr.clients.discord").debug("Attempting to message to channel " + channelName);
    Set<String> supportedDiscordChannels = Sets.newHashSet(Splitter.on(',').trimResults().split(Config.getProperty(Config.Constants.DISCORD_CHANNELS)));
    for (TextChannel textChannel : jda.getTextChannels()) {
      if (!supportedDiscordChannels.contains(textChannel.getName())) {
        LogManager.getLogger("com.botdarr.clients.discord").debug("Channel  " + textChannel.getName() + " not supported");
        continue;
      }
      if (channelName != null && !channelName.equalsIgnoreCase(textChannel.getName())) {
        LogManager.getLogger("com.botdarr.clients.discord").debug("Channel  " + textChannel.getName() + " and " + channelName + " dont match");
        continue;
      }
      LogManager.getLogger("com.botdarr.clients.discord").debug("Sending to channel " + textChannel.getName());
      messageSender.send(textChannel);
    }
  }

  private interface MessageSender {
    void send(TextChannel channel);
  }
  private final JDA jda;
}
