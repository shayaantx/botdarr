package com.botdarr.clients.telegram;

import com.botdarr.clients.ChatClientResponse;
import j2html.tags.DomContent;

import java.util.List;

public class TelegramResponse implements ChatClientResponse {
  public TelegramResponse(List<DomContent> containerTags) {
    this.domContent = containerTags;
  }

  public String getHtml() {
    StringBuilder stringBuilder = new StringBuilder();
    for (DomContent domContent : domContent) {
      stringBuilder.append(domContent.render()).append("\n");
    }
    return stringBuilder.toString();
  }

  private final List<DomContent> domContent;
}
