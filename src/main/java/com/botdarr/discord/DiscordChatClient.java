package com.botdarr.discord;

import com.botdarr.Config;
import com.botdarr.clients.ChatClient;
import com.botdarr.commands.CommandResponse;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.*;
import java.util.List;

public class DiscordChatClient implements ChatClient<DiscordResponse> {
  public DiscordChatClient(JDA readyEventJda) {
    this.jda = readyEventJda;
  }

  public void sendMessage(DiscordResponse chatClientResponse, String channelName) {
    sendMessages(channel -> channel.sendMessage(chatClientResponse.getMessage()).queue(), channelName);
  }

  public void sendMessage(List<DiscordResponse> chatClientResponses, String channelName) {
    sendMessages(channel -> {
      for (DiscordResponse discordResponse : chatClientResponses) {
        channel.sendMessage(discordResponse.getMessage()).queue();
      }
    }, channelName);
  }

  public void sendMessage(CommandResponse<DiscordResponse> commandResponse, String targetChannel) {
    if (commandResponse.getSingleChatClientResponse() != null) {
      sendMessage(commandResponse.getSingleChatClientResponse(), targetChannel);
    } else if (commandResponse.getMultipleChatClientResponses() != null) {
      sendMessage(commandResponse.getMultipleChatClientResponses(), targetChannel);
    } else {
      //TODO: err
    }
  }

  private void sendMessages(MessageSender messageSender, String channelName) {
    Set<String> supportedDiscordChannels = Sets.newHashSet(Splitter.on(',').trimResults().split(Config.getProperty(Config.Constants.DISCORD_CHANNELS)));
    for (TextChannel textChannel : jda.getTextChannels()) {
      if (!supportedDiscordChannels.contains(textChannel.getName())) {
        continue;
      }
      if (channelName != null && !channelName.equalsIgnoreCase(textChannel.getName())) {
        continue;
      }
      messageSender.send(textChannel);
    }
  }

  @Override
  public void sendToConfiguredChannels(DiscordResponse chatClientResponse) {
    sendMessage(chatClientResponse, null);
  }

  @Override
  public void sendToConfiguredChannels(List<DiscordResponse> chatClientResponses) {
    sendMessage(chatClientResponses, null);
  }

  private interface MessageSender {
    void send(TextChannel channel);
  }
  private final JDA jda;
}
