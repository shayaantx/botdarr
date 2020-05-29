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
    this.contentType = contentType;
  }

  public abstract List<T> lookupContent(String search) throws Exception;
  public abstract List<T> lookupItemById(String id) throws Exception;
  public abstract boolean doesItemExist(T content);
  public abstract String getItemId(T item);
  public abstract ChatClientResponse addContent(T content);
  public abstract ChatClientResponse getResponse(T item);
  protected abstract void cacheContent(T addContent);

  public ChatClientResponse addWithSearchId(String searchText, String id) {
    try {
      List<T> items = lookupContent(searchText);
      if (items.isEmpty()) {
        LOGGER.warn("Search text " + searchText + "yielded no " + this.contentType + "s, trying id");
        items = lookupItemById(id);
      }
      if (items.isEmpty()) {
        LOGGER.warn("Search id " + id + "yielded no " + this.contentType + "s, stopping");
        return chatClientResponseBuilder.createErrorMessage("No " + this.contentType.getDisplayName() + "s found");
      }
      for (T item : items) {
        if (getItemId(item).equalsIgnoreCase(id)) {
          if (doesItemExist(item)) {
            return chatClientResponseBuilder.createErrorMessage(this.contentType.getDisplayName() + " already exists");
          }
          ChatClientResponse chatClientResponse = addContent(item);
          cacheContent(item);
          return chatClientResponse;
        }
      }
      return chatClientResponseBuilder.createErrorMessage("Could not find " + this.contentType.getDisplayName() + " with search text=" + searchText + " and id=" + id);
    } catch (Exception e) {
      LOGGER.error("Error trying to add " + this.contentType, e);
      return chatClientResponseBuilder.createErrorMessage("Error adding content, e=" + e.getMessage());
    }
  }

  public List<ChatClientResponse> addWithSearchTitle(String searchText) {
    try {
      List<T> items = lookupContent(searchText);
      if (items.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createInfoMessage("No " + this.contentType.getDisplayName() + "s found"));
      }

      if (items.size() == 1) {
        T item = items.get(0);
        if (doesItemExist(item)) {
          return Arrays.asList(chatClientResponseBuilder.createErrorMessage(this.contentType.getDisplayName() + " already exists"));
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
        restOfItems.add(0, chatClientResponseBuilder.createInfoMessage("Too many " + this.contentType.getDisplayName() + "s found, please narrow search"));
      }
      if (restOfItems.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createInfoMessage("No new " + this.contentType.getDisplayName() + "s found, check existing " + this.contentType.getDisplayName() + "s"));
      }
      return restOfItems;
    } catch (Exception e) {
      LOGGER.error("Error trying to add " + this.contentType, e);
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Error trying to add " + this.contentType + " " + searchText + ", e=" + e.getMessage()));
    }
  }

  private final ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  private final ContentType contentType;
  private static final Logger LOGGER = LogManager.getLogger();
  private final int MAX_RESULTS_TO_SHOW = new ApiRequests().getMaxResultsToShow();
}
