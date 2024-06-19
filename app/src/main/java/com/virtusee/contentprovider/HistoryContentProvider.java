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
import com.virtusee.db.FormTable;
import com.virtusee.db.StoreTable;
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
public class HistoryContentProvider extends ContentProvider {

    private VSDbHelper database;

	@Bean
	AuthHelper authHelper;

	// used for the UriMacher
	private static final int CONTENT_ALLROW = 10;
	private static final int CONTENT_SINGLE = 20;
    private static final int CONTENT_TODAY = 30;
    private static final int CONTENT_ACTIVITY = 40;
    private static final int CONTENT_STAT_TOKO = 50;
    private static final int CONTENT_STAT_FORM = 60;

	private static final String AUTHORITY = "com.virtusee.core.provider.history";

	private static final String BASE_PATH = "history";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, CONTENT_ALLROW);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CONTENT_SINGLE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/today", CONTENT_TODAY);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/stats/toko", CONTENT_STAT_TOKO);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/stats/form", CONTENT_STAT_FORM);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/activity/#", CONTENT_ACTIVITY);
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
	    //checkColumns(projection);

	    // Set the table
	    queryBuilder.setTables("answer,store,form");
        selection = "answer.id_form = form.form_id and answer.id_store = store.store_id ";
        selection += " and answer.project=? and answer.id_usr=?" ;

        sortOrder = "answer.whenupdate desc";

    	List<String> seList = new ArrayList<String>();
    	seList.add(authHelper.getDomain());
    	seList.add(authHelper.getUserid());

	    int uriType = sURIMatcher.match(uri);
        String sql = "";

        switch (uriType) {
            case CONTENT_ALLROW:
                break;
		    case CONTENT_SINGLE:
		    	// adding the ID to the original query
		    	selection += " and " + AnswerTable.TABLE+"."+AnswerTable.COLUMN_ID + " = ? ";
		    	seList.add(uri.getLastPathSegment());
	    	break;
		    case CONTENT_TODAY:
		    	// adding the ID to the original query
		    	List<String> segment =  uri.getPathSegments();
		    	selection += " and " +  AnswerTable.TABLE+"."+AnswerTable.COLUMN_ID_STORE + "=? and "+
                        AnswerTable.TABLE+"."+AnswerTable.COLUMN_ID_FORM + "=? and "+
		    				"date("+AnswerTable.TABLE+"."+AnswerTable.COLUMN_WHEN + ") = date('now','localtime')";

		    	seList.add(segment.get(2));
		    	seList.add(segment.get(4));

		    	//selectionArgs = new String[] {segment.get(2),segment.get(4)};
		    	break;

            case CONTENT_ACTIVITY:
                String dd = uri.getLastPathSegment();

                sql = "select a._id as _id, a.id_form as id_form, a.id_store as id_store, a.content as content, date(a.whenupdate) as tgl, time(a.whenupdate) as jam, a.lastsync as lastsync, b.store_name as store_name, c.title as title, d.tot as imgtot, d.lastsync as imgsync  " +
                        "from answer a  "+
                        "left join store b on a.id_store = b.store_id  " +
                        "left join form c on a.id_form = c.form_id  " +
                        "left join (select id_answer, count(_id) as tot,min(lastsync) as lastsync from photo where whenupdate > date('now','localtime','-"+dd+" day') group by id_answer) as d on d.id_answer = a._id " +
                        "where date(a.whenupdate) > date('now','-"+dd+" day')  " +
                        "and a.id_form <> 'ABSENSI' " +
                        " UNION ALL  " +
                        " SELECT DISTINCT -100 as _id, '' as id_form, '' as id_store, '' as content, date(whenupdate) as tgl, time('23:59:59') as jam, 0 as lastsync, '' as store_name, '' as title,  0 as imgtot, 0 as imgsync   " +
                        " from answer   " +
                        " where date(whenupdate) > date('now','-"+dd+" day')  " +
                        "ORDER BY tgl DESC, jam DESC, _id ASC ";

                break;

            case CONTENT_STAT_TOKO:
                sql = "SELECT count(distinct id_store) as total from answer " +
                        "where date(whenupdate) = date('now','localtime') " +
                        "and id_form <> 'ABSENSI' and id_form <> '5ac8e403d143990660316bbe' " +
						"and id_form <> '5ac617b3d143998106316bbe' and id_form <> '5ac617b3d143998106316bbe-2' " +
                        "and id_store <> '5a49cfe6d14399dc62f49dec' and id_store <> '60c48a93d14399490420ca00' " +
						"and id_form <> '610d3bd980ab882b088b4567' and id_form <> '5fe1b607d143995a5c2d8f29'";
				if (authHelper.getDomain().equals("indolakto"))
					sql.concat(" and id_form = '5ac701f7d14399df79316bbe' and id_form = '5ac70219d143994e68316bbf'");
                break;

            case CONTENT_STAT_FORM:
                sql = "SELECT count(id_form) as total from answer " +
                        "where date(whenupdate) = date('now','localtime') " +
                        "and id_form <> 'ABSENSI' and id_form <> '5ac8e403d143990660316bbe' " +
						"and id_form <> '5ac617b3d143998106316bbe' and id_form <> '5ac617b3d143998106316bbe-2' " +
						"and id_store <> '5a49cfe6d14399dc62f49dec' and id_store <> '60c48a93d14399490420ca00' " +
						"and id_form <> '610d3bd980ab882b088b4567' and id_form <> '5fe1b607d143995a5c2d8f29'";
				if (authHelper.getDomain().equals("indolakto"))
					sql.concat(" and id_form = '5ac701f7d14399df79316bbe' and id_form = '5ac70219d143994e68316bbf'");
                break;

            default:
		    	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }

	    selectionArgs = seList.toArray(new String[seList.size()]);

	    SQLiteDatabase db = database.getReadableDatabase();

        Cursor cursor;
        if(!sql.equals("")){
			Log.d("HistoryContent", "sql: " + sql);
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
		    	id = sqlDB.insert(AnswerTable.TABLE, null, values);
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
	    getContext().getContentResolver().notifyChange(uri, null);
	    return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
	    String[] available = {
            StoreTable.COLUMN_STORE_ID, StoreTable.COLUMN_STORE_NAME,
            FormTable.COLUMN_FORM_ID,FormTable.COLUMN_FORM_TITLE,
	    	AnswerTable.COLUMN_ID_FORM,AnswerTable.COLUMN_ID_STORE,
	        AnswerTable.COLUMN_CONTENT, AnswerTable.COLUMN_WHO,
	        AnswerTable.COLUMN_LONG, AnswerTable.COLUMN_LAT,
	        AnswerTable.COLUMN_LAST_SYNC, AnswerTable.COLUMN_WHEN,
	        AnswerTable.COLUMN_GPS_TIME,AnswerTable.COLUMN_GPS_ACCURACY,
	        AnswerTable.TABLE+"."+AnswerTable.COLUMN_ID, AnswerTable.COLUMN_PROJECT, AnswerTable.COLUMN_USER };

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
