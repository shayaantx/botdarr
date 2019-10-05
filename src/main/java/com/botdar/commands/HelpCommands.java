package com.botdar.commands;

import net.dv8tion.jda.api.EmbedBuilder;

public enum HelpCommands implements Command {
  HELP("help", "") {
    @Override
    public CommandResponse execute(String command) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setTitle("Commands");
      embedBuilder.addField("movies help", "Shows all the commands for movies", false);
      embedBuilder.addField("shows help", "Shows all the commands for shows", false);
      return new CommandResponse(embedBuilder.build());
    }
  },
  HELP_MOVIES("movies help", "") {
    @Override
    public CommandResponse execute(String command) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setTitle("Commands");
      for (RadarrCommands com : RadarrCommands.values()) {
        embedBuilder.addField(com.commandText, com.description, false);
      }
      return new CommandResponse(embedBuilder.build());
    }
  },
  HELP_SHOWS("shows help", "") {
    @Override
    public CommandResponse execute(String command) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setTitle("Commands");
      for (SonarrCommands com : SonarrCommands.values()) {
        embedBuilder.addField(com.commandText, com.description, false);
      }
      return new CommandResponse(embedBuilder.build());
    }
  };
  private HelpCommands(String commandText, String description) {
    this.commandText = commandText;
    this.description = description;
  }

  @Override
  public String getIdentifier() {
    return commandText.toLowerCase();
  }

  private final String description;
  private final String commandText;
}
