package com.virtusee.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.virtusee.core.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by winata on 5/25/17.
 */

public class WdayReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("notif","jalan");
        String whenupdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent yesIntent = new Intent(context, WdayCoutReceiver_.class);
        yesIntent.putExtra("EXTRA_DETAILS_ID", 42);
        yesIntent.putExtra("EXTRA_WHENTRIGGERED", whenupdate);

        PendingIntent coutPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                yesIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent noIntent = new Intent(context, WdayExtendReceiver_.class);
        noIntent.putExtra("EXTRA_DETAILS_ID", 0);
        PendingIntent extendPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                noIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notiflogo)
                        .setColor(ContextCompat.getColor(context,R.color.vsblue))
                        .setContentTitle("10 Hrs work has been achieved!")
                        .setContentText("End your workday now?")
                        .addAction(android.R.drawable.ic_menu_compass, "Yes", coutPendingIntent)
                        .addAction(android.R.drawable.ic_menu_directions, "No", extendPendingIntent);


        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}