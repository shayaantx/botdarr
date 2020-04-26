package com.botdarr.api.lidarr;

public class LidarrRating {
  public Integer getVotes() {
    return votes;
  }

  public void setVotes(Integer votes) {
    this.votes = votes;
  }

  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
  }

  private Integer votes;
  private Double value;
}
