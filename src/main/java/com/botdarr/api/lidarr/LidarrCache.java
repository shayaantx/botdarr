package com.botdarr.api.lidarr;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LidarrCache {

  public boolean doesArtistExist(LidarrArtist content) {
    return existingArtistNamesToIds.containsKey(content.getArtistName().toLowerCase());
  }

  public void addArtist(LidarrArtist artist) {
    existingForeignArtistIdToArtist.put(artist.getForeignArtistId(), artist);
    existingArtistNamesToIds.put(artist.getArtistName().toLowerCase(), artist.getForeignArtistId());
  }

  public void addQualityProfile(LidarrQualityProfile lidarrQualityProfile) {
    existingQualityProfiles.put(lidarrQualityProfile.getKey(), lidarrQualityProfile);
  }

  public void addMetadataProfile(LidarrMetadataProfile lidarrMetadataProfile) {
    existingMetadataProfiles.put(lidarrMetadataProfile.getKey(), lidarrMetadataProfile);
  }

  public LidarrArtist getExistingArtist(LidarrArtist lidarrArtist) {
    return existingForeignArtistIdToArtist.get(lidarrArtist.getForeignArtistId());
  }

  public LidarrQualityProfile getQualityProfile(String qualityProfileName) {
    return existingQualityProfiles.get(qualityProfileName.toLowerCase());
  }

  public LidarrMetadataProfile getMetadataProfile(String metadataProfileName) {
    return existingMetadataProfiles.get(metadataProfileName.toLowerCase());
  }

  public void removeDeletedQualityProfiles(List<String> addUpdatedProfiles) {
    existingQualityProfiles.keySet().retainAll(addUpdatedProfiles);
  }

  public void removeDeletedMetadataProfiles(List<String> addUpdatedProfiles) {
    existingMetadataProfiles.keySet().retainAll(addUpdatedProfiles);
  }

  public void removeDeletedArtists(List<String> addUpdatedArtists) {
    existingArtistNamesToIds.values().retainAll(addUpdatedArtists);
    existingForeignArtistIdToArtist.keySet().retainAll(addUpdatedArtists);
  }

  private Map<String, String> existingArtistNamesToIds = new ConcurrentHashMap<>();
  private Map<String, LidarrArtist> existingForeignArtistIdToArtist = new ConcurrentHashMap<>();
  private Map<String, LidarrQualityProfile> existingQualityProfiles = new ConcurrentHashMap<>();
  private Map<String, LidarrMetadataProfile> existingMetadataProfiles = new ConcurrentHashMap<>();
}
