package com.botdarr.commands.responses;

import com.botdarr.api.lidarr.LidarrArtist;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class MusicArtistResponse implements CommandResponse {
    private final LidarrArtist lidarrArtist;
    public MusicArtistResponse(LidarrArtist lidarrArtist) {
        this.lidarrArtist = lidarrArtist;
    }

    public LidarrArtist getArtist() {
        return lidarrArtist;
    }

    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
