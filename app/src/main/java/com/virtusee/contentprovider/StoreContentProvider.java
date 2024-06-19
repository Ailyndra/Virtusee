package com.virtusee.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.virtusee.db.StoreTable;
import com.virtusee.db.VSDbHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.FileHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

@EProvider
public class StoreContentProvider extends ContentProvider {

    private VSDbHelper database;

	@Bean
	AuthHelper authHelper;

	// used for the UriMacher
	private static final int CONTENT_ALLROW = 10;
	private static final int CONTENT_SINGLE = 20;
    private static final int CONTENT_FILTER = 30;
    private static final int CONTENT_TAG = 40;
    private static final int CONTENT_TAG_FILTER = 50;
    private static final int CONTENT_TAG_SINGLE = 60;
    private static final int CONTENT_CIN = 70;

    private static final String AUTHORITY = "com.virtusee.core.provider.store";

	private static final String BASE_PATH = "store";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, CONTENT_ALLROW);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/cin", CONTENT_CIN);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/tagall", CONTENT_TAG);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/q/*", CONTENT_FILTER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/tag/*/q/*", CONTENT_TAG_FILTER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/tag/*", CONTENT_TAG_SINGLE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", CONTENT_SINGLE);
	}

    @Override
    public boolean onCreate() {
        database = VSDbHelper.getInstance(getContext());
        return false;
    }

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
		String[] selectionArgs, String sortOrder) {

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        String sql = "select 1";

        // Uisng SQLiteQueryBuilder instead of query() method
	    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

	    // check if the caller has requested a column which does not exists
	    checkColumns(projection);

	    // Set the table
	    queryBuilder.setTables(StoreTable.TABLE);

	    selection = StoreTable.COLUMN_PROJECT + "=?";
    	List<String> seList = new ArrayList<String>();
    	seList.add(authHelper.getDomain());

	    int uriType = sURIMatcher.match(uri);
//Log.e("store",String.valueOf(uriType));
	    switch (uriType) {
	    	case CONTENT_ALLROW:
                selection += " and  substr(day,"+day+",1) = '1' ";
//                selection += " and  substr(week,"+week+",1) = '1' ";
		    	break;
		    case CONTENT_SINGLE:
		    	selection += " and " + StoreTable.COLUMN_ID + " = ? ";
		    	seList.add(uri.getLastPathSegment());
		    	break;
		    case CONTENT_FILTER:
                selection += " and  substr(day,"+day+",1) = '1' ";
//                selection += " and  substr(week,"+week+",1) = '1' ";
		    	selection += " and (" + StoreTable.COLUMN_STORE_CODE + " like ? or "+StoreTable.COLUMN_STORE_NAME+" like ? ) ";
		    	seList.add("%"+uri.getLastPathSegment()+"%");
		    	seList.add("%"+uri.getLastPathSegment()+"%");
		    	break;

            case CONTENT_TAG:
                sql = "SELECT distinct a.tag as _id FROM store_tag a, store b " +
						"where a.id_store = b._id " +
						" and  substr(b.day,"+day+",1) = '1' " +
						" order by tag";
                break;

            case CONTENT_TAG_SINGLE:

                sql = "SELECT distinct a._id as _id, a.store_name as store_name, a.store_code as store_code, a.store_id as store_id, a.day as day, a.week as week, a.attr as attr, a.locked as locked, a.locked_when as locked_when, a.longitude as longitude, a.latitude as latitude, a.short as short  " +
                        " FROM store a, store_tag b where a._id = b.id_store " +
                        " and  substr(a.day,"+day+",1) = '1' " +
                        " and b.tag IN("+uri.getLastPathSegment()+")";

                break;

            case CONTENT_CIN:
                sql = "SELECT a._id as _id, a.store_name as store_name, a.store_code as store_code, a.store_id as store_id, a.day as day, a.week as week, a.attr as attr, a.locked as locked, a.locked_when as locked_when, a.longitude as longitude, a.latitude as latitude, a.short as short  " +
                        " FROM store a where locked = 1 and date(locked_when) = date('now','localtime')";
  //              Log.e("store",sql);
                break;

            case CONTENT_TAG_FILTER:
                List<String> segment =  uri.getPathSegments();

                String tagfilter = segment.get(2);
                String keyfilter = DatabaseUtils.sqlEscapeString("%" + segment.get(4) + "%");

                sql = "SELECT distinct a._id as _id, a.store_name as store_name, a.store_code as store_code, a.store_id as store_id, a.day as day, a.week as week, a.attr as attr, a.locked as locked, a.locked_when as locked_when, a.longitude as longitude, a.latitude as latitude, a.short as short  " +
                        "FROM store a, store_tag b where a._id = b.id_store" +
                        " and  substr(a.day,"+day+",1) = '1' " +
                        " and b.tag IN("+tagfilter+") " +
                        " and (store_code like "+keyfilter+" or store_name like "+keyfilter+" ) ";

                break;

		    default:
		    	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    selectionArgs = seList.toArray(new String[seList.size()]);
		if (authHelper.getDomain().equals("ulipc")) {
			sortOrder = StoreTable.COLUMN_STORE_NAME + " asc";
		}

	    SQLiteDatabase db = database.getReadableDatabase();
	    Cursor cursor;

        if(uriType==CONTENT_TAG || uriType==CONTENT_TAG_SINGLE || uriType==CONTENT_TAG_FILTER || uriType==CONTENT_CIN ){
            cursor = db.rawQuery(sql,null);
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
		    	id = sqlDB.replace(StoreTable.TABLE, null, values);
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
	      rowsDeleted = sqlDB.delete(StoreTable.TABLE, selection,
	          selectionArgs);
	      break;
	    case CONTENT_SINGLE:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsDeleted = sqlDB.delete(StoreTable.TABLE,
	            StoreTable.COLUMN_ID + "= ?", 
	            new String[]{id});
	      } else {
	        rowsDeleted = sqlDB.delete(StoreTable.TABLE,
	            StoreTable.COLUMN_ID + "=" + id 
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
	      rowsUpdated = sqlDB.update(StoreTable.TABLE, 
	          values, 
	          selection,
	          selectionArgs);
	      break;

	    case CONTENT_SINGLE:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsUpdated = sqlDB.update(StoreTable.TABLE, 
	            values,
	            StoreTable.COLUMN_ID + "=?" ,
                    new String[]{id});
	      } else {
	        rowsUpdated = sqlDB.update(StoreTable.TABLE, 
	            values,
	            StoreTable.COLUMN_ID + "=" + id 
	            + " and " 
	            + selection,
	            selectionArgs);
	      }
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
        //getContext().getContentResolver().notifyChange(uri, null);
        getContext().getContentResolver().notifyChange(CONTENT_URI, null);
	    return rowsUpdated;
	}

	private void checkColumns(String[] projection) {

	    String[] available = { StoreTable.COLUMN_STORE_ID,
	        StoreTable.COLUMN_STORE_CODE, StoreTable.COLUMN_STORE_NAME,
	        StoreTable.COLUMN_ID, StoreTable.COLUMN_PROJECT ,
            StoreTable.COLUMN_DAY , StoreTable.COLUMN_WEEK, StoreTable.COLUMN_ATTR,
                StoreTable.COLUMN_LONG,StoreTable.COLUMN_LAT,
                StoreTable.COLUMN_LOCKED,StoreTable.COLUMN_LOCKED_WHEN,
				StoreTable.COLUMN_SHORT
        };
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
