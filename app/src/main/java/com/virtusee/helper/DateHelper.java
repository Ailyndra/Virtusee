package com.virtusee.helper;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.SystemClock;

import org.androidannotations.annotations.EBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.AlertDialog;


@EBean
public class DateHelper
{
    private Activity context;

    public int init(Activity ctx){
        context = ctx;
        int isauto = checkAutoDateSetting();
//        if(isauto==0) dismiss();
        return isauto;
    }

    public int checkAutoDateSetting(){
        int isauto;
        if(Build.VERSION.SDK_INT>=17) {
            isauto = android.provider.Settings.Global.getInt(context.getContentResolver(), android.provider.Settings.Global.AUTO_TIME, 0);
        } else {
            isauto = android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0);
        }
        return isauto;

    }

    public String getCurrentTimestamp(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public long chronoBase(String dateString){
        long base;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(dateString);
            long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            long longdate = date.getTime();

            base = longdate - elapsedRealtimeOffset;

        } catch (ParseException e) {
            base = SystemClock.elapsedRealtime();
        }
        return base;
    }

    public String getCurrentDate(){
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public int getCurrentWeek(){
        return 0;
    }


    private void dismiss(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Please use automatic date & time for your date setting!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                        context.finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
