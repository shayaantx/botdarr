package com.botdar.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class EmbedHelper {
  public static MessageEmbed createErrorMessage(String message) {
    return createMessageEmbed("Error!", Color.RED, message);
  }

  public static MessageEmbed createInfoMessage(String message) {
    return createMessageEmbed("Info", Color.WHITE, message);
  }

  public static MessageEmbed createSuccessMessage(String message) {
    return createMessageEmbed("Success!", Color.GREEN, message);
  }

  private static MessageEmbed createMessageEmbed(String title, Color color, String message) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(title);
    embedBuilder.setDescription(message);
    embedBuilder.setColor(color);
    return embedBuilder.build();
  }
}
