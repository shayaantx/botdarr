package com.botdar.slack;

import com.botdar.Config;
import com.botdar.clients.ChatClient;
import com.botdar.commands.CommandResponse;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.request.channels.ChannelsListRequest;
import com.github.seratch.jslack.api.methods.response.channels.ChannelsListResponse;
import com.github.seratch.jslack.api.model.Channel;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.github.seratch.jslack.api.rtm.message.Message;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  public void connect() throws Exception {
    // must connect within 30 seconds after establishing wss endpoint
    this.rtm.connect();
    //once we are connected, don't exist
    while(true) {
      Thread.sleep(1000);
    }
  }

  @Override
  public void sendMessage(SlackResponse chatClientResponse) {
    //TODO: process slack response
    sendMessages(channelId -> rtm.sendMessage(Message.builder()
      .id(System.currentTimeMillis())
      .channel(channelId)
      .text("test")
      .build().toJSONString()));
  }

  @Override
  public void sendMessage(List<SlackResponse> chatClientResponses) {
    sendMessages(new MessageSender() {
      @Override
      public void send(String channel) {
        for (SlackResponse slackResponse : chatClientResponses) {
          //TODO: process slack response
        }
      }
    });
  }

  @Override
  public void sendMessage(CommandResponse<SlackResponse> commandResponse) {

  }

  private void sendMessages(MessageSender messageSender) {
    try {
      ChannelsListResponse channelsResponse = Slack.getInstance().methods().channelsList(
        ChannelsListRequest.builder().token(Config.getProperty(Config.Constants.SLACK_TOKEN)).build());
      Map<String, String> channelNamesToIds = new HashMap<>();
      for (Channel channel : channelsResponse.getChannels()) {
        channelNamesToIds.put(channel.getName(), channel.getId());
      }

      Set<String> supportedSlackChannels = Sets.newHashSet(Splitter.on(',').trimResults().split(Config.getProperty(Config.Constants.SLACK_CHANNELS)));
      for (String slackChannel : supportedSlackChannels) {
        String channelId = channelNamesToIds.get(slackChannel);
        if (Strings.isBlank(channelId)) {
          return;
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
  private static final Logger LOGGER = LogManager.getLogger(SlackChatClient.class);
}
