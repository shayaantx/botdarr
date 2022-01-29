package com.botdarr.commands.responses;

import com.botdarr.api.radarr.RadarrProfile;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class MovieProfileResponse implements CommandResponse {
    private final RadarrProfile radarrProfile;
    public MovieProfileResponse(RadarrProfile radarrProfile) {
        this.radarrProfile = radarrProfile;
    }
    public RadarrProfile getRadarrProfile() {
        return radarrProfile;
    }
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
