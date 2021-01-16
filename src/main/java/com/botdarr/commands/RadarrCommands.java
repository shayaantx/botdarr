package com.botdarr.commands;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.api.radarr.RadarrApi;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
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
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse<>(radarrApi.discover());
        }
      });
      add(new BaseCommand("movie id add", "movie id add <movie-title> <movie-tmdbid>", "Adds a movie using search text and tmdb id (i.e., movie id add John Wick 484737). The easiest" +
        " way to use this command is to use \"movie find new TITLE\", then the results will contain the movie add command for you") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          int lastSpace = command.lastIndexOf(" ");
          if (lastSpace == -1) {
            throw new RuntimeException("Missing expected arguments - usage: movie id add MOVIE_TITLE_HERE MOVIE_ID_HERE");
          }
          String searchText = command.substring(0, lastSpace);
          String id = command.substring(lastSpace + 1);
          validateMovieTitle(searchText);
          validateMovieId(id);
          return new CommandResponse(radarrApi.addWithId(searchText, id));
        }
      });
      add(new BaseCommand("movie title add", "movie title add <movie-title>", "Adds a movie with just a title. Since many movies can have same title or very similar titles, the trakt" +
        " search can return multiple movies, if we detect multiple new films, we will return those films, otherwise we will add the single film.") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String searchText) {
          validateMovieTitle(searchText);
          return new CommandResponse(radarrApi.addWithTitle(searchText));
        }
      });
      add(new BaseCommand("movie profiles", "Displays all the profiles available to search for movies under (i.e., movie title add MOVIE_TITLE_HERE)") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(radarrApi.getProfiles());
        }
      });
      add(new BaseCommand("movie find new", "movie find new <movie-title>", "Finds a new movie using radarr (i.e., movie find new John Wick)") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String searchText) {
          validateMovieTitle(searchText);
          return new CommandResponse(radarrApi.lookup(searchText, true));
        }
      });
      add(new BaseCommand("movie find existing", "movie find existing <movie-title>", "Finds an existing movie using radarr (i.e., movie find existing Princess Fudgecake)") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String searchText) {
          validateMovieTitle(searchText);
          return new CommandResponse(radarrApi.lookup(searchText, false));
        }
      });
      add(new BaseCommand("movie downloads", "Shows all the active movies downloading in radarr") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(radarrApi.downloads());
        }
      });
    }};
  }

  public static String getAddMovieCommandStr(String title, long tmdbId) {
    return new CommandProcessor().getPrefix() + "movie id add " + title + " " + tmdbId;
  }

  public static String getHelpMovieCommandStr() {
    return new CommandProcessor().getPrefix() + "movies help";
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
