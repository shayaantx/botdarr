package com.botdar.commands;

import com.botdar.sonarr.SonarrApi;

public enum SonarrCommands implements Command {
  ADD_ID_SHOW("show id add", "Adds a show using search text and tmdb id (i.e., show id add 30 rock 484737). The easiest" +
    " way to use this command is to use \"show find new TITLE\", then the results will contain the show add command for you") {
    @Override
    public CommandResponse execute(String command) {
      int lastSpace = command.lastIndexOf(" ");
      String searchText = command.substring(0, lastSpace);
      String id = command.substring(lastSpace + 1);
      return new CommandResponse(SONARR_API.addWithId(searchText, id));
    }
  },
  ADD_TITLE_SHOW("show title add", "Adds a show with just a title. Since there can be multiple shows that match search criteria" +
    " we will either add the show or return all the shows that match your search.") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(SONARR_API.addWithTitle(command));
    }
  },
  SHOW_DOWNLOADS("show downloads", "Shows all the active shows downloading in sonarr") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(SONARR_API.downloads());
    }
  },
  SHOW_PROFILES("show profiles", "Displays all the profiles available to search for shows under (i.e., show add ANY)") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(SONARR_API.getProfiles());
    }
  },
  FIND_EXISTING_SHOW("show find existing", "Finds a existing show using sonarr (i.e., show find existing Ahh! Real fudgecakes)") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(SONARR_API.lookup(command, false));
    }
  },
  FIND_NEW_SHOW("show find new", "Finds a new show using sonarr (i.e., show find new Fresh Prince of Fresh air)") {
    @Override
    public CommandResponse execute(String command) {
      return new CommandResponse(SONARR_API.lookup(command, true));
    }
  };

  private SonarrCommands(String commandText, String description) {
    this.description = description;
    this.commandText = commandText;
  }

  @Override
  public String getIdentifier() {
    return commandText.toLowerCase();
  }

  protected final String description;
  protected final String commandText;
  private static final SonarrApi SONARR_API = new SonarrApi();
}
