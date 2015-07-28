package com.udacity.nanodegree.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class MoviesFragment extends Fragment {

    private static final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private ArrayAdapter<String> mMoviePostersAdapter;

    public MoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        this.mMoviePostersAdapter = new CustomImageArrayAdapter(this.getActivity(), R.layout.grid_item_movie, R.id.grid_item_movie_imageview);

        GridView moviePostersGridView = (GridView)rootView.findViewById(R.id.gridview_movieposter);
        moviePostersGridView.setAdapter(this.mMoviePostersAdapter);

        new FetchMoviesTask().execute();

        return rootView;
    }

    private class CustomImageArrayAdapter extends ArrayAdapter<String> {

        private final String LOG_TAG = CustomImageArrayAdapter.class.getSimpleName();
        private Context mContext;
        private int mResource;
        private int mFieldId;
        private LayoutInflater mInflater;

        public CustomImageArrayAdapter(Context context, int resourceId, int imageViewResourceId) {
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
                view = this.mInflater.inflate(this.mResource, parent, false);
            } else {
                view = convertView;
            }

            try {
                if (this.mFieldId == 0) {
                    //  If no custom field is assigned, assume the whole resource is an ImageView
                    image = (ImageView) view;
                } else {
                    //  Otherwise, find the ImageView field within the layout
                    image = (ImageView) view.findViewById(this.mFieldId);
                }
            } catch (ClassCastException e) {
                Log.e(this.LOG_TAG, "You must supply a resource ID for an ImageView");
                throw new IllegalStateException(
                        "CustomImageArrayAdapter requires the resource ID to be an ImageView", e);
            }

            String item = getItem(position);
            Picasso.with(this.mContext).load(item).into(image);

            return view;
        }
    }

    private class FetchMoviesTask extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        private final String POPULAR_MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
        private final String SORT_PARAM = "sort_by";
        private final String API_KEY_PARAM = "api_key";

        @Override
        protected String[] doInBackground(Void... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            String sortOption = "popularity.desc";
            String apiKey = getString(R.string.themoviedb_api_key);

            try {
                // Construct the URL for the themoviedb.org query (/discover/movie endpoint)
                // http://docs.themoviedb.apiary.io/#
                Uri.Builder uriBuilder = Uri.parse(this.POPULAR_MOVIES_BASE_URL).buildUpon();
                uriBuilder.appendQueryParameter(this.SORT_PARAM, sortOption)
                        .appendQueryParameter(this.API_KEY_PARAM, apiKey);
                URL url = new URL(uriBuilder.build().toString());

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
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();

                return this.getMoviesDataFromJson(moviesJsonStr);
            } catch (IOException e) {
                Log.e(this.LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } catch (JSONException je) {
                Log.e(this.LOG_TAG, je.getMessage(), je);
                return null;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(this.LOG_TAG, "Error closing stream", e);
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String[] strings) {
            MoviesFragment.this.mMoviePostersAdapter.clear();
            for (int i=0; i<strings.length; i++) {
                MoviesFragment.this.mMoviePostersAdapter.add(strings[i]);
            }
        }

        /**
         * Take the String representing the complete obtained movies data in JSON Format and
         * pull out the data we need
         */
        private String[] getMoviesDataFromJson(String moviesJsonStr)
                throws JSONException {

            // This is the base URL for all poster images
            final String TMDB_POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
            // This is the image size used in Popular Movies app
            final String TMDB_IMAGE_SIZE = "w185";
            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_POSTER_PATH = "poster_path";


            JSONObject forecastJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = forecastJson.getJSONArray(TMDB_RESULTS);

            String[] resultStrs = new String[moviesArray.length()];
            for(int i = 0; i < moviesArray.length(); i++) {

                // Get the JSON object representing the movie
                JSONObject movieResult = moviesArray.getJSONObject(i);

                resultStrs[i] = TMDB_POSTER_BASE_URL + TMDB_IMAGE_SIZE + movieResult.getString(TMDB_POSTER_PATH);
            }

            return resultStrs;
        }
    }
}
