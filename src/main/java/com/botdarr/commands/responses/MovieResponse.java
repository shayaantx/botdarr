package com.botdarr.commands.responses;

import com.botdarr.api.radarr.RadarrMovie;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class MovieResponse implements CommandResponse {
    private final RadarrMovie movie;
    public MovieResponse(RadarrMovie item) {
        this.movie = item;
    }
    public RadarrMovie getRadarrMovie() {
        return movie;
    }
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
