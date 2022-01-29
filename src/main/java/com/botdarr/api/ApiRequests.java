package com.botdarr.api;

import com.botdarr.Config;
import com.botdarr.database.DatabaseHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ApiRequests {
  public int getMaxDownloadsToShow() {
    String maxDownloadsToShow = Config.getProperty(Config.Constants.MAX_DOWNLOADS_TO_SHOW);
    if (!Strings.isEmpty(maxDownloadsToShow)) {
      try {
        return Integer.parseInt(maxDownloadsToShow);
      } catch (NumberFormatException e) {
        LOGGER.error("Invalid max downloads to show configuration", e);
      }
    }
    return 20;
  }

  public int getMaxResultsToShow() {
    String maxResultsToShow = Config.getProperty(Config.Constants.MAX_RESULTS_TO_SHOW);
    if (!Strings.isEmpty(maxResultsToShow)) {
      try {
        return Integer.parseInt(maxResultsToShow);
      } catch (NumberFormatException e) {
        LOGGER.error("Invalid max results to show configuration", e);
      }
    }
    return 20;
  }

  public boolean checkRequestLimits(ApiRequestType requestType) {
    String maxRequestsThreshold = Config.getProperty(Config.Constants.MAX_REQUESTS_THRESHOLD);
    if (Strings.isEmpty(maxRequestsThreshold)) {
      return false;
    }
    if (!requestType.isConfigured()) {
      return false;
    }
    try {
      ApiRequestThreshold.valueOf(maxRequestsThreshold);
    } catch (Exception e) {
      String allRequestThresholds = Arrays.stream(ApiRequestThreshold.values())
        .sorted(Comparator.comparing(ApiRequestThreshold::getReadableName))
        .map(ApiRequestThreshold::getReadableName)
        .collect(Collectors.joining(", "));
      LOGGER.error("Invalid threshold option, valid options: " + allRequestThresholds, e);
      return false;
    }
    return true;
  }

  public boolean canMakeRequests(ApiRequestType requestType, String username) {
    ApiRequestThreshold requestThreshold = ApiRequestThreshold.valueOf(Config.getProperty(Config.Constants.MAX_REQUESTS_THRESHOLD));
    int maxRequestsPerUser = requestType.getMaxRequestsAllowed();
    String url = databaseHelper.getJdbcUrl();
    try (Connection conn = DriverManager.getConnection(url)) {
      ResultSet rs = requestThreshold.getThresholdQuery(conn, username, requestType).executeQuery();
      while (rs.next()) {
        return rs.getInt(1) < maxRequestsPerUser;
      }
    } catch (Exception e) {
      LOGGER.error("Error trying to check if user has exceeded max requests", e);
      throw new RuntimeException(e);
    }
    return false;
  }

  public void auditRequest(ApiRequestType apiRequestType, String username, String title) {
    try (Connection conn = DriverManager.getConnection(databaseHelper.getJdbcUrl())) {
      PreparedStatement preparedStatement = conn.prepareStatement("insert into user_requests (user, title, dt, request_type) values (?, ?, ?, ?)");
      preparedStatement.setString(1, username.toLowerCase());
      preparedStatement.setString(2, title);
      preparedStatement.setObject(3, LocalDate.now());
      preparedStatement.setInt(4, apiRequestType.ordinal());
      preparedStatement.execute();
    } catch (SQLException e) {
      LOGGER.error("Error trying to insert request", e);
    }
  }

  private final DatabaseHelper databaseHelper = new DatabaseHelper();
  private static final Logger LOGGER = LogManager.getLogger();
}
