package com.botdarr.commands;

import com.botdarr.clients.ChatClientResponse;

public interface Command {
  public String getCommandText();
  public String getDescription();
  public String getIdentifier();
  public CommandResponse<? extends ChatClientResponse> execute(String command);
}
