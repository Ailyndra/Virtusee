package com.virtusee.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class TokenHelper {
    private static final String SP_NAME = "firebase_pref";
    private static final String TOKEN_KEY = "_firebase_token";
    private static final String TOKEN_LASTUPDATE_KEY = "_firebase_lastupdate";

    public static void PutToken(Context context,String token){
        SharedPreferences sh = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor shEdit = sh.edit();
        shEdit.putString(TOKEN_KEY,token);
        shEdit.commit();
    }

    public static String GetToken(Context context) {
        SharedPreferences sh = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sh.getString(TOKEN_KEY,"");
    }

    public static void PutLastTokenUpdate(Context context){
        SharedPreferences sh = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        Date d = new Date();
        long now = d.getTime() / 1000;
        SharedPreferences.Editor shEdit = sh.edit();
        shEdit.putLong(TOKEN_LASTUPDATE_KEY,now);
        shEdit.commit();
    }

    public static long GetLastTokenUpdate(Context context) {
        SharedPreferences sh = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sh.getLong(TOKEN_LASTUPDATE_KEY,0);
    }
}
