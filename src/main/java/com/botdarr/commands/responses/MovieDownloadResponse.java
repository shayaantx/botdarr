package com.botdarr.commands.responses;

import com.botdarr.api.radarr.RadarrQueue;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class MovieDownloadResponse implements CommandResponse {
    private final RadarrQueue radarrQueue;
    public MovieDownloadResponse(RadarrQueue radarrQueue) {
        this.radarrQueue = radarrQueue;
    }
    public RadarrQueue getRadarrQueue() {
        return radarrQueue;
    }

    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
