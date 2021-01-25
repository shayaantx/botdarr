package com.botdarr.api.radarr;

import java.util.List;

public class RadarrQueuePage {
  public List<RadarrQueue> getRecords() {
    return records;
  }

  public void setRecords(List<RadarrQueue> records) {
    this.records = records;
  }

  private Integer page;
  private Integer pageSize;
  private String sortKey;
  private String sortDirection;
  private Integer totalRecords;
  private List<RadarrQueue> records = null;
}
