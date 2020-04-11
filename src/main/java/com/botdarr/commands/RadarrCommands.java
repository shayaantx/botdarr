package com.botdarr.commands;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.api.RadarrApi;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
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
      add(new BaseCommand("movie id add", "Adds a movie using search text and tmdb id (i.e., movie id add John Wick 484737). The easiest" +
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
      add(new BaseCommand("movie title add", "Adds a movie with just a title. Since many movies can have same title or very similar titles, the trakt" +
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
      add(new BaseCommand("movie find new", "Finds a new movie using radarr (i.e., movie find new John Wick)") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String searchText) {
          validateMovieTitle(searchText);
          return new CommandResponse(radarrApi.lookup(searchText, true));
        }
      });
      add(new BaseCommand("movie find existing", "Finds an existing movie using radarr (i.e., movie find existing Princess Fudgecake)") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String searchText) {
          validateMovieTitle(searchText);
          return new CommandResponse(radarrApi.lookup(searchText, false));
        }
      });
      add(new BaseCommand("movie find downloads", "Lists all the available (not rejected) torrents for a movie (i.e., movie find downloads TITLE OF MOVIE). " +
        "You can get the title by using \"movie find existing\". This can be a SLOW operation depending on the number of indexers configured" +
        " in your Radarr settings and particularly how fast each indexer is. Also these are torrents that have not been marked as rejected based" +
        " on whatever quality/profile settings are configured in Radarr") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String searchText) {
          validateMovieTitle(searchText);
          return new CommandResponse(radarrApi.lookupTorrents(searchText, false));
        }
      });
      add(new BaseCommand("movie find all downloads", "List all the available torrents for a movie whether they are rejected by radarr or not") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String searchText) {
          validateMovieTitle(searchText);
          return new CommandResponse(radarrApi.lookupTorrents(searchText, true));
        }
      });
      add(new BaseCommand("movie hash download", "Force downloads a movie using a hash string, you can only get from the command 'movie find all downloads'") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(radarrApi.forceDownload(command));
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
      add(new BaseCommand("movie cancel download", "Cancels a download (NOT IMPLEMENTED YET)") {
        @Override
        public CommandResponse<? extends ChatClientResponse> execute(String command) {
          return new CommandResponse(radarrApi.cancelDownload(command));
        }
      });
    }};
  }

  public static String getAddMovieCommandStr(String title, long tmdbId) {
    return new CommandProcessor().getPrefix() + "movie id add " + title + " " + tmdbId;
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
