package com.botdarr.clients;

import com.botdarr.api.*;
import com.botdarr.Config;
import com.botdarr.commands.*;
import com.botdarr.discord.DiscordChatClient;
import com.botdarr.discord.DiscordResponse;
import com.botdarr.discord.DiscordResponseBuilder;
import com.botdarr.scheduling.Scheduler;
import com.botdarr.slack.SlackChatClient;
import com.botdarr.slack.SlackMessage;
import com.botdarr.slack.SlackResponse;
import com.botdarr.slack.SlackResponseBuilder;
import com.botdarr.telegram.TelegramChatClient;
import com.botdarr.telegram.TelegramResponse;
import com.botdarr.telegram.TelegramResponseBuilder;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.rtm.RTMMessageHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import javax.annotation.Nonnull;
import java.util.*;

import static com.botdarr.api.RadarrApi.ADD_MOVIE_COMMAND_FIELD_PREFIX;
import static com.botdarr.api.SonarrApi.ADD_SHOW_COMMAND_FIELD_PREFIX;

public enum ChatClientType {
  TELEGRAM() {
    @Override
    public void init() throws Exception {
      ChatClientResponseBuilder<TelegramResponse> responseChatClientResponseBuilder = new TelegramResponseBuilder();
      ApisAndCommandConfig config = buildConfig(responseChatClientResponseBuilder);
      TelegramChatClient telegramChatClient = new TelegramChatClient();

      initScheduling(telegramChatClient, config.apis);
      telegramChatClient.addUpdateListener(list -> {
        try {
          for (Update update : list) {
            com.pengrad.telegrambot.model.Message message = update.channelPost();
            if (message != null) {
              String text = message.text();
              //TODO: the telegram api doesn't seem return "from" field in channel posts for some reason
              //for now we leave the author as "telegram" till a better solution arises
              String author = "telegram";
              CommandResponse commandResponse =
                processMessage(config.commands, text, author, responseChatClientResponseBuilder);
              if (commandResponse != null) {
                telegramChatClient.sendMessage(commandResponse, message.chat());
              }
            }
          }
        } catch (Throwable t) {
          LOGGER.error("Error during telegram updates", t);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
      });
    }

    @Override
    public boolean isConfigured(Properties properties) {
      return
        !Strings.isBlank(properties.getProperty(Config.Constants.TELEGRAM_TOKEN)) &&
        !Strings.isBlank(properties.getProperty(Config.Constants.TELEGRAM_PRIVATE_CHANNELS));
    }

    @Override
    public String getReadableName() {
      return "Telegram";
    }
  },
  DISCORD() {
    @Override
    public void init() throws Exception {
      try {
        ChatClientResponseBuilder<DiscordResponse> responseChatClientResponseBuilder = new DiscordResponseBuilder();
        ApisAndCommandConfig config = buildConfig(responseChatClientResponseBuilder);

        JDA jda = new JDABuilder(Config.getProperty(Config.Constants.DISCORD_TOKEN)).addEventListeners(new ListenerAdapter() {
          @Override
          public void onGenericEvent(@Nonnull GenericEvent event) {
            super.onGenericEvent(event);
          }

          @Override
          public void onReady(@Nonnull ReadyEvent event) {
            LogManager.getLogger("DiscordLog").info("Connected to discord");
            ChatClient chatClient = new DiscordChatClient(event.getJDA());
            //start the scheduler threads that send notifications and cache data periodically
            initScheduling(chatClient, config.apis);
            super.onReady(event);
          }

          @Override
          public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
            if (event.getReactionEmote().getName().equalsIgnoreCase(THUMBS_UP_EMOTE)) {
              MessageHistory.MessageRetrieveAction me = event.getChannel().getHistoryAround(event.getMessageId(), 1);
              me.queue(messageHistory -> {
                List<Message> messageHistories = messageHistory.getRetrievedHistory();
                messageLoop:
                for (Message message : messageHistories) {
                  List<MessageEmbed> embeds = message.getEmbeds();
                  fieldLoop:
                  for (MessageEmbed.Field field : embeds.get(0).getFields()) {
                    if (field.getName().equals(ADD_MOVIE_COMMAND_FIELD_PREFIX) || field.getName().equals(ADD_SHOW_COMMAND_FIELD_PREFIX)) {
                      //capture/process the command
                      handleCommand(event.getJDA(), field.getValue(), event.getUser().getName(), event.getChannel().getName());
                      break messageLoop;
                    }
                  }
                }
              });
            }
            super.onGuildMessageReactionAdd(event);
          }

          @Override
          public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
            handleCommand(event.getJDA(), event.getMessage().getContentStripped(), event.getAuthor().getName(), event.getChannel().getName());
            LogManager.getLogger("DiscordLog").debug(event.getMessage().getContentRaw());
            super.onMessageReceived(event);
          }

          private void handleCommand(JDA jda, String message, String author, String channelName) {
            //build chat client
            DiscordChatClient discordChatClient = new DiscordChatClient(jda);

            //capture/process command
            CommandResponse commandResponse = processMessage(
              config.commands,
              message,
              author,
              responseChatClientResponseBuilder);
            if (commandResponse != null) {
              //then send the response
              discordChatClient.sendMessage(commandResponse, channelName);
            }
          }

          private static final String THUMBS_UP_EMOTE = "\uD83D\uDC4D";
        }).build();
        jda.awaitReady();
      } catch (Throwable e) {
        LogManager.getLogger("DiscordLog").error("Error caught during main", e);
        throw e;
      }
    }

    @Override
    public boolean isConfigured(Properties properties) {
      return
        !Strings.isBlank(properties.getProperty(Config.Constants.DISCORD_TOKEN)) &&
        !Strings.isBlank(properties.getProperty(Config.Constants.DISCORD_CHANNELS));
    }

    @Override
    public String getReadableName() {
      return "Discord";
    }
  },
  SLACK() {
    @Override
    public void init() throws Exception {
      JsonParser jsonParser = new JsonParser();
      SlackChatClient slackChatClient = new SlackChatClient(Slack.getInstance().rtm(Config.getProperty(Config.Constants.SLACK_BOT_TOKEN)));

      ChatClientResponseBuilder<SlackResponse> responseChatClientResponseBuilder = new SlackResponseBuilder();
      ApisAndCommandConfig config = buildConfig(responseChatClientResponseBuilder);

      slackChatClient.addMessageHandler(new RTMMessageHandler() {
        @Override
        public void handle(String message) {
          JsonObject json = jsonParser.parse(message).getAsJsonObject();
          SlackMessage slackMessage = new Gson().fromJson(json, SlackMessage.class);
          if (slackMessage.getType() != null) {
            if (slackMessage.getType().equalsIgnoreCase("message")) {
              handleCommand(slackMessage.getText(), slackChatClient.getUser(slackMessage.getUserId()).getName(), slackMessage.getChannel());
            } else if (slackMessage.getType().equalsIgnoreCase("reaction_added") && slackMessage.getReaction().equalsIgnoreCase("+1")) {
              //thumbsup = +1 in slack for some reason
              try {
                //search public channels first
                List<com.github.seratch.jslack.api.model.Message> conversationMessages = slackChatClient.getPublicMessages(slackMessage);
                if (conversationMessages == null || conversationMessages.isEmpty()) {
                  //check private channels if necessary
                  conversationMessages = slackChatClient.getPrivateMessages(slackMessage);
                }
                if (conversationMessages != null) {
                  conversationMessageLoop:
                  for (com.github.seratch.jslack.api.model.Message conversationMessage : conversationMessages) {
                    for (LayoutBlock layoutBlock : conversationMessage.getBlocks()) {
                      if (layoutBlock.getType().equals("section")) {
                        SectionBlock sectionBlock = (SectionBlock) layoutBlock;
                        if (sectionBlock.getText() instanceof MarkdownTextObject) {
                          String markdownText = ((MarkdownTextObject) sectionBlock.getText()).getText();
                          if (markdownText != null &&
                            (markdownText.startsWith(ADD_MOVIE_COMMAND_FIELD_PREFIX) || markdownText.startsWith(ADD_SHOW_COMMAND_FIELD_PREFIX))) {
                            String postProcessedCommand = markdownText
                              .replaceAll(ADD_MOVIE_COMMAND_FIELD_PREFIX + " - ", "")
                              .replaceAll(ADD_SHOW_COMMAND_FIELD_PREFIX + " - ", "");
                            handleCommand(postProcessedCommand, slackChatClient.getUser(slackMessage.getUserId()).getName(), slackMessage.getItem().getChannel());
                            break conversationMessageLoop;
                          }
                        }
                      }
                    }
                  }
                }
              } catch (Exception e) {
                LogManager.getLogger("SlackLog").error("Error fetching conversation history", e);
              }
            }
          }
          LogManager.getLogger("SlackLog").debug(json);
        }

        private void handleCommand(String text, String userId, String channel) {
          //capture/process the command
          CommandResponse commandResponse = processMessage(
            config.commands,
            text,
            userId,
            responseChatClientResponseBuilder);
          if (commandResponse != null) {
            //then send the response
            slackChatClient.sendMessage(commandResponse, channel);
          }
        }
      });

      //start the scheduler threads that send notifications and cache data periodically
      initScheduling(slackChatClient, config.apis);

      slackChatClient.connect();
    }

    @Override
    public boolean isConfigured(Properties properties) {
      return
        !Strings.isBlank(properties.getProperty(Config.Constants.SLACK_BOT_TOKEN)) &&
        !Strings.isBlank(properties.getProperty(Config.Constants.SLACK_CHANNELS));
    }

    @Override
    public String getReadableName() {
      return "Slack";
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
  public abstract boolean isConfigured(Properties properties);
  public abstract String getReadableName();

  private static <T extends ChatClientResponse> ApisAndCommandConfig buildConfig(ChatClientResponseBuilder<T> responseChatClientResponseBuilder) {
    RadarrApi radarrApi = new RadarrApi(responseChatClientResponseBuilder);
    SonarrApi sonarrApi = new SonarrApi(responseChatClientResponseBuilder);

    List<Command> radarrCommands = RadarrCommands.getCommands(radarrApi);
    List<Command> sonarrCommands = SonarrCommands.getCommands(sonarrApi);

    List<Command> commands = new ArrayList<>();
    List<Api> apis = new ArrayList<>();
    if (Config.isRadarrEnabled()) {
      commands.addAll(radarrCommands);
      apis.add(radarrApi);
    }
    if (Config.isSonarrEnabled()) {
      commands.addAll(sonarrCommands);
      apis.add(sonarrApi);
    }
    commands.addAll(HelpCommands.getCommands(responseChatClientResponseBuilder, radarrCommands, sonarrCommands));
    return new ApisAndCommandConfig(apis, commands);
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
