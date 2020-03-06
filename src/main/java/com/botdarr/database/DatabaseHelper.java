package com.botdarr.database;

import com.botdarr.Config;

import java.io.File;

public class DatabaseHelper {
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
}
