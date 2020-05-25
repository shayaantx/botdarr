package com.botdarr.api;

import java.util.ArrayList;
import java.util.List;

public abstract class CacheProfileStrategy<Z extends KeyBased<X>, X> {
  public abstract void deleteFromCache(List<X> profilesAddUpdated);
  public abstract List<Z> getProfiles();
  public abstract void addProfile(Z profile);

  public void cacheData() {
    List<X> profilesAddUpdated = new ArrayList<>();
    List<Z> profiles = getProfiles();
    for (Z profile : profiles) {
      addProfile(profile);
      profilesAddUpdated.add(profile.getKey());
    }
    deleteFromCache(profilesAddUpdated);
  }
}
