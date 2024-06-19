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

import com.virtusee.db.FormTable;
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
public class FormContentProvider extends ContentProvider {

    private VSDbHelper database;

	@Bean
	AuthHelper authHelper;

	// used for the UriMacher
    private static final int CONTENT_ALLROW = 10;
	private static final int CONTENT_SINGLE = 20;
	private static final int CONTENT_FILTER = 30;
    private static final int CONTENT_TAGALL = 40;
    private static final int CONTENT_TAGFILTER = 50;
    private static final int CONTENT_TAGFORM = 60;
    private static final int CONTENT_TAGFORM_FILTER = 70;
    private static final int CONTENT_TAGFORM_SINGLE = 80;
    private static final int CONTENT_CHECK_MANDATORY = 90;

	private static final String AUTHORITY = "com.virtusee.core.provider.form";

	private static final String BASE_PATH = "form";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

	/*public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
      + "/todos";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
      + "/todo";
*/
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, CONTENT_ALLROW);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/check_mandatory/*", CONTENT_CHECK_MANDATORY);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/tagall/*", CONTENT_TAGFORM);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/tag/*/t/*/q/*", CONTENT_TAGFORM_FILTER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/tag/*/t/*", CONTENT_TAGFORM_SINGLE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/t/*/q/*", CONTENT_TAGFILTER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/t/*", CONTENT_TAGALL);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/q/*", CONTENT_FILTER);
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

	    // Uisng SQLiteQueryBuilder instead of query() method
	    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

	    // check if the caller has requested a column which does not exists
//	    checkColumns(projection);

	    // Set the table
	    queryBuilder.setTables(FormTable.TABLE);

        String sql = "select 1";

	    selection = FormTable.COLUMN_PROJECT + "=?";
    	List<String> seList = new ArrayList<String>();
    	seList.add(authHelper.getDomain());


	    int uriType = sURIMatcher.match(uri);
	    switch (uriType) {
	    	case CONTENT_ALLROW:
		    	break;

		    case CONTENT_SINGLE:
		    	selection += " and " + FormTable.COLUMN_ID + " = ? ";
		    	seList.add(uri.getLastPathSegment());
		    	break;

			case CONTENT_FILTER:
				selection += " and (" + FormTable.COLUMN_FORM_TITLE + " like ? ) ";
				seList.add("%"+uri.getLastPathSegment()+"%");
				break;

            case CONTENT_TAGALL:
                String store = DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());

                sql =   "SELECT distinct  a._id as _id, a.title as title,a.content as content,a.sticky as sticky," +
                        "a.form_id as form_id,a.urut as urut,a.mandatory as mandatory, e.id_form as jawab  " +
                        "FROM form a\n" +
                        "left join form_tag b on a._id = b.id_form \n" +
                        "left join store_tag c on b.tag = c.tag and c.id_store = "+store+"\n" +
                        "left join answer e on e.id_form = a._id and e.id_store = "+store+" and date(e.whenupdate) = date('now','localtime') " +
                        "where (c.id_store is not null or b.id_form is null)\n" +
                        "order by a.urut\n";

                break;

            case CONTENT_TAGFILTER:
                List<String> segment =  uri.getPathSegments();

                String storefilter = DatabaseUtils.sqlEscapeString(segment.get(2));
                String keyfilter = DatabaseUtils.sqlEscapeString("%" + segment.get(4) + "%");

                sql =   "SELECT distinct  a._id as _id, a.title as title,a.content as content,a.sticky as sticky," +
                        "a.form_id as form_id,a.urut as urut,a.mandatory as mandatory,e.id_form as jawab " +
                        "FROM form a\n" +
                        "left join form_tag b on a._id = b.id_form \n" +
                        "left join store_tag c on b.tag = c.tag and c.id_store = "+storefilter+"\n" +
                        "left join answer e on e.id_form = a._id and e.id_store = "+storefilter+" and date(e.whenupdate) = date('now','localtime') " +
                        "where (c.id_store is not null or b.id_form is null)\n" +
                        "and title like " + keyfilter + " \n" +
                        "order by a.urut\n";

                break;
            case CONTENT_TAGFORM:

                String storetag= DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());

                sql =   "SELECT distinct a.tag as _id  FROM form_tag_own a \n" +
                        "left join `form_tag` b on a.id_form= b.id_form \n" +
                        "left join store_tag c on b.tag = c.tag and c.id_store = "+storetag+"\n" +
                        "where (c.id_store is not null or b.id_form is null)";

                break;
            case CONTENT_TAGFORM_SINGLE:
                List<String> segmenttag1 =  uri.getPathSegments();
                String tag1 = segmenttag1.get(2);
                String store2 = DatabaseUtils.sqlEscapeString(segmenttag1.get(4));

                sql = "SELECT distinct a._id as _id, a.title as title,a.content as content,a.sticky as sticky," +
                        "a.form_id as form_id,a.urut as urut,a.mandatory as mandatory,e.id_form as jawab " +
                        "FROM form a\n" +
                        "join form_tag_own d on a._id = d.id_form \n" +
                        "left join form_tag b on a._id = b.id_form \n" +
                        "left join store_tag c on b.tag = c.tag and c.id_store = "+store2+"\n" +
                        "left join answer e on e.id_form = a._id and e.id_store = "+store2+" and date(e.whenupdate) = date('now','localtime') " +
                        "where (c.id_store is not null or b.id_form is null)\n" +
                        "and d.tag in ( "+tag1+")\n " +
                        "order by a.urut";

                break;
            case CONTENT_TAGFORM_FILTER:
                List<String> segmenttag =  uri.getPathSegments();

                String tagformtag = segmenttag.get(2);
                String store2filter = DatabaseUtils.sqlEscapeString(segmenttag.get(4));
                String keyformfilter = DatabaseUtils.sqlEscapeString("%" + segmenttag.get(6) + "%");


                sql = "SELECT distinct a._id as _id, a.title as title,a.content as content,a.sticky as sticky," +
                        "a.form_id as form_id,a.urut as urut,a.mandatory as mandatory,e.id_form as jawab " +
                        "FROM form a\n" +
                        "join form_tag_own d on a._id = d.id_form \n" +
                        "left join form_tag b on a._id = b.id_form \n" +
                        "left join store_tag c on b.tag = c.tag and c.id_store = "+store2filter+"\n" +
                        "left join answer e on e.id_form = a._id and e.id_store = "+store2filter+" and date(e.whenupdate) = date('now','localtime') " +
                        "where (c.id_store is not null or b.id_form is null)\n" +
                        "and d.tag in ( "+tagformtag+")\n " +
                        "and a.title like " + keyformfilter + " \n" +
                        "order by a.urut";
                break;


            case CONTENT_CHECK_MANDATORY:

                String storeman= DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());

                sql =   "SELECT count(*) as total FROM form a \n" +
                        "left join form_tag b on a._id = b.id_form \n" +
                        "left join store_tag c on b.tag = c.tag and c.id_store = "+storeman+"\n" +
                        "left join answer d on d.id_form = a._id and d.id_store = "+storeman+" and date(whenupdate) = date('now','localtime') \n" +
                        "where a.mandatory = 1 and (c.id_store is not null or b.id_form is null)\n" +
                        "and d.id_form is null";
                break;
            default:
		    	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    selectionArgs = seList.toArray(new String[seList.size()]);
	    sortOrder = FormTable.COLUMN_FORM_ORDER + " asc";
	    
	    SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor;

        if(uriType==CONTENT_TAGFILTER || uriType==CONTENT_TAGALL  || uriType==CONTENT_TAGFORM ||uriType==CONTENT_TAGFORM_FILTER || uriType==CONTENT_TAGFORM_SINGLE  || uriType==CONTENT_CHECK_MANDATORY ){
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
		    	id = sqlDB.replace(FormTable.TABLE, null, values);
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
	      rowsDeleted = sqlDB.delete(FormTable.TABLE, selection,
	          selectionArgs);
	      break;
	    case CONTENT_SINGLE:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsDeleted = sqlDB.delete(FormTable.TABLE,
	            FormTable.COLUMN_ID + "= ?" , 
	            new String[]{id});
	      } else {
	        rowsDeleted = sqlDB.delete(FormTable.TABLE,
	            FormTable.COLUMN_ID + "=" + id 
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
	      rowsUpdated = sqlDB.update(FormTable.TABLE, 
	          values, 
	          selection,
	          selectionArgs);
	      break;

	    case CONTENT_SINGLE:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsUpdated = sqlDB.update(FormTable.TABLE, 
	            values,
	            FormTable.COLUMN_ID + "=" + id, 
	            null);
	      } else {
	        rowsUpdated = sqlDB.update(FormTable.TABLE, 
	            values,
	            FormTable.COLUMN_ID + "=" + id 
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
	    String[] available = { FormTable.COLUMN_FORM_ID,
	        FormTable.COLUMN_FORM_TITLE, FormTable.COLUMN_FORM_DESC,
	        FormTable.COLUMN_FORM_CONTENT,FormTable.COLUMN_ID, FormTable.COLUMN_PROJECT,
	        FormTable.COLUMN_FORM_ORDER,FormTable.COLUMN_FORM_STICKY,FormTable.COLUMN_FORM_MANDATORY};
	    
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
