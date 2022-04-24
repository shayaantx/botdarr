package com.botdarr.commands;

import com.botdarr.api.ContentType;
import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.InfoResponse;

import java.util.List;

public class CommandResponseUtil {
    public List<CommandResponse> addEmptyDownloadsMessage(List<CommandResponse> commandResponseList, ContentType contentType) {
        if (commandResponseList.isEmpty()) {
            commandResponseList.add(new InfoResponse("No " + contentType.getDisplayName() + "s downloading"));
        }
        return commandResponseList;
    }
}
