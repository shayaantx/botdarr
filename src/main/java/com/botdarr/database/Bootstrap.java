package com.botdarr.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Bootstrap {
  private Bootstrap() {}

  public static void init() {
    Bootstrap bootstrap = new Bootstrap();
    if (!bootstrap.databaseExists()) {
      bootstrap.createDatabase();
    }
    bootstrap.upgradeDatabase();
  }

  private boolean databaseExists() {
    File currentWorkingDir = DatabaseHelper.getDatabaseFile();
    return currentWorkingDir.exists();
  }

  private void createDatabase() {
    String url = DatabaseHelper.getJdbcUrl();
    try (Connection conn = DriverManager.getConnection(url)) {
      if (conn != null) {
        DatabaseMetaData meta = conn.getMetaData();
        LOGGER.info("created database");
      } else {
        throw new RuntimeException("Could not create database");
      }

    } catch (SQLException e) {
      LOGGER.error("Error trying to create database", e);
      throw new RuntimeException(e);
    }
  }

  private void upgradeDatabase() {
    String url = DatabaseHelper.getJdbcUrl();
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl(url);
    Flyway flyway = Flyway.configure().locations("classpath:upgrade").dataSource(dataSource).load();
    flyway.migrate();
  }

  private static final Logger LOGGER = LogManager.getLogger();
}
