package com.botdarr.clients.matrix.transactions;

public class MatrixLoginInformation {
  public MatrixLoginFlow[] getFlows() {
    return flows;
  }

  private MatrixLoginFlow[] flows = new MatrixLoginFlow[]{};
}
