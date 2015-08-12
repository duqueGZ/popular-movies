package com.udacity.nanodegree.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class represents a Movie object inside Popular Movies app.
 */
public class Movie implements Parcelable {

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

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

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

    private Movie(Parcel in) {
        id = in.readInt();
        originalTitle = in.readString();
        posterPath = in.readString();
        overview = in.readString();
        voteAverage = in.readDouble();
        releaseDate = new Date(in.readLong());
        SDF = new SimpleDateFormat(in.readString());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(originalTitle);
        out.writeString(posterPath);
        out.writeString(overview);
        out.writeDouble(voteAverage);
        out.writeLong(releaseDate.getTime());
        out.writeString(SDF.toPattern());
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
