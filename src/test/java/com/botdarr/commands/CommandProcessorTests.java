package com.botdarr.commands;

import com.botdarr.TestResponse;
import com.botdarr.TestResponseBuilder;
import com.botdarr.api.RadarrApi;
import com.botdarr.api.SonarrApi;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
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
  public void processMessage_missingMovieTitleAddCommand() {
    validateInvalidCommand("!movie id add 541515", "");
  }

  private void validateNoArgCommands(String command) {
    //validate an extra character after the command is invalid
    validateInvalidCommandIdentifier(command + "x");
    //validate an extra space around the extra character after the command is invalid
    validateInvalidCommandIdentifier(command + " x ");
    //validate an extra space after the command is trimmed and the command is still valid
    validateValidCommandIdentifier(command + " ");
    //validate the command itself is valid
    validateValidCommandIdentifier(command);
  }

  private void validateInvalidCommandIdentifier(String invalidCommand) {
    validateInvalidCommand(invalidCommand, "Invalid command - type !help for command usage");
  }

  private void validateValidCommandIdentifier(String validCommand) {
    CommandProcessor commandProcessor = new CommandProcessor();
    CommandResponse<TestResponse> commandResponse =
      commandProcessor.processMessage(getCommandsToTest(), validCommand, "user1", responseBuilder);
    //we are just making sure no response is returned as another tests validates that part of the command/api
    if (commandResponse.getSingleChatClientResponse() != null) {
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
    List<Command> commands = new ArrayList<>(HelpCommands.getCommands(responseBuilder, radarrCommands, sonarrCommands));
    commands.addAll(radarrCommands);
    commands.addAll(sonarrCommands);
    return commands;
  }

  @Injectable
  private RadarrApi radarrApi;

  @Injectable
  private SonarrApi sonarrApi;

  private TestResponseBuilder responseBuilder = new TestResponseBuilder();
}
