package com.botdarr.api.radarr;

public class RadarrAlternateTitle {
    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public RadarrLanguage getLanguage() {
        return language;
    }

    public void setLanguage(RadarrLanguage language) {
        this.language = language;
    }

    private String sourceType;
    private int movieId;
    private String title;
    private int sourceId;
    private int votes;
    private int voteCount;
    private RadarrLanguage language;
}
