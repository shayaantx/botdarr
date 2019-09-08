package com.botdar;

public enum Command {
  FIND_MOVIE("movie find", "Finds a movie using radarr") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(new RadarrApi().lookup(command));
    }
  },
  HELP("help", "") {
    @Override
    public CommandResponse execute(String command) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("**Commands**\n");
      for (Command com : Command.values()) {
        if (com == HELP) {
          continue;
        }
        stringBuilder.append(com.commandText + "                  " + com.description + "\n");
      }
      return new CommandResponse(stringBuilder.toString());
    }
  };

  private Command(String commandText, String description) {
    this.commandText = commandText;
    this.description = description;
  }

  public String getIdentifier() {
    return commandText.toLowerCase();
  }

  public abstract CommandResponse execute(String command);

  private final String description;
  private final String commandText;
}
