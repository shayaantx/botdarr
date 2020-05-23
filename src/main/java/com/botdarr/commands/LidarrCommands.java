package com.botdarr.commands;

import com.botdarr.api.lidarr.LidarrApi;
import com.botdarr.clients.ChatClientResponse;

import java.util.ArrayList;
import java.util.List;

public class LidarrCommands {
  public static List<Command> getCommands(LidarrApi lidarrApi) {
    return new ArrayList<Command>() {{
      add(new BaseCommand("music artist add", "Adds an artist using search text (i.e., music add <ARTIST-NAME-HERE>") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String artistToSearch) {
          return new CommandResponse<>(lidarrApi.addArtist(artistToSearch));
        }
      });
      add(new BaseCommand("music artist id add", "") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse<>(lidarrApi.addArtist(command));
        }
      });
      add(new BaseCommand("music artist find existing", "") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse<>(lidarrApi.lookupArtists(command, false));
        }
      });
      add(new BaseCommand("music artist find new", "") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse<>(lidarrApi.lookupArtists(command, true));
        }
      });
      //TODO: lookup by album
      //TODO: add by album id
      //TODO: search by existing track
    }};
  }

  public static String getAddArtistCommandStr(String artistName, String foreignArtistId) {
    return new CommandProcessor().getPrefix() + "music artist id add " + artistName + " " + foreignArtistId;
  }

  public static String getHelpCommandStr() {
    return new CommandProcessor().getPrefix() + "music help";
  }
}
