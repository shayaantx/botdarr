package com.botdarr.clients.matrix.transactions;

public class MatrixSendImageRequest {
  public void setUrl(String url) {
    this.url = url;
  }

  private String msgtype = "m.image";
  private String url;
  private String body = "image-text";
}
