package com.botdarr.commands.responses;

import com.botdarr.api.sonarr.SonarrProfile;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;

public class ShowProfileResponse implements CommandResponse {
    private final SonarrProfile showProfile;
    public ShowProfileResponse(SonarrProfile showProfile) {
        this.showProfile = showProfile;
    }
    public SonarrProfile getShowProfile() {
        return showProfile;
    }
    @Override
    public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
        return builder.build(this);
    }
}
