package com.botdarr.commands;

import com.botdarr.clients.ChatClientResponse;

import java.util.List;

public class CommandResponse<T extends ChatClientResponse> {
  public CommandResponse(T singleChatClientResponse) {
    this.singleChatClientResponse = singleChatClientResponse;
  }

  public CommandResponse(List<T> multipleChatClientResponses) {
    this.multipleChatClientResponses = multipleChatClientResponses;
  }

  public T getSingleChatClientResponse() {
    return singleChatClientResponse;
  }

  public List<T> getMultipleChatClientResponses() {
    return multipleChatClientResponses;
  }

  private T singleChatClientResponse;
  private List<T> multipleChatClientResponses;
}
