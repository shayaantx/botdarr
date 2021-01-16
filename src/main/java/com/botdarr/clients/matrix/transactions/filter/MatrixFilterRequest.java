package com.botdarr.clients.matrix.transactions.filter;

public class MatrixFilterRequest {
  public void setEventFields(String[] event_fields) {
    this.event_fields = event_fields;
  }

  public void setAccountData(MatrixEventFilter account_data) {
    this.account_data = account_data;
  }

  public void setPresence(MatrixEventFilter presence) {
    this.presence = presence;
  }

  public void setRoom(MatrixRoomFilter room) {
    this.room = room;
  }

  private String[] event_fields;
  private MatrixEventFilter presence;
  private MatrixEventFilter account_data;
  private MatrixRoomFilter room;
}
