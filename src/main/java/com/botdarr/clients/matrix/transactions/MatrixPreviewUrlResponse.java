package com.botdarr.clients.matrix.transactions;

import com.google.gson.annotations.SerializedName;

public class MatrixPreviewUrlResponse {
  public Integer getSize() {
    return size;
  }

  public String getMxcUri() {
    return mxcUri;
  }

  @SerializedName("matrix:image:size")
  private Integer size;
  @SerializedName("og:image")
  private String mxcUri;
}
