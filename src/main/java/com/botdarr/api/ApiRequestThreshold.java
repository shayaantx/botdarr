package com.botdarr.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;

public enum ApiRequestThreshold {
  DAY() {
    @Override
    public String getReadableName() {
      return "day";
    }

    @Override
    public PreparedStatement getThresholdQuery(Connection conn, String username, ApiRequestType requestType) throws SQLException {
      PreparedStatement preparedStatement = conn.prepareStatement("select count(*) from user_requests where user = ? and dt = ? and request_type = ?");
      preparedStatement.setString(1, username.toLowerCase());
      LocalDate today = LocalDate.now();
      preparedStatement.setObject(2, today);
      preparedStatement.setInt(3, requestType.ordinal());
      return preparedStatement;
    }
  },
  WEEK() {
    @Override
    public String getReadableName() {
      return "week";
    }

    @Override
    public PreparedStatement getThresholdQuery(Connection conn, String username, ApiRequestType requestType) throws SQLException {
      PreparedStatement preparedStatement = conn.prepareStatement("select count(*) from user_requests where user = ? and dt between ? and ? and request_type = ?");
      preparedStatement.setString(1, username.toLowerCase());
      LocalDate today = LocalDate.now();
      preparedStatement.setObject(2, today.with(DayOfWeek.MONDAY));
      preparedStatement.setObject(3, today.with(DayOfWeek.SUNDAY));
      preparedStatement.setInt(4, requestType.ordinal());
      return preparedStatement;
    }
  },
  MONTH() {
    @Override
    public String getReadableName() {
      return "month";
    }

    @Override
    public PreparedStatement getThresholdQuery(Connection conn, String username, ApiRequestType requestType) throws SQLException {
      PreparedStatement preparedStatement = conn.prepareStatement("select count(*) from user_requests where user = ? and dt between ? and ? and request_type = ?");
      preparedStatement.setString(1, username.toLowerCase());
      LocalDate today = LocalDate.now();
      preparedStatement.setObject(2, today.withDayOfMonth(1));
      preparedStatement.setObject(3, today.withDayOfMonth(today.lengthOfMonth()));
      preparedStatement.setInt(4, requestType.ordinal());
      return preparedStatement;
    }
  };

  public abstract String getReadableName();
  public abstract PreparedStatement getThresholdQuery(Connection conn, String username, ApiRequestType requestType) throws SQLException;
}
