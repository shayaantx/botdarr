package com.botdarr.commands;

import com.botdarr.api.sonarr.SonarrApi;
import com.botdarr.clients.ChatClientResponse;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.List;

public class SonarrCommands {
  public static List<Command> getCommands(SonarrApi sonarrApi) {
    return new ArrayList<Command>() {{
      add(new BaseCommand("show id add", "Adds a show using search text and tmdb id (i.e., show id add 30 rock 484737). The easiest" +
        " way to use this command is to use \"show find new TITLE\", then the results will contain the show add command for you") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          int lastSpace = command.lastIndexOf(" ");
          if (lastSpace == -1) {
            throw new RuntimeException("Missing expected arguments - usage: show id add SHOW_TITLE_HERE SHOW_ID_HERE");
          }
          String searchText = command.substring(0, lastSpace);
          String id = command.substring(lastSpace + 1);
          validateShowTitle(searchText);
          validateShowId(id);
          return new CommandResponse(sonarrApi.addWithId(searchText, id));
        }
      });
      add(new BaseCommand("show title add", "Adds a show with just a title. Since there can be multiple shows that match search criteria" +
        " we will either add the show or return all the shows that match your search.") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          validateShowTitle(command);
          return new CommandResponse(sonarrApi.addWithTitle(command));
        }
      });
      add(new BaseCommand("show downloads", "Shows all the active shows downloading in sonarr") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(sonarrApi.downloads());
        }
      });
      add(new BaseCommand("show profiles", "Displays all the profiles available to search for shows under (i.e., show add ANY)") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(sonarrApi.getProfiles());
        }
      });
      add(new BaseCommand("show find existing", "Finds a existing show using sonarr (i.e., show find existing Ahh! Real fudgecakes)") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          validateShowTitle(command);
          return new CommandResponse(sonarrApi.lookup(command, false));
        }
      });
      add(new BaseCommand("show find new", "Finds a new show using sonarr (i.e., show find new Fresh Prince of Fresh air)") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          validateShowTitle(command);
          return new CommandResponse(sonarrApi.lookup(command, true));
        }
      });
    }};
  }

  public static String getAddShowCommandStr(String title, long tvdbId) {
    return new CommandProcessor().getPrefix() + "show id add " + title + " " + tvdbId;
  }

  public static String getHelpShowCommandStr() {
    return new CommandProcessor().getPrefix() + "shows help";
  }

  private static void validateShowTitle(String movieTitle) {
    if (Strings.isEmpty(movieTitle)) {
      throw new IllegalArgumentException("Show title is missing");
    }
  }

  private static void validateShowId(String id) {
    if (Strings.isEmpty(id)) {
      throw new IllegalArgumentException("Show id is missing");
    }
    try {
      Integer.valueOf(id);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Show id is not a number");
    }
  }
}