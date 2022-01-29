package com.botdarr.commands.responses;

import com.botdarr.api.sonarr.SonarrQueue;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class ShowDownloadResponse implements CommandResponse {
    private final SonarrQueue showQueue;
    public ShowDownloadResponse(SonarrQueue showQueue) {
        this.showQueue = showQueue;
    }
    public SonarrQueue getShowQueue() {
        return showQueue;
    }

    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
