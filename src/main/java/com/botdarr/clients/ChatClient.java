package com.botdarr.clients;

import java.util.List;

public interface ChatClient<T extends ChatClientResponse> {
  void sendToConfiguredChannels(T chatClientResponse);
  void sendToConfiguredChannels(List<T> chatClientResponses);
}
