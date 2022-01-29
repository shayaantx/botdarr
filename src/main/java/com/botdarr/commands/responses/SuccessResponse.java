package com.botdarr.commands.responses;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class SuccessResponse implements CommandResponse {
    private final String successMessage;
    public SuccessResponse(String successMessage) {
        this.successMessage = successMessage;
    }
    public String getSuccessMessage() {
        return successMessage;
    }
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
