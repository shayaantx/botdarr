package com.botdarr.api;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class LookupApiHandler<T> {
  public LookupApiHandler(ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder, ContentType contentType) {
    this.chatClientResponseBuilder = chatClientResponseBuilder;
    this.contentType = contentType;
  }

  public abstract T lookupExistingItem(T lookupItem);
  public abstract List<T> lookup(String searchTerm);
  public abstract ChatClientResponse getNewOrExistingItem(T lookupItem, T existingItem, boolean findNew);

  public List<ChatClientResponse> lookup(String search, boolean findNew) {
    try {
      List<ChatClientResponse> responses = new ArrayList<>();
      List<T> lookupItems = lookup(search);
      for (T lookupItem : lookupItems) {
        T existingItem = lookupExistingItem(lookupItem);
        boolean isExistingItem = existingItem != null;
        boolean skip = findNew ? isExistingItem : !isExistingItem;
        if (skip) {
          continue;
        }
        responses.add(getNewOrExistingItem(lookupItem, existingItem, findNew));
      }
      if (responses.size() == 0) {
        return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Could not find any " + (findNew ? "new" : "existing") + this.contentType + " for search term=" + search));
      }
      if (responses.size() > MAX_RESULTS_TO_SHOW) {
        responses = subList(responses, MAX_RESULTS_TO_SHOW);
        responses.add(0, chatClientResponseBuilder.createInfoMessage("Too many " + this.contentType + " found, limiting results to " + MAX_RESULTS_TO_SHOW));
      }
      return responses;
    } catch (Exception e) {
      LOGGER.error("Error trying to lookup " + this.contentType + ", searchText=" + search, e);
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Error looking up " + this.contentType + ", e=" + e.getMessage()));
    }
  }

  //TODO: move somewhere else
  private List<ChatClientResponse> subList(List<ChatClientResponse> responses, int max) {
    return responses.subList(0, responses.size() > max ? max - 1 : responses.size());
  }

  private final ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  private static final Logger LOGGER = LogManager.getLogger();
  private final int MAX_RESULTS_TO_SHOW = new ApiRequests().getMaxResultsToShow();
  private final ContentType contentType;
}
