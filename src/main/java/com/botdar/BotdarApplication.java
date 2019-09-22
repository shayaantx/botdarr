package com.botdar;

import com.botdar.commands.Command;
import com.botdar.commands.CommandResponse;
import com.botdar.discord.EmbedHelper;
import com.botdar.radarr.RadarrApi;
import com.botdar.scheduling.Scheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
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
          List<Api> apis = Arrays.asList(RadarrApi.get());
          Scheduler scheduler = Scheduler.getScheduler();
          scheduler.initApiNotifications(apis, readyEventJda);
          scheduler.initApiCaching(apis, readyEventJda);
          super.onReady(event);
        }

        @Override
        public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
          executeCommand(event);
          super.onMessageReceived(event);
        }
      }).build();
      jda.awaitReady();
    } catch (Throwable e) {
	    LOGGER.error("Error caught during main", e);
	    throw e;
    }
	}

	private static void executeCommand(@Nonnull MessageReceivedEvent event) {
    String strippedMessage = event.getMessage().getContentStripped();
	  try {
      for (Command command : Command.values()) {
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

  private static final Logger LOGGER = LogManager.getLogger();
}
