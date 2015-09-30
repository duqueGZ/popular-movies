package com.udacity.nanodegree.android.popularmovies.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.util.Log;

import com.udacity.nanodegree.android.popularmovies.R;
import com.udacity.nanodegree.android.popularmovies.sync.PopularMoviesSyncAdapter;

public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    //Popular Movies does not consider movies with a lower number of votes than MIN_VOTE_COUNT
    public static final String MIN_VOTE_COUNT = "100";
    //Popular Movies always shows a maximum of MAX_MOVIES_TO_QUERY in main activity grid view
    public static final String MAX_MOVIES = "20";

    //WebView parameters
    public static final String HTML_TEXT_FORMAT =
            "<html><body style=\"text-align:justify\"> %s </body></Html>";
    public static final String HTML_TEXT_MIME_TYPE = "text/html; charset=utf-8";
    public static final String HTML_TEXT_ENCODING = "UTF-8";

    public static String getCurrentQueryPreference(Context context) {
        //Get sort_order preference value. By default, use sort by popularity option
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        return preferences.getString(context.getString(R.string.pref_sort_order_key),
                context.getString(R.string.pref_sort_order_popularity));
    }

    public static void openMovieTrailer(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + url + ", no receiving apps installed!");
        }
    }

    public static void updateMoviesInfo(Fragment fragment, int[] loaderIds,
                                 LoaderManager.LoaderCallbacks callbacks) {
        for (int loaderId : loaderIds) {
            fragment.getLoaderManager().restartLoader(loaderId, null, callbacks);
        }
        PopularMoviesSyncAdapter.syncImmediately(fragment.getActivity());
    }
}
