package com.virtusee.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.virtusee.db.MasterTable;
import com.virtusee.db.VSDbHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.FileHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@EProvider
public class MasterContentProvider extends ContentProvider {
    @Bean
    AuthHelper authHelper;

    private VSDbHelper database;

    //user for the UriMatcher
    private static final int CONTENT_ALLROW = 10;
    private static final int CONTENT_TYPE = 20;
    private static final int CONTENT_WITH_PARENT = 30;

    private static final String AUTHORITY = "com.virtusee.core.provider.master";

    private static final String BASE_PATH = "master";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, CONTENT_ALLROW);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/type/*", CONTENT_TYPE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/type/*/parent_id/*", CONTENT_WITH_PARENT);
    }

    @Override
    public boolean onCreate() {
        database = VSDbHelper.getInstance(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exist
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(MasterTable.TABLE);

        selection = MasterTable.COLUMN_PROJECT + "=?";
        List<String> seList = new ArrayList<>();
        seList.add(authHelper.getDomain());

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case CONTENT_ALLROW:
                break;

            case CONTENT_TYPE:
                selection += " and " + MasterTable.COLUMN_TYPE + " like ?";
                seList.add(uri.getLastPathSegment());
                break;

            case CONTENT_WITH_PARENT:
                // adding the ID to the original query
                List<String> segment =  uri.getPathSegments();

                selection += " and " + MasterTable.COLUMN_TYPE + " like ? and " + MasterTable.COLUMN_PARENT_ID + " =?";
                seList.add(segment.get(2));
                seList.add(segment.get(4));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        selectionArgs = seList.toArray(new String[seList.size()]);
        sortOrder = MasterTable.COLUMN_NAME + " asc";

        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor;

        cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void checkColumns(String[] projection) {
        String[] available = {
                MasterTable.COLUMN_ID,MasterTable.COLUMN_NAME,
                MasterTable.COLUMN_TYPE, MasterTable.COLUMN_PARENT_ID, MasterTable.COLUMN_GOTO };

        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
