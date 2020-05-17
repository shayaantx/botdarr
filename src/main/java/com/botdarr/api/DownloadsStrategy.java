package com.botdarr.api;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.connections.ConnectionHelper;
import com.botdarr.utilities.ListUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DownloadsStrategy {
  public DownloadsStrategy(Api api,
                           String url,
                           ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder,
                           ContentType contentType) {
    this.api = api;
    this.url = url;
    this.chatClientResponseBuilder = chatClientResponseBuilder;
    this.contentType = contentType;
  }

  public abstract ChatClientResponse getResponse(JsonElement rawElement);

  public List<ChatClientResponse> downloads() {
    if (MAX_DOWNLOADS_TO_SHOW <= 0) {
      return Collections.emptyList();
    }
    List<ChatClientResponse> chatClientResponses = getContentDownloads();
    if (chatClientResponses.isEmpty()) {
      chatClientResponses.add(chatClientResponseBuilder.createInfoMessage("No " + this.contentType.name() + "s downloading"));
    }
    return chatClientResponses;
  }

  public List<ChatClientResponse> getContentDownloads() {
    return ConnectionHelper.makeGetRequest(this.api, this.url, new ConnectionHelper.SimpleMessageEmbedResponseHandler(chatClientResponseBuilder) {
      @Override
      public List<ChatClientResponse> onSuccess(String response) {
        List<ChatClientResponse> chatClientResponses = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        boolean tooManyDownloads = json.size() >= MAX_DOWNLOADS_TO_SHOW;
        for (int i = 0; i < json.size(); i++) {
          ChatClientResponse chatClientResponse = getResponse(json.get(i));
          if (chatClientResponse == null) {
            continue;
          }
          chatClientResponses.add(chatClientResponse);
        }
        if (tooManyDownloads) {
          chatClientResponses = ListUtils.subList(chatClientResponses, MAX_DOWNLOADS_TO_SHOW);
          chatClientResponses.add(0, chatClientResponseBuilder.createInfoMessage("Too many downloads, limiting results to " + MAX_DOWNLOADS_TO_SHOW));
        }
        return chatClientResponses;
      }
    });
  }

  private final Api api;
  private final String url;
  private final ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  private static final Logger LOGGER = LogManager.getLogger();
  private final int MAX_DOWNLOADS_TO_SHOW = new ApiRequests().getMaxDownloadsToShow();
  private final ContentType contentType;
}
