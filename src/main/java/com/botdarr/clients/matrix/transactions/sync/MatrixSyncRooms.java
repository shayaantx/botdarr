package com.botdarr.clients.matrix.transactions.sync;

import java.util.HashMap;
import java.util.Map;

public class MatrixSyncRooms {
  public Map<String, MatrixSyncJoinRoom> getJoin() {
    return join;
  }

  private Map<String, MatrixSyncJoinRoom> join = new HashMap<>();
}
