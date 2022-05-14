package com.botdarr.clients.discord;

import com.botdarr.Config;
import com.botdarr.clients.ChatClient;
import com.botdarr.clients.ChatClientBootstrap;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.scheduling.Scheduler;
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
import org.apache.logging.log4j.util.Strings;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Properties;

import static com.botdarr.api.lidarr.LidarrApi.ADD_ARTIST_COMMAND_FIELD_PREFIX;
import static com.botdarr.api.radarr.RadarrApi.ADD_MOVIE_COMMAND_FIELD_PREFIX;
import static com.botdarr.api.sonarr.SonarrApi.ADD_SHOW_COMMAND_FIELD_PREFIX;

public class DiscordBootstrap extends ChatClientBootstrap {
    @Override
    public void init() throws Exception {
        try {
            ChatClientBootstrap.ApisAndCommandConfig config = buildConfig();
            ChatClientResponseBuilder<DiscordResponse> responseBuilder = new DiscordResponseBuilder();

            JDA jda = JDABuilder.createDefault((Config.getProperty(Config.Constants.DISCORD_TOKEN))).addEventListeners(new ListenerAdapter() {
                @Override
                public void onGenericEvent(@Nonnull GenericEvent event) {
                    super.onGenericEvent(event);
                }

                @Override
                public void onReady(@Nonnull ReadyEvent event) {
                    LogManager.getLogger("com.botdarr.clients.discord").info("Connected to discord");
                    ChatClient<DiscordResponse> chatClient = new DiscordChatClient(event.getJDA());
                    //start the scheduler threads that send notifications and cache data periodically
                    initScheduling(chatClient, responseBuilder, config.getApis());
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
                                for (MessageEmbed.Field field : embeds.get(0).getFields()) {
                                    String name = field.getName();
                                    if (Strings.isEmpty(name)) {
                                        continue;
                                    }
                                    if (name.equals(ADD_MOVIE_COMMAND_FIELD_PREFIX) ||
                                            name.equals(ADD_SHOW_COMMAND_FIELD_PREFIX) ||
                                            name.equals(ADD_ARTIST_COMMAND_FIELD_PREFIX)) {
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
                    LogManager.getLogger("com.botdarr.clients.discord").debug(event.getMessage().getContentRaw());
                    super.onMessageReceived(event);
                }

                private void handleCommand(JDA jda, String message, String author, String channelName) {
                    LogManager.getLogger("com.botdarr.clients.discord").debug("Handling command " + message);
                    //build chat client
                    DiscordChatClient discordChatClient = new DiscordChatClient(jda);

                    //capture/process command
                    Scheduler.getScheduler().executeCommand(() -> {

                        LogManager.getLogger("com.botdarr.clients.discord").debug("Executing command for " + message);
                        runAndProcessCommands(message, author, responseBuilder, chatClientResponse -> {

                            LogManager.getLogger("com.botdarr.clients.discord").debug("Attempting to send response for " + message);
                            discordChatClient.sendMessage(chatClientResponse, channelName);
                        });
                        return null;
                    });
                }

                private static final String THUMBS_UP_EMOTE = "\uD83D\uDC4D";
            }).build();
            jda.awaitReady();
        } catch (Throwable e) {
            LogManager.getLogger("com.botdarr.clients.discord").error("Error caught during main", e);
            throw e;
        }
    }

    @Override
    public boolean isConfigured(Properties properties) {
        return !Strings.isBlank(properties.getProperty(Config.Constants.DISCORD_TOKEN)) &&
               !Strings.isBlank(properties.getProperty(Config.Constants.DISCORD_CHANNELS));
    }
}
