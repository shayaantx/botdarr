package com.botdarr.slack;

import com.botdarr.Config;
import com.botdarr.clients.ChatClient;
import com.botdarr.commands.CommandResponse;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.conversations.ConversationsListRequest;
import com.github.seratch.jslack.api.methods.response.conversations.ConversationsListResponse;
import com.github.seratch.jslack.api.model.Conversation;
import com.github.seratch.jslack.api.model.ConversationType;
import com.github.seratch.jslack.api.model.block.DividerBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.github.seratch.jslack.api.rtm.RTMMessageHandler;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

public class SlackChatClient implements ChatClient<SlackResponse> {
  public SlackChatClient(RTMClient rtmClient) {
    this.rtm = rtmClient;
    rtm.addCloseHandler(reason -> {
      //TODO: if we close do need to reconnect?
      LOGGER.error("Error caught during slack close handler", reason.toString());
    });
    rtm.addErrorHandler(reason -> {
      LOGGER.error("Error caught from slack error handler", reason);
    });
  }

  public void addMessageHandler(RTMMessageHandler messageHandler) {
    rtm.addMessageHandler(messageHandler);
  }

  public void connect() throws Exception {
    // must connect within 30 seconds after establishing wss endpoint
    this.rtm.connect();
    //once we are connected, don't exist
    while(true) {
      Thread.sleep(1000);
    }
  }

  @Override
  public void sendMessage(SlackResponse chatClientResponse, String channel) {
    sendMessages(channelId -> {
      try {
        Slack.getInstance().methods().chatPostMessage(ChatPostMessageRequest.builder()
          .token(Config.getProperty(Config.Constants.SLACK_TOKEN))
          .blocks(chatClientResponse.getBlocks())
          .channel(channelId).build());
      } catch (Exception e) {
        LOGGER.error("Error sending slack message", e);
      }
    }, channel);
  }

  @Override
  public void sendMessage(List<SlackResponse> chatClientResponses, String channel) {
    sendMessages(channelId -> {
      for (SlackResponse slackResponse : chatClientResponses) {
        try {
          List<LayoutBlock> blocks = slackResponse.getBlocks();
          blocks.add(DividerBlock.builder().build());
          Slack.getInstance().methods().chatPostMessage(ChatPostMessageRequest.builder()
            .token(Config.getProperty(Config.Constants.SLACK_TOKEN))
            .blocks(blocks)
            .channel(channelId).build());
          Thread.sleep(1000); //slack is rate limited
        } catch (Exception e) {
          LOGGER.error("Error sending slack message", e);
        }
      }
    }, channel);
  }

  @Override
  public void sendMessage(CommandResponse<SlackResponse> commandResponse, String targetChannel) {
    if (commandResponse.getSingleChatClientResponse() != null) {
      sendMessage(commandResponse.getSingleChatClientResponse(), targetChannel);
    } else if (commandResponse.getMultipleChatClientResponses() != null) {
      sendMessage(commandResponse.getMultipleChatClientResponses(), targetChannel);
    } else {
      //TODO: err
    }
  }

  private void sendMessages(MessageSender messageSender, String targetChannel) {
    try {
      Map<String, String> conversationNamesToIds = new HashMap<>();
      ConversationsListResponse conversationsListResponse =
        Slack.getInstance().methods().conversationsList(ConversationsListRequest.builder()
          .token(Config.getProperty(Config.Constants.SLACK_TOKEN))
          .types(Arrays.asList(ConversationType.PRIVATE_CHANNEL, ConversationType.PUBLIC_CHANNEL)).build());
      for (Conversation conversation : conversationsListResponse.getChannels()) {
        conversationNamesToIds.put(conversation.getName(), conversation.getId());
      }

      Set<String> supportedSlackChannels = Sets.newHashSet(Splitter.on(',').trimResults().split(Config.getProperty(Config.Constants.SLACK_CHANNELS)));
      for (String slackChannel : supportedSlackChannels) {
        String channelId = conversationNamesToIds.get(slackChannel);
        if (Strings.isBlank(channelId)) {
          continue;
        }
        if (targetChannel != null && !channelId.equalsIgnoreCase(targetChannel)) {
          continue;
        }
        messageSender.send(channelId);
      }
    } catch (Exception e) {
      LOGGER.error("Error sending slack messages", e);
    }
  }

  private interface MessageSender {
    void send(String channel);
  }

  private final RTMClient rtm;
  private static final Logger LOGGER = LogManager.getLogger("SlackLog");
}
