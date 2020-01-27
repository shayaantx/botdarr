package com.botdarr.slack;

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

  private String type;
  private String text;
  private String user; //slack user id
  private String channel;
}
