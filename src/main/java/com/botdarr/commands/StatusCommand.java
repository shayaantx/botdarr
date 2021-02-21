package com.botdarr.commands;

import com.botdarr.Config;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusCommand<T extends ChatClientResponse> extends BaseCommand {
  public StatusCommand(ChatClientResponseBuilder<T> responseChatClientResponseBuilder) {
    super(STATUS_COMMAND, STATUS_COMMAND_DESCRIPTION);
    this.responseChatClientResponseBuilder = responseChatClientResponseBuilder;
  }

  @Override
  public CommandResponse<? extends ChatClientResponse> execute(String command) {
    List<StatusEndPoint> endpoints = Config.getStatusEndpoints();
    if (!endpoints.isEmpty()) {
      Map<String, Boolean> endpointStatuses = new HashMap<>();
      for (StatusEndPoint endPoint : endpoints) {
        try (Socket socket = new Socket()) {
          socket.connect(new InetSocketAddress(endPoint.hostname, endPoint.port), 2000);
          endpointStatuses.put(endPoint.name, true);
        } catch (IOException e) {
          LOGGER.error("Error fetching status for " + endPoint.name, e);
          endpointStatuses.put(endPoint.name, false);
        }
      }
      return new CommandResponse<ChatClientResponse>(responseChatClientResponseBuilder.getStatusCommandResponse(endpointStatuses));
    }
    return new CommandResponse<ChatClientResponse>(
      responseChatClientResponseBuilder.createErrorMessage("No status endpoints configured"));
  }
  private final ChatClientResponseBuilder<T> responseChatClientResponseBuilder;

  public static class StatusEndPoint {
    public StatusEndPoint(String name, String hostname, int port) {
      this.name = name;
      this.hostname = hostname;
      this.port = port;
    }
    private final String name;
    private final String hostname;
    private final int port;
  }
  public static final String STATUS_COMMAND = "status";
  public static final String STATUS_COMMAND_DESCRIPTION = "Gets the statuses of configured endpoints";
  private static Logger LOGGER = LogManager.getLogger();
}
