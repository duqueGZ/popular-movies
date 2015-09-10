package com.udacity.nanodegree.android.popularmovies;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieDetailFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final String TMDB_MOVIE_BASE_URL = "https://www.themoviedb.org/movie/";
    private static final String HTML_TEXT_FORMAT =
            "<html><body style=\"text-align:justify\"> %s </body></Html>";

    @Bind({R.id.movieRatingStar1, R.id.movieRatingStar2, R.id.movieRatingStar3,
           R.id.movieRatingStar4, R.id.movieRatingStar5, R.id.movieRatingStar6,
           R.id.movieRatingStar7, R.id.movieRatingStar8, R.id.movieRatingStar9,
           R.id.movieRatingStar10})
    List<ImageView> mMovieRatingStars;
    @Bind(R.id.moviePoster) ImageView mMoviePoster;
    @Bind(R.id.movieOriginalTitle) TextView mMovieOriginalTitle;
    @Bind(R.id.movieReleaseDate) TextView mMovieReleaseDate;
    @Bind(R.id.movieOverview) WebView mMovieOverview;
    @Bind(R.id.movieRating) TextView mMovieRating;
    private String mMovieTmdbUrl;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(Boolean.TRUE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, rootView);

        Intent activityIntent = getActivity().getIntent();
        if (activityIntent != null) {
            if (activityIntent.hasExtra(Movie.TMDB_MOVIE_ID)) {
                int movieId = activityIntent.getIntExtra(Movie.TMDB_MOVIE_ID, 0);
                mMovieTmdbUrl = TMDB_MOVIE_BASE_URL + Integer.valueOf(movieId).toString();
            }
            if (activityIntent.hasExtra(Movie.TMDB_MOVIE_ORIGINAL_TITLE)) {
                mMovieOriginalTitle.setText(activityIntent
                        .getStringExtra(Movie.TMDB_MOVIE_ORIGINAL_TITLE));
            }
            String posterPath = null;
            if (activityIntent.hasExtra(Movie.TMDB_MOVIE_POSTER_PATH)) {
                posterPath = activityIntent.getStringExtra(Movie.TMDB_MOVIE_POSTER_PATH);
            }
            if (posterPath!=null) {
                Picasso.with(getActivity()).load(posterPath).into(mMoviePoster);
            } else {
                Picasso.with(getActivity()).load(R.drawable.no_photo_movie_poster)
                        .into(mMoviePoster);
            }
            if (activityIntent.hasExtra(Movie.TMDB_MOVIE_RELEASE_DATE)) {
                mMovieReleaseDate.setText(activityIntent
                        .getStringExtra(Movie.TMDB_MOVIE_RELEASE_DATE));
            }
            if (activityIntent.hasExtra(Movie.TMDB_MOVIE_OVERVIEW)) {
                mMovieOverview.loadData(String.format(HTML_TEXT_FORMAT, activityIntent
                        .getStringExtra(Movie.TMDB_MOVIE_OVERVIEW)), "text/html; charset=utf-8",
                        "UTF-8");
                mMovieOverview.setBackgroundColor(Color.TRANSPARENT);
            }
            if (activityIntent.hasExtra(Movie.TMDB_MOVIE_VOTE_AVERAGE)) {
                Double voteAverage = activityIntent
                                .getDoubleExtra(Movie.TMDB_MOVIE_VOTE_AVERAGE, 0);
                        mMovieRating.setText(voteAverage.toString());
                configureMovieRatingStars(voteAverage);
            }
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_moviedetailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.
        if (mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(createShareMovieIntent());
        } else {
            Log.e(LOG_TAG, "Share Action Provider is null?");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.share_movie_base_message) + " - " + mMovieTmdbUrl + " - " +
                        getString(R.string.share_movie_hashtag));
        return shareIntent;
    }

    private void configureMovieRatingStars(Double voteAverage) {

        int integerPart = (int) Math.floor(voteAverage.doubleValue());
        double decimalPart = voteAverage - integerPart;

        int i;
        for (i=0;i<integerPart;i++) {
            Picasso.with(getActivity()).load(R.drawable.star1).into(mMovieRatingStars.get(i));
        }

        int drawableId = -1;
        if (decimalPart>=0.9) {
            drawableId = R.drawable.star09;
        } else if (decimalPart>=0.8) {
            drawableId = R.drawable.star08;
        } else if (decimalPart>=0.7) {
            drawableId = R.drawable.star07;
        } else if (decimalPart>=0.6) {
            drawableId = R.drawable.star06;
        } else if (decimalPart>=0.5) {
            drawableId = R.drawable.star05;
        } else if (decimalPart>=0.4) {
            drawableId = R.drawable.star04;
        } else if (decimalPart>=0.3) {
            drawableId = R.drawable.star03;
        } else if (decimalPart>=0.2) {
            drawableId = R.drawable.star02;
        } else if (decimalPart>=0.1) {
            drawableId = R.drawable.star01;
        }
        if (drawableId != -1) {
            Picasso.with(getActivity()).load(drawableId).into(mMovieRatingStars.get(i));
        }

    }
}
