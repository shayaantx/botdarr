package com.botdarr.api;

import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.ErrorResponse;
import com.botdarr.commands.responses.InfoResponse;
import com.botdarr.utilities.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LookupStrategy<T> {
  public LookupStrategy(ContentType contentType) {
    this.contentType = contentType;
  }

  public abstract T lookupExistingItem(T lookupItem);
  public abstract List<T> lookup(String searchTerm) throws Exception;
  public abstract CommandResponse getExistingItem(T existingItem);
  public abstract CommandResponse getNewItem(T lookupItem);
  public abstract boolean isPathBlacklisted(T item);

  public List<CommandResponse> lookup(String search, boolean findNew) {
    try {
      List<CommandResponse> responses = new ArrayList<>();
      List<T> lookupItems = lookup(search);
      if (lookupItems == null) {
        return Collections.singletonList(new ErrorResponse("Something failed during lookup for search term=" + search));
      }
      for (T lookupItem : lookupItems) {
        T existingItem = lookupExistingItem(lookupItem);
        boolean isExistingItem = existingItem != null;
        if (isExistingItem && isPathBlacklisted(existingItem)) {
          //skip any items that have blacklisted paths
          continue;
        }
        boolean skip = findNew == isExistingItem;
        if (skip) {
          continue;
        }
        responses.add(isExistingItem ? getExistingItem(existingItem) : getNewItem(lookupItem));
      }
      if (responses.size() == 0) {
        return Collections.singletonList(new ErrorResponse("Could not find any " + (findNew ? "new" : "existing") + " " + this.contentType.getDisplayName() + "s for search term=" + search));
      }
      if (responses.size() > MAX_RESULTS_TO_SHOW) {
        responses = ListUtils.subList(responses, MAX_RESULTS_TO_SHOW);
        responses.add(0, new InfoResponse("Too many " + this.contentType.getDisplayName() + "s found, limiting results to " + MAX_RESULTS_TO_SHOW));
      }
      return responses;
    } catch (Exception e) {
      LOGGER.error("Error trying to lookup " + this.contentType.getDisplayName() + ", searchText=" + search, e);
      return Collections.singletonList(new ErrorResponse("Error looking up " + this.contentType.getDisplayName() + ", e=" + e.getMessage()));
    }
  }

  private static Logger LOGGER = LogManager.getLogger();
  private final int MAX_RESULTS_TO_SHOW = new ApiRequests().getMaxResultsToShow();
  private final ContentType contentType;
}
