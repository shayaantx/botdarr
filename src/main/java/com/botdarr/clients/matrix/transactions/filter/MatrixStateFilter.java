package com.botdarr.clients.matrix.transactions.filter;

public class MatrixStateFilter {
  public void setTypes(String[] types) {
    this.types = types;
  }

  public void setRooms(String[] rooms) {
    this.rooms = rooms;
  }

  public void setNotSenders(String[] not_senders) {
    this.not_senders = not_senders;
  }

  private String[] types;
  private String[] rooms;
  private String[] not_senders;
}
