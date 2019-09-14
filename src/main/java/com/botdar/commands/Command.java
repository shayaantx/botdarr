package com.botdar.commands;

import com.botdar.discord.EmbedHelper;
import com.botdar.radarr.RadarrApi;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public enum Command {
  ADD_MOVIE("movie add", "Adds a movie using search text and tmdb id (i.e., movie add John Wick 484737). The easiest" +
    " way to use this command is to use \"movie find new TITLE\", then the results will contain the movie add command for you") {
    @Override
    public CommandResponse execute(String command) {
      int lastSpace = command.lastIndexOf(" ");
      String searchText = command.substring(0, lastSpace);
      String id = command.substring(lastSpace + 1, command.length());
      return new CommandResponse(new RadarrApi().add(searchText, id));
    }
  },
  ADD_TITLE_MOVIE("movie title add", "Adds a movie with just a title. Since many movies can have same title or very similar titles, the trakt" +
    " search can return multiple movies, if we detect multiple new films, we will return those films, otherwise we will add the single film.") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(new RadarrApi().addTitle(command));
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
  FIND_MOVIE_DOWNLOADS("movie find downloads", "Lists all the available torrents for a movie (i.e., movie find downloads TITLE OF MOVIE). " +
    "You can get the title by using \"movie find existing\". This can be a SLOW operation depending on the number of indexers configured" +
    " in your Radarr settings and particularly how fast each indexer is.") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(new RadarrApi().lookupTorrents(command));
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
  CANCEL_DOWNLOAD("movie cancel download", "Cancels a download") {
    @Override
    public CommandResponse execute(String command) {
      try {
        Long id = Long.valueOf(command);
        return new CommandResponse(new RadarrApi().cancelDownload(id));
      } catch (NumberFormatException e) {
        return new CommandResponse(EmbedHelper.createErrorMessage("Require an id value to cancel a download, e=" + e.getMessage()));
      }
    }
  },
  HELP("help", "") {
    @Override
    public CommandResponse execute(String command) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setTitle("Commands");
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
