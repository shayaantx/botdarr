package com.botdarr.clients.telegram;

import com.botdarr.Config;
import com.botdarr.clients.ChatClientBootstrap;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.scheduling.Scheduler;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.apache.logging.log4j.util.Strings;

import java.util.Properties;

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
                    if (message != null && !Strings.isEmpty(message.text())) {
                        String text = message.text();
                        //TODO: the telegram api doesn't seem return "from" field in channel posts for some reason
                        //for now we leave the author as "telegram" till a better solution arises
                        String author = "telegram";
                        Scheduler.getScheduler().executeCommand(() -> {
                            runAndProcessCommands(text, author, responseChatClientResponseBuilder, chatClientResponse -> {
                                telegramChatClient.sendMessage(chatClientResponse, message.chat());
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
        return isTelegramConfigured && (telegramPrivateChannelsExist || telegramPrivateGroupsExist);
    }
}
