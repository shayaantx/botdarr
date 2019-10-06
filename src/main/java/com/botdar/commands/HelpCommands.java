package com.botdar.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

public enum HelpCommands implements Command {
  HELP("help", "") {
    @Override
    public CommandResponse execute(String command) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setTitle("Commands");
      addVersionToMessage(embedBuilder);
      embedBuilder.addField("movies help", "Shows all the commands for movies", false);
      embedBuilder.addField("shows help", "Shows all the commands for shows", false);
      return new CommandResponse(embedBuilder.build());
    }
  },
  HELP_MOVIES("movies help", "") {
    @Override
    public CommandResponse execute(String command) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setTitle("Commands");
      for (RadarrCommands com : RadarrCommands.values()) {
        embedBuilder.addField(com.commandText, com.description, false);
      }
      return new CommandResponse(embedBuilder.build());
    }
  },
  HELP_SHOWS("shows help", "") {
    @Override
    public CommandResponse execute(String command) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setTitle("Commands");
      for (SonarrCommands com : SonarrCommands.values()) {
        embedBuilder.addField(com.commandText, com.description, false);
      }
      return new CommandResponse(embedBuilder.build());
    }
  };
  private HelpCommands(String commandText, String description) {
    this.commandText = commandText;
    this.description = description;
  }

  @Override
  public String getIdentifier() {
    return commandText.toLowerCase();
  }

  /**
   * We add version.txt during the build of the jar (in the Jenkinsfile)
   * so local builds won't show it
   * @param embedBuilder EmbedBuilder
   */
  private static void addVersionToMessage(EmbedBuilder embedBuilder) {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream is = classloader.getResourceAsStream("version.txt");
    if (is != null) {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()))) {
        String versionTxt = br.lines().collect(Collectors.joining(System.lineSeparator()));
        embedBuilder.setDescription(versionTxt);
      } catch (IOException e) {
        LOGGER.error("Error trying to get version.txt", e);
      }
    }
  }

  private final String description;
  private final String commandText;
  private static final Logger LOGGER = LogManager.getLogger();
}
