package com.botdar;

import com.botdar.commands.CommandResponse;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public interface Api {
  /**
   * Get this api's url endpoint
   */
  String getApiUrl(String path);

  /**
   * Attempt to add content using the given search text and id (the tmdbid)
   */
  MessageEmbed addWithId(String searchText, String id);

  /**
   * Attempt to add a title with the given search text
   */
  List<MessageEmbed> addWithTitle(String searchText);

  /**
   * Lookup content with the api
   * @param search The content search text
   * @param findNew Whether the content is new or existing
   * @return
   */
  List<MessageEmbed> lookup(String search, boolean findNew);

  /**
   * Lookup torrents using the api
   * @param command The lookup command
   * @param showRejected Whether to show rejected torrents or not
   */
  List<MessageEmbed> lookupTorrents(String command, boolean showRejected);

  /**
   * Gets all the in-progress downloads
   */
  List<MessageEmbed> downloads();

  /**
   * Cancels an in-progress download
   */
  List<MessageEmbed> cancelDownload(long id);

  /**
   * Get the quality profiles available in the api
   */
  List<MessageEmbed> getProfiles();

  /**
   * Forces a download using the api
   */
  List<MessageEmbed> forceDownload(String command);

  /**
   * Notifications that are sent every 10 minutes
   */
  void sendPeriodicNotifications(JDA jda);

  /**
   * Data cached from jda directly in the api
   */
  void cacheData(JDA jda);

  /**
   * Gets the auth token for this api
   */
  String getApiToken();

  default String getApiUrl(String apiUrlKey, String apiTokenKey, String path) {
    return Config.getProperty(apiUrlKey) + "/api/" + path + "?apikey=" + Config.getProperty(apiTokenKey);
  }

  default void sendDownloadUpdates(JDA jda) {
    for (TextChannel textChannel : jda.getTextChannels()) {
      String discordChannel = Config.getProperty(Config.Constants.DISCORD_CHANNEL);
      if (discordChannel == null || discordChannel.isEmpty()) {
        continue;
      }
      if (discordChannel.equalsIgnoreCase(textChannel.getName())) {
        List<MessageEmbed> downloads = downloads();
        if (downloads != null && downloads.size() > 0) {
          //TODO: this is fragile, should fix
          if (downloads.size() == 1 &&
            downloads.get(0).getDescription() != null &&
            downloads.get(0).getDescription().equalsIgnoreCase("No downloads currently")) {
            continue;
          }
          new CommandResponse(downloads).send(textChannel);
        }
      }
    }
  }
}
