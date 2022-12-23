package com.botdarr.api;

import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.InfoResponse;
import com.botdarr.connections.ConnectionHelper;
import com.botdarr.connections.RequestBuilder;
import com.botdarr.utilities.ListUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DownloadsStrategy {
  public abstract CommandResponse getResponse(JsonElement rawElement);
  public abstract List<CommandResponse> getContentDownloads();

  public List<CommandResponse> downloads() {
    if (MAX_DOWNLOADS_TO_SHOW <= 0) {
      return Collections.emptyList();
    }
    return getContentDownloads();
  }

  public List<CommandResponse> parseContent(String response) {
    List<CommandResponse> chatClientResponses = new ArrayList<>();
    JsonArray json = JsonParser.parseString(response).getAsJsonArray();
    boolean tooManyDownloads = json.size() >= MAX_DOWNLOADS_TO_SHOW;
    for (int i = 0; i < json.size(); i++) {
      CommandResponse chatClientResponse = getResponse(json.get(i));
      if (chatClientResponse == null) {
        continue;
      }
      chatClientResponses.add(chatClientResponse);
    }
    if (tooManyDownloads && !chatClientResponses.isEmpty()) {
      chatClientResponses = ListUtils.subList(chatClientResponses, MAX_DOWNLOADS_TO_SHOW);
      chatClientResponses.add(0, new InfoResponse("Too many downloads, limiting results to " + MAX_DOWNLOADS_TO_SHOW));
    }
    return chatClientResponses;
  }

  private final int MAX_DOWNLOADS_TO_SHOW = new ApiRequests().getMaxDownloadsToShow();
  private static Logger LOGGER = LogManager.getLogger();
}
