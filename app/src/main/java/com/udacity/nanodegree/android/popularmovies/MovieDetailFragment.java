package com.udacity.nanodegree.android.popularmovies;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
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
import com.udacity.nanodegree.android.popularmovies.data.MovieContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final String TMDB_MOVIE_BASE_URL = "https://www.themoviedb.org/movie/";
    private static final String HTML_TEXT_FORMAT =
            "<html><body style=\"text-align:justify\"> %s </body></Html>";
    private static final int DETAIL_MOVIE_LOADER_ID = 1;
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_FAVORITED
    };
    // These indices are tied to MOVIE_COLUMNS. If MOVIE_COLUMNS changes, these must change too.
    private static final int COL_ID = 0;
    private static final int COL_ORIGINAL_TITLE = 1;
    private static final int COL_VOTE_AVERAGE = 2;
    private static final int COL_RELEASE_DATE = 3;
    private static final int COL_POSTER_PATH = 4;
    private static final int COL_OVERVIEW = 5;
    private static final int COL_FAVORITED = 6;

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
    private ShareActionProvider mShareActionProvider;

    public MovieDetailFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_MOVIE_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
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

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_moviedetailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        return new CursorLoader(getActivity(),
                intent.getData(), MOVIE_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        mMovieTmdbUrl = TMDB_MOVIE_BASE_URL +
                Integer.valueOf(data.getInt(COL_ID)).toString();
        mMovieOriginalTitle.setText(data.getString(COL_ORIGINAL_TITLE));
        String posterPath = data.getString(COL_POSTER_PATH);
        if (posterPath!=null) {
            Picasso.with(getActivity()).load(posterPath).into(mMoviePoster);
        } else {
            Picasso.with(getActivity()).load(R.drawable.no_photo_movie_poster)
                    .into(mMoviePoster);
        }
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.sdf_format));
        mMovieReleaseDate.setText(sdf.format(new Date(data.getLong(COL_RELEASE_DATE))));
        mMovieOverview.loadData(String.format(HTML_TEXT_FORMAT,
                data.getString(COL_OVERVIEW)), "text/html; charset=utf-8", "UTF-8");
        mMovieOverview.setBackgroundColor(Color.TRANSPARENT);
        Double voteAverage = data.getDouble(COL_VOTE_AVERAGE);
        mMovieRating.setText(voteAverage.toString());
        configureMovieRatingStars(voteAverage);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareMovieIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Do nothing
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
