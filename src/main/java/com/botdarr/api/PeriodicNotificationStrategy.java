package com.botdarr.api;

import com.botdarr.clients.ChatClientResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public abstract class PeriodicNotificationStrategy<T extends ChatClientResponse> {
  public PeriodicNotificationStrategy(ContentType contentType, DownloadsStrategy downloadsStrategy) {
    this.contentType = contentType;
    this.downloadsStrategy = downloadsStrategy;
  }

  public abstract void sendToConfiguredChannels(List<ChatClientResponse> downloads);

  public void sendPeriodicNotifications() {
    if (MAX_DOWNLOADS_TO_SHOW <= 0) {
      LOGGER.debug("Bot configured to show no downloads");
      return;
    }
    List<ChatClientResponse> downloads = downloadsStrategy.getContentDownloads();
    if (downloads != null && !downloads.isEmpty()) {
      sendToConfiguredChannels(downloads);
    } else {
      LOGGER.debug("No " + this.contentType + " downloads available for sending");
    }
  }

  private final DownloadsStrategy downloadsStrategy;
  private final ContentType contentType;
  private final int MAX_DOWNLOADS_TO_SHOW = new ApiRequests().getMaxDownloadsToShow();
  private static final Logger LOGGER = LogManager.getLogger();
}
