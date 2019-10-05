package com.botdar.commands;

import com.botdar.sonarr.SonarrApi;

public enum SonarrCommands implements Command {
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
