package com.virtusee.db;

import android.database.sqlite.SQLiteDatabase;

public class FormTagOwnTable {
	public static final String TABLE = "form_tag_own";
	public static final String COLUMN_STORE_ID = "id_form";
	public static final String COLUMN_STORE_TAG = "tag";

	public static void onCreate(SQLiteDatabase database) {
		String DATABASE_CREATE = "CREATE TABLE form_tag_own (id_form text, tag text, PRIMARY KEY (id_form,tag ))";
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if(oldVersion<=15) {
            database.execSQL("DROP TABLE IF EXISTS form_tag_own");
            onCreate(database);
        }

	}
}
 