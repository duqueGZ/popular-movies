package com.udacity.nanodegree.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.udacity.nanodegree.android.popularmovies.adapters.MovieAdapter;
import com.udacity.nanodegree.android.popularmovies.data.MovieContract;
import com.udacity.nanodegree.android.popularmovies.data.MovieProvider;
import com.udacity.nanodegree.android.popularmovies.util.Utility;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MoviesFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String QUERY_PREFERENCE_KEY = "QUERY_PREFERENCE";
    private static final String LANGUAGE_KEY = "LANGUAGE";
    private static final String SELECTED_POSITION_KEY = "SELECTED_POSITION";
    private static final int MOVIE_LOADER_ID = 0;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH
    };
    // These indices are tied to MOVIE_COLUMNS. If MOVIE_COLUMNS changes, these must change too.
    public static final int COL_ID = 0;
    public static final int COL_POSTER_PATH = 1;

    private MovieAdapter mMovieAdapter;
    private String mQueryPreference;
    private String mCurrentLanguage;
    private boolean mUseTwoPaneLayout;
    private boolean mPreferenceHasChanged;
    private int mSelectedPosition;

    @Bind(R.id.gridview_movies) GridView mMoviePostersGridView;
    @Bind(R.id.no_movie_data_imageview) ImageView mNoDataRetrieved;
    @Bind(R.id.swipe_container) SwipeRefreshLayout mSwipeLayout;

    public MoviesFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(Boolean.TRUE);

        mQueryPreference = null;
        mSelectedPosition = ListView.INVALID_POSITION;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(QUERY_PREFERENCE_KEY)) {
                mQueryPreference = savedInstanceState.getString(QUERY_PREFERENCE_KEY);
            }
            if (savedInstanceState.containsKey(SELECTED_POSITION_KEY)) {
                mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION_KEY);
            }
        }
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mCurrentLanguage = sharedPref.getString(LANGUAGE_KEY, null);

        getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);
        mMoviePostersGridView.setAdapter(mMovieAdapter);
        mMoviePostersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                if (cursor != null) {
                    Uri uri = null;
                    if (cursor.getCount()>0) {
                        uri = MovieContract.MovieEntry
                                .buildMovieUri(cursor.getInt(COL_ID));
                    }
                    ((Callback) getActivity())
                            .onItemSelected(uri);
                }
                mSelectedPosition = i;
            }
        });
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.primary, R.color.accent, R.color.primary_dark);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mPreferenceHasChanged = Boolean.FALSE;
        String currentQueryPreference = Utility.getCurrentQueryPreference(getActivity());
        String currentLanguage = Locale.getDefault().getLanguage();
        // If the sort order shared preference or the used language have changed, it's necessary
        // to call TMDB API again in order to retrieve the new desired data
        if ((!currentQueryPreference.equals(mQueryPreference)) ||
                (!currentLanguage.equals(mCurrentLanguage))) {
            mPreferenceHasChanged = Boolean.TRUE;
            mQueryPreference = currentQueryPreference;
            mCurrentLanguage = currentLanguage;
            Utility.updateMoviesInfo(this, new int[]{MOVIE_LOADER_ID}, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSelectedPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_POSITION_KEY, mSelectedPosition);
        }
        outState.putString(QUERY_PREFERENCE_KEY, mQueryPreference);
    }

    @Override
    public void onRefresh() {
        Utility.updateMoviesInfo(this, new int[]{MOVIE_LOADER_ID}, this);
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

    public void setUseTwoPaneLayout(boolean useTwoPaneLayout) {
        mUseTwoPaneLayout = useTwoPaneLayout;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri moviesUri = MovieContract.MovieEntry.CONTENT_URI;
        String currentQueryPreference = Utility.getCurrentQueryPreference(getActivity());
        String sortOrder;
        String selection = null;
        String[] selectionArgs = null;
        if (currentQueryPreference.equals(getString(R.string.pref_sort_order_favorites))) {
            //Popular Movies will show favorited movies sorted by popularity
            sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
            selection = MovieProvider.sFavoritedMoviesSelection;
            selectionArgs = new String[]{"1"};
        } else if (currentQueryPreference.equals(getString(R.string.pref_sort_order_rating))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        } else { // Popularity sort order is also the default used preference
            sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        }

        return new CursorLoader(getActivity(), moviesUri, MOVIE_COLUMNS, selection,
                selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //mSwipeLayout.setRefreshing(false);
        mMovieAdapter.swapCursor(data);
        if (data.getCount()>0) {
            MoviesFragment.this.mMoviePostersGridView.setVisibility(View.VISIBLE);
            MoviesFragment.this.mNoDataRetrieved.setVisibility(View.INVISIBLE);
            int position = mSelectedPosition;
            if (position==ListView.INVALID_POSITION) {
                position = 0;
            }
            if ((mPreferenceHasChanged) && (mUseTwoPaneLayout)) {
                CustomRunnable customRunnable = new CustomRunnable(position);
                mMoviePostersGridView.postDelayed(customRunnable, 0);
            }
            mMoviePostersGridView.smoothScrollToPosition(position);
        } else {
            //Movies data cannot be retrieved from DB
            MoviesFragment.this.mMoviePostersGridView.setVisibility(View.INVISIBLE);
            MoviesFragment.this.mNoDataRetrieved.setVisibility(View.VISIBLE);
            if (mUseTwoPaneLayout) {
                CustomRunnable customRunnable = new CustomRunnable(0);
                mMoviePostersGridView.postDelayed(customRunnable, 0);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    public void updateSwipeRefreshLayout(boolean isRefreshing) {
        mSwipeLayout.setRefreshing(isRefreshing);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {

        void onItemSelected(Uri dateUri);
    }

    private class CustomRunnable implements Runnable {

        private int position;

        public CustomRunnable(int position) {
            this.position = position;
        }

        @Override
        public void run() {
            mMoviePostersGridView.setSoundEffectsEnabled(Boolean.FALSE);
            mMoviePostersGridView.performItemClick(mMoviePostersGridView.getChildAt(position),
                    position, mMoviePostersGridView.getItemIdAtPosition(position));
            mMoviePostersGridView.setSoundEffectsEnabled(Boolean.TRUE);
        }
    }
}
