package com.botdarr;

import com.botdarr.clients.ChatClientBootstrap;
import com.botdarr.database.DatabaseBootstrap;

public class BotdarrApplication {
  public static void main(String[] args) throws Exception {
    // bootstrap the database
    DatabaseBootstrap.init();

    // boostrap the chat client/bot
    ChatClientBootstrap chatClientBootstrap = Config.getChatClientBootstrap();
    chatClientBootstrap.init();
  }
}
