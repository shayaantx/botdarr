package com.botdarr.commands;

import com.botdarr.TestResponse;
import com.botdarr.TestResponseBuilder;
import com.botdarr.api.lidarr.LidarrApi;
import com.botdarr.api.radarr.RadarrApi;
import com.botdarr.api.sonarr.SonarrApi;
import mockit.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * These tests specifically test commands that take no arguments and commands that take arguments
 */
public class CommandProcessorTests {
  @Before
  public void beforeEachTest() {
    mockCommandPrefix("!");
  }

  @Test
  public void processMessage_validateHelpCommand() {
    validateInvalidCommandIdentifier("!helpx");
  }

  @Test
  public void processMessage_validateMoviesHelpCommand() {
    validateNoArgCommands("!movies help");
  }

  @Test
  public void processMessage_validateShowsHelpCommand() {
    validateNoArgCommands("!shows help");
  }

  @Test
  public void processMessage_validateMoviesDiscoverCommand() {
    validateNoArgCommands("!movie discover");
  }

  @Test
  public void processMessage_validateMovieProfilesCommand() {
    validateNoArgCommands("!movie profiles");
  }

  @Test
  public void processMessage_validateMovieDownloadsCommand() {
    validateNoArgCommands("!movie downloads");
  }

  @Test
  public void processMessage_validateShowDownloadsCommand() {
    validateNoArgCommands("!show downloads");
  }

  @Test
  public void processMessage_validateShowProfilesCommand() {
    validateNoArgCommands("!show profiles");
  }

  @Test
  public void processMessage_missingMovieTitleAndIdForAddCommand() {
    validateInvalidCommand("!movie id add",
      "Error trying to parse command !movie id add, " +
        "error=Missing expected arguments - usage: movie id add MOVIE_TITLE_HERE MOVIE_ID_HERE");
  }

  @Test
  public void processMessage_missingMovieTitleForAddCommand() {
    validateInvalidCommand("!movie id add 541515",
      "Error trying to parse command !movie id add 541515, " +
      "error=Missing expected arguments - usage: movie id add MOVIE_TITLE_HERE MOVIE_ID_HERE");
  }

  @Test
  public void processMessage_missingMovieIdForAddCommand() {
    validateInvalidCommand("!movie id add Princess5",
      "Error trying to parse command !movie id add Princess5, " +
        "error=Missing expected arguments - usage: movie id add MOVIE_TITLE_HERE MOVIE_ID_HERE");
  }

  @Test
  public void processMessage_invalidMovieIdForAddCommand() {
    validateInvalidCommand("!movie id add Princess5 4647x5",
      "Error trying to parse command !movie id add Princess5 4647x5, " +
        "error=Movie id is not a number");
  }

  @Test
  public void processMessage_validMovieTitleAndIdForAddCommand() {
    new Expectations() {{
      radarrApi.addWithId("princess5", "46475"); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!movie id add Princess5 46475");
  }

  @Test
  public void processMessage_validMovieWithSpacesInTitleForAddCommand() {
    new Expectations() {{
      radarrApi.addWithId("princess 5", "46475"); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!movie id add Princess 5 46475");
  }

  @Test
  public void processMessage_invalidMovieTitleForAddCommand() {
    validateInvalidCommand("!movie title add",
      "Error trying to parse command !movie title add, " +
        "error=Movie title is missing");
  }

  @Test
  public void processMessage_validMovieTitleForAddCommand() {
    new Expectations() {{
      radarrApi.addWithTitle("princess5"); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!movie title add Princess5");
  }

  @Test
  public void processMessage_validMovieTitleWithSpacesForAddCommand() {
    new Expectations() {{
      radarrApi.addWithTitle("princess 5"); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!movie title add Princess 5");
  }

  @Test
  public void processMessage_invalidMovieTitleForFindNewMovieCommand() {
    validateInvalidCommand("!movie find new",
      "Error trying to parse command !movie find new, " +
      "error=Movie title is missing");
  }

  @Test
  public void processMessage_validMovieTitleForFindNewMovieCommand() {
    new Expectations() {{
      radarrApi.lookup("princess5", true); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!movie find new Princess5");
  }

  @Test
  public void processMessage_validMovieTitleWithSpacesForFindNewMovieCommand() {
    new Expectations() {{
      radarrApi.lookup("princess 5", true); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!movie find new Princess 5");
  }

  @Test
  public void processMessage_invalidMovieTitleForFindExistingMovieCommand() {
    validateInvalidCommand("!movie find existing",
      "Error trying to parse command !movie find existing, " +
        "error=Movie title is missing");
  }

  @Test
  public void processMessage_validMovieTitleForFindExistingMovieCommand() {
    new Expectations() {{
      radarrApi.lookup("princess5", false); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!movie find existing Princess5");
  }

  @Test
  public void processMessage_validMovieTitleWithSpacesForFindExistingMovieCommand() {
    new Expectations() {{
      radarrApi.lookup("princess 5", false); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!movie find existing Princess 5");
  }

  @Test
  public void processMessage_missingShowTitleAndIdForAddCommand() {
    validateInvalidCommand("!show id add",
      "Error trying to parse command !show id add, " +
        "error=Missing expected arguments - usage: show id add SHOW_TITLE_HERE SHOW_ID_HERE");
  }

  @Test
  public void processMessage_missingShowTitleForAddCommand() {
    validateInvalidCommand("!show id add 541515",
      "Error trying to parse command !show id add 541515, " +
        "error=Missing expected arguments - usage: show id add SHOW_TITLE_HERE SHOW_ID_HERE");
  }

  @Test
  public void processMessage_missingShowIdForAddCommand() {
    validateInvalidCommand("!show id add Princess5",
      "Error trying to parse command !show id add Princess5, " +
        "error=Missing expected arguments - usage: show id add SHOW_TITLE_HERE SHOW_ID_HERE");
  }

  @Test
  public void processMessage_invalidShowIdForAddCommand() {
    validateInvalidCommand("!show id add Princess5 4647x5",
      "Error trying to parse command !show id add Princess5 4647x5, " +
        "error=Show id is not a number");
  }

  @Test
  public void processMessage_validShowTitleAndIdForAddCommand() {
    new Expectations() {{
      sonarrApi.addWithId("princess5", "46475"); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!show id add Princess5 46475");
  }

  @Test
  public void processMessage_invalidShowTitleForAddCommand() {
    validateInvalidCommand("!show title add",
      "Error trying to parse command !show title add, " +
        "error=Show title is missing");
  }

  @Test
  public void processMessage_validShowTitleForAddCommand() {
    new Expectations() {{
      sonarrApi.addWithTitle("princess5"); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!show title add Princess5");
  }

  @Test
  public void processMessage_validShowTitleWithSpacesForAddCommand() {
    new Expectations() {{
      sonarrApi.addWithTitle("princess 5"); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!show title add Princess 5");
  }

  @Test
  public void processMessage_invalidShowTitleForFindNewShowCommand() {
    validateInvalidCommand("!show find new",
      "Error trying to parse command !show find new, " +
        "error=Show title is missing");
  }

  @Test
  public void processMessage_validShowTitleForFindNewShowCommand() {
    new Expectations() {{
      sonarrApi.lookup("princess5", true); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!show find new Princess5");
  }

  @Test
  public void processMessage_validShowTitleWithSpacesForFindNewShowCommand() {
    new Expectations() {{
      sonarrApi.lookup("princess 5", true); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!show find new Princess 5");
  }

  @Test
  public void processMessage_invalidShowTitleForFindExistingShowCommand() {
    validateInvalidCommand("!show find existing",
      "Error trying to parse command !show find existing, " +
        "error=Show title is missing");
  }

  @Test
  public void processMessage_validShowTitleForFindExistingShowCommand() {
    new Expectations() {{
      sonarrApi.lookup("princess5", false); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!show find existing Princess5");
  }

  @Test
  public void processMessage_validShowTitleWithSpacesForFindExistingShowCommand() {
    new Expectations() {{
      sonarrApi.lookup("princess 5", false); times = 1; result = new TestResponse();
    }};
    validateValidCommand("!show find existing Princess 5");
  }

  private void validateNoArgCommands(String command) {
    //validate an extra character after the command is invalid
    validateInvalidCommandIdentifier(command + "x");
    //validate an extra space around the extra character after the command is invalid
    validateInvalidCommandIdentifier(command + " x ");
    //validate an extra space after the command is trimmed and the command is still valid
    validateValidCommand(command + " ");
    //validate the command itself is valid
    validateValidCommand(command);
  }

  private void validateInvalidCommandIdentifier(String invalidCommand) {
    validateInvalidCommand(invalidCommand, "Invalid command - type !help for command usage");
  }

  private void validateValidCommand(String validCommand) {
    CommandProcessor commandProcessor = new CommandProcessor();
    CommandResponse<TestResponse> commandResponse =
      commandProcessor.processMessage(getCommandsToTest(), validCommand, "user1", responseBuilder);
    //we are just making sure no response is returned as another tests validates that part of the command/api
    if (commandResponse.getSingleChatClientResponse() != null && commandResponse.getSingleChatClientResponse() instanceof TestResponse) {
      Assert.assertNull(commandResponse.getSingleChatClientResponse().getResponseMessage());
    }
  }

  private void validateInvalidCommand(String invalidCommand, String expectedMessage) {
    CommandProcessor commandProcessor = new CommandProcessor();
    CommandResponse<TestResponse> commandResponse =
      commandProcessor.processMessage(getCommandsToTest(), invalidCommand, "user1", responseBuilder);
    Assert.assertEquals(expectedMessage, commandResponse.getSingleChatClientResponse().getResponseMessage());
  }

  private void mockCommandPrefix(String prefix) {
    new MockUp<CommandProcessor>() {
      @Mock
      String getPrefix() {
        return prefix;
      }
    };
  }

  private List<Command> getCommandsToTest() {
    List<Command> radarrCommands = RadarrCommands.getCommands(radarrApi);
    List<Command> sonarrCommands = SonarrCommands.getCommands(sonarrApi);
    List<Command> lidarrCommands = LidarrCommands.getCommands(lidarrApi);
    List<Command> commands = new ArrayList<>(HelpCommands.getCommands(responseBuilder, radarrCommands, sonarrCommands, lidarrCommands));
    commands.addAll(radarrCommands);
    commands.addAll(sonarrCommands);
    commands.addAll(lidarrCommands);
    return commands;
  }

  @Injectable
  private RadarrApi radarrApi;

  @Injectable
  private SonarrApi sonarrApi;

  @Injectable
  private LidarrApi lidarrApi;

  private TestResponseBuilder responseBuilder = new TestResponseBuilder();
}
