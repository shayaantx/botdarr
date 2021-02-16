package com.botdarr.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class CacheProfileStrategy<Z extends KeyBased<X>, X> {
  public abstract void deleteFromCache(List<X> profilesAddUpdated);
  public abstract List<Z> getProfiles();
  public abstract void addProfile(Z profile);

  public void cacheData() {
    List<X> profilesAddUpdated = new ArrayList<>();
    List<Z> profiles = getProfiles();
    if (profiles == null) {
      LOGGER.warn("Did not find any profiles available for caching, class=" + this.getClass().toString());
      return;
    }
    for (Z profile : profiles) {
      addProfile(profile);
      profilesAddUpdated.add(profile.getKey());
    }
    deleteFromCache(profilesAddUpdated);
  }
  private static final Logger LOGGER = LogManager.getLogger();
}
