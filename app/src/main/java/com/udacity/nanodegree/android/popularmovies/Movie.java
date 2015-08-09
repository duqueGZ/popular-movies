package com.udacity.nanodegree.android.popularmovies;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class represents a Movie object inside Popular Movies app.
 */
public class Movie {

    //Names of the JSON objects that need to be extracted from themoviedb.org API data.
    public static final String TMDB_RESULTS = "results";
    public static final String TMDB_MOVIE_ID = "id";
    public static final String TMDB_MOVIE_ORIGINAL_TITLE = "original_title";
    public static final String TMDB_MOVIE_POSTER_PATH = "poster_path";
    public static final String TMDB_MOVIE_OVERVIEW = "overview";
    public static final String TMDB_MOVIE_VOTE_AVERAGE = "vote_average";
    public static final String TMDB_MOVIE_RELEASE_DATE = "release_date";

    private final SimpleDateFormat SDF;

    private int id;
    private String originalTitle;
    private String posterPath;
    private String overview;
    private Double voteAverage;
    private Date releaseDate;

    public Movie(int id, String originalTitle, String posterPath, String overview,
                 Double voteAverage, Date releaseDate, String dateFormat) {
        this.id = id;
        this.originalTitle = originalTitle;
        this.posterPath = posterPath;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;

        SDF = new SimpleDateFormat(dateFormat);
    }

    @Override
    public String toString() {
        return "MOVIE: id - " + id + " | original title - " + originalTitle + " | poster path - " +
                posterPath + " | overview - " + overview + " | vote average - " +
                voteAverage.toString() + " | release date - " + SDF.format(releaseDate);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getReleaseDateString() {
        return SDF.format(releaseDate);
    }
}
