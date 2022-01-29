package com.botdarr.commands.responses;

import com.botdarr.api.radarr.RadarrMovie;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class NewMovieResponse implements CommandResponse {
    private final RadarrMovie newMovie;
    public NewMovieResponse(RadarrMovie lookupItem) {
        this.newMovie = lookupItem;
    }
    public RadarrMovie getRadarrMovie() {
        return newMovie;
    }
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
