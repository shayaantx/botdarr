package com.botdarr;

import com.botdarr.api.radarr.RadarrMovie;
import com.botdarr.api.radarr.RadarrQueue;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.responses.CommandResponse;

public class TestCommandResponse implements CommandResponse {
  public TestCommandResponse() {}
  public TestCommandResponse(RadarrMovie radarrMovie) {
    this.radarrMovie = radarrMovie;
  }
  public TestCommandResponse(String responseMessage) {
    this.responseMessage = responseMessage;
  }
  public TestCommandResponse(RadarrQueue radarrQueue) {
    this.radarrQueue = radarrQueue;
  }

  public String getResponseMessage() {
    return responseMessage;
  }

  public RadarrMovie getRadarrMovie() {
    return radarrMovie;
  }

  public RadarrQueue getRadarrQueue() {
    return radarrQueue;
  }

  private String responseMessage;
  private RadarrMovie radarrMovie;
  private RadarrQueue radarrQueue;

  @Override
  public <T extends ChatClientResponse> T convertToChatClientResponse(ChatClientResponseBuilder<T> builder) {
    return null;
  }
}