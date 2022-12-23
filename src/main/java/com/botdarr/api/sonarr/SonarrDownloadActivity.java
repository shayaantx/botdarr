package com.botdarr.api.sonarr;

public class SonarrDownloadActivity {
    public SonarrDownloadActivity(String title,
                                  long seasonNumber,
                                  long episodeNumber,
                                  String qualityProfileName,
                                  String status,
                                  String timeleft,
                                  String overview,
                                  String[] statusMessages) {
        this.title = title;
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
        this.qualityProfileName = qualityProfileName;
        this.status = status;
        this.timeleft = timeleft;
        this.statusMessages = statusMessages;
        this.overview = overview;
    }

    public String getTitle() {
        return title;
    }

    public long getSeasonNumber() {
        return seasonNumber;
    }

    public long getEpisodeNumber() {
        return episodeNumber;
    }

    public String getQualityProfileName() {
        return qualityProfileName;
    }

    public String getStatus() {
        return status;
    }

    public String getTimeleft() {
        return timeleft;
    }

    public String[] getStatusMessages() {
        return statusMessages;
    }

    public String getOverview() {
        return overview;
    }

    private final String title;
    private final String overview;
    private final long seasonNumber;
    private final long episodeNumber;
    private final String qualityProfileName;
    private final String status;
    private final String timeleft;
    private final String[] statusMessages;
}
