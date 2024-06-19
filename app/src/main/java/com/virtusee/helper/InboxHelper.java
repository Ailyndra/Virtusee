package com.virtusee.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.google.gson.Gson;
import com.virtusee.db.VSDbHelper;
import com.virtusee.model.InboxModel;

/**
 * Created by winata on 10/6/17.
 */

public class InboxHelper {

    public static void SaveToDb(Context ctx, String content, String att){
        SQLiteDatabase db = VSDbHelper.getInstance(ctx).getWritableDatabase();
        Gson gson = new Gson();
        InboxModel[] inboxModel = null;

        Log.e("inbox",att);
        if(att!=null && !att.equals("")){
            inboxModel = gson.fromJson(att, InboxModel[].class);
        }


        String sql = "insert into inbox(content,url) values(?,?)";
        SQLiteStatement statement = db.compileStatement(sql);

        db.beginTransaction();

        if(inboxModel!=null){
            for (InboxModel im : inboxModel) {
                statement.clearBindings();
                statement.bindString(1, im.content);
                statement.bindString(2, im.url);
                statement.execute();
            }
        }

        if(!content.equals("")) {
            statement.clearBindings();
            statement.bindString(1, content);
            statement.bindString(2, "");
            statement.execute();
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
    }

}
