package com.botdarr.api;

import java.util.List;

public abstract class CacheProfileStrategy<Z> {
  public abstract void resetCache();
  public abstract List<Z> getProfiles();
  public abstract void addProfile(Z profile);

  public void cacheData() {
    resetCache();
    List<Z> profiles = getProfiles();
    for (Z profile : profiles) {
      addProfile(profile);
    }
  }
}
