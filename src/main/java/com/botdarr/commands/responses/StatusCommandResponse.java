package com.botdarr.commands.responses;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

import java.util.Map;

public class StatusCommandResponse implements CommandResponse {
    private final Map<String, Boolean> endpointStatuses;
    public StatusCommandResponse(Map<String, Boolean> endpointStatuses) {
        this.endpointStatuses = endpointStatuses;
    }
    public Map<String, Boolean> getEndpoints() {
        return endpointStatuses;
    }
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
