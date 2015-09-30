package com.udacity.nanodegree.android.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.nanodegree.android.popularmovies.MovieDetailFragment;
import com.udacity.nanodegree.android.popularmovies.R;

/**
 * {@link TrailerAdapter} exposes a list of reviews
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class TrailerAdapter extends CursorAdapter {

    public TrailerAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    // These views are reused as needed
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_trailer, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    // Fill-in the views with the contents of the cursor
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String trailerName = cursor.getString(MovieDetailFragment.COL_TRAILER_NAME);
        viewHolder.trailerName.setText(trailerName);
    }

    /**
     * Cache of the children views for a movies grid view.
     */
    private static class ViewHolder {
        public final TextView trailerName;

        public ViewHolder(View view) {
            trailerName = (TextView) view.findViewById(R.id.list_item_trailer_name);
        }
    }
}