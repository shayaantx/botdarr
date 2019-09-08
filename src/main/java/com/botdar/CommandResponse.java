package com.botdar;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class CommandResponse {
  public CommandResponse(String response) {
    this.response = response;
  }

  public CommandResponse(MessageEmbed htmlResponse) {
    this.htmlResponse = htmlResponse;
  }

  public void send(MessageChannel messageChannel) {
    final MessageAction messageAction;
    if (response != null) {
      messageAction = messageChannel.sendMessage(response);
    } else {
      messageAction = messageChannel.sendMessage(htmlResponse);
    }
    messageAction.queue();
  }

  private String response;
  private MessageEmbed htmlResponse;
}
