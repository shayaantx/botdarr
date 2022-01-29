package com.botdarr.commands.responses;

import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.Command;

import java.util.List;

public class ShowsHelpResponse implements CommandResponse {
    private final List<Command> sonarrCommands;

    public ShowsHelpResponse(List<Command> sonarrCommands) {
        this.sonarrCommands = sonarrCommands;
    }

    public List<Command> getSonarrCommands() {
        return sonarrCommands;
    }

    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
