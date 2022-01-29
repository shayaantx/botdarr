package com.botdarr.commands;

import com.botdarr.Config;
import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.ErrorResponse;
import com.botdarr.commands.responses.StatusCommandResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusCommand extends BaseCommand {
  public StatusCommand() {
    super(STATUS_COMMAND, STATUS_COMMAND_DESCRIPTION);
  }

  @Override
  public List<CommandResponse> execute(String command) {
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
      return Collections.singletonList(new StatusCommandResponse(endpointStatuses));
    }
    return Collections.singletonList(new ErrorResponse("No status endpoints configured"));
  }

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
