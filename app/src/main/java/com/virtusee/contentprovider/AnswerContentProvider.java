package com.virtusee.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.virtusee.db.AnswerTable;
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
public class AnswerContentProvider extends ContentProvider {


	@Bean
	AuthHelper authHelper;

    private VSDbHelper database;

	// used for the UriMacher
	private static final int CONTENT_ALLROW = 10;
	private static final int CONTENT_SINGLE = 20;
	private static final int CONTENT_TODAY = 30;
	private static final int CONTENT_NOTSYNC = 40;
    private static final int CONTENT_STICKY = 50;
    private static final int CONTENT_FORM = 60;
    private static final int CONTENT_RESEND = 70;
    private static final int CONTENT_COUNT = 80;
    private static final int CONTENT_SINGLE_STORE = 90;

    private static final String AUTHORITY = "com.virtusee.core.provider.answer";

	private static final String BASE_PATH = "answer";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, CONTENT_ALLROW);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/all", CONTENT_ALLROW);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CONTENT_SINGLE);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/store/*/form/*/today", CONTENT_TODAY);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/store/*/form/*/sticky", CONTENT_STICKY);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/notsync", CONTENT_NOTSYNC);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/resend", CONTENT_RESEND);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/form/*/today", CONTENT_FORM);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/count", CONTENT_COUNT);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", CONTENT_SINGLE_STORE);
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

	    // check if the caller has requested a column which does not exists
	    checkColumns(projection);

	    // Set the table
	    queryBuilder.setTables(AnswerTable.TABLE);
	    String sql = "select 1";
    	selection = AnswerTable.COLUMN_PROJECT + "=? and "+AnswerTable.COLUMN_USER + "=?";
    	List<String> seList = new ArrayList<String>();
    	seList.add(authHelper.getDomain());
    	seList.add(authHelper.getUserid());
    	
	    int uriType = sURIMatcher.match(uri);
	    switch (uriType) {
            case CONTENT_ALLROW:
                break;
		    case CONTENT_SINGLE:
		    	// adding the ID to the original query
		    	selection += " and " + AnswerTable.COLUMN_ID + " = ? ";
		    	seList.add(uri.getLastPathSegment());

	    	break;
		    case CONTENT_TODAY:
		    	// adding the ID to the original query
		    	List<String> segment =  uri.getPathSegments();

		    	selection += " and " +  AnswerTable.COLUMN_ID_STORE + "=? and "+
		    				AnswerTable.COLUMN_ID_FORM + "=? and "+ 
		    				"date("+AnswerTable.COLUMN_WHEN + ") = date('now','localtime')";


		    	seList.add(segment.get(2));
		    	seList.add(segment.get(4));

		    	break;
		    case CONTENT_STICKY:
		    	// adding the ID to the original query
		    	List<String> segment2 =  uri.getPathSegments();
		    	selection += " and " +  AnswerTable.COLUMN_ID_STORE + "=? and "+ 
		    				AnswerTable.COLUMN_ID_FORM + "=? ";
		    	
		    	seList.add(segment2.get(2));
		    	seList.add(segment2.get(4));
		    	
		    	//selectionArgs = new String[] {segment.get(2),segment.get(4)};
		    	break;
		    case CONTENT_NOTSYNC:
		    	// adding the ID to the original query
		    	selection += " and (lastsync = 0 or lastsync is null or lastsync = '')";
                    break;
            case CONTENT_RESEND:
                // adding the ID to the original query
                selection += " and date("+AnswerTable.COLUMN_WHEN + ") >= '2017-08-18' and " +
                        "date("+AnswerTable.COLUMN_WHEN + ") <= '2017-08-30' ";
                break;
            case CONTENT_FORM:
                // adding the ID to the original query
                List<String> segment3 =  uri.getPathSegments();

                selection += " and  id_store <> 'wday' and "+
                        AnswerTable.COLUMN_ID_FORM + "=? and "+
                        "date("+AnswerTable.COLUMN_WHEN + ") = date('now','localtime')";


                seList.add(segment3.get(2));

                break;
			case CONTENT_COUNT:
				sql = "SELECT count(DISTINCT(id_store)) AS total FROM answer " +
						"WHERE date(whenupdate) = date('now','localtime') " +
						"AND id_form <> 'ABSENSI' AND id_store <> '5a49cfe6d14399dc62f49dec' " +
						"AND id_form <> '5ac8e403d143990660316bbe'";
				break;
			case CONTENT_SINGLE_STORE:
				selection += "and " + AnswerTable.COLUMN_ID_STORE + " =? and " +
						"date("+AnswerTable.COLUMN_WHEN + ") = date('now','localtime')";
				seList.add(uri.getLastPathSegment());
				break;
            default:
		    	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    
	    selectionArgs = seList.toArray(new String[seList.size()]);

	    SQLiteDatabase db = database.getReadableDatabase();
	    Cursor cursor;
	    if(uriType == CONTENT_COUNT) {
	    	cursor = db.rawQuery(sql, null);
		} else {
			cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		}
	    // make sure that potential listeners are getting notified
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
		    	id = sqlDB.insert(AnswerTable.TABLE, null, values);
		    	break;
		    default:
		    	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    
	    //getContext().getContentResolver().notifyChange(uri, null);
        getContext().getContentResolver().notifyChange(FormContentProvider.CONTENT_URI, null);

        return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
	    int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsDeleted = 0;
	    switch (uriType) {
	    case CONTENT_ALLROW:
	      rowsDeleted = sqlDB.delete(AnswerTable.TABLE, selection,
	          selectionArgs);
	      break;
	    case CONTENT_SINGLE:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsDeleted = sqlDB.delete(AnswerTable.TABLE,
	            AnswerTable.COLUMN_ID + "=" + id, 
	            null);
	      } else {
	        rowsDeleted = sqlDB.delete(AnswerTable.TABLE,
	            AnswerTable.COLUMN_ID + "=" + id 
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
	      rowsUpdated = sqlDB.update(AnswerTable.TABLE, 
	          values, 
	          selection,
	          selectionArgs);
	      break;

	    case CONTENT_SINGLE:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsUpdated = sqlDB.update(AnswerTable.TABLE, 
	            values,
	            AnswerTable.COLUMN_ID + "=" + id, 
	            null);
	      } else {
	        rowsUpdated = sqlDB.update(AnswerTable.TABLE, 
	            values,
	            AnswerTable.COLUMN_ID + "=" + id 
	            + " and " 
	            + selection,
	            selectionArgs);
	      }
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    //getContext().getContentResolver().notifyChange(uri, null);
        getContext().getContentResolver().notifyChange(FormContentProvider.CONTENT_URI, null);
        return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
	    String[] available = { 
	    	AnswerTable.COLUMN_ID_FORM,AnswerTable.COLUMN_ID_STORE,
	        AnswerTable.COLUMN_CONTENT, AnswerTable.COLUMN_WHO,
	        AnswerTable.COLUMN_LONG, AnswerTable.COLUMN_LAT,
	        AnswerTable.COLUMN_LAST_SYNC, AnswerTable.COLUMN_WHEN,
	        AnswerTable.COLUMN_GPS_TIME,AnswerTable.COLUMN_GPS_ACCURACY,
	        AnswerTable.COLUMN_ID, AnswerTable.COLUMN_PROJECT, AnswerTable.COLUMN_USER , AnswerTable.COLUMN_ENTER };
	    
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
