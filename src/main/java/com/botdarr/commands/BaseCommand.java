package com.botdarr.commands;


import java.util.Collections;
import java.util.List;

public abstract class BaseCommand implements Command {
  public BaseCommand(String commandText, String description) {
    this(commandText, description, Collections.emptyList());
  }

  public BaseCommand(String commandText, String description, List<String> input) {
    this.commandText = commandText;
    this.description = description;
    this.usageText = commandText + (input != null && !input.isEmpty() ? " " + String.join(" ", input) : "");
    this.input = input;
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
    return this.usageText;
  }

  @Override
  public List<String> getInput() {
    return input;
  }

  private final String description;
  private final String commandText;
  private final String usageText;
  private final List<String> input;
}
