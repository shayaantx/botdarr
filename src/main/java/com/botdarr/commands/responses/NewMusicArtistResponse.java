package com.botdarr.commands.responses;

import com.botdarr.api.lidarr.LidarrArtist;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class NewMusicArtistResponse implements CommandResponse {
    private final LidarrArtist lookupItem;
    public NewMusicArtistResponse(LidarrArtist lookupItem) {
        this.lookupItem = lookupItem;
    }

    public LidarrArtist getLidarrArtist() {
        return this.lookupItem;
    }

    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
