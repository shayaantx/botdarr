package com.botdarr.commands;

import com.botdarr.Config;
import com.botdarr.api.Api;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.util.List;

public class CommandProcessor {
  public <T extends ChatClientResponse, Z extends Api> CommandResponse processMessage(List<Command> apiCommands,
                                                                               String strippedMessage,
                                                                               String username,
                                                                               ChatClientResponseBuilder<T> chatClientResponseBuilder) {
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
        return new CommandResponse(chatClientResponseBuilder.createErrorMessage("Invalid command - type " + commandPrefix + "help for command usage"));
      }

    } catch (Exception e) {
      LOGGER.error("Error trying to execute command " + strippedMessage, e);
      return new CommandResponse(chatClientResponseBuilder.createErrorMessage("Error trying to parse command " + strippedMessage  + ", error=" + e.getMessage()));
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
