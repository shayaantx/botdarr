package com.botdar;

import com.botdar.commands.Command;
import com.botdar.commands.CommandResponse;
import com.botdar.radarr.RadarrApi;
import com.botdar.scheduling.Scheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class BotdarApplication {
	public static void main(String[] args) throws Exception {
    JDA jda = new JDABuilder(Config.getProperty(Config.Constants.TOKEN)).addEventListeners(new ListenerAdapter() {
      @Override
      public void onReady(@Nonnull ReadyEvent event) {
        JDA readyEventJda = event.getJDA();
        List<Api> apis = Arrays.asList(RadarrApi.get());
        Scheduler scheduler = Scheduler.getScheduler();
        scheduler.initNotifications(apis, readyEventJda);
        scheduler.initCaching(apis, readyEventJda);
        super.onReady(event);
      }

      @Override
      public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        executeCommand(event);
        super.onMessageReceived(event);
      }
    }).build();
    jda.awaitReady();
	}

	private static void executeCommand(@Nonnull MessageReceivedEvent event) {
    String strippedMessage = event.getMessage().getContentStripped();
    for (Command command : Command.values()) {
      if (strippedMessage.startsWith(command.getIdentifier())) {
        String commandOperation = strippedMessage.replaceAll(command.getIdentifier(), "");
        CommandResponse response = command.execute(commandOperation.trim());
        response.send(event.getChannel());
        return;
      }
    }
  }
}
