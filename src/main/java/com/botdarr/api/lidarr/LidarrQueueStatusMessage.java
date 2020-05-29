package com.botdarr.api.lidarr;

import java.util.List;

public class LidarrQueueStatusMessage {
  public List<String> getMessages() {
    return messages;
  }

  private String title;
  private List<String> messages = null;
}
