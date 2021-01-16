package com.botdarr.clients.matrix.transactions;

public class MatrixSendMessageRequest {
  public void setBody(String body) {
    this.body = body;
    this.formatted_body = body;
  }

  private String msgtype = "m.text";
  private String format = "org.matrix.custom.html";
  private String formatted_body;
  private String body;
}
