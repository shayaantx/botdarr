package com.botdarr.api.sonarr;

import com.botdarr.api.KeyBased;

import java.util.List;

public interface SonarrProfile extends KeyBased<String> {
    public long getId();
    public String getCutOffDisplayStr();
    public List<SonarrProfileQualityItem> getItems();
    public String getName();
}
