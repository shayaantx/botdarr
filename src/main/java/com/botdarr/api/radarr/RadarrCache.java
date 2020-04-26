package com.botdarr.api.radarr;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RadarrCache {
  public RadarrMovie getExistingMovie(long tmdbid) {
    return existingTmdbIdsToMovies.get(tmdbid);
  }

  public boolean doesMovieExist(String title) {
    return existingMovieTitlesToIds.containsKey(title.toLowerCase());
  }

  public Long getMovieSonarrId(String title) {
    return existingMovieTitlesToIds.get(title.toLowerCase());
  }

  public Collection<RadarrProfile> getQualityProfiles() {
    return Collections.unmodifiableCollection(existingProfiles.values());
  }

  public void add(RadarrMovie movie) {
    existingTmdbIdsToMovies.put(movie.getTmdbId(), movie);
    existingMovieTitlesToIds.put(movie.getTitle().toLowerCase(), movie.getId());
  }

  public void addProfile(RadarrProfile qualityProfile) {
    existingProfiles.put(qualityProfile.getName().toLowerCase(), qualityProfile);
  }

  public RadarrProfile getProfile(String qualityProfileName) {
    return existingProfiles.get(qualityProfileName.toLowerCase());
  }

  public void reset() {
    existingProfiles.clear();
    existingMovieTitlesToIds.clear();
    existingTmdbIdsToMovies.clear();
  }

  private Map<String, RadarrProfile> existingProfiles = new ConcurrentHashMap<>();
  private Map<String, Long> existingMovieTitlesToIds = new ConcurrentHashMap<>();
  private Map<Long, RadarrMovie> existingTmdbIdsToMovies = new ConcurrentHashMap<>();
}
