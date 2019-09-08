package com.botdar.commands;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.List;

public class CommandResponse {
  public CommandResponse(String response) {
    this.response = response;
  }

  public CommandResponse(MessageEmbed htmlResponse) {
    this.htmlResponse = htmlResponse;
  }

  public CommandResponse(List<MessageEmbed> htmlResponses) {
    this.htmlResponses = htmlResponses;
  }

  public void send(MessageChannel messageChannel) {
    if (response != null) {
      messageChannel.sendMessage(response).queue();
    } else if (htmlResponses != null) {
      for (MessageEmbed messageEmbed : htmlResponses) {
        messageChannel.sendMessage(messageEmbed).queue();
      }
    } else{
      messageChannel.sendMessage(htmlResponse).queue();
    }
  }

  private String response;
  private MessageEmbed htmlResponse;
  private List<MessageEmbed> htmlResponses;
}
