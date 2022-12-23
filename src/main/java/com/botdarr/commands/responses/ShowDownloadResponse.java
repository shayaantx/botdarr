package com.botdarr.commands.responses;

import com.botdarr.api.sonarr.SonarrDownloadActivity;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class ShowDownloadResponse implements CommandResponse {
    private final SonarrDownloadActivity showQueue;
    public ShowDownloadResponse(SonarrDownloadActivity showQueue) {
        this.showQueue = showQueue;
    }
    public SonarrDownloadActivity getShowQueue() {
        return showQueue;
    }

    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
