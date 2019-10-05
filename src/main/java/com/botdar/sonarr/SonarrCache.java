package com.botdar.sonarr;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SonarrCache {
  public SonarrShow getExistingShowFromTvdbId(long tvdbId) {
    return existingTvdbIdsToMovies.get(tvdbId);
  }

  public SonarrShow getExistingShowFromTvrageId(long tvrageId) {
    return existingTvrageIdsToMovies.get(tvrageId);
  }

  public SonarrShow getExistingShowFromTvmazeId(long tvmazeId) {
    return existingTvmazeIdsToMovies.get(tvmazeId);
  }

  public void add(SonarrShow show) {
    existingTvdbIdsToMovies.put(show.getTvdbId(), show);
    existingTvrageIdsToMovies.put(show.getTvRageId(), show);
    existingTvmazeIdsToMovies.put(show.getTvMazeId(), show);
    existingMovieTitlesToIds.put(show.getTitle().toLowerCase(), show.getId());
  }

  public Long getSonarrId(String title) {
    return existingMovieTitlesToIds.get(title.toLowerCase());
  }

  public Collection<SonarrProfile> getQualityProfiles() {
    return Collections.unmodifiableCollection(existingProfiles.values());
  }

  public void addProfile(SonarrProfile qualityProfile) {
    existingProfiles.put(qualityProfile.getName().toLowerCase(), qualityProfile);
  }

  public SonarrProfile getProfile(String qualityProfileName) {
    return existingProfiles.get(qualityProfileName.toLowerCase());
  }

  private Map<String, SonarrProfile> existingProfiles = new ConcurrentHashMap<>();
  private Map<String, Long> existingMovieTitlesToIds = new ConcurrentHashMap<>();
  private Map<Long, SonarrShow> existingTvdbIdsToMovies = new ConcurrentHashMap<>();
  private Map<Long, SonarrShow> existingTvrageIdsToMovies = new ConcurrentHashMap<>();
  private Map<Long, SonarrShow> existingTvmazeIdsToMovies = new ConcurrentHashMap<>();
}
