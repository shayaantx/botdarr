package com.botdarr.api;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.utilities.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AddStrategy<T> {
  public AddStrategy(ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder, ContentType contentType) {
    this.chatClientResponseBuilder = chatClientResponseBuilder;
    this.contentDisplayName = contentType.getDisplayName();
  }

  public abstract List<T> lookupContent(String search) throws Exception;
  public abstract List<T> lookupItemById(String id) throws Exception;
  public abstract boolean doesItemExist(T content);
  public abstract String getItemId(T item);
  public abstract ChatClientResponse addContent(T content);
  public abstract ChatClientResponse getResponse(T item);

  public ChatClientResponse addWithSearchId(String searchText, String id) {
    try {
      List<T> items = lookupContent(searchText);
      if (items.isEmpty()) {
        LOGGER.warn("Search text " + searchText + "yielded no " + this.contentDisplayName + "s, trying id");
        items = lookupItemById(id);
      }
      if (items.isEmpty()) {
        LOGGER.warn("Search id " + id + "yielded no " + this.contentDisplayName + "s, stopping");
        return chatClientResponseBuilder.createErrorMessage("No " + this.contentDisplayName + "s found");
      }
      for (T item : items) {
        if (getItemId(item).equalsIgnoreCase(id)) {
          if (doesItemExist(item)) {
            return chatClientResponseBuilder.createErrorMessage(this.contentDisplayName + " already exists");
          }
          ChatClientResponse chatClientResponse = addContent(item);
          return chatClientResponse;
        }
      }
      return chatClientResponseBuilder.createErrorMessage("Could not find " + contentDisplayName + " with search text=" + searchText + " and id=" + id);
    } catch (Exception e) {
      LOGGER.error("Error trying to add " + contentDisplayName, e);
      return chatClientResponseBuilder.createErrorMessage("Error adding content, e=" + e.getMessage());
    }
  }

  public List<ChatClientResponse> addWithSearchTitle(String searchText) {
    try {
      List<T> items = lookupContent(searchText);
      if (items.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createInfoMessage("No " + contentDisplayName + "s found"));
      }

      if (items.size() == 1) {
        T item = items.get(0);
        if (doesItemExist(item)) {
          return Arrays.asList(chatClientResponseBuilder.createErrorMessage(contentDisplayName + " already exists"));
        }
        return Arrays.asList(addContent(items.get(0)));
      }
      List<ChatClientResponse> restOfItems = new ArrayList<>();
      for (T item : items) {
        if (doesItemExist(item)) {
          //skip existing items
          continue;
        }
        restOfItems.add(getResponse(item));
      }
      if (restOfItems.size() > 1) {
        restOfItems = ListUtils.subList(restOfItems, MAX_RESULTS_TO_SHOW);
        restOfItems.add(0, chatClientResponseBuilder.createInfoMessage("Too many " + contentDisplayName + "s found, please narrow search or increase max results to show"));
      }
      if (restOfItems.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createInfoMessage("No new " + contentDisplayName + "s found, check existing " + contentDisplayName + "s"));
      }
      return restOfItems;
    } catch (Exception e) {
      LOGGER.error("Error trying to add " + contentDisplayName, e);
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Error trying to add " + contentDisplayName + ", for search text=" + searchText + ", e=" + e.getMessage()));
    }
  }

  private final ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  private final String contentDisplayName;
  private static Logger LOGGER = LogManager.getLogger();
  private final int MAX_RESULTS_TO_SHOW = new ApiRequests().getMaxResultsToShow();
}
