package com.botdar;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public interface Api {
  String getApiUrl(String path);
  MessageEmbed add(String searchText, String id);
  List<MessageEmbed> lookup(String search, boolean findNew);
  List<MessageEmbed> downloads();
  List<MessageEmbed> addTitle(String searchText);
  List<MessageEmbed> getProfiles();
  List<MessageEmbed> lookupTorrents(String command);
  List<MessageEmbed> cancelDownload(long id);
  void sendNotifications(JDA jda);
  void cacheData(JDA jda);
  String getApiToken();
}
