package com.botdarr.commands.responses;

import com.botdarr.api.radarr.RadarrMovie;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class ExistingMovieResponse implements CommandResponse {
    private final RadarrMovie existingMovie;
    public ExistingMovieResponse(RadarrMovie existingItem) {
        this.existingMovie = existingItem;
    }
    public RadarrMovie getRadarrMovie() {
        return existingMovie;
    }
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
