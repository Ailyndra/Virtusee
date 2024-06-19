package com.virtusee.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.virtusee.contentprovider.AnswerContentProvider;
import com.virtusee.core.BuildConfig;
import com.virtusee.core.R;
import com.virtusee.db.AnswerTable;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.GpsHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.SystemService;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by winata on 5/25/17.
 */

@EReceiver
public class WdayCoutReceiver extends BroadcastReceiver {

    @Bean
    GpsHelper gpsHelper;

    @Bean
    AuthHelper authHelper;

    @SystemService
    NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        String extraWhen = intent.getExtras().getString("EXTRA_WHENTRIGGERED");

        String longitude, latitude, gpsTime, gpsAccuracy;
        String project = authHelper.getDomain();
        String idUsr = authHelper.getUserid();
        longitude = latitude = gpsTime = gpsAccuracy = "";


        ContentValues values = new ContentValues();
        values.put(AnswerTable.COLUMN_ID_FORM, "ABSENSI");
        values.put(AnswerTable.COLUMN_ID_STORE, "wday");
        values.put(AnswerTable.COLUMN_CONTENT, "2");
        values.put(AnswerTable.COLUMN_WHEN, extraWhen);
        values.put(AnswerTable.COLUMN_LAST_SYNC, 0);
        values.put(AnswerTable.COLUMN_LONG, longitude);
        values.put(AnswerTable.COLUMN_LAT, latitude);
        values.put(AnswerTable.COLUMN_GPS_TIME, gpsTime);
        values.put(AnswerTable.COLUMN_GPS_ACCURACY, gpsAccuracy);
        values.put(AnswerTable.COLUMN_PROJECT, project);
        values.put(AnswerTable.COLUMN_USER, idUsr);
        values.put(AnswerTable.COLUMN_ENTER, extraWhen);
        values.put(AnswerTable.COLUMN_APP_VERSION, BuildConfig.VERSION_NAME);

        context.getContentResolver().insert(AnswerContentProvider.CONTENT_URI, values);

        PendingIntent noIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notiflogo)
                        .setAutoCancel(true)
                        .setColor(ContextCompat.getColor(context,R.color.vsblue))
                        .setContentIntent(noIntent)
                        .setContentTitle("Virtusee")
                        .setContentText("Workday Ended!");

        notificationManager.notify(WdayReceiver.NOTIFICATION_ID, mBuilder.build());

        Intent i = new Intent("com.virtusee.receiver.WDAY").putExtra("actionid", 42);
        context.sendBroadcast(i);

    }
}