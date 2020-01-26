package com.botdar.commands;

public abstract class BaseCommand implements Command {
  public BaseCommand(String commandText, String description) {
    this.commandText = commandText;
    this.description = description;
  }

  @Override
  public String getIdentifier() {
    return commandText.toLowerCase();
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getCommandText() {
    return commandText;
  }

  private final String description;
  private final String commandText;
}
