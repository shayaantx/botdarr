package com.botdarr.clients.telegram;

import com.botdarr.clients.ChatClientResponse;
import com.pengrad.telegrambot.model.request.Keyboard;
import j2html.tags.DomContent;

import java.util.List;

public class TelegramResponse implements ChatClientResponse {
  public TelegramResponse(List<DomContent> containerTags) {
    this(containerTags, null);
  }

  public TelegramResponse(List<DomContent> containerTags, Keyboard interactiveMarkup) {
    this.domContent = containerTags;
    this.interactiveMarkup = interactiveMarkup;
  }

  public String getHtml() {
    StringBuilder stringBuilder = new StringBuilder();
    for (DomContent domContent : domContent) {
      stringBuilder.append(domContent.render()).append("\n");
    }
    return stringBuilder.toString();
  }

  public Keyboard getInteractiveMarkup() {
    return this.interactiveMarkup;
  }

  private final Keyboard interactiveMarkup;
  private final List<DomContent> domContent;
}
