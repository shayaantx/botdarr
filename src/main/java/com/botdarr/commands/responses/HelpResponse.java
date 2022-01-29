package com.botdarr.commands.responses;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class HelpResponse implements CommandResponse {
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
