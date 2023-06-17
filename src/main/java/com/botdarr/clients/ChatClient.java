package com.botdarr.clients;

import java.util.List;

public interface ChatClient<T extends ChatClientResponse> {
  void sendToConfiguredChannels(List<T> chatClientResponses);
  void cleanup();
}
