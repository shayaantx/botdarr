package com.botdarr.api.radarr;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RadarrCache {
  public RadarrMovie getExistingMovie(long tmdbid) {
    return existingTmdbIdsToMovies.get(tmdbid);
  }

  public RadarrMovie getExistingMovieWithRadarrId(long radarrId) {
    return existingRadarrMovieIdsToMovies.get(radarrId);
  }

  public boolean doesMovieExist(String title) {
    return existingMovieTitlesToIds.containsKey(title.toLowerCase());
  }

  public Collection<RadarrProfile> getQualityProfiles() {
    return Collections.unmodifiableCollection(existingProfiles.values());
  }

  public void add(RadarrMovie movie) {
    existingTmdbIdsToMovies.put(movie.getKey(), movie);
    existingMovieTitlesToIds.put(movie.getTitle().toLowerCase(), movie.getId());
    existingRadarrMovieIdsToMovies.put(movie.getId(), movie);
  }

  public void addProfile(RadarrProfile qualityProfile) {
    existingProfiles.put(qualityProfile.getKey(), qualityProfile);
  }

  public RadarrProfile getProfile(String qualityProfileName) {
    return existingProfiles.get(qualityProfileName.toLowerCase());
  }

  public void removeDeletedProfiles(List<String> addUpdatedProfiles) {
    existingProfiles.keySet().retainAll(addUpdatedProfiles);
  }

  public void removeDeletedMovies(List<Long> addUpdatedMovies) {
    List<String> existingMovieTitles = new ArrayList<>();
    List<Long> existingMovieIds = new ArrayList<>();
    for (Long tmdbId : addUpdatedMovies) {
      RadarrMovie radarrMovie = existingTmdbIdsToMovies.get(tmdbId);
      if (radarrMovie != null) {
        existingMovieTitles.add(radarrMovie.getTitle().toLowerCase());
        existingMovieIds.add(radarrMovie.getId());
      }
    }
    existingTmdbIdsToMovies.keySet().retainAll(addUpdatedMovies);
    existingMovieTitlesToIds.keySet().retainAll(existingMovieTitles);
    existingRadarrMovieIdsToMovies.keySet().retainAll(existingMovieIds);
  }

  private Map<String, RadarrProfile> existingProfiles = new ConcurrentHashMap<>();
  private Map<String, Long> existingMovieTitlesToIds = new ConcurrentHashMap<>();
  private Map<Long, RadarrMovie> existingRadarrMovieIdsToMovies = new ConcurrentHashMap<>();
  private Map<Long, RadarrMovie> existingTmdbIdsToMovies = new ConcurrentHashMap<>();
}
