package com.botdarr.api.sonarr;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SonarrCache {
  public SonarrShow getExistingShowFromTvdbId(long tvdbId) {
    return existingTvdbIdsToShows.get(tvdbId);
  }

  public SonarrShow getExistingShowFromSonarrId(long sonarrId) {
    return existingSonarrIdsToShows.get(sonarrId);
  }

  public SonarrEpisodeInformation getEpisode(long seriesId, long episodeNumber) {
    if (existingSeriesToEpisodes.get(seriesId) == null) {
       return null;
    }
    return existingSeriesToEpisodes.get(seriesId).get(episodeNumber);
  }

  public void addEpisode(long seriesId, long episodeNumber, SonarrEpisodeInformation sonarrEpisodeInformation) {
    Map<Long, SonarrEpisodeInformation> episodeInformationMap = existingSeriesToEpisodes.get(seriesId);
    if (episodeInformationMap == null) {
      episodeInformationMap = new ConcurrentHashMap<>();
      existingSeriesToEpisodes.put(seriesId, episodeInformationMap);
    }
    episodeInformationMap.put(episodeNumber, sonarrEpisodeInformation);
  }

  public boolean doesShowExist(String title) {
    return existingShowTitlesToSonarrId.containsKey(title.toLowerCase());
  }

  public void add(SonarrShow show) {
    existingTvdbIdsToShows.put(show.getKey(), show);
    existingShowTitlesToSonarrId.put(show.getTitle().toLowerCase(), show.getId());
    existingSonarrIdsToShows.put(show.getId(), show);
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
    List<Long> existingShowIds = new ArrayList<>();
    for (Long tvdbId : addUpdatedTvdbShowIds) {
      SonarrShow sonarrShow = existingTvdbIdsToShows.get(tvdbId);
      if (sonarrShow != null) {
        existingShowTitles.add(sonarrShow.getTitle().toLowerCase());
        existingShowIds.add(sonarrShow.getId());
      }
    }
    existingShowTitlesToSonarrId.keySet().retainAll(existingShowTitles);
    existingTvdbIdsToShows.keySet().retainAll(addUpdatedTvdbShowIds);
    existingSonarrIdsToShows.keySet().retainAll(existingShowIds);
    existingSeriesToEpisodes.keySet().retainAll(existingShowIds);
  }

  private Map<String, SonarrProfile> existingProfiles = new ConcurrentHashMap<>();
  private Map<String, Long> existingShowTitlesToSonarrId = new ConcurrentHashMap<>();
  private Map<Long, SonarrShow> existingTvdbIdsToShows = new ConcurrentHashMap<>();
  private Map<Long, SonarrShow> existingSonarrIdsToShows = new ConcurrentHashMap<>();
  private Map<Long, Map<Long, SonarrEpisodeInformation>> existingSeriesToEpisodes = new ConcurrentHashMap<>();
}
