package com.botdar.commands;

import com.botdar.discord.EmbedHelper;
import com.botdar.radarr.RadarrApi;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public enum Command {
  ADD_MOVIE("movie add", "Adds a movie using search text and tmdb id (i.e., movie add John Wick 484737), get tmdb ids using find") {
    @Override
    public CommandResponse execute(String command) {
      int lastSpace = command.lastIndexOf(" ");
      String searchText = command.substring(0, lastSpace);
      String id = command.substring(lastSpace + 1, command.length());
      return new CommandResponse(new RadarrApi().add2(searchText, id));
    }
  },
  PROFILES("movie profiles", "Displays all the profiles available to search for movies under (i.e., movie add ANY)") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(new RadarrApi().getProfiles());
    }
  },
  FIND_NEW_MOVIE("movie find new", "Finds a new movie using radarr (i.e., movie find John Wick)") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(new RadarrApi().lookup(command, true));
    }
  },
  FIND_EXISTING_MOVIE("movie find existing", "Finds an existing movie using radarr (i.e., movie find Princess Fudgecake") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(new RadarrApi().lookup(command, false));
    }
  },
  MOVIE_DOWNLOADS("movie downloads", "Shows all the active movies downloading in radarr") {
    @Override
    public CommandResponse execute(String command) {
      List<MessageEmbed> embedList = new RadarrApi().downloads();
      if (embedList == null || embedList.size() == 0) {
        return new CommandResponse(EmbedHelper.createInfoMessage("No downloads currently"));
      }
      return new CommandResponse(embedList);
    }
  },
  HELP("help", "") {
    @Override
    public CommandResponse execute(String command) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setTitle("Commands");
      int maxCommandTextLength = 0;
      for (Command com : Command.values()) {
        if (com == HELP) {
          continue;
        }
        embedBuilder.addField(com.commandText, com.description, false);
      }
      return new CommandResponse(embedBuilder.build());
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
