package com.botdarr.database;

import com.botdarr.Config;

import java.io.File;

public class DatabaseHelper {
  public String getJdbcUrl() {
    File currentWorkingDir = getDatabaseFile();
    return "jdbc:sqlite:" + currentWorkingDir;
  }

  public File getDatabaseFile() {
    return new File(System.getProperty("user.dir"), Config.Constants.DATABASE_NAME);
  }
}
