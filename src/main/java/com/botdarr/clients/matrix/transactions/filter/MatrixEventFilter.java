package com.botdarr.clients.matrix.transactions.filter;

public class MatrixEventFilter {
  public void setTypes(String[] types) {
    this.types = types;
  }

  public void setNotTypes(String[] not_types) {
    this.not_types = not_types;
  }

  private String[] types;
  private String[] not_types;
}
