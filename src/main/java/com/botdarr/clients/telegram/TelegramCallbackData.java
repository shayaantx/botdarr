package com.botdarr.clients.telegram;

public class TelegramCallbackData {
    private String command;

    public TelegramCallbackData() {}
    public TelegramCallbackData(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
