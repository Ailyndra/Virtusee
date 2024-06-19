package com.virtusee.db;

import android.database.sqlite.SQLiteDatabase;

public class StoreTagTable {
	public static final String TABLE = "store_tag";
	public static final String COLUMN_STORE_ID = "id_store";
	public static final String COLUMN_STORE_TAG = "tag";

	public static void onCreate(SQLiteDatabase database) {
		String DATABASE_CREATE = "CREATE TABLE store_tag (id_store text, tag text, PRIMARY KEY (id_store,tag ))";
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if(oldVersion<=13) {
            onCreate(database);
        }

	}
}
 