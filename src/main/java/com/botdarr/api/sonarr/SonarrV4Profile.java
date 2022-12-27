package com.botdarr.api.sonarr;

import java.util.List;

public class SonarrV4Profile implements SonarrProfile {
    @Override
    public String getKey() {
        return name.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SonarrProfileQualityItem> getItems() {
        return items;
    }

    public void setItems(List<SonarrProfileQualityItem> items) {
        this.items = items;
    }

    public long getId() {
        return id;
    }

    @Override
    public String getCutOffDisplayStr() {
        return cutoff != null ? "" + cutoff : "";
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCutoff(Integer cutoff) {
        this.cutoff = cutoff;
    }

    public Integer getCutoff() {
        return cutoff;
    }

    private Integer cutoff;
    private String name;
    private List<SonarrProfileQualityItem> items;
    private long id;
}
