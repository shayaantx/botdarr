package com.botdarr.utilities;

import com.botdarr.clients.ChatClientResponse;

import java.util.List;

public class ListUtils {
  public static List<ChatClientResponse> subList(List<ChatClientResponse> responses, int max) {
    return responses.subList(0, responses.size() > max ? max - 1 : responses.size());
  }
}
