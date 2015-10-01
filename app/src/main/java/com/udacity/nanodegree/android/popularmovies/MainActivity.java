package com.udacity.nanodegree.android.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.udacity.nanodegree.android.popularmovies.sync.PopularMoviesSyncAdapter;
import com.udacity.nanodegree.android.popularmovies.util.Utility;

public class MainActivity extends AppCompatActivity
        implements MoviesFragment.Callback, MovieDetailFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    private double mSyncSeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                MovieDetailFragment detailFragment = new MovieDetailFragment();
                Bundle arguments = new Bundle();
                arguments.putBoolean(MovieDetailFragment.IS_TWO_PANE, mTwoPane);
                detailFragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, detailFragment,
                                DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            if (getSupportActionBar()!=null) {
                getSupportActionBar().setElevation(0f);
            }
        }

        MoviesFragment moviesFragment = ((MoviesFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_container));
        moviesFragment.setUseTwoPaneLayout(mTwoPane);

        PopularMoviesSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(syncStartedReceiver,
                new IntentFilter(PopularMoviesSyncAdapter.SYNC_STARTED));
        registerReceiver(syncFinishedReceiver,
                new IntentFilter(PopularMoviesSyncAdapter.SYNC_FINISHED));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(syncStartedReceiver);
        unregisterReceiver(syncFinishedReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (dateUri==null) {
                MovieDetailFragment detailFragment =
                        ((MovieDetailFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.movie_detail_container));
                detailFragment.hideDetailLayout();
            }
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.DETAIL_URI, dateUri);
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class)
                    .setData(dateUri);
            startActivity(intent);
        }
    }

    @Override
    public void onTrailerItemSelected(String url) {
        Utility.openMovieTrailer(this, url);
    }

    private BroadcastReceiver syncStartedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
           mSyncSeed = intent.getDoubleExtra(PopularMoviesSyncAdapter.SEED, 0);
        }
    };

    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            double finishSeed = intent.getDoubleExtra(PopularMoviesSyncAdapter.SEED, 0);
            if (finishSeed==mSyncSeed) {
                MoviesFragment moviesFragment = ((MoviesFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.main_container));
                moviesFragment.stopSwipeRefreshLayout();
            }
        }
    };
}
