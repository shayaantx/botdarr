package com.botdar;

import com.botdar.commands.*;
import com.botdar.discord.EmbedHelper;
import com.botdar.radarr.RadarrApi;
import com.botdar.scheduling.Scheduler;
import com.botdar.sonarr.SonarrApi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotdarApplication {
  public static void main(String[] args) throws Exception {
    try {
      JDA jda = new JDABuilder(Config.getProperty(Config.Constants.TOKEN)).addEventListeners(new ListenerAdapter() {
        @Override
        public void onReady(@Nonnull ReadyEvent event) {
          LOGGER.info("Connected to discord");
          JDA readyEventJda = event.getJDA();
          List<Api> apis = Arrays.asList(new RadarrApi(), new SonarrApi());
          Scheduler scheduler = Scheduler.getScheduler();
          scheduler.initApiNotifications(apis, readyEventJda);
          scheduler.initApiCaching(apis, readyEventJda);
          super.onReady(event);
        }

        @Override
        public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
          try {
            Context.getConfig().setUsername(event.getAuthor().getName());
            processMessage(event);
            super.onMessageReceived(event);
          } finally {
            Context.reset();
          }
        }
      }).build();
      jda.awaitReady();
    } catch (Throwable e) {
      LOGGER.error("Error caught during main", e);
      throw e;
    }
  }

  private static void processMessage(@Nonnull MessageReceivedEvent event) {
    String strippedMessage = event.getMessage().getContentStripped();
    try {
      for (Command command : ALL_COMMANDS) {
        if (strippedMessage.startsWith(command.getIdentifier())) {
          String commandOperation = strippedMessage.replaceAll(command.getIdentifier(), "");
          CommandResponse response = command.execute(commandOperation.trim());
          response.send(event.getChannel());
          return;
        }
      }
    } catch (Exception e) {
      LOGGER.error("Error trying to execute command " + strippedMessage, e);
      new CommandResponse(EmbedHelper.createErrorMessage("Error trying to parse command " + strippedMessage)).send(event.getChannel());
    }
  }

  private static final List<Command> ALL_COMMANDS = new ArrayList<Command>() {{
    addAll(Arrays.asList(HelpCommands.values()));
    addAll(Arrays.asList(RadarrCommands.values()));
    addAll(Arrays.asList(SonarrCommands.values()));
  }};
  private static final Logger LOGGER = LogManager.getLogger(BotdarApplication.class);
}
