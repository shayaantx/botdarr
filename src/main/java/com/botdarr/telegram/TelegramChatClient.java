package com.botdarr.telegram;

import com.botdarr.Config;
import com.botdarr.clients.ChatClient;
import com.botdarr.commands.CommandResponse;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
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

  public void sendMessage(CommandResponse<TelegramResponse> commandResponse, Chat chat) {
    if (commandResponse.getSingleChatClientResponse() != null) {
      sendMessage(commandResponse.getSingleChatClientResponse(), chat);
    } else if (commandResponse.getMultipleChatClientResponses() != null) {
      sendMessage(commandResponse.getMultipleChatClientResponses(), chat);
    } else {
      //TODO: err
    }
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
    Set<String> supportedTelegramChannels = Sets.newHashSet(Splitter.on(',').trimResults().split(Config.getProperty(Config.Constants.TELEGRAM_CHANNELS)));
    String telegramChannel = chat.title();
    if (!supportedTelegramChannels.contains(telegramChannel)) {
      return;
    }
    messageSender.send(chat);
  }

  @Override
  public void sendToConfiguredChannels(TelegramResponse chatClientResponse) {
    //TODO: need to get channel id
    //https://github.com/pengrad/java-telegram-bot-api/issues/15 implies you can use @CHANNEL-NAME
  }

  @Override
  public void sendToConfiguredChannels(List<TelegramResponse> chatClientResponses) {
    //TODO:
  }

  private interface MessageSender {
    void send(Chat chatChannel);
  }

  private final TelegramBot bot;
  private static final Logger LOGGER = LogManager.getLogger("TelegramLog");
}
