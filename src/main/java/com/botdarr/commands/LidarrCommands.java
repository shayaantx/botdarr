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
        public CommandResponse<? extends ChatClientResponse> execute(String artist) {
          return new CommandResponse<>(lidarrApi.addArtist(artist));
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
      //TODO: add by artist id
      //TODO: lookup by album
      //TODO: add by album id
      //TODO: search by existing track
    }};
  }
}
