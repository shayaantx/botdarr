package com.botdarr.commands;

import com.botdarr.Config;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.ErrorResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.util.Collections;
import java.util.List;

public class CommandProcessor {
  public <T extends ChatClientResponse> List<CommandResponse> processRequestMessage(List<Command> apiCommands,
                                                                              String strippedMessage,
                                                                              String username) {
    try {
      String rawMessage = strippedMessage.toLowerCase();
      String commandPrefix = getPrefix();

      if (rawMessage.startsWith(commandPrefix)) {
        //remove the prefix character from the message
        String rawMessageWithoutPrefix = rawMessage.trim().substring(1);
        for (Command apiCommand : apiCommands) {
          String command = apiCommand.getIdentifier().toLowerCase();
          boolean foundCommand = apiCommand.hasArguments() ? rawMessageWithoutPrefix.startsWith(command) : rawMessageWithoutPrefix.equalsIgnoreCase(command);
          if (foundCommand) {
            String commandOperation = rawMessageWithoutPrefix.replaceAll(command, "");
            try {
              CommandContext
                .start()
                .setUsername(username);
              return apiCommand.execute(commandOperation.trim());
            } finally {
              CommandContext.end();
            }
          }
        }
        return Collections.singletonList(new ErrorResponse("Invalid command - type " + commandPrefix + "help for command usage"));
      }

    } catch (Throwable e) {
      LOGGER.error("Error trying to execute command " + strippedMessage, e);
      return Collections.singletonList(new ErrorResponse("Error trying to parse command " + strippedMessage  + ", error=" + e.getMessage()));
    }
    return null;
  }

  public String getPrefix() {
    String configuredPrefix = Config.getProperty(Config.Constants.COMMAND_PREFIX);
    if (!Strings.isEmpty(configuredPrefix)) {
      return configuredPrefix;
    }
    return "!";
  }

  private static final Logger LOGGER = LogManager.getLogger(CommandProcessor.class);
}
