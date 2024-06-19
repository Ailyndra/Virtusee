package com.virtusee.helper;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.virtusee.contentprovider.AnswerContentProvider;
import com.virtusee.contentprovider.FormContentProvider;
import com.virtusee.contentprovider.StoreContentProvider;
import com.virtusee.core.BuildConfig;
import com.virtusee.db.AnswerTable;
import com.virtusee.db.StoreTable;
import com.virtusee.model.StoreAbsenModel;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

/**
 * Created by winata on 5/26/17.
 */

@EBean
public class AbsenHelper {
    private Activity context;

    @Bean
    AuthHelper authHelper;

    @Bean
    DateHelper dateHelper;

    final String idForm = "ABSENSI";

    public void init(Activity ctx){
        context = ctx;
        dateHelper.init(ctx);
    }

    public void SaveAbsen(int status, Location location){
        SaveAbsen(status,"wday",location);
    }


    public int GetStatusWday(){
        return GetStatus("wday");
    }

    public StoreAbsenModel GetStatus(){
        String[] projection = {AnswerTable.COLUMN_ID,AnswerTable.COLUMN_ID_STORE,AnswerTable.COLUMN_CONTENT,AnswerTable.COLUMN_WHEN};
        String orderby = AnswerTable.COLUMN_ID + " desc";
        Uri dtUri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/form/" + idForm + "/today");
        Cursor cursor = context.getContentResolver().query(dtUri, projection , null, null, orderby);

        int absenstatus = 0;
        String idStore = "";
        String whenupdate = "";

        try {
            if( cursor != null && cursor.moveToFirst() ) {
                absenstatus = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_CONTENT)));
                idStore = cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID_STORE));
                whenupdate = cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_WHEN));
                Log.e("cin",idStore);
                Log.e("cinx","hello");

                Log.e("cin2",whenupdate);

                Log.e("cin3",String.valueOf(absenstatus));
            } else {
                absenstatus = 0;
                idStore = "";
                Log.e("cin",idStore);
                Log.e("cinx","hello3");

                Log.e("cin2",whenupdate);

                Log.e("cin3",String.valueOf(absenstatus));
            }
        } catch (Exception e) {
            absenstatus = 0;
            idStore = "";
            Log.e("cin",idStore);
            Log.e("cinx","hello2");

            Log.e("cin2",whenupdate);

            Log.e("cin3",String.valueOf(absenstatus));
        } finally {
            if( cursor != null) cursor.close();
        }




        return new StoreAbsenModel(idStore,absenstatus,whenupdate);
    }

    public int GetStatus(String idStore){
        String[] projection = {AnswerTable.COLUMN_ID, AnswerTable.COLUMN_CONTENT};
        String orderby = AnswerTable.COLUMN_ID + " desc";
        Uri dtUri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/store/" + idStore + "/form/" + idForm + "/today");
        Cursor cursor = context.getContentResolver().query(dtUri, projection , null, null, orderby);

        int absenstatus = 0;
        try {
            if( cursor != null && cursor.moveToFirst() ) {
                absenstatus = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_CONTENT)));
            } else {
                absenstatus = 0;
            }
        } catch (Exception e) {
            absenstatus = 0;
        }
        cursor.close();

        return absenstatus;
    }

    public void SaveAbsen(int status,String idStore, Location location) {
        String whenupdate = dateHelper.getCurrentTimestamp();
        String longitude, latitude, gpsTime, gpsAccuracy;
        String project = authHelper.getDomain();
        String idUsr = authHelper.getUserid();

        if (location != null) {
            longitude = Double.toString(location.getLongitude());
            latitude = Double.toString(location.getLatitude());
            gpsTime = Long.toString(location.getTime());
            gpsAccuracy = Float.toString(location.getAccuracy());
        } else {
            longitude = latitude = gpsTime = gpsAccuracy = "";
        }


        ContentValues values = new ContentValues();
        values.put(AnswerTable.COLUMN_ID_FORM, idForm);
        values.put(AnswerTable.COLUMN_ID_STORE, idStore);
        values.put(AnswerTable.COLUMN_CONTENT, String.valueOf(status));
        values.put(AnswerTable.COLUMN_WHEN, whenupdate);
        values.put(AnswerTable.COLUMN_LAST_SYNC, 0);
        values.put(AnswerTable.COLUMN_LONG, longitude);
        values.put(AnswerTable.COLUMN_LAT, latitude);
        values.put(AnswerTable.COLUMN_GPS_TIME, gpsTime);
        values.put(AnswerTable.COLUMN_GPS_ACCURACY, gpsAccuracy);
        values.put(AnswerTable.COLUMN_PROJECT, project);
        values.put(AnswerTable.COLUMN_USER, idUsr);
        values.put(AnswerTable.COLUMN_ENTER, whenupdate);
        values.put(AnswerTable.COLUMN_APP_VERSION, BuildConfig.VERSION_NAME);

        context.getContentResolver().insert(AnswerContentProvider.CONTENT_URI, values);


//        if(!idStore.equals("wday") && status==1) CheckMandatory(idStore);
    }

    public void CheckMandatory(String idStore){
        Uri dtUri = Uri.parse(FormContentProvider.CONTENT_URI + "/check_mandatory/" + idStore);
        Cursor cursor = context.getContentResolver().query(dtUri, null, null, null, null);
        int tot = 0;
        try {
            if( cursor != null && cursor.moveToFirst() ) {
                tot = cursor.getInt(0);
            }
        } catch (Exception e) {
        } finally {
            if( cursor != null) cursor.close();
        }

        int locked = (tot>0) ? 1 : 0;
        String whenupdate = dateHelper.getCurrentTimestamp();

        ContentValues values = new ContentValues();
        values.put(StoreTable.COLUMN_LOCKED, locked);
        values.put(StoreTable.COLUMN_LOCKED_WHEN, whenupdate);

        context.getContentResolver().update(Uri.parse(StoreContentProvider.CONTENT_URI + "/" + idStore), values, null, null);
    }

}
