package com.virtusee.db;

import android.database.sqlite.SQLiteDatabase;

public class InboxTable {
	public static final String TABLE = "inbox";
	public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_DOWNLOAD = "last_download";
    public static final String COLUMN_WHEN = "whenupdate";
    public static final String COLUMN_READ = "isread";

	public static void onCreate(SQLiteDatabase database) {
		String DATABASE_CREATE = "CREATE TABLE inbox (_id integer primary key autoincrement, content text, url text, whenupdate text not null default (datetime('now','localtime')), isread integer not null default 0, last_download integer not null default 0 )";
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS inbox");
        onCreate(database);
	}

}
