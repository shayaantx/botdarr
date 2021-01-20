package com.botdarr.clients.matrix.transactions.sync;

public class MatrixSyncEventContent {
  public String getMsgtype() {
    return msgtype;
  }

  public String getBody() {
    return body;
  }

  private String msgtype;
  private String body;
}
