package com.botdarr;

import com.botdarr.database.Bootstrap;

public class BotdarrApplication {
  public static void main(String[] args) throws Exception {
    Bootstrap.init();
    Config.getChatClientType().init();
  }
}
