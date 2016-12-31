package com.example.abdullah.weatherapp.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import com.example.abdullah.weatherapp.utilities.MyDateUtils;


/**
 * Created by Abdullah on 12/14/2016.
 */

public class WeatherProvider extends ContentProvider {

    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 101;

    private UriMatcher sUriMatcher = buildUriMatcher();
    private WeatherDbHelper openHelper;


    public static UriMatcher buildUriMatcher(){
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = WeatherContract.CONTENT_AUTHORITY;
        matcher.addURI(authority,WeatherContract.PATH_WEATHER,CODE_WEATHER);

        matcher.addURI(authority,WeatherContract.PATH_WEATHER + "/#",CODE_WEATHER_WITH_DATE);

        return matcher;

    }

    @Override
    public boolean onCreate() {

        openHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        switch (sUriMatcher.match(uri)){
            case CODE_WEATHER_WITH_DATE:{
                String normalizedUtcDate = uri.getLastPathSegment();

                String[] selectionArguments = new String[]{normalizedUtcDate};
                cursor = openHelper.getReadableDatabase().query(WeatherContract.WeatherEntry.TABLE_NAME, projection
                ,WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ", selectionArguments,null,null,sortOrder);
                break;
            }
            case CODE_WEATHER:{
                cursor = openHelper.getReadableDatabase().query(WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        /* Users of the delete method will expect the number of rows deleted to be returned. */
        int numRowsDeleted;

        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)) {

//          COMPLETED (2) Only implement the functionality, given the proper URI, to delete ALL rows in the weather table
            case CODE_WEATHER:
                numRowsDeleted = openHelper.getWritableDatabase().delete(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

//      COMPLETED (3) Return the number of rows deleted
        return numRowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)){
            case CODE_WEATHER: {
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long weatherdate = value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                        if (!MyDateUtils.isDateNormalized(weatherdate)) {
                            throw new IllegalArgumentException("Date must be normalized to insert");
                        }

                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            }
            default:
                return super.bulkInsert(uri,null);

        }

    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        openHelper.close();
        super.shutdown();
    }
}
