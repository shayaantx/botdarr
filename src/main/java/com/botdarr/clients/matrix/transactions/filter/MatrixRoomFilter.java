package com.botdarr.clients.matrix.transactions.filter;

public class MatrixRoomFilter {
  public void setRooms(String[] rooms) {
    this.rooms = rooms;
  }

  public void setState(MatrixStateFilter state) {
    this.state = state;
  }

  public void setAccountData(MatrixRoomEventFilter account_data) {
    this.account_data = account_data;
  }

  public void setTimeline(MatrixRoomEventFilter timeline) {
    this.timeline = timeline;
  }

  private String[] rooms;
  private MatrixStateFilter state;
  private MatrixRoomEventFilter account_data;
  private MatrixRoomEventFilter timeline;
}
