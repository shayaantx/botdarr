package com.botdarr;

import com.botdarr.api.radarr.RadarrMovie;
import com.botdarr.api.radarr.RadarrQueue;
import com.botdarr.clients.ChatClientResponse;

public class TestResponse implements ChatClientResponse {
  public TestResponse() {}
  public TestResponse(RadarrMovie radarrMovie) {
    this.radarrMovie = radarrMovie;
  }
  public TestResponse(String responseMessage) {
    this.responseMessage = responseMessage;
  }
  public TestResponse(RadarrQueue radarrQueue) {
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
}