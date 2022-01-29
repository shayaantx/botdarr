package com.botdarr.commands.responses;

import com.botdarr.api.sonarr.SonarrShow;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class ExistingShowResponse implements CommandResponse {
    private final SonarrShow existingShow;
    public ExistingShowResponse(SonarrShow existingShow) {
        this.existingShow = existingShow;
    }
    public SonarrShow getExistingShow() {
        return existingShow;
    }
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
