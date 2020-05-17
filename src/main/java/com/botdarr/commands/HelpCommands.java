package com.botdarr.commands;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

import java.util.ArrayList;
import java.util.List;

public class HelpCommands {
  public static List<Command> getCommands(ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder,
                                          List<Command> radarrCommands,
                                          List<Command> sonarrCommands,
                                          List<Command> lidarrCommands) {
    return new ArrayList<Command>() {{
      add(new BaseHelpCommand("help", "") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(chatClientResponseBuilder.getHelpResponse());
        }
      });
      add(new BaseHelpCommand("movies help", "") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(chatClientResponseBuilder.getMoviesHelpResponse(radarrCommands));
        }
      });
      add(new BaseHelpCommand("shows help", "") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(chatClientResponseBuilder.getShowsHelpResponse(sonarrCommands));
        }
      });
      add(new BaseHelpCommand("music help", "") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(chatClientResponseBuilder.getMusicHelpResponse(lidarrCommands));
        }
      });
    }};
  }

  private static abstract class BaseHelpCommand extends BaseCommand {

    public BaseHelpCommand(String commandText, String description) {
      super(commandText, description);
    }

    @Override
    public boolean hasArguments() {
      return false;
    }
  }
}
