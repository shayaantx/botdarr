package com.botdar;

import net.dv8tion.jda.api.entities.MessageEmbed;

public interface Api {
  MessageEmbed lookup(String search);
  String getApiUrl(String path);
}
