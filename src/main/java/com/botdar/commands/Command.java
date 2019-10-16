package com.botdar.commands;

public interface Command {
  public String getIdentifier();
  public CommandResponse execute(String command);
}
