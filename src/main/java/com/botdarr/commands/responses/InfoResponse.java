package com.botdarr.commands.responses;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class InfoResponse implements CommandResponse {
    private final String infoMessage;
    public InfoResponse(String infoMessage) {
        this.infoMessage = infoMessage;
    }
    public String getInfoMessage() {
        return infoMessage;
    }
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
