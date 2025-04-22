package com.botdarr.database;

import com.botdarr.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class DatabaseHelper {
  private static final Logger LOGGER = LogManager.getLogger();

  public String getJdbcUrl() {
    File currentWorkingDir = getDatabaseFile();
    return "jdbc:sqlite:" + currentWorkingDir;
  }

  public File getDatabaseFile() {
    File databaseFile = new File("database", Config.Constants.DATABASE_NAME);
    if (!databaseFile.getParentFile().exists()) {
      databaseFile.getParentFile().mkdir();
    }
    return databaseFile;
  }

  public boolean addMacro(String newCommand, String oldCommand, String profile) {
    DatabaseHelper databaseHelper = new DatabaseHelper();
    String url = databaseHelper.getJdbcUrl();
    try (Connection conn = DriverManager.getConnection(url)) {
      PreparedStatement statement =
        conn.prepareStatement("INSERT INTO command_macros (command, original, profile) VALUES (?, ?, ?)");
      statement.setString(1, newCommand);
      statement.setString(2, oldCommand);
      statement.setString(3, profile);
      if (!statement.execute()) {
        //TODO: log
        return false;
      }
      return true;
    } catch (Exception e) {
      LOGGER.error("Error trying to check if user has exceeded max requests", e);
      throw new RuntimeException(e);
    }
  }

  //TODO: get macros
  //TODO: remove macros
}
