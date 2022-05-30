package com.botdarr.clients.discord;

import com.botdarr.clients.ChatClientResponse;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.Component;

import java.util.ArrayList;
import java.util.List;

public class DiscordResponse implements ChatClientResponse {
  public DiscordResponse(MessageEmbed message) {
    this.message = message;
  }

  public DiscordResponse(MessageEmbed message, List<Component> actionComponents) {
    this(message);
    this.actionComponents = actionComponents;
  }

  public MessageEmbed getMessage() {
    return message;
  }

  public List<Component> getActionComponents() {
    return actionComponents;
  }

  private List<Component> actionComponents = new ArrayList<>();
  private final MessageEmbed message;
}
