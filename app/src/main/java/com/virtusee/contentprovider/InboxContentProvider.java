package com.virtusee.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.virtusee.db.InboxTable;
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
public class InboxContentProvider extends ContentProvider {

    private VSDbHelper database;

	@Bean
	AuthHelper authHelper;

	// used for the UriMacher
	private static final int CONTENT_ALLROW = 10;
	private static final int CONTENT_SINGLE = 20;
    private static final int CONTENT_TODAY = 30;
    private static final int CONTENT_ACTIVITY = 40;

	private static final String AUTHORITY = "com.virtusee.core.provider.inbox";

	private static final String BASE_PATH = "inbox";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, CONTENT_ALLROW);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CONTENT_SINGLE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/today", CONTENT_TODAY);
	}



    @Override
    public boolean onCreate() {
        database = VSDbHelper.getInstance(getContext());
        return false;
    }

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
		String[] selectionArgs, String sortOrder) {

	    // Uisng SQLiteQueryBuilder instead of query() method
	    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

	    // Set the table
	    queryBuilder.setTables("inbox");


    	List<String> seList = new ArrayList<String>();

	    int uriType = sURIMatcher.match(uri);
        String sql = "select 1";

        switch (uriType) {
            case CONTENT_ALLROW:
                break;

		    default:
		    	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    
	    selectionArgs = seList.toArray(new String[seList.size()]);

	    SQLiteDatabase db = database.getReadableDatabase();

        Cursor cursor;
        cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);

	    return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();

	    long id = 0;
	    switch (uriType) {
		    case CONTENT_ALLROW:
		    	id = sqlDB.insert(InboxTable.TABLE, null, values);
		    	break;
		    default:
		    	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    
	    getContext().getContentResolver().notifyChange(uri, null);
	    return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
	    int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsDeleted = 0;
	    switch (uriType) {
	    case CONTENT_ALLROW:
	      rowsDeleted = sqlDB.delete(InboxTable.TABLE, selection,
	          selectionArgs);
	      break;
	    case CONTENT_SINGLE:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsDeleted = sqlDB.delete(InboxTable.TABLE,
	            InboxTable.COLUMN_ID + "=" + id, 
	            null);
	      } else {
	        rowsDeleted = sqlDB.delete(InboxTable.TABLE,
	            InboxTable.COLUMN_ID + "=" + id 
	            + " and " + selection,
	            selectionArgs);
	      }
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    getContext().getContentResolver().notifyChange(uri, null);
	    return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
	      String[] selectionArgs) {

	    int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsUpdated = 0;
	    switch (uriType) {
	    
	    case CONTENT_ALLROW:
	      rowsUpdated = sqlDB.update(InboxTable.TABLE, 
	          values, 
	          selection,
	          selectionArgs);
	      break;

	    case CONTENT_SINGLE:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsUpdated = sqlDB.update(InboxTable.TABLE, 
	            values,
	            InboxTable.COLUMN_ID + "=" + id, 
	            null);
	      } else {
	        rowsUpdated = sqlDB.update(InboxTable.TABLE, 
	            values,
	            InboxTable.COLUMN_ID + "=" + id 
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

	private void checkColumns(String[] projection) {
	    String[] available = {
	        InboxTable.COLUMN_CONTENT, InboxTable.COLUMN_ID,
	        InboxTable.COLUMN_URL, InboxTable.COLUMN_READ, InboxTable.COLUMN_DOWNLOAD};
	    
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
