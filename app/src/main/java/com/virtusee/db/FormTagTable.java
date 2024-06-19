package com.virtusee.db;

import android.database.sqlite.SQLiteDatabase;

public class FormTagTable {
	public static final String TABLE = "form_tag";
	public static final String COLUMN_STORE_ID = "id_form";
	public static final String COLUMN_STORE_TAG = "tag";

	public static void onCreate(SQLiteDatabase database) {
		String DATABASE_CREATE = "CREATE TABLE form_tag (id_form text, tag text, PRIMARY KEY (id_form,tag ))";
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if(oldVersion<=13) {
            onCreate(database);
        }

	}
}
 