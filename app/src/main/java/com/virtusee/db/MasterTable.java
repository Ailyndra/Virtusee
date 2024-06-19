package com.virtusee.db;

import android.database.sqlite.SQLiteDatabase;

public class MasterTable {
    public static final String TABLE = "master";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_PARENT_ID = "parent_id";
    public static final String COLUMN_GOTO = "goto";
    public static final String COLUMN_PROJECT = "project";

    public static void onCreate(SQLiteDatabase database) {
        String DATABASE_CREATE = "CREATE TABLE master (_id text primary key, name text not null, type text not null, parent_id, goto text not null, project text)";
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS master");
        onCreate(database);
    }
}
