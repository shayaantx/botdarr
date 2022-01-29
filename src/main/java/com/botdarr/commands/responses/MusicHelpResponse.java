package com.botdarr.commands.responses;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.Command;

import java.util.List;

public class MusicHelpResponse implements CommandResponse {
    private final List<Command> lidarrCommands;

    public MusicHelpResponse(List<Command> lidarrCommands) {
        this.lidarrCommands = lidarrCommands;
    }

    public List<Command> getLidarrCommands() {
        return lidarrCommands;
    }

    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
