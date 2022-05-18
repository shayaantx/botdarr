package com.botdarr.api.lidarr;

import com.botdarr.Config;
import com.botdarr.api.ContentType;
import com.botdarr.api.lidarr.LidarrApi;
import com.botdarr.commands.*;
import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.InfoResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LidarrCommands {
  public static List<Command> getCommands(LidarrApi lidarrApi) {
    return new ArrayList<Command>() {{
      add(new BaseCommand(
              "music artist add",
              "Adds an artist using search text (i.e., music add Dre Fudgington)",
              Collections.singletonList("artist-name")) {
        @Override
        public List<CommandResponse> execute(String artistToSearch) {
          return lidarrApi.addArtist(artistToSearch);
        }
      });
      add(new BaseCommand(
              "music artist id add",
              "Adds an artist using lidarr artist id and artist name (i.e., music artist id add F894MN4-F84J4 Beastie Girls).",
              Arrays.asList("lidar-artist-id", "artist-name")) {
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
      add(new BaseCommand(
              "music artist find existing",
              "Finds an existing artist using lidarr (i.e., music artist find existing ArtistA)",
              Collections.singletonList("artist-name")) {
        @Override
        public List<CommandResponse> execute(String command) {
          return lidarrApi.lookupArtists(command, false);
        }
      });
      add(new BaseCommand(
              "music artist find new",
              "Finds a new artist using lidarr (i.e., music find new artist ArtistB)",
              Collections.singletonList("artist-name")) {
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
          return new CommandResponseUtil().addEmptyDownloadsMessage(lidarrApi.downloads(), ContentType.ARTIST);
        }
      });
    }};
  }

  public static String getAddArtistCommandStr(String artistName, String foreignArtistId) {
    return CommandContext.getConfig().getPrefix() + "music artist id add " + artistName + " " + foreignArtistId;
  }

  public static String getHelpCommandStr() {
    return CommandContext.getConfig().getPrefix() + "music help";
  }
}
