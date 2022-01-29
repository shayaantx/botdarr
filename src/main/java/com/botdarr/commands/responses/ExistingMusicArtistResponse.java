package com.botdarr.commands.responses;

import com.botdarr.api.lidarr.LidarrArtist;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class ExistingMusicArtistResponse implements CommandResponse {
    private final LidarrArtist existingItem;
    public ExistingMusicArtistResponse(LidarrArtist existingItem) {
        this.existingItem = existingItem;
    }

    public LidarrArtist getLidarrArtist() {
        return existingItem;
    }

    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
