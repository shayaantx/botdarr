package com.botdarr.clients.matrix.transactions.filter;

public class MatrixRoomEventFilter {
  public void setNotTypes(String[] not_types) {
    this.not_types = not_types;
  }

  private String[] not_types;
  //this setting makes it so we never receive historical events from the server
  //be careful reusing this type outside of MatrixRoomFilter
  private int limit = 0;
}
