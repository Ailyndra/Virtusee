package com.virtusee.db;

import android.database.sqlite.SQLiteDatabase;

import com.virtusee.core.BuildConfig;

public class AnswerTable {
	public static final String TABLE = "answer";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ID_FORM = "id_form";
	public static final String COLUMN_ID_STORE = "id_store";
	public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_ENTER = "entertime";
    public static final String COLUMN_WHEN = "whenupdate";
	public static final String COLUMN_WHO = "whoupdate";
	public static final String COLUMN_LAST_SYNC = "lastsync";
	public static final String COLUMN_LONG = "long";
	public static final String COLUMN_LAT = "lat";
	public static final String COLUMN_GPS_TIME = "gps_time";
	public static final String COLUMN_GPS_ACCURACY = "accuracy";
	public static final String COLUMN_PROJECT = "project";
	public static final String COLUMN_USER = "id_usr";
	public static final String COLUMN_APP_VERSION = "app_version";
	public static final String COLUMN_DEVICE_NAME = "device_name";

	public static void onCreate(SQLiteDatabase database) {
		String DATABASE_CREATE = "CREATE TABLE answer (_id integer primary key autoincrement, id_form text not null, id_store text, content text not null, whoupdate text, whenupdate text, lastsync integer, long text, lat text, gps_time integer, accuracy integer, project text, id_usr text, entertime text, app_version text, device_name text)";
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

        if(oldVersion<=7) {
            upgradeto7(database);
        }

        if(oldVersion<=20) {
        	upgradeto20(database);
		}

        if(oldVersion<=21) {
        	upgradeto21(database);
		}
	}


    public static void upgradeto7(SQLiteDatabase database){
        try {
            database.execSQL("Alter table answer add column entertime text");
            database.execSQL("update answer set entertime = whenupdate");
        } catch (Exception e){
        }
    }

    public static void upgradeto20(SQLiteDatabase database) {
		try {
			database.execSQL("Alter table answer add column app_version text");
			database.execSQL("update answer set app_version = " + BuildConfig.VERSION_NAME);
		} catch (Exception e) {
		}
	}

	public static void upgradeto21(SQLiteDatabase database) {
		try {
			database.execSQL("Alter table answer add column device_name text");
			database.execSQL("update answer set device_name = " + android.os.Build.MODEL);
		} catch (Exception e) {
		}
	}
}
