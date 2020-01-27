package com.botdarr.discord;

import com.botdarr.clients.ChatClientResponse;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class DiscordResponse implements ChatClientResponse {
  public DiscordResponse(MessageEmbed message) {
    this.message = message;
  }

  public MessageEmbed getMessage() {
    return message;
  }

  private final MessageEmbed message;
}
