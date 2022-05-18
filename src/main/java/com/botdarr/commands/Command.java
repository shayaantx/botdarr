package com.botdarr.commands;

import com.botdarr.commands.responses.CommandResponse;

import java.util.List;

public interface Command {
  String getCommandText();
  String getDescription();
  String getIdentifier();
  List<String> getInput();
  default String getCommandUsage() {
    return "";
  }
  default boolean hasArguments() {
    //by default all commands have arguments unless explicitly overridden
    return true;
  }
  List<CommandResponse> execute(String command);
}
