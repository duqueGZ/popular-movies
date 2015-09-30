/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.udacity.nanodegree.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manages a local database for movie data.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 12;
    private static final String DATABASE_NAME = "popular.movies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
                MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " INTEGER, " +
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
                MovieContract.MovieEntry.COLUMN_VOTE_COUNT + " INTEGER, " +
                MovieContract.MovieEntry.COLUMN_POPULARITY + " REAL, " +
                MovieContract.MovieEntry.COLUMN_FAVORITED + " INTEGER " +
                ");";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " +
                MovieContract.ReviewEntry.TABLE_NAME + " (" +
                MovieContract.ReviewEntry._ID + " TEXT PRIMARY KEY," +
                MovieContract.ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                MovieContract.ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                MovieContract.ReviewEntry.COLUMN_LANGUAGE + " TEXT NOT NULL, " +
                MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + MovieContract.ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieContract.MovieEntry.TABLE_NAME + " (" + MovieContract.MovieEntry._ID + "));";

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " +
                MovieContract.TrailerEntry.TABLE_NAME + " (" +
                MovieContract.TrailerEntry._ID + " TEXT PRIMARY KEY," +
                MovieContract.TrailerEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                MovieContract.TrailerEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                MovieContract.TrailerEntry.COLUMN_LANGUAGE + " TEXT NOT NULL, " +
                MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + MovieContract.TrailerEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieContract.MovieEntry.TABLE_NAME + " (" + MovieContract.MovieEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.TrailerEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
