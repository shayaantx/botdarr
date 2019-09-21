package com.botdar.commands;

import com.botdar.discord.EmbedHelper;
import com.botdar.radarr.RadarrApi;
import net.dv8tion.jda.api.EmbedBuilder;

public enum Command {
  ADD_MOVIE("movie add", "Adds a movie using search text and tmdb id (i.e., movie add John Wick 484737). The easiest" +
    " way to use this command is to use \"movie find new TITLE\", then the results will contain the movie add command for you") {
    @Override
    public CommandResponse execute(String command) {
      int lastSpace = command.lastIndexOf(" ");
      String searchText = command.substring(0, lastSpace);
      String id = command.substring(lastSpace + 1);
      return new CommandResponse(RADARR_API.add(searchText, id));
    }
  },
  ADD_TITLE_MOVIE("movie title add", "Adds a movie with just a title. Since many movies can have same title or very similar titles, the trakt" +
    " search can return multiple movies, if we detect multiple new films, we will return those films, otherwise we will add the single film.") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(RADARR_API.addTitle(command));
    }
  },
  PROFILES("movie profiles", "Displays all the profiles available to search for movies under (i.e., movie add ANY)") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(RADARR_API.getProfiles());
    }
  },
  FIND_NEW_MOVIE("movie find new", "Finds a new movie using radarr (i.e., movie find John Wick)") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(RADARR_API.lookup(command, true));
    }
  },
  FIND_EXISTING_MOVIE("movie find existing", "Finds an existing movie using radarr (i.e., movie find Princess Fudgecake)") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(RADARR_API.lookup(command, false));
    }
  },
  FIND_MOVIE_DOWNLOADS("movie find downloads", "Lists all the available (not rejected) torrents for a movie (i.e., movie find downloads TITLE OF MOVIE). " +
    "You can get the title by using \"movie find existing\". This can be a SLOW operation depending on the number of indexers configured" +
    " in your Radarr settings and particularly how fast each indexer is. Also these are torrents that have not been marked as rejected based" +
    " on whatever quality/profile settings are configured in Radarr") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(RadarrApi.get().lookupTorrents(command, false));
    }
  },
  FIND_MOVIE_ALL_DOWNLOADS("movie find all downloads", "List all the available torrents for a movie whether they are rejected by radarr or not") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(RadarrApi.get().lookupTorrents(command, true));
    }
  },
  MOVIE_DOWNLOADS("movie downloads", "Shows all the active movies downloading in radarr") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(RADARR_API.downloads());
    }
  },
  CANCEL_DOWNLOAD("movie cancel download", "Cancels a download") {
    @Override
    public CommandResponse execute(String command) {
      try {
        Long id = Long.valueOf(command);
        return new CommandResponse(RADARR_API.cancelDownload(id));
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
  private static final RadarrApi RADARR_API = RadarrApi.get();
}
