package com.botdarr.api.lidarr;

import java.util.List;

public class LidarrQueueRecord {
  public Integer getArtistId() {
    return artistId;
  }

  public Integer getAlbumId() {
    return albumId;
  }

  public String getTitle() {
    return title;
  }

  public String getTimeleft() {
    return timeleft;
  }

  public List<LidarrQueueStatusMessage> getStatusMessages() {
    return statusMessages;
  }

  public String getStatus() {
    return status;
  }

  private Integer artistId;
  private Integer albumId;
  private Double size;
  private String title;
  private Double sizeleft;
  private String timeleft;
  private String estimatedCompletionTime;
  private String status;
  private String trackedDownloadStatus;
  private List<LidarrQueueStatusMessage> statusMessages = null;
  private String downloadId;
  private String protocol;
  private String downloadClient;
  private String indexer;
  private String outputPath;
  private Boolean downloadForced;
  private Integer id;
}
