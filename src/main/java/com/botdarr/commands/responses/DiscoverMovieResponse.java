package com.botdarr.commands.responses;

import com.botdarr.api.radarr.RadarrMovie;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class DiscoverMovieResponse implements CommandResponse {
    private final RadarrMovie radarrMovie;
    public DiscoverMovieResponse(RadarrMovie radarrMovie) {
        this.radarrMovie = radarrMovie;
    }

    public RadarrMovie getRadarrMovie() {
        return radarrMovie;
    }

    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
