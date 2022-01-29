package com.botdarr.api;

import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.InfoResponse;
import com.botdarr.connections.ConnectionHelper;
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
  public DownloadsStrategy(Api api,
                           String url,
                           ContentType contentType) {
    this.api = api;
    this.url = url;
    this.contentType = contentType;
  }

  public abstract CommandResponse getResponse(JsonElement rawElement);

  public List<CommandResponse> downloads() {
    if (MAX_DOWNLOADS_TO_SHOW <= 0) {
      return Collections.emptyList();
    }
    List<CommandResponse> chatClientResponses = getContentDownloads();
    if (chatClientResponses.isEmpty()) {
      chatClientResponses.add(new InfoResponse("No " + this.contentType.getDisplayName() + "s downloading"));
    }
    return chatClientResponses;
  }

  public List<CommandResponse> getContentDownloads() {
    return ConnectionHelper.makeGetRequest(this.api, this.url, new ConnectionHelper.SimpleMessageEmbedResponseHandler() {

      @Override
      public List<CommandResponse> onConnectException(HttpHostConnectException e) {
        String message = "Error trying to connect to " + DownloadsStrategy.this.api.getApiUrl(DownloadsStrategy.this.url);
        LOGGER.error(message);
        return Collections.emptyList();
      }

      @Override
      public List<CommandResponse> onSuccess(String response) {
        return parseContent(response);
      }
    });
  }

  public List<CommandResponse> parseContent(String response) {
    List<CommandResponse> chatClientResponses = new ArrayList<>();
    JsonParser parser = new JsonParser();
    JsonArray json = parser.parse(response).getAsJsonArray();
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

  private final Api api;
  private final String url;
  private final int MAX_DOWNLOADS_TO_SHOW = new ApiRequests().getMaxDownloadsToShow();
  private final ContentType contentType;
  private static Logger LOGGER = LogManager.getLogger();
}
