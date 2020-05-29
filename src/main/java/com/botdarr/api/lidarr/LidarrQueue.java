package com.botdarr.api.lidarr;

import java.util.List;

public class LidarrQueue {
  public List<LidarrQueueRecord> getRecords() {
    return records;
  }

  private Integer page;
  private Integer pageSize;
  private String sortKey;
  private String sortDirection;
  private Integer totalRecords;
  private List<LidarrQueueRecord> records = null;
}
