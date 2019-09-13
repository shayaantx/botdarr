package com.botdar;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public interface Api {
  String getApiUrl(String path);
  List<MessageEmbed> lookup(String search, boolean findNew);
  List<MessageEmbed> downloads();
  MessageEmbed add2(String searchText, String id);
  List<MessageEmbed> getProfiles();
}
