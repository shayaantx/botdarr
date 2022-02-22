package com.botdarr.api.radarr;

public class RadarrRatings {
    public RadarrImdb getImdb() {
        return imdb;
    }

    public void setImdb(RadarrImdb imdb) {
        this.imdb = imdb;
    }

    public RadarrTmdb getTmdb() {
        return tmdb;
    }

    public void setTmdb(RadarrTmdb tmdb) {
        this.tmdb = tmdb;
    }

    private RadarrImdb imdb;
    private RadarrTmdb tmdb;
}
