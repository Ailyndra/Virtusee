package com.virtusee.db;

import android.database.sqlite.SQLiteDatabase;

public class FormTable {
	public static final String TABLE = "form";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_FORM_ID = "form_id";
	public static final String COLUMN_FORM_TITLE = "title";
	public static final String COLUMN_FORM_DESC = "desc";
	public static final String COLUMN_FORM_CONTENT = "content";
	public static final String COLUMN_PROJECT = "project";
	public static final String COLUMN_FORM_ORDER = "urut";
    public static final String COLUMN_FORM_STICKY = "sticky";
    public static final String COLUMN_FORM_MANDATORY = "mandatory";

	public static void onCreate(SQLiteDatabase database) {
		String DATABASE_CREATE = "CREATE TABLE form (_id text primary key, form_id text not null, title text not null, desc text, content text, project text, urut integer not null default 1, sticky integer not null default 0, mandatory integer not null default 0)";
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if(oldVersion==1) {
            upgradeto2(database);
        }

        if(oldVersion<=17){
            upgradeto18(database);
        }
	}

    public static void upgradeto2(SQLiteDatabase database){
        try {
            database.execSQL("DROP TABLE IF EXISTS form");
            onCreate(database);
        } catch (Exception e){
        }

    }

    public static void upgradeto18(SQLiteDatabase database){
        try {
            database.execSQL("Alter table form add column mandatory integer not null default 0");
            database.execSQL("update form set mandatory=0");
        } catch (Exception e){
        }

    }
}
