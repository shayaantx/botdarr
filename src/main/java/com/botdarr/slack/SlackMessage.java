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

  private String type;
  private String text;
  private String user; //slack user id
}
