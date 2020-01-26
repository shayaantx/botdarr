package com.botdar.clients;

import com.botdar.api.*;
import com.botdar.Config;
import com.botdar.commands.*;
import com.botdar.discord.DiscordChatClient;
import com.botdar.discord.DiscordResponse;
import com.botdar.discord.DiscordResponseBuilder;
import com.botdar.scheduling.Scheduler;
import com.botdar.slack.SlackChatClient;
import com.botdar.slack.SlackMessage;
import com.botdar.slack.SlackResponse;
import com.botdar.slack.SlackResponseBuilder;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

public enum ChatClientType {
  DISCORD() {
    @Override
    public void init() throws Exception {
      try {
        ChatClientResponseBuilder<DiscordResponse> responseChatClientResponseBuilder = new DiscordResponseBuilder();
        ApisAndCommandConfig config = buildConfig(responseChatClientResponseBuilder);

        JDA jda = new JDABuilder(Config.getProperty(Config.Constants.DISCORD_TOKEN)).addEventListeners(new ListenerAdapter() {
          @Override
          public void onReady(@Nonnull ReadyEvent event) {
            LOGGER.info("Connected to discord");
            ChatClient chatClient = new DiscordChatClient(event.getJDA());
            //start the scheduler threads that send notifications and cache data periodically
            initScheduling(chatClient, config.apis);
            super.onReady(event);
          }

          @Override
          public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
            //build chat client
            ChatClient<DiscordResponse> discordChatClient = new DiscordChatClient(event.getJDA());

            //capture/process the command
            CommandResponse commandResponse = processMessage(
              config.commands,
              event.getMessage().getContentStripped(),
              event.getAuthor().getName(),
              responseChatClientResponseBuilder);
            if (commandResponse != null) {
              //then send the response
              discordChatClient.sendMessage(commandResponse);
            }
            super.onMessageReceived(event);
          }
        }).build();
        jda.awaitReady();
      } catch (Throwable e) {
        LOGGER.error("Error caught during main", e);
        throw e;
      }
    }
  },
  SLACK() {
    @Override
    public void init() throws Exception {
      JsonParser jsonParser = new JsonParser();
      SlackChatClient slackChatClient = new SlackChatClient(Slack.getInstance().rtm(Config.getProperty(Config.Constants.SLACK_TOKEN)));

      ChatClientResponseBuilder<SlackResponse> responseChatClientResponseBuilder = new SlackResponseBuilder();
      ApisAndCommandConfig config = buildConfig(responseChatClientResponseBuilder);

      slackChatClient.addMessageHandler((message) -> {
        JsonObject json = jsonParser.parse(message).getAsJsonObject();
        SlackMessage slackMessage = new Gson().fromJson(json, SlackMessage.class);
        if (slackMessage.getType() != null && slackMessage.getType().equalsIgnoreCase("message")) {
          //capture/process the command
          CommandResponse commandResponse = processMessage(
            config.commands,
            slackMessage.getText(),
            //TODO: map user id to actual username
            slackMessage.getUserId(),
            responseChatClientResponseBuilder);
          if (commandResponse != null) {
            //then send the response
            slackChatClient.sendMessage(commandResponse);
          }
        }
        LogManager.getLogger("SlackLog").debug(json);
      });

      slackChatClient.connect();

      //start the scheduler threads that send notifications and cache data periodically
      initScheduling(slackChatClient, config.apis);
    }
  };

  void initScheduling(ChatClient chatClient, List<Api> apis) {
    Scheduler scheduler = Scheduler.getScheduler();
    scheduler.initApiNotifications(apis, chatClient);
    scheduler.initApiCaching(apis);
  }

  <T extends ChatClientResponse, Z extends Api> CommandResponse processMessage(List<Command> apiCommands,
                                                                              String strippedMessage,
                                                                              String name,
                                                                              ChatClientResponseBuilder<T> chatClientResponseBuilder) {
    try {
      for (Command apiCommand : apiCommands) {
        if (strippedMessage.startsWith(apiCommand.getIdentifier())) {
          String commandOperation = strippedMessage.replaceAll(apiCommand.getIdentifier().toLowerCase(), "");
          try {
            CommandContext
              .start()
              .setUsername(name);
            return apiCommand.execute(commandOperation.trim());
          } finally {
            CommandContext.end();
          }
        }
      }
    } catch (Exception e) {
      LOGGER.error("Error trying to execute command " + strippedMessage, e);
      return new CommandResponse(chatClientResponseBuilder.createErrorMessage("Error trying to parse command " + strippedMessage));
    }
    return null;
  }

  public abstract void init() throws Exception;

  private static <T extends ChatClientResponse> ApisAndCommandConfig buildConfig(ChatClientResponseBuilder<T> responseChatClientResponseBuilder) {
    RadarrApi radarrApi = new RadarrApi(responseChatClientResponseBuilder);
    SonarrApi sonarrApi = new SonarrApi(responseChatClientResponseBuilder);

    List<Command> radarrCommands = RadarrCommands.getCommands(radarrApi);
    List<Command> sonarrCommands = SonarrCommands.getCommands(sonarrApi);

    List<Command> commands = new ArrayList<>();
    commands.addAll(radarrCommands);
    commands.addAll(sonarrCommands);
    commands.addAll(HelpCommands.getCommands(responseChatClientResponseBuilder, radarrCommands, sonarrCommands));
    return new ApisAndCommandConfig(Arrays.asList(radarrApi, sonarrApi), commands);
  }

  private static class ApisAndCommandConfig {
    private ApisAndCommandConfig(List<Api> apis, List<Command> commands) {
      this.apis = apis;
      this.commands = commands;
    }
    private final List<Api> apis;
    private final List<Command> commands;
  }

  private static final Logger LOGGER = LogManager.getLogger(ChatClientType.class);
}
