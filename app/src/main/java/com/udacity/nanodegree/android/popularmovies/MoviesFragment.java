package com.udacity.nanodegree.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

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
import java.util.ArrayList;
import java.util.Date;

public class MoviesFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {

    private static final String MOVIES_KEY = "MOVIES";
    private static final String QUERY_PREFERENCE_KEY = "QUERY_PREF";

    private ArrayAdapter<Movie> mMovieAdapter;
    private String mQueryPreference;
    private GridView mMoviePostersGridView;
    private ImageView mNoDataRetrieved;
    private SwipeRefreshLayout mSwipeLayout;
    private FetchMoviesTask mAsyncTask;

    public MoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(Boolean.TRUE);

        String currentQueryPreference = getCurrentQueryPreference();
        mQueryPreference = currentQueryPreference;
        mMovieAdapter = new CustomMovieArrayAdapter(getActivity(), R.layout.grid_item_movie,
                R.id.grid_item_movie_poster);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(QUERY_PREFERENCE_KEY)) {
                mQueryPreference = savedInstanceState.getString(QUERY_PREFERENCE_KEY);
                // If the sort order shared preference has changed, it's necessary to call
                // TMDB API again in order to retrieve the new desired data
                if ((currentQueryPreference.equals(mQueryPreference)) && (savedInstanceState
                        .containsKey(MOVIES_KEY))) {
                    ArrayList<Movie> movies = (ArrayList<Movie>)savedInstanceState.get(MOVIES_KEY);
                    populateMovieAdapter(movies);
                    return;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mNoDataRetrieved = (ImageView)rootView.findViewById(R.id.no_movie_data_imageview);
        mMoviePostersGridView = (GridView)rootView.findViewById(R.id.gridview_movies);
        mMoviePostersGridView.setAdapter(mMovieAdapter);
        mMoviePostersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {

                Intent detailActivityIntent = new Intent(MoviesFragment.this.getActivity(),
                        MovieDetailActivity.class);
                detailActivityIntent.putExtra(Movie.TMDB_MOVIE_ID,
                        MoviesFragment.this.mMovieAdapter.getItem(i).getId());
                detailActivityIntent.putExtra(Movie.TMDB_MOVIE_ORIGINAL_TITLE,
                        MoviesFragment.this.mMovieAdapter.getItem(i).getOriginalTitle());
                String posterPath = MoviesFragment.this.mMovieAdapter.getItem(i).getPosterPath();
                if (posterPath != null) {
                    detailActivityIntent.putExtra(Movie.TMDB_MOVIE_POSTER_PATH, posterPath);
                }
                detailActivityIntent.putExtra(Movie.TMDB_MOVIE_OVERVIEW,
                        MoviesFragment.this.mMovieAdapter.getItem(i).getOverview());
                Double voteAverage = MoviesFragment.this.mMovieAdapter.getItem(i).getVoteAverage();
                if (voteAverage != null) {
                    detailActivityIntent.putExtra(Movie.TMDB_MOVIE_VOTE_AVERAGE, voteAverage);
                }
                detailActivityIntent.putExtra(Movie.TMDB_MOVIE_RELEASE_DATE,
                        MoviesFragment.this.mMovieAdapter.getItem(i).getReleaseDateString());
                MoviesFragment.this.startActivity(detailActivityIntent);
            }
        });
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.primary, R.color.accent, R.color.primary_dark);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        String currentQueryPreference = getCurrentQueryPreference();

        // If movie list has already been retrieved from previous saved state and query
        // preference is still the same, avoid calling TMDB API again, as it is not needed
        if ((mMovieAdapter.isEmpty()) || (!mQueryPreference.equals(currentQueryPreference))) {
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

        ArrayList<Movie> movies = new ArrayList<>();
        int numMovies = mMovieAdapter.getCount();
        for (int i=0; i<numMovies; i++) {
            movies.add(mMovieAdapter.getItem(i));
        }
        outState.putParcelableArrayList(MOVIES_KEY, movies);
        outState.putString(QUERY_PREFERENCE_KEY, mQueryPreference);
    }

    @Override public void onRefresh() {
        updateMoviesInfo(getCurrentQueryPreference());
    }

    private String getCurrentQueryPreference() {
        //Get sort_order preference value. By default, use sort by popularity option
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        return preferences.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_sort_order_popularity));
    }

    private void populateMovieAdapter(ArrayList<Movie> movies) {
        MoviesFragment.this.mMovieAdapter.clear();
        for (int i=0; i<movies.size(); i++) {
            MoviesFragment.this.mMovieAdapter.add(movies.get(i));
        }
    }

    private void updateMoviesInfo(String queryPreference) {
        mQueryPreference = queryPreference;
        mAsyncTask = new FetchMoviesTask();
        mAsyncTask.execute(queryPreference);
    }

    private class CustomMovieArrayAdapter extends ArrayAdapter<Movie> {

        private Context mContext;
        private int mResource;
        private int mFieldId;
        private LayoutInflater mInflater;

        public CustomMovieArrayAdapter(Context context, int resourceId, int imageViewResourceId) {
            super(context, resourceId, imageViewResourceId);
            this.mContext = context;
            this.mResource = resourceId;
            this.mFieldId = imageViewResourceId;
            this.mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;
            ImageView image;

            if (convertView == null) {
                view = mInflater.inflate(mResource, parent, false);
            } else {
                view = convertView;
            }

            try {
                if (mFieldId == 0) {
                    //  If no custom field is assigned, assume the whole resource is an ImageView
                    image = (ImageView) view;
                } else {
                    //  Otherwise, find the ImageView field within the layout
                    image = (ImageView) view.findViewById(mFieldId);
                }
            } catch (ClassCastException e) {
                throw new IllegalStateException("CustomMovieArrayAdapter requires the resource ID" +
                        " to be an ImageView", e);
            }

            String posterPath = getItem(position).getPosterPath();
            if (posterPath!=null) {
                Picasso.with(mContext).load(posterPath).into(image);
            } else {
                Picasso.with(mContext).load(R.drawable.no_photo_movie_poster).into(image);
            }

            return view;
        }
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        private final String POPULAR_MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
        private final String SORT_PARAM = "sort_by";
        private final String VOTE_COUNT_PARAM = "vote_count.gte";
        private final String API_KEY_PARAM = "api_key";
        //Popular Movies does not consider movies with a lower number of votes than MIN_VOTE_COUNT
        private final String MIN_VOTE_COUNT = "100";
        private final String TMDB_MOVIE_RELEASE_DATE_FORMAT = "yyyy-MM-dd";
        private final SimpleDateFormat TMDB_MOVIE_RELEASE_DATE_SDF =
                new SimpleDateFormat(TMDB_MOVIE_RELEASE_DATE_FORMAT);
        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
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
                        .appendQueryParameter(VOTE_COUNT_PARAM, MIN_VOTE_COUNT)
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
        protected void onPostExecute(ArrayList<Movie> movies) {
            if (movies!=null) {
                MoviesFragment.this.mMoviePostersGridView.setVisibility(View.VISIBLE);
                MoviesFragment.this.mNoDataRetrieved.setVisibility(View.INVISIBLE);
                MoviesFragment.this.populateMovieAdapter(movies);
            } else {
                //Movies data cannot be retrieved correctly
                MoviesFragment.this.mMovieAdapter.clear();
                MoviesFragment.this.mMoviePostersGridView.setVisibility(View.INVISIBLE);
                MoviesFragment.this.mNoDataRetrieved.setVisibility(View.VISIBLE);
            }
            MoviesFragment.this.mSwipeLayout.setRefreshing(false);
        }

        /**
         * Take the String representing the complete obtained movies data in JSON Format and
         * pull out the needed data
         */
        private ArrayList<Movie> getMoviesDataFromJson(String moviesJsonStr) throws JSONException {

            // Base URL for all poster images
            final String TMDB_POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
            // Image size that is going to be requested
            final String TMDB_IMAGE_SIZE = "w185";

            JSONArray moviesArray = new JSONObject(moviesJsonStr).getJSONArray(Movie.TMDB_RESULTS);
            ArrayList<Movie> resultMovies = new ArrayList<Movie>(moviesArray.length());

            for(int i = 0; i < moviesArray.length(); i++) {
                // Get the JSON object representing the movie
                JSONObject movieResult = moviesArray.getJSONObject(i);

                // If some of the retrieved movies have no id, discard it because it's going
                // to be no possible to do any further needed API call for them
                if (!movieResult.has(Movie.TMDB_MOVIE_ID)) {
                    continue;
                }

                // Get required movie data, checking for possible missing and null values
                int movieId = movieResult.getInt(Movie.TMDB_MOVIE_ID);
                String movieOriginalTitle = checkForMissingOrNullValues(movieResult,
                        Movie.TMDB_MOVIE_ORIGINAL_TITLE,
                        getString(R.string.no_original_title_found));
                String posterPath = checkForMissingOrNullValues(movieResult,
                        Movie.TMDB_MOVIE_POSTER_PATH, (String)null);
                String movieOverview = checkForMissingOrNullValues(movieResult,
                        Movie.TMDB_MOVIE_OVERVIEW,
                        getString(R.string.no_overview_found));
                Double movieVoteAverage = checkForMissingOrNullValues(movieResult,
                        Movie.TMDB_MOVIE_VOTE_AVERAGE, (Double)null);
                Date movieReleaseDate = checkForMissingOrNullValues(movieResult,
                        Movie.TMDB_MOVIE_RELEASE_DATE, (Date)null);

                resultMovies.add(new Movie(movieId, movieOriginalTitle,
                        (posterPath!=null) ?
                                TMDB_POSTER_BASE_URL + TMDB_IMAGE_SIZE +
                                        movieResult.getString(Movie.TMDB_MOVIE_POSTER_PATH) : null,
                        movieOverview, movieVoteAverage, movieReleaseDate,
                        getString(R.string.sdf_format)));
            }

            return resultMovies;
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
