package com.botdarr.api.sonarr;

import com.botdarr.api.ContentType;
import com.botdarr.commands.*;
import com.botdarr.commands.responses.CommandResponse;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SonarrCommands {
  public static List<Command> getCommands(SonarrApi sonarrApi) {
    return new ArrayList<Command>() {{
      add(new BaseCommand(
              "show id add",
        "Adds a show using search text and tmdb id (i.e., show id add 30 clock 484767)",
              Arrays.asList("show-title", "show-tvdbid")) {
        @Override
        public List<CommandResponse> execute(String command) {
          int lastSpace = command.lastIndexOf(" ");
          if (lastSpace == -1) {
            throw new RuntimeException("Missing expected arguments - usage: show id add SHOW_TITLE_HERE SHOW_ID_HERE");
          }
          String searchText = command.substring(0, lastSpace);
          String id = command.substring(lastSpace + 1);
          validateShowTitle(searchText);
          validateShowId(id);
          return Collections.singletonList(sonarrApi.addWithId(searchText, id));
        }
      });
      add(new BaseCommand(
              "show title add",
        "Adds a show with just a title.",
              Collections.singletonList("show-title")) {
        @Override
        public List<CommandResponse> execute(String command) {
          validateShowTitle(command);
          return sonarrApi.addWithTitle(command);
        }
      });
      add(new BaseCommand("show downloads", "Shows all the active shows downloading in sonarr") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public List<CommandResponse> execute(String command) {
          return new CommandResponseUtil().addEmptyDownloadsMessage(sonarrApi.downloads(), ContentType.SHOW);
        }
      });
      add(new BaseCommand("show profiles", "Displays all the profiles available to search for shows under (i.e., show add ANY)") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public List<CommandResponse> execute(String command) {
          return sonarrApi.getProfiles();
        }
      });
      add(new BaseCommand(
              "show find existing",
              "Finds a existing show using sonarr (i.e., show find existing Ahh! Real fudgecakes)",
              Collections.singletonList("show-title")) {
        @Override
        public List<CommandResponse> execute(String command) {
          validateShowTitle(command);
          return sonarrApi.lookup(command, false);
        }
      });
      add(new BaseCommand(
              "show find new",
              "Finds a new show using sonarr (i.e., show find new Fresh Prince of Fresh air)",
              Collections.singletonList("show-title")) {
        @Override
        public List<CommandResponse> execute(String command) {
          validateShowTitle(command);
          return sonarrApi.lookup(command, true);
        }
      });
    }};
  }

  public static String getAddShowCommandStr(String title, long tvdbId) {
    return CommandContext.getConfig().getPrefix() + "show id add " + title + " " + tvdbId;
  }

  public static String getHelpShowCommandStr() {
    return CommandContext.getConfig().getPrefix() + "shows help";
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