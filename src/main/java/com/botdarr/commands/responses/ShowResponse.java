package com.botdarr.commands.responses;

import com.botdarr.api.sonarr.SonarrShow;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class ShowResponse implements CommandResponse {
    private final SonarrShow show;
    public ShowResponse(SonarrShow show) {
        this.show = show;
    }
    public SonarrShow getShow() {
        return show;
    }
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
