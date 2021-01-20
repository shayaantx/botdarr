package com.botdarr.clients.matrix.transactions.sync;

public class MatrixSyncResponse {
  public String getNextBatch() {
    return next_batch;
  }

  public MatrixSyncRooms getRooms() {
    return rooms;
  }

  private String next_batch;
  private MatrixSyncRooms rooms = new MatrixSyncRooms();
}
