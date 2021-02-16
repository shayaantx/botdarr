package com.botdarr.clients.slack;

public class SlackMessage {
  public String getType() {
    return type;
  }

  public String getText() {
    return text;
  }

  public String getUserId() {
    return user;
  }

  public String getChannel() {
    return channel;
  }

  public String getReaction() {
    return reaction;
  }

  public SlackMessageItem getItem() {
    return item;
  }

  @Override
  public String toString() {
    return "SlackMessage{" +
      "type='" + type + '\'' +
      ", text='" + text + '\'' +
      ", user='" + user + '\'' +
      ", channel='" + channel + '\'' +
      ", reaction='" + reaction + '\'' +
      ", item=" + item +
      '}';
  }

  private String type;
  private String text;
  private String user; //slack user id
  private String channel;
  private String reaction;
  private SlackMessageItem item;
}
