package com.virtusee.db;

import android.database.sqlite.SQLiteDatabase;

public class StoreTable {
	public static final String TABLE = "store";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_STORE_ID = "store_id";
	public static final String COLUMN_STORE_CODE = "store_code";
	public static final String COLUMN_STORE_NAME = "store_name";
    public static final String COLUMN_PROJECT = "project";
	public static final String COLUMN_ISREAD = "isread";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_WEEK = "week";
    public static final String COLUMN_ATTR = "attr";
    public static final String COLUMN_LONG = "longitude";
    public static final String COLUMN_LAT = "latitude";
    public static final String COLUMN_ORDER = "urut";
    public static final String COLUMN_LOCKED = "locked";
    public static final String COLUMN_LOCKED_WHEN = "locked_when";
    public static final String COLUMN_SHORT = "short";

    public static void onCreate(SQLiteDatabase database) {
        String DATABASE_CREATE = "CREATE TABLE store (_id text primary key, store_id text not null, store_code text, store_name text, project text, isread integer not null default 0, day text not null default '1111111',longitude text, latitude text, week text not null default '11', attr text,urut integer not null default 1 ,locked integer not null default 0, locked_when text, short text)";
        database.execSQL(DATABASE_CREATE);

        database.execSQL("CREATE INDEX IF NOT EXISTS slck ON store (locked,locked_when)");



    }

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if(oldVersion<=7) {
            upgradeto7(database);
        }

        if(oldVersion<=11) {
            upgradeto11(database);
        }

        if(oldVersion<=12) {
            upgradeto12(database);
        }

        if(oldVersion<=13) {
            upgradeto13(database);
        }

        if(oldVersion<=17) {
            upgradeto17(database);
        }

        if(oldVersion<=23) {
            upgradeto23(database);
        }
	}

    public static void upgradeto23(SQLiteDatabase database) {
        try {
            database.execSQL("alter table store add column short text");
        } catch (Exception e) {}
    }

    public static void upgradeto17(SQLiteDatabase database){
        try {
            database.execSQL("Alter table store add column locked integer not null default 0");
            database.execSQL("Alter table store add column locked_when text");
            database.execSQL("update store set locked=0");
            database.execSQL("CREATE INDEX IF NOT EXISTS slck ON store (locked,locked_when)");
        } catch (Exception e){
        }

    }

    public static void upgradeto7(SQLiteDatabase database){
        try {
            database.execSQL("Alter table store add column day text");
            database.execSQL("Alter table store add column week text");

            database.execSQL("update store set day='1111111',week='11' where day is null or day = ''");
        } catch (Exception e){
        }

    }

    public static void upgradeto11(SQLiteDatabase database){
        try {
            database.execSQL("Alter table store add column attr text");
        } catch (Exception e){
        }

    }

    public static void upgradeto12(SQLiteDatabase database){
        try {
            database.execSQL("Alter table store add column longitude text");
            database.execSQL("Alter table store add column latitude text");
        } catch (Exception e){
        }
    }

    public static void upgradeto13(SQLiteDatabase database){
        try {
            database.execSQL("Alter table store add column urut integer");
            database.execSQL("update store set urut=1");
        } catch (Exception e){
        }

    }

}
 