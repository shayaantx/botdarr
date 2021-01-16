package com.botdarr.clients.matrix.transactions;

public class MatrixLoginResponse {
  public String getUserId() {
    return user_id;
  }

  public String getHomeServer() {
    return home_server;
  }

  public String getAccessToken() {
    return access_token;
  }

  private String user_id;
  private String home_server;
  private String access_token;
}
