package com.botdarr.commands;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

import java.util.ArrayList;
import java.util.List;

public class HelpCommands {
  public static List<Command> getCommands(ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder,
                                          List<Command> radarrCommands,
                                          List<Command> sonarrCommands) {
    return new ArrayList<Command>() {{
      add(new BaseCommand("help", "") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(chatClientResponseBuilder.getHelpResponse());
        }
      });
      add(new BaseCommand("movies help", "") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(chatClientResponseBuilder.getMoviesHelpResponse(radarrCommands));
        }
      });
      add(new BaseCommand("shows help", "") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(chatClientResponseBuilder.getShowsHelpResponse(sonarrCommands));
        }
      });
    }};
  }
}
