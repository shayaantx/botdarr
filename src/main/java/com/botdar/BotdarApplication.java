package com.botdar;

import com.botdar.commands.Command;
import com.botdar.commands.CommandResponse;
import com.botdar.discord.EmbedHelper;
import com.botdar.radarr.RadarrApi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BotdarApplication {
	public static void main(String[] args) throws Exception {
    JDA jda = new JDABuilder(Config.getProperty(Config.Constants.TOKEN)).addEventListeners(new ListenerAdapter() {
      @Override
      public void onReady(@Nonnull ReadyEvent event) {
        Executors.newScheduledThreadPool(1).schedule(new Runnable() {
          @Override
          public void run() {
            new RadarrApi().sendPeriodNotifications(event.getJDA());
          }
        }, 1, TimeUnit.MINUTES);
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
