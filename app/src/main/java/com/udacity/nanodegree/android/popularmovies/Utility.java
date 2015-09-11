package com.udacity.nanodegree.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {

    //Popular Movies does not consider movies with a lower number of votes than MIN_VOTE_COUNT
    public static final String MIN_VOTE_COUNT = "100";
    //Popular Movies always shows a maximum of MAX_MOVIES_TO_QUERY in main activity grid view
    public static final String MAX_MOVIES = "20";

    public static String getCurrentQueryPreference(Context context) {
        //Get sort_order preference value. By default, use sort by popularity option
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        return preferences.getString(context.getString(R.string.pref_sort_order_key),
                context.getString(R.string.pref_sort_order_popularity));
    }
}
