package com.botdarr.commands;

import com.botdarr.clients.ChatClientResponse;

public interface Command {
  public String getCommandText();
  public String getDescription();
  public String getIdentifier();
  public default String getCommandUsage() {
    return "";
  }
  public default boolean hasArguments() {
    //by default all commands have arguments unless explicitly overridden
    return true;
  }
  public CommandResponse<? extends ChatClientResponse> execute(String command);
}
