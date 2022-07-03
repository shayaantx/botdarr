package com.botdarr.clients.telegram;

import com.botdarr.Config;
import com.botdarr.clients.ChatClientBootstrap;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.CommandContext;
import com.botdarr.scheduling.Scheduler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import org.apache.logging.log4j.util.Strings;

import java.util.Properties;
import java.util.Set;

public class TelegramBootstrap extends ChatClientBootstrap {
    private boolean isUsingChannels() {
        String telegramGroups = Config.getProperty(Config.Constants.TELEGRAM_PRIVATE_GROUPS);
        return telegramGroups == null || telegramGroups.isEmpty();
    }
    @Override
    public void init() throws Exception {
        ChatClientResponseBuilder<TelegramResponse> responseChatClientResponseBuilder = new TelegramResponseBuilder();
        ChatClientBootstrap.ApisAndCommandConfig config = buildConfig();
        TelegramChatClient telegramChatClient = new TelegramChatClient();

        initScheduling(telegramChatClient, responseChatClientResponseBuilder, config.getApis());
        telegramChatClient.addUpdateListener(list -> {
            try {
                for (Update update : list) {
                    com.pengrad.telegrambot.model.Message message = isUsingChannels() ? update.channelPost() : update.message();
                    final String text;
                    final Chat chat;
                    if (message != null && !Strings.isEmpty(message.text())) {
                        // regular commands
                        text = message.text();
                        chat = message.chat();
                    } else if (!Strings.isEmpty(update.callbackQuery().data())) {
                        // interactive commands with callback data
                        ObjectMapper mapper = new ObjectMapper();
                        TelegramCallbackData callbackData = mapper.readValue(update.callbackQuery().data(), TelegramCallbackData.class);
                        text = callbackData.getCommand();
                        chat = update.callbackQuery().message().chat();
                    } else {
                        // couldn't find a message or a chat to reply to
                        text = "";
                        chat = null;
                    }
                    if (!Strings.isEmpty(text) && chat != null) {
                        //TODO: the telegram api doesn't seem return "from" field in channel posts for some reason
                        //for now we leave the author as "telegram" till a better solution arises
                        String author = "telegram";
                        Scheduler.getScheduler().executeCommand(() -> {
                            TelegramBootstrap.this.runAndProcessCommands(CommandContext.getConfig().getPrefix(), text, author, responseChatClientResponseBuilder, chatClientResponse -> {
                                telegramChatClient.sendMessage(chatClientResponse, chat);
                            });
                            return null;
                        });
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
        boolean isTelegramConfigured = !Strings.isBlank(properties.getProperty(Config.Constants.TELEGRAM_TOKEN));
        boolean telegramPrivateChannelsExist = !Strings.isBlank(properties.getProperty(Config.Constants.TELEGRAM_PRIVATE_CHANNELS));
        boolean telegramPrivateGroupsExist = !Strings.isBlank(properties.getProperty(Config.Constants.TELEGRAM_PRIVATE_GROUPS));
        if (isTelegramConfigured && telegramPrivateChannelsExist && telegramPrivateGroupsExist) {
            throw new RuntimeException("Cannot configure telegram for private channels and groups, you must pick one or the other");
        }

        if (telegramPrivateChannelsExist || telegramPrivateGroupsExist) {
            String channels = properties.getProperty(Config.Constants.TELEGRAM_PRIVATE_CHANNELS);
            if (Strings.isBlank(channels)) {
                channels = properties.getProperty(Config.Constants.TELEGRAM_PRIVATE_GROUPS);
            }
            Set<String> configTelegramChannels = Sets.newHashSet(Splitter.on(',').trimResults().split(channels));
            for (String channel : configTelegramChannels) {
                String[] fields = channel.split(":");
                if (fields == null || fields.length == 0) {
                    throw new RuntimeException("Configured telegram channels not in correct format. i.e., CHANNEL_NAME:ID,CHANNEL_NAME2:ID2");
                }
                if (fields[1].startsWith("-100")) {
                    throw new RuntimeException("Telegram channel or group contains -100, which is not necessary. We automatically add this to your channel at runtime, you can remove it.");
                }
            }
        }
        return isTelegramConfigured && (telegramPrivateChannelsExist || telegramPrivateGroupsExist);
    }
}
