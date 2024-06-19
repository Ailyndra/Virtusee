package com.virtusee.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VSDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "virtusee.db";
    private static final int DATABASE_VERSION = 23;
    private static VSDbHelper db;


    public static synchronized VSDbHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (db== null) {
            db = new VSDbHelper(context.getApplicationContext());
        }
        return db;
    }

    private VSDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        StoreTable.onCreate(database);
        FormTable.onCreate(database);
        AnswerTable.onCreate(database);
        PhotoTable.onCreate(database);
        StoreTagTable.onCreate(database);
        FormTagTable.onCreate(database);
        FormTagOwnTable.onCreate(database);
        InboxTable.onCreate(database);
        MasterTable.onCreate(database);
        AudioTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

        FormTable.onUpgrade(database, oldVersion, newVersion);
        PhotoTable.onUpgrade(database, oldVersion, newVersion);
        AnswerTable.onUpgrade(database, oldVersion, newVersion);
        StoreTable.onUpgrade(database, oldVersion, newVersion);
        StoreTagTable.onUpgrade(database, oldVersion, newVersion);
        FormTagTable.onUpgrade(database, oldVersion, newVersion);
        FormTagOwnTable.onUpgrade(database, oldVersion, newVersion);
        InboxTable.onUpgrade(database, oldVersion, newVersion);
        MasterTable.onUpgrade(database, oldVersion, newVersion);
        AudioTable.onUpgrade(database, oldVersion, newVersion);
    }

}
	 

