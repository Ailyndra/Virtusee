package com.virtusee.db;

import android.database.sqlite.SQLiteDatabase;

public class AudioTable {
    public static final String TABLE = "audio";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ID_ANSWER = "id_answer";
    public static final String COLUMN_ID_QUESTION = "id_question";
    public static final String COLUMN_AUDIO = "audio";
    public static final String COLUMN_CSUM = "csum";
    public static final String COLUMN_WHEN = "whenupdate";
    public static final String COLUMN_WHO = "whoupdate";
    public static final String COLUMN_LAST_SYNC = "lastsync";

    public static void onCreate(SQLiteDatabase database) {
        String DATABASE_CREATE = "CREATE TABLE audio (_id integer primary key autoincrement, id_answer integer not null, id_question text, audio text not null,csum text not null, whoupdate text, whenupdate text, lastsync integer)";
        database.execSQL(DATABASE_CREATE);
    }



    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {

        if(oldVersion<=19) {
            upgradeto19(database);
        }

    }

    public static void upgradeto19(SQLiteDatabase database){
        try {
            database.execSQL("DROP TABLE IF EXISTS audio");
            onCreate(database);
        } catch (Exception e){
        }

    }
}
