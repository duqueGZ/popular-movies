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
 * {@link ReviewAdapter} exposes a list of reviews
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ReviewAdapter extends CursorAdapter {

    public ReviewAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    // These views are reused as needed
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_review, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    // Fill-in the views with the contents of the cursor
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String reviewAuthor = cursor.getString(MovieDetailFragment.COL_REVIEW_AUTHOR);
        viewHolder.reviewAuthor.setText(reviewAuthor);
        String reviewContent = cursor.getString(MovieDetailFragment.COL_REVIEW_CONTENT);
        viewHolder.reviewContent.setText(reviewContent);
    }

    /**
     * Cache of the children views for a movies grid view.
     */
    private static class ViewHolder {
        public final TextView reviewAuthor;
        public final TextView reviewContent;

        public ViewHolder(View view) {
            reviewAuthor = (TextView) view.findViewById(R.id.list_item_review_author);
            reviewContent = (TextView) view.findViewById(R.id.list_item_review_content);
        }
    }
}