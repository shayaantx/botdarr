package com.botdar;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public interface Api {
  String getApiUrl(String path);
  List<MessageEmbed> lookup(String search);
  List<MessageEmbed> downloads();
  MessageEmbed add(String command);
}
