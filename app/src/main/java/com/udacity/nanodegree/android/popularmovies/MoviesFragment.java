package com.udacity.nanodegree.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.udacity.nanodegree.android.popularmovies.data.MovieContract;
import com.udacity.nanodegree.android.popularmovies.data.MovieProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MoviesFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private static final String QUERY_PREFERENCE_KEY = "QUERY_PREF";
    private static final int MOVIE_LOADER_ID = 0;
    static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH
    };
    // These indices are tied to MOVIE_COLUMNS. If MOVIE_COLUMNS changes, these must change too.
    static final int COL_ID = 0;
    static final int COL_POSTER_PATH = 1;

    @Bind(R.id.gridview_movies) GridView mMoviePostersGridView;
    @Bind(R.id.no_movie_data_imageview) ImageView mNoDataRetrieved;
    @Bind(R.id.swipe_container) SwipeRefreshLayout mSwipeLayout;

    private MovieAdapter mMovieAdapter;
    private String mQueryPreference;
    private FetchMoviesTask mAsyncTask;

    public MoviesFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(Boolean.TRUE);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(QUERY_PREFERENCE_KEY)) {
                mQueryPreference = savedInstanceState.getString(QUERY_PREFERENCE_KEY);
            }
        }
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
                    Intent detailActivityIntent = new Intent(MoviesFragment.this.getActivity(),
                            MovieDetailActivity.class);
                    detailActivityIntent.setData(
                            MovieContract.MovieEntry.buildMovieUri(cursor.getInt(COL_ID)));
                    MoviesFragment.this.startActivity(detailActivityIntent);
                }
            }
        });
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.primary, R.color.accent, R.color.primary_dark);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        String currentQueryPreference = Utility.getCurrentQueryPreference(getActivity());
        // If the sort order shared preference has changed, it's necessary to call
        // TMDB API again in order to retrieve the new desired data
        if (!currentQueryPreference.equals(mQueryPreference)) {
            updateMoviesInfo(currentQueryPreference);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mAsyncTask!=null) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(QUERY_PREFERENCE_KEY, mQueryPreference);
    }

    @Override
    public void onRefresh() {
        updateMoviesInfo(Utility.getCurrentQueryPreference(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri moviesUri = MovieContract.MovieEntry.CONTENT_URI;
        String currentQueryPreference = Utility.getCurrentQueryPreference(getActivity());
        String sortOrder;
        if (currentQueryPreference.equals(getString(R.string.pref_sort_order_rating))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        } else { // Popularity sort order is also the default used preference
            sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        }

        return new CursorLoader(getActivity(), moviesUri, MOVIE_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
        if (data.getCount()>0) {
            MoviesFragment.this.mMoviePostersGridView.setVisibility(View.VISIBLE);
            MoviesFragment.this.mNoDataRetrieved.setVisibility(View.INVISIBLE);
        } else {
            //Movies data cannot be retrieved from DB
            MoviesFragment.this.mMoviePostersGridView.setVisibility(View.INVISIBLE);
            MoviesFragment.this.mNoDataRetrieved.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    public void updateMoviesInfo(String queryPreference) {
        Log.d(LOG_TAG, "GOING TO UPDATE MOVIES INFO: " + queryPreference);
        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
        mQueryPreference = queryPreference;
        mAsyncTask = new FetchMoviesTask(getActivity());
        mAsyncTask.execute(queryPreference);
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, Integer> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private final String POPULAR_MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
        private final String SORT_PARAM = "sort_by";
        private final String VOTE_COUNT_PARAM = "vote_count.gte";
        private final String API_KEY_PARAM = "api_key";

        private static final String TMDB_RESULTS = "results";
        private static final String TMDB_MOVIE_ID = "id";
        private static final String TMDB_MOVIE_ORIGINAL_TITLE = "original_title";
        private static final String TMDB_MOVIE_POSTER_PATH = "poster_path";
        private static final String TMDB_MOVIE_OVERVIEW = "overview";
        private static final String TMDB_MOVIE_VOTE_AVERAGE = "vote_average";
        private static final String TMDB_MOVIE_RELEASE_DATE = "release_date";
        private static final String TMDB_MOVIE_POPULARITY = "popularity";

        private final String TMDB_MOVIE_RELEASE_DATE_FORMAT = "yyyy-MM-dd";
        private final SimpleDateFormat TMDB_MOVIE_RELEASE_DATE_SDF =
                new SimpleDateFormat(TMDB_MOVIE_RELEASE_DATE_FORMAT);
        private final Context mContext;

        public FetchMoviesTask(Context context) {
            mContext = context;
        }

        @Override
        // The returned value indicates the number of retrieved results
        protected Integer doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String apiKey = getString(R.string.themoviedb_api_key);

            try {
                // Construct the URL for the themoviedb.org query (/discover/movie endpoint)
                // http://docs.themoviedb.apiary.io/#reference/discover/discovermovie
                if (params.length!=1) {
                    throw new InvalidParameterException("ERROR. Not valid parameter (sort order) " +
                            " passed to " + LOG_TAG);
                }
                String sortOrder = params[0];

                URL url = new URL(Uri.parse(POPULAR_MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sortOrder)
                        .appendQueryParameter(VOTE_COUNT_PARAM, Utility.MIN_VOTE_COUNT)
                        .appendQueryParameter(API_KEY_PARAM, apiKey)
                        .build().toString());

                // Create the request to themoviedb.org, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                return getMoviesDataFromJson(buffer.toString());
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in
                // attempting to parse it.
                return null;
            } catch (JSONException je) {
                Log.e(LOG_TAG, je.getMessage(), je);
                return null;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException ioe) {
                        Log.e(LOG_TAG, "Error closing stream", ioe);
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Integer results) {
            if (results==null) {
                //Movies data cannot be retrieved correctly
                Toast.makeText(getActivity(), getString(R.string.no_data_retrieved_from_tmdb),
                        Toast.LENGTH_LONG)
                        .show();
            }
            MoviesFragment.this.mSwipeLayout.setRefreshing(false);
        }

        /**
         * Take the String representing the complete obtained movies data in JSON Format and
         * pull out the needed data
         */
        private Integer getMoviesDataFromJson(String moviesJsonStr) throws JSONException {

            // Base URL for all poster images
            final String TMDB_POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
            // Image size that is going to be requested
            final String TMDB_IMAGE_SIZE = "w185";

            JSONArray moviesArray = new JSONObject(moviesJsonStr).getJSONArray(TMDB_RESULTS);
            Vector<ContentValues> resultMovies = new Vector<ContentValues>(moviesArray.length());
            Vector<String> resultTmdbIds = new Vector<>();

            for(int i = 0; ((i < moviesArray.length())
                    && (i < Integer.valueOf(Utility.MAX_MOVIES))); i++) {
                // Get the JSON object representing the movie
                JSONObject movieResult = moviesArray.getJSONObject(i);

                // If some of the retrieved movies have no id, discard it because it's going
                // to be no possible to do any further needed API call for them
                if (!movieResult.has(TMDB_MOVIE_ID)) {
                    continue;
                }

                // Get required movie data, checking for possible missing and null values
                int movieId = movieResult.getInt(TMDB_MOVIE_ID);
                String movieOriginalTitle = checkForMissingOrNullValues(movieResult,
                        TMDB_MOVIE_ORIGINAL_TITLE,
                        getString(R.string.no_original_title_found));
                String posterPath = checkForMissingOrNullValues(movieResult,
                        TMDB_MOVIE_POSTER_PATH, (String)null);
                String movieOverview = checkForMissingOrNullValues(movieResult,
                        TMDB_MOVIE_OVERVIEW,
                        getString(R.string.no_overview_found));
                Double movieVoteAverage = checkForMissingOrNullValues(movieResult,
                        TMDB_MOVIE_VOTE_AVERAGE, (Double)null);
                Date movieReleaseDate = checkForMissingOrNullValues(movieResult,
                        TMDB_MOVIE_RELEASE_DATE, (Date)null);
                Double moviePopularity = checkForMissingOrNullValues(movieResult,
                        TMDB_MOVIE_POPULARITY, (Double)null);

                resultTmdbIds.add(Integer.valueOf(movieId).toString());

                ContentValues movie = new ContentValues();
                movie.put(MovieContract.MovieEntry._ID, movieId);
                movie.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, movieOriginalTitle);
                movie.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movieOverview);
                movie.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, (posterPath!=null) ?
                        TMDB_POSTER_BASE_URL + TMDB_IMAGE_SIZE + posterPath : null);
                movie.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movieVoteAverage);
                movie.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate.getTime());
                movie.put(MovieContract.MovieEntry.COLUMN_POPULARITY, moviePopularity);

                resultMovies.add(movie);
            }

            // First delete existing values in DB for retrieved movies, in order to keep them
            // correctly updated in DB
            for (String id: resultTmdbIds) {
                mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                        MovieProvider.sMovieSelection, new String[]{id});
            }

            // Add to DB
            if (resultMovies.size() > 0 ) {
                ContentValues[] values = new ContentValues[resultMovies.size()];
                resultMovies.toArray(values);
                mContext.getContentResolver()
                        .bulkInsert(MovieContract.MovieEntry.CONTENT_URI, values);
            }

            return resultMovies.size();
        }

        private String checkForMissingOrNullValues(JSONObject jsonObject, String fieldName,
                                                   String defaultValue) throws JSONException {
            if ((jsonObject.has(fieldName)) && (!jsonObject.getString(fieldName).equals(""))
                && (!jsonObject.getString(fieldName).equals("null"))){
                return jsonObject.getString(fieldName);
            } else {
                return defaultValue;
            }
        }

        private Double checkForMissingOrNullValues(JSONObject jsonObject, String fieldName,
                                                   Double defaultValue) throws JSONException {
            if (jsonObject.has(fieldName)) {
                return jsonObject.getDouble(fieldName);
            } else {
                return defaultValue;
            }
        }

        private Date checkForMissingOrNullValues(JSONObject jsonObject, String fieldName,
                                                   Date defaultValue) throws JSONException {
            if (jsonObject.has(fieldName)) {
                try {
                    return TMDB_MOVIE_RELEASE_DATE_SDF.parse(jsonObject.getString(fieldName));
                } catch (ParseException e) {
                    Log.w(LOG_TAG, "Release date could not been correctly parsed");
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }
        }
    }
}
