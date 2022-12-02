package com.botdarr.api.sonarr;

public class SonarrEpisodeInformation {
    public SonarrEpisodeInformation(long seasonNumber, long episodeNumber, String title, String overview) {
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.overview = overview;
    }

    public long getSeasonNumber() {
        return seasonNumber;
    }

    public long getEpisodeNumber() {
        return episodeNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    private final long seasonNumber;
    private final long episodeNumber;
    private final String title;
    private final String overview;
}
