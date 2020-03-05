package com.botdarr.api;

import com.botdarr.Config;
import com.botdarr.database.DatabaseHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ApiRequests {
  public boolean checkRequestLimits() {
    String maxRequestsPerUser = Config.getProperty(Config.Constants.MAX_REQUESTS_PER_USER);
    String maxRequestsThreshold = Config.getProperty(Config.Constants.MAX_REQUESTS_THRESHOLD);
    if (Strings.isEmpty(maxRequestsPerUser)) {
      return false;
    }
    if (Strings.isEmpty(maxRequestsThreshold)) {
      return false;
    }
    try {
      Integer.valueOf(maxRequestsPerUser);
    } catch (NumberFormatException e) {
      LOGGER.error("Invalid max requests per user configuration", e);
      return false;
    }
    try {
      ApiRequestThreshold.valueOf(maxRequestsThreshold);
    } catch (Exception e) {
      String allRequestThresholds = Arrays.asList(ApiRequestThreshold.values())
        .stream()
        .sorted(Comparator.comparing(ApiRequestThreshold::getReadableName))
        .map(ApiRequestThreshold::getReadableName)
        .collect(Collectors.joining(", "));
      LOGGER.error("Invalid threshold option: " + allRequestThresholds, e);
      return false;
    }
    return true;
  }

  public boolean canMakeRequests(String username) {
    ApiRequestThreshold requestThreshold = ApiRequestThreshold.valueOf(Config.getProperty(Config.Constants.MAX_REQUESTS_THRESHOLD));
    int maxRequestsPerUser = Integer.valueOf(Config.getProperty(Config.Constants.MAX_REQUESTS_PER_USER));
    String url = DatabaseHelper.getJdbcUrl();
    try (Connection conn = DriverManager.getConnection(url)) {
      ResultSet rs = requestThreshold.getThresholdQuery(conn, username).executeQuery();
      while (rs.next()) {
        return rs.getInt(1) < maxRequestsPerUser;
      }
    } catch (Exception e) {
      LOGGER.error("Error trying to check if user has exceeded max requests", e);
      throw new RuntimeException(e);
    }
    return false;
  }

  public void auditRequest(String username, String title) {
    try (Connection conn = DriverManager.getConnection(DatabaseHelper.getJdbcUrl())) {
      PreparedStatement preparedStatement = conn.prepareStatement("insert into user_requests (user, title, dt) values (?, ?, ?)");
      preparedStatement.setString(1, username.toLowerCase());
      preparedStatement.setString(2, title);
      preparedStatement.setObject(3, LocalDate.now());
      preparedStatement.execute();
    } catch (SQLException e) {
      LOGGER.error("Error trying to insert request", e);
    }
  }

  private static final Logger LOGGER = LogManager.getLogger();
}
