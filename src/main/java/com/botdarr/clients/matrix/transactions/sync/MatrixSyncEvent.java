package com.botdarr.clients.matrix.transactions.sync;

public class MatrixSyncEvent {
  public String getSender() {
    return sender;
  }

  public String getEvent_id() {
    return event_id;
  }

  public MatrixSyncEventContent getContent() {
    return content;
  }
  private String sender;
  private String event_id;
  private MatrixSyncEventContent content;
}
