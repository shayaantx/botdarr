package com.botdarr.commands.responses;

import com.botdarr.api.lidarr.LidarrQueueRecord;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class MusicArtistDownloadResponse implements CommandResponse {
    private final LidarrQueueRecord lidarrQueueRecord;
    public MusicArtistDownloadResponse(LidarrQueueRecord lidarrQueueRecord) {
        this.lidarrQueueRecord = lidarrQueueRecord;
    }

    public LidarrQueueRecord getQueueRecord() {
        return this.lidarrQueueRecord;
    }

    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
