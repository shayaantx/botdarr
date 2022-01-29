package com.botdarr.clients.telegram;

import com.botdarr.Config;
import com.botdarr.clients.ChatClient;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TelegramChatClient implements ChatClient<TelegramResponse> {
  public TelegramChatClient() {
    this.bot = new TelegramBot(Config.getProperty(Config.Constants.TELEGRAM_TOKEN));
  }

  public void addUpdateListener(UpdatesListener updatesListener) {
    this.bot.setUpdatesListener(updatesListener);
  }

  public void sendMessage(TelegramResponse telegramResponse, Chat chat) {
    sendMessages(chatChannel -> {
      sendTelegramMessage(chatChannel.id(), telegramResponse.getHtml());
    }, chat);
  }

  public void sendMessage(List<TelegramResponse> telegramResponses, Chat chat) {
    sendMessages(chatChannel -> {
      for (TelegramResponse telegramResponse : telegramResponses) {
        sendTelegramMessage(chatChannel.id(), telegramResponse.getHtml());
      }
    }, chat);
  }

  private String getMessageEndpoint() {
    String channels = Config.getProperty(Config.Constants.TELEGRAM_PRIVATE_CHANNELS);
    if (Strings.isBlank(channels)) {
      return Config.getProperty(Config.Constants.TELEGRAM_PRIVATE_GROUPS);
    }
    return channels;
  }

  private void sendTelegramMessage(long id, String html) {
    SendResponse sendResponse = bot.execute(new SendMessage(id, html).parseMode(ParseMode.HTML));
    if (LOGGER.isDebugEnabled() && sendResponse.errorCode() == 0) {
      LOGGER.debug("send response =" + sendResponse.toString());
    }
    if (sendResponse.errorCode() > 0) {
      LOGGER.error("Error sending response", sendResponse.toString());
    }
  }

  private void sendMessages(MessageSender messageSender, Chat chat) {
    Set<String> configTelegramChannels = Sets.newHashSet(Splitter.on(',').trimResults().split(getMessageEndpoint()));
    Map<String, String> supportedTelegramChannels = new HashMap<>();
    for (String channel : configTelegramChannels) {
      String[] fields = channel.split(":");
      if (fields == null || fields.length == 0) {
        throw new RuntimeException("Configured telegram channels not in correct format. i.e., CHANNEL_NAME:ID,CHANNEL_NAME2:ID2");
      }
      supportedTelegramChannels.put(fields[0], fields[1]);
    }
    if (chat != null) {
      String telegramChannel = chat.title();
      if (!supportedTelegramChannels.containsKey(telegramChannel)) {
        LOGGER.warn("Channel " + telegramChannel + " not allowed, check properties file");
        return;
      }
      messageSender.send(chat);
    } else {
      for (Map.Entry<String, String> supportedTelegramChannel : supportedTelegramChannels.entrySet()) {
        Chat validChat = this.bot.execute(new GetChat("-100" + supportedTelegramChannel.getValue())).chat();
        messageSender.send(validChat);
      }
    }
  }

  @Override
  public void sendToConfiguredChannels(List<TelegramResponse> chatClientResponses) {
    sendMessage(chatClientResponses, null);
  }

  private interface MessageSender {
    void send(Chat chatChannel);
  }

  private final TelegramBot bot;
  private static final Logger LOGGER = LogManager.getLogger("TelegramLog");
}
