package com.botdarr.clients;

import com.botdarr.commands.CommandResponse;

import java.util.List;

public interface ChatClient<T extends ChatClientResponse> {
  void sendMessage(T chatClientResponse, String channel);
  void sendMessage(List<T> chatClientResponses, String channel);
  void sendMessage(CommandResponse<T> commandResponse, String channel);
}
