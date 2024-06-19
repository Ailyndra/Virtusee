package com.virtusee.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.virtusee.db.AudioTable;
import com.virtusee.db.PhotoTable;
import com.virtusee.db.VSDbHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.FileHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EProvider;

import java.util.ArrayList;
import java.util.List;

@EProvider
public class AudioContentProvider extends ContentProvider {
    private VSDbHelper database;

    @Bean
    AuthHelper authHelper;

    // used for the UriMacher
    private static final int CONTENT_ALLROW = 10;
    private static final int CONTENT_SINGLE = 20;
    private static final int CONTENT_ANSWER = 30;
    private static final int CONTENT_NOTSYNC = 40;

    private static final String AUTHORITY = "com.virtusee.core.provider.audio";

    private static final String BASE_PATH = "audio";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, CONTENT_ALLROW);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CONTENT_SINGLE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/answer/#", CONTENT_ANSWER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/notsync", CONTENT_NOTSYNC);
    }

    @Override
    public boolean onCreate() {
        database = VSDbHelper.getInstance(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
//	    checkColumns(projection);

        // Set the table
        queryBuilder.setTables(AudioTable.TABLE);
        List<String> seList = new ArrayList<String>();

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case CONTENT_ALLROW:
                break;
            case CONTENT_SINGLE:
                // adding the ID to the original query
                selection = AudioTable.COLUMN_ID + " = ? ";
                seList.add(uri.getLastPathSegment());

                break;

            case CONTENT_NOTSYNC:
                selection = "(lastsync = 0 or lastsync is null or lastsync = '')";
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if(seList.size()>0)
            selectionArgs = seList.toArray(new String[seList.size()]);
        sortOrder = PhotoTable.COLUMN_WHEN + " desc";

        SQLiteDatabase db = database.getReadableDatabase();
//        Log.e("imagerest",selection);
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();

        long id = 0;
        switch (uriType) {
            case CONTENT_ALLROW:
                id = sqlDB.insert(AudioTable.TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case CONTENT_ALLROW:
                rowsDeleted = sqlDB.delete(AudioTable.TABLE, selection,
                        selectionArgs);
                break;
            case CONTENT_SINGLE:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(AudioTable.TABLE,
                            AudioTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(AudioTable.TABLE,
                            AudioTable.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            case CONTENT_ANSWER:
                String answer = uri.getLastPathSegment();
                rowsDeleted = sqlDB.delete(AudioTable.TABLE,
                        AudioTable.COLUMN_ID_ANSWER + "=" + answer,
                        null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {

            case CONTENT_ALLROW:
                rowsUpdated = sqlDB.update(AudioTable.TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;

            case CONTENT_SINGLE:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(AudioTable.TABLE,
                            values,
                            PhotoTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(AudioTable.TABLE,
                            values,
                            AudioTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
