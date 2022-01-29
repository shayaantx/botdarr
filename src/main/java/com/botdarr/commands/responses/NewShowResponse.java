package com.botdarr.commands.responses;

import com.botdarr.api.sonarr.SonarrShow;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class NewShowResponse implements CommandResponse {
    private final SonarrShow newShow;
    public NewShowResponse(SonarrShow newShow) {
        this.newShow = newShow;
    }
    public SonarrShow getNewShow() {
        return newShow;
    }
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
