package com.botdar.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class EmbedHelper {
  public static MessageEmbed createErrorMessage(String message) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Error!");
    embedBuilder.setDescription(message);
    embedBuilder.setColor(Color.RED);
    return embedBuilder.build();
  }

  public static MessageEmbed createInfoMessage(String message) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Info");
    embedBuilder.setDescription(message);
    embedBuilder.setColor(Color.white);
    return embedBuilder.build();
  }
}
