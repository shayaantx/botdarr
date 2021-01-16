package com.botdarr.clients.matrix.transactions;

public class MatrixLoginRequest {
  public void setUser(String user) {
    this.user = user;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  private String user;
  private String password;
  private String type = "m.login.password";
}
