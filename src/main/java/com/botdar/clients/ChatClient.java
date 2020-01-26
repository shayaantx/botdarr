package com.botdar.clients;

import com.botdar.commands.CommandResponse;

import java.util.List;

public interface ChatClient<T extends ChatClientResponse> {
  void sendMessage(T chatClientResponse);
  void sendMessage(List<T> chatClientResponses);
  void sendMessage(CommandResponse<T> commandResponse);
}
