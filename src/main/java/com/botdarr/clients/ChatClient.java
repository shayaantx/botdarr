package com.botdarr.clients;

import com.botdarr.commands.CommandResponse;

import java.util.List;

public interface ChatClient<T extends ChatClientResponse> {
  void sendMessage(T chatClientResponse);
  void sendMessage(List<T> chatClientResponses);
  void sendMessage(CommandResponse<T> commandResponse);
}
