package com.virtusee.db;

import android.database.sqlite.SQLiteDatabase;

public class PhotoTable {
	public static final String TABLE = "photo";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ID_ANSWER = "id_answer";
	public static final String COLUMN_ID_QUESTION = "id_question";
    public static final String COLUMN_IMG = "img";
    public static final String COLUMN_CSUM = "csum";
    public static final String COLUMN_WHEN = "whenupdate";
	public static final String COLUMN_WHO = "whoupdate";
	public static final String COLUMN_LAST_SYNC = "lastsync";

	public static void onCreate(SQLiteDatabase database) {
		String DATABASE_CREATE = "CREATE TABLE photo (_id integer primary key autoincrement, id_answer integer not null, id_question text, img text not null,csum text not null, whoupdate text, whenupdate text, lastsync integer)";
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
		int newVersion) {

        if(oldVersion<=9) {
            upgradeto9(database);
        }

	}

    public static void upgradeto9(SQLiteDatabase database){
        try {
            database.execSQL("DROP TABLE IF EXISTS photo");
            onCreate(database);
        } catch (Exception e){
        }

    }
}
