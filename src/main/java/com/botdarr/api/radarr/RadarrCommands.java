package com.botdarr.api.radarr;

import com.botdarr.api.ContentType;
import com.botdarr.commands.*;
import com.botdarr.commands.responses.CommandResponse;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RadarrCommands {
  public static List<Command> getCommands(RadarrApi radarrApi) {
    return new ArrayList<Command>() {{
      add(new BaseCommand("movie discover", "Finds new movies based on radarr recommendations (from trakt)") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public List<CommandResponse> execute(String command) {
          return radarrApi.discover();
        }
      });
      add(new BaseCommand(
              "movie id add",
              "Adds a movie using search text and tmdb id (i.e., movie id add John Wick 484737).",
              Arrays.asList("movie-title", "movie-tmdbid")) {
        @Override
        public List<CommandResponse> execute(String command) {
          int lastSpace = command.lastIndexOf(" ");
          if (lastSpace == -1) {
            throw new RuntimeException("Missing expected arguments - usage: movie id add MOVIE_TITLE_HERE MOVIE_ID_HERE");
          }
          String searchText = command.substring(0, lastSpace);
          String id = command.substring(lastSpace + 1);
          validateMovieTitle(searchText);
          validateMovieId(id);
          return Collections.singletonList(radarrApi.addWithId(searchText, id));
        }
      });
      add(new BaseCommand(
              "movie title add",
              "Adds a movie with just a title. Since many movies can have same title or very similar titles",
              Collections.singletonList("movie-title")) {
        @Override
        public List<CommandResponse> execute(String searchText) {
          validateMovieTitle(searchText);
          return radarrApi.addWithTitle(searchText);
        }
      });
      add(new BaseCommand("movie profiles", "Displays all the profiles available to search for movies under (i.e., movie title add MOVIE_TITLE_HERE)") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public List<CommandResponse> execute(String command) {
          return radarrApi.getProfiles();
        }
      });
      add(new BaseCommand(
              "movie find new",
              "Finds a new movie using radarr (i.e., movie find new John Wick)",
              Collections.singletonList("movie-title")) {
        @Override
        public List<CommandResponse> execute(String searchText) {
          validateMovieTitle(searchText);
          return radarrApi.lookup(searchText, true);
        }
      });
      add(new BaseCommand(
              "movie find existing",
              "Finds an existing movie using radarr (i.e., movie find existing Princess Fudgecake)",
              Collections.singletonList("movie-title")) {
        @Override
        public List<CommandResponse> execute(String searchText) {
          validateMovieTitle(searchText);
          return radarrApi.lookup(searchText, false);
        }
      });
      add(new BaseCommand("movie downloads", "Shows all the active movies downloading in radarr") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public List<CommandResponse> execute(String command) {
          return new CommandResponseUtil().addEmptyDownloadsMessage(radarrApi.downloads(), ContentType.MOVIE);
        }
      });
    }};
  }

  public static String getAddMovieCommandStr(String title, long tmdbId) {
    return CommandContext.getConfig().getPrefix() + "movie id add " + title + " " + tmdbId;
  }

  public static String getHelpMovieCommandStr() {
    return CommandContext.getConfig().getPrefix() + "movies help";
  }

  private static void validateMovieTitle(String movieTitle) {
    if (Strings.isEmpty(movieTitle)) {
      throw new IllegalArgumentException("Movie title is missing");
    }
  }

  private static void validateMovieId(String id) {
    if (Strings.isEmpty(id)) {
      throw new IllegalArgumentException("Movie id is missing");
    }
    try {
      Integer.valueOf(id);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Movie id is not a number");
    }
  }
}
