package com.virtusee.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.virtusee.core.R;
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
public class WdayExtendReceiver extends BroadcastReceiver {
    @Bean
    GpsHelper gpsHelper;

    @Bean
    AuthHelper authHelper;

    @SystemService
    NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingIntent noIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notiflogo)
                        .setAutoCancel(true)
                        .setColor(ContextCompat.getColor(context,R.color.vsblue))
                        .setContentIntent(noIntent)
                        .setContentTitle("Virtusee")
                        .setContentText("Workday Extended!");
        notificationManager.notify(WdayReceiver.NOTIFICATION_ID, mBuilder.build());
    }
}