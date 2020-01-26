package com.botdar.commands;

import com.botdar.clients.ChatClientResponse;

public interface Command {
  public String getCommandText();
  public String getDescription();
  public String getIdentifier();
  public CommandResponse<? extends ChatClientResponse> execute(String command);
}
