package com.botdarr.commands.responses;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.Command;

import java.util.List;

public class MoviesHelpResponse implements CommandResponse {
    private final List<Command> radarrCommands;

    public MoviesHelpResponse(List<Command> radarrCommands) {
        this.radarrCommands = radarrCommands;
    }

    public List<Command> getRadarrCommands() {
        return radarrCommands;
    }

    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
