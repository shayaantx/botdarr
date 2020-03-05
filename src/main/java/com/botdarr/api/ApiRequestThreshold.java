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
    public PreparedStatement getThresholdQuery(Connection conn, String username) throws SQLException {
      PreparedStatement preparedStatement = conn.prepareStatement("select count(*) from user_requests where user = ? and dt = ?");
      preparedStatement.setString(1, username.toLowerCase());
      LocalDate today = LocalDate.now();
      preparedStatement.setObject(2, today);
      return preparedStatement;
    }
  },
  WEEK() {
    @Override
    public String getReadableName() {
      return "week";
    }

    @Override
    public PreparedStatement getThresholdQuery(Connection conn, String username) throws SQLException {
      PreparedStatement preparedStatement = conn.prepareStatement("select count(*) from user_requests where user = ? and dt between ? and ?");
      preparedStatement.setString(1, username.toLowerCase());
      LocalDate today = LocalDate.now();
      preparedStatement.setObject(2, today.with(DayOfWeek.MONDAY));
      preparedStatement.setObject(3, today.with(DayOfWeek.SUNDAY));
      return preparedStatement;
    }
  },
  MONTH() {
    @Override
    public String getReadableName() {
      return "month";
    }

    @Override
    public PreparedStatement getThresholdQuery(Connection conn, String username) throws SQLException {
      PreparedStatement preparedStatement = conn.prepareStatement("select count(*) from user_requests where user = ? and dt between ? and ?");
      preparedStatement.setString(1, username.toLowerCase());
      LocalDate today = LocalDate.now();
      preparedStatement.setObject(2, today.withDayOfMonth(1));
      preparedStatement.setObject(3, today.withDayOfMonth(today.lengthOfMonth()));
      return preparedStatement;
    }
  };

  public abstract String getReadableName();
  public abstract PreparedStatement getThresholdQuery(Connection conn, String username) throws SQLException;
}
