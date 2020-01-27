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

  @Override
  public void sendMessage(DiscordResponse chatClientResponse) {
    sendMessages(channel -> channel.sendMessage(chatClientResponse.getMessage()).queue());
  }

  @Override
  public void sendMessage(List<DiscordResponse> chatClientResponses) {
    sendMessages(channel -> {
      for (DiscordResponse discordResponse : chatClientResponses) {
        channel.sendMessage(discordResponse.getMessage()).queue();
      }
    });
  }

  @Override
  public void sendMessage(CommandResponse<DiscordResponse> commandResponse) {
    if (commandResponse.getSingleChatClientResponse() != null) {
      sendMessage(commandResponse.getSingleChatClientResponse());
    } else if (commandResponse.getMultipleChatClientResponses() != null) {
      sendMessage(commandResponse.getMultipleChatClientResponses());
    } else {
      //TODO: err
    }
  }

  private void sendMessages(MessageSender messageSender) {
    Set<String> supportedDiscordChannels = Sets.newHashSet(Splitter.on(',').trimResults().split(Config.getProperty(Config.Constants.DISCORD_CHANNELS)));
    for (TextChannel textChannel : jda.getTextChannels()) {
      if (supportedDiscordChannels.contains(textChannel.getName())) {
        messageSender.send(textChannel);
      }
    }
  }

  private interface MessageSender {
    void send(TextChannel channel);
  }
  private final JDA jda;
}
