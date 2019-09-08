package com.botdar;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public interface Api {
  List<MessageEmbed> lookup(String search);
  String getApiUrl(String path);
  List<MessageEmbed> downloads();
}
