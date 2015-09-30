package com.udacity.nanodegree.android.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.udacity.nanodegree.android.popularmovies.MoviesFragment;
import com.udacity.nanodegree.android.popularmovies.R;

/**
 * {@link MovieAdapter} exposes a list of movies
 * from a {@link android.database.Cursor} to a {@link android.widget.GridView}.
 */
public class MovieAdapter extends CursorAdapter {

    public MovieAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    // These views are reused as needed
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movie, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    // Fill-in the views with the contents of the cursor
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String posterPath = cursor.getString(MoviesFragment.COL_POSTER_PATH);
        if (posterPath!=null) {
            Picasso.with(mContext).load(posterPath).into(viewHolder.posterView);
        } else {
            Picasso.with(mContext).load(R.drawable.no_photo_movie_poster)
                    .into(viewHolder.posterView);
        }
    }

    /**
     * Cache of the children views for a movies grid view.
     */
    private static class ViewHolder {
        public final ImageView posterView;

        public ViewHolder(View view) {
            posterView = (ImageView) view.findViewById(R.id.grid_item_movie_poster);
        }
    }
}