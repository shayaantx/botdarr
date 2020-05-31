package com.botdarr.commands;

import com.google.common.base.Strings;

public abstract class BaseCommand implements Command {
  public BaseCommand(String commandText, String description) {
    this(commandText, "", description);
  }

  public BaseCommand(String commandText, String usageText, String description) {
    this.commandText = commandText;
    this.description = description;
    this.usageText = usageText;
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

  @Override
  public String getCommandUsage() {
    return Strings.isNullOrEmpty(usageText) ? getCommandText() : usageText;
  }

  private final String description;
  private final String commandText;
  private final String usageText;
}
