package com.botdarr.api.sonarr;

import java.util.*;
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

  public boolean doesShowExist(String title) {
    return existingShowTitlesToSonarrId.containsKey(title.toLowerCase());
  }

  public void add(SonarrShow show) {
    existingTvdbIdsToMovies.put(show.getKey(), show);
    existingTvrageIdsToMovies.put(show.getTvRageId(), show);
    existingTvmazeIdsToMovies.put(show.getTvMazeId(), show);
    existingShowTitlesToSonarrId.put(show.getTitle().toLowerCase(), show.getId());
  }

  public Collection<SonarrProfile> getQualityProfiles() {
    return Collections.unmodifiableCollection(existingProfiles.values());
  }

  public void addProfile(SonarrProfile qualityProfile) {
    existingProfiles.put(qualityProfile.getKey(), qualityProfile);
  }

  public SonarrProfile getProfile(String qualityProfileName) {
    return existingProfiles.get(qualityProfileName.toLowerCase());
  }

  public void removeDeletedProfiles(List<String> addUpdatedProfiles) {
    existingProfiles.keySet().retainAll(addUpdatedProfiles);
  }

  public void removeDeletedShows(List<Long> addUpdatedTvdbShowIds) {
    List<String> existingShowTitles = new ArrayList<>();
    List<Long> existingTvRageIds = new ArrayList<>();
    List<Long> existingTvmazeIds = new ArrayList<>();
    for (Long tvdbId : addUpdatedTvdbShowIds) {
      SonarrShow sonarrShow = existingTvdbIdsToMovies.get(tvdbId);
      if (sonarrShow != null) {
        existingShowTitles.add(sonarrShow.getTitle());
        existingTvRageIds.add(sonarrShow.getTvRageId());
        existingTvmazeIds.add(sonarrShow.getTvMazeId());
      }
    }
    existingShowTitlesToSonarrId.keySet().retainAll(addUpdatedTvdbShowIds);
    existingTvdbIdsToMovies.keySet().retainAll(addUpdatedTvdbShowIds);
    existingTvrageIdsToMovies.keySet().retainAll(existingTvRageIds);
    existingTvmazeIdsToMovies.keySet().retainAll(existingTvmazeIds);
  }

  private Map<String, SonarrProfile> existingProfiles = new ConcurrentHashMap<>();
  private Map<String, Long> existingShowTitlesToSonarrId = new ConcurrentHashMap<>();
  private Map<Long, SonarrShow> existingTvdbIdsToMovies = new ConcurrentHashMap<>();
  private Map<Long, SonarrShow> existingTvrageIdsToMovies = new ConcurrentHashMap<>();
  private Map<Long, SonarrShow> existingTvmazeIdsToMovies = new ConcurrentHashMap<>();
}
