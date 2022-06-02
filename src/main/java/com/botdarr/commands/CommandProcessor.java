package com.botdarr.commands;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.ErrorResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

public class CommandProcessor {
  public <T extends ChatClientResponse> List<CommandResponse> processCommand(String commandPrefix,
                                                                             List<Command> apiCommands,
                                                                             String strippedMessage) {
    try {
      String rawMessage = strippedMessage.toLowerCase();

      final boolean isFreeFormCommand = rawMessage.startsWith(commandPrefix);
      final String processedCommand;
      if (isFreeFormCommand) {
        //remove the prefix character from the message
        processedCommand = rawMessage.trim().substring(1);
      } else {
        processedCommand = rawMessage;
      }
      for (Command apiCommand : apiCommands) {
        String command = apiCommand.getIdentifier().toLowerCase();
        boolean foundCommand = apiCommand.hasArguments() ? processedCommand.startsWith(command) : processedCommand.equalsIgnoreCase(command);
        if (foundCommand) {
          String commandOperation = processedCommand.replaceAll(command, "");
          return apiCommand.execute(commandOperation.trim());
        }
      }
      if (isFreeFormCommand) {
        // only print this command for commands that come in with a prefix
        // since these are free form and not automatically created by something like slash commands
        return Collections.singletonList(new ErrorResponse("Invalid command - type " + commandPrefix + "help for command usage"));
      }
      return Collections.emptyList();
    } catch (Throwable e) {
      LOGGER.error("Error trying to execute command " + strippedMessage, e);
      return Collections.singletonList(new ErrorResponse("Error trying to parse command " + strippedMessage  + ", error=" + e.getMessage()));
    }
  }

  private static final Logger LOGGER = LogManager.getLogger(CommandProcessor.class);
}
