package com.botdarr.api;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.commands.responses.CommandResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;

public interface Api {
  /**
   * Gets all the in-progress downloads
   */
  List<CommandResponse> downloads();

  /**
   * Data cached from jda directly in the api
   */
  void cacheData();

  default List<ChatClientResponse> subList(List<ChatClientResponse> responses, int max) {
    return responses.subList(0, responses.size() > max ? max - 1 : responses.size());
  }

  Logger LOGGER = LogManager.getLogger();
}
