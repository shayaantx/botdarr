package com.botdarr.commands;

import com.botdarr.api.SonarrApi;
import com.botdarr.clients.ChatClientResponse;

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
          String searchText = command.substring(0, lastSpace);
          String id = command.substring(lastSpace + 1);
          return new CommandResponse(sonarrApi.addWithId(searchText, id));
        }
      });
      add(new BaseCommand("show title add", "Adds a show with just a title. Since there can be multiple shows that match search criteria" +
        " we will either add the show or return all the shows that match your search.") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(sonarrApi.addWithTitle(command));
        }
      });
      add(new BaseCommand("show downloads", "Shows all the active shows downloading in sonarr") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(sonarrApi.downloads());
        }
      });
      add(new BaseCommand("show profiles", "Displays all the profiles available to search for shows under (i.e., show add ANY)") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(sonarrApi.getProfiles());
        }
      });
      add(new BaseCommand("show find existing", "Finds a existing show using sonarr (i.e., show find existing Ahh! Real fudgecakes)") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(sonarrApi.lookup(command, false));
        }
      });
      add(new BaseCommand("show find new", "Finds a new show using sonarr (i.e., show find new Fresh Prince of Fresh air)") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(sonarrApi.lookup(command, true));
        }
      });
    }};
  }
}