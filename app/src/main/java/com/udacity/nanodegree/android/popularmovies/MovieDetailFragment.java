package com.udacity.nanodegree.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.nanodegree.android.popularmovies.adapters.ReviewAdapter;
import com.udacity.nanodegree.android.popularmovies.adapters.TrailerAdapter;
import com.udacity.nanodegree.android.popularmovies.components.NonScrollListView;
import com.udacity.nanodegree.android.popularmovies.data.MovieContract;
import com.udacity.nanodegree.android.popularmovies.data.MovieProvider;
import com.udacity.nanodegree.android.popularmovies.util.Utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String DETAIL_URI = "URI";
    public static final String IS_TWO_PANE = "IS_TWO_PANE";

    private static final String LANGUAGE_KEY = "LANGUAGE";
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final int DETAIL_MOVIE_LOADER_ID = 1;
    private static final int DETAIL_MOVIE_TRAILERS_LOADER_ID = 2;
    private static final int DETAIL_MOVIE_REVIEWS_LOADER_ID = 3;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_VOTE_COUNT,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_FAVORITED
    };
    // These indices are tied to MOVIE_COLUMNS. If MOVIE_COLUMNS changes, these must change too.
    public static final int COL_ID = 0;
    public static final int COL_ORIGINAL_TITLE = 1;
    public static final int COL_VOTE_AVERAGE = 2;
    public static final int COL_VOTE_COUNT = 3;
    public static final int COL_RELEASE_DATE = 4;
    public static final int COL_POSTER_PATH = 5;
    public static final int COL_OVERVIEW = 6;
    public static final int COL_FAVORITED = 7;

    private static final String[] TRAILER_COLUMNS = {
            MovieContract.TrailerEntry._ID,
            MovieContract.TrailerEntry.COLUMN_NAME,
            MovieContract.TrailerEntry.COLUMN_KEY,
    };
    // These indices are tied to TRAILER_COLUMNS. If TRAILER_COLUMNS changes, these must change too.
    public static final int COL_TRAILER_ID = 0;
    public static final int COL_TRAILER_NAME = 1;
    public static final int COL_TRAILER_KEY = 2;

    private static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewEntry._ID,
            MovieContract.ReviewEntry.COLUMN_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_CONTENT,
    };
    // These indices are tied to REVIEW_COLUMNS. If REVIEW_COLUMNS changes, these must change too.
    public static final int COL_REVIEW_ID = 0;
    public static final int COL_REVIEW_AUTHOR = 1;
    public static final int COL_REVIEW_CONTENT = 2;

    private static final int DETAIL_IMAGE_WIDTH = 370;
    private static final int DETAIL_IMAGE_HEIGHT = 554;

    private ShareActionProvider mShareActionProvider;
    private Uri mUri;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private int mMovieId;
    private boolean mIsFavorited;
    private String mCurrentLanguage;
    private boolean mUseTwoPaneLayout;

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
    @Bind(R.id.movieVoteCount) TextView mMovieVoteCount;
    @Bind(R.id.listview_trailers) NonScrollListView mTrailersListView;
    @Bind(R.id.listview_reviews) NonScrollListView mReviewsListView;
    @Bind(R.id.buttonFavorite) ImageView mFavoriteButton;
    @Bind(R.id.detailRootView) View mRootView;

    public MovieDetailFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_MOVIE_LOADER_ID, null, this);
        getLoaderManager().initLoader(DETAIL_MOVIE_TRAILERS_LOADER_ID, null, this);
        getLoaderManager().initLoader(DETAIL_MOVIE_REVIEWS_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(Boolean.TRUE);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mCurrentLanguage = sharedPref.getString(LANGUAGE_KEY, null);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUseTwoPaneLayout = arguments
                    .getBoolean(MovieDetailFragment.IS_TWO_PANE, Boolean.FALSE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(MovieDetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, rootView);

        mTrailerAdapter = new TrailerAdapter(getActivity(), null, 0);
        mTrailersListView.setAdapter(mTrailerAdapter);
        mTrailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {

                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onTrailerItemSelected(MovieContract.TrailerEntry
                                    .buildTrailerYoutubeUrlFromKey(cursor
                                            .getString(COL_TRAILER_KEY)));
                }
            }
        });
        mReviewAdapter = new ReviewAdapter(getActivity(), null, 0);
        mReviewsListView.setAdapter(mReviewAdapter);

        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton favoriteButton = (ImageButton) v;
                int imageResource;
                int favorited;
                if (mIsFavorited) {
                    imageResource = R.drawable.favorite_button_unselected;
                    favorited = 0;
                } else {
                    imageResource = R.drawable.favorite_button_selected;
                    favorited = 1;
                }
                favoriteButton.setImageResource(imageResource);

                ContentValues movie = new ContentValues();
                movie.put(MovieContract.MovieEntry._ID, mMovieId);
                movie.put(MovieContract.MovieEntry.COLUMN_FAVORITED, favorited);
                getActivity().getContentResolver()
                        .update(MovieContract.MovieEntry.CONTENT_URI,
                                movie,
                                MovieProvider.sMovieSelection,
                                new String[]{Integer.valueOf(mMovieId).toString()});
            }
        });

        mRootView.setVisibility(View.INVISIBLE);

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
            mShareActionProvider.setShareIntent(createShareMovieIntent(null));
        } else {
            Log.e(LOG_TAG, "Share Action Provider is null?");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        String currentLanguage = Locale.getDefault().getLanguage();
        // If the used language has changed (and we are not using two-pane mode), it's necessary
        // to call TMDB API again in order to retrieve the new desired data
        if ((!currentLanguage.equals(mCurrentLanguage)) && (!mUseTwoPaneLayout)) {
            mCurrentLanguage = currentLanguage;
            Utility.updateMoviesInfo(this, new int[]{DETAIL_MOVIE_LOADER_ID,
                    DETAIL_MOVIE_REVIEWS_LOADER_ID, DETAIL_MOVIE_TRAILERS_LOADER_ID}, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LANGUAGE_KEY, mCurrentLanguage);
        editor.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            switch (id) {
                case DETAIL_MOVIE_LOADER_ID: {
                    return new CursorLoader(getActivity(),
                            mUri, MOVIE_COLUMNS, null, null, null);
                }
                case DETAIL_MOVIE_TRAILERS_LOADER_ID: {
                    return new CursorLoader(getActivity(),
                            MovieContract.TrailerEntry
                                    .buildTrailersUriByMovieId(Long.valueOf(MovieContract.TrailerEntry
                                            .getMovieIdFromUri(mUri))), TRAILER_COLUMNS,
                            null, null, null);
                }
                case DETAIL_MOVIE_REVIEWS_LOADER_ID: {
                    return new CursorLoader(getActivity(),
                            MovieContract.ReviewEntry
                                    .buildReviewsUriByMovieId(Long.valueOf(MovieContract.ReviewEntry
                                            .getMovieIdFromUri(mUri))), REVIEW_COLUMNS,
                            null, null, null);
                }
            }
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        long loaderId = loader.getId();
        if (loaderId == DETAIL_MOVIE_LOADER_ID) {
            onDetailMovieLoadFinished(data);
        } else if (loaderId == DETAIL_MOVIE_TRAILERS_LOADER_ID) {
            onDetailMovieTrailersLoadFinished(data);
        } else if (loaderId == DETAIL_MOVIE_REVIEWS_LOADER_ID) {
            onDetailMovieReviewsLoadFinished(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        long loaderId = loader.getId();
        if (loaderId == DETAIL_MOVIE_LOADER_ID) {
            //Do nothing
        } else if (loaderId == DETAIL_MOVIE_TRAILERS_LOADER_ID) {
            mTrailerAdapter.swapCursor(null);
        } else if (loaderId == DETAIL_MOVIE_REVIEWS_LOADER_ID) {
            mReviewAdapter.swapCursor(null);
        }
    }

    public void hideDetailLayout() {
        mRootView.setVisibility(View.INVISIBLE);
    }

    private void onDetailMovieLoadFinished(Cursor data) {
        mMovieId = data.getInt(COL_ID);
        if (data.getInt(COL_FAVORITED)==1) {
            mIsFavorited = Boolean.TRUE;
        } else {
            mIsFavorited = Boolean.FALSE;
        }
        mMovieOriginalTitle.setText(data.getString(COL_ORIGINAL_TITLE));
        String posterPath = data.getString(COL_POSTER_PATH);
        if (posterPath!=null) {
            Picasso.with(getActivity()).load(posterPath)
                    .resize(DETAIL_IMAGE_WIDTH, DETAIL_IMAGE_HEIGHT).into(mMoviePoster);
        } else {
            Picasso.with(getActivity()).load(R.drawable.no_photo_movie_poster)
                    .into(mMoviePoster);
        }
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.sdf_format));
        mMovieReleaseDate.setText(sdf.format(new Date(data.getLong(COL_RELEASE_DATE))));
        mMovieOverview.loadData(String.format(Utility.HTML_TEXT_FORMAT,
                data.getString(COL_OVERVIEW)), Utility.HTML_TEXT_MIME_TYPE,
                Utility.HTML_TEXT_ENCODING);
        mMovieOverview.setBackgroundColor(Color.TRANSPARENT);
        Double voteAverage = data.getDouble(COL_VOTE_AVERAGE);
        mMovieRating.setText(voteAverage.toString());
        Integer voteCount = data.getInt(COL_VOTE_COUNT);
        mMovieVoteCount.setText("(" + voteCount + " " +
                getActivity().getString(R.string.votes_label) + ")");
        configureMovieRatingStars(voteAverage);

        int favorited = data.getInt(COL_FAVORITED);
        int imageResource;
        if (favorited==1) {
            imageResource = R.drawable.favorite_button_selected;
        } else {
            imageResource = R.drawable.favorite_button_unselected;
        }
        mFavoriteButton.setImageResource(imageResource);

        mRootView.setVisibility(View.VISIBLE);
    }

    private void onDetailMovieTrailersLoadFinished(Cursor data) {
        mTrailerAdapter.swapCursor(data);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if ((data.moveToFirst()) && (mShareActionProvider != null)) {
            mShareActionProvider.setShareIntent(createShareMovieIntent(data
                    .getString(COL_TRAILER_KEY)));
        }
    }

    private void onDetailMovieReviewsLoadFinished(Cursor data) {
        mReviewAdapter.swapCursor(data);
    }

    private Intent createShareMovieIntent(String trailerKey) {
        if (trailerKey==null) {
            return null;
        }
        String trailerUrl = MovieContract.TrailerEntry
                .buildTrailerYoutubeUrlFromKey(trailerKey);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.share_movie_base_message) + " - " + trailerUrl + " - " +
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

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onTrailerItemSelected(String trailerKey);
    }
}
