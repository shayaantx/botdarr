package com.botdarr.clients.matrix.transactions.sync;

public class MatrixSyncTimeline {
  public MatrixSyncEvent[] getEvents() {
    return events;
  }

  private MatrixSyncEvent[] events = new MatrixSyncEvent[]{};
}
