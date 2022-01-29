package com.botdarr.api.lidarr;

import com.botdarr.api.lidarr.LidarrApi;
import com.botdarr.commands.BaseCommand;
import com.botdarr.commands.Command;
import com.botdarr.commands.CommandProcessor;
import com.botdarr.commands.responses.CommandResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LidarrCommands {
  public static List<Command> getCommands(LidarrApi lidarrApi) {
    return new ArrayList<Command>() {{
      add(new BaseCommand("music artist add", "music artist add <artist-name>",
        "Adds an artist using search text (i.e., music add Dre Fudgington)") {
        @Override
        public List<CommandResponse> execute(String artistToSearch) {
          return lidarrApi.addArtist(artistToSearch);
        }
      });
      add(new BaseCommand("music artist id add", "music artist id add <lidarr-artist-id> <artist-name>",
        "Adds an artist using lidarr artist id and artist name (i.e., music artist id add F894MN4-F84J4 Beastie Girls). The easiest" +
        " way to use this command is using the find commands to find new artists, which have the add commands or you can use thumbs reaction (in slack/discord)") {
        @Override
        public List<CommandResponse> execute(String command) {
          int lastSpace = command.lastIndexOf(" ");
          if (lastSpace == -1) {
            throw new RuntimeException("Missing expected arguments - usage: music artist id add ARTIST_NAME_HERE ARTIST_ID_HERE");
          }
          String searchText = command.substring(0, lastSpace);
          String id = command.substring(lastSpace + 1);
          return Collections.singletonList(lidarrApi.addArtistWithId(id, searchText));
        }
      });
      add(new BaseCommand("music artist find existing", "music artist find existing <artist-name>",
        "Finds an existing artist using lidarr (i.e., music artist find existing ArtistA)") {
        @Override
        public List<CommandResponse> execute(String command) {
          return lidarrApi.lookupArtists(command, false);
        }
      });
      add(new BaseCommand("music artist find new", "music artist find new <artist-name>",
        "Finds a new artist using lidarr (i.e., music find new artist ArtistB)") {
        @Override
        public List<CommandResponse> execute(String command) {
          return lidarrApi.lookupArtists(command, true);
        }
      });
      add(new BaseCommand("music downloads", "Shows all the active artist downloading in lidarr") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public List<CommandResponse> execute(String command) {
          return lidarrApi.downloads();
        }
      });
    }};
  }

  public static String getAddArtistCommandStr(String artistName, String foreignArtistId) {
    return new CommandProcessor().getPrefix() + "music artist id add " + artistName + " " + foreignArtistId;
  }

  public static String getHelpCommandStr() {
    return new CommandProcessor().getPrefix() + "music help";
  }
}
