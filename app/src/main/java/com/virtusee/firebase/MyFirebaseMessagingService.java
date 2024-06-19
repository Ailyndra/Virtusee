package com.virtusee.firebase;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.virtusee.core.Drawer_;
import com.virtusee.core.R;
import com.virtusee.helper.InboxHelper;
import com.virtusee.helper.TokenHelper;
import com.virtusee.services.SyncServ;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by winata on 10/2/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final int NOTIFICATION_ID = 11;
    private static final String CHANNEL_ID = "inbox_1";
    private static final CharSequence CHANNEL_NAME = "Inbox notification";

    @Override
    public void onNewToken(String token) {
        TokenHelper.PutToken(getApplicationContext(),token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("Notif", "remoteMessage: " + remoteMessage.getMessageId());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            InboxHelper.SaveToDb(getApplicationContext(),remoteMessage.getData().get("content"),remoteMessage.getData().get("att"));
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            sendNotif(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
        }
    }

    private void sendNotif(String title, String body){
        Context ctx = getApplicationContext();

        Intent targetIntent = new Intent(this, Drawer_.class);
        targetIntent.putExtra("fcm",true);
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.ic_notiflogo)
                        .setColor(ContextCompat.getColor(ctx,R.color.vsblue))
                        .setContentTitle(title)
                        .setContentText(body);

        mBuilder.setContentIntent(null);
        mBuilder.setAutoCancel(true);

        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        nManager.notify(NOTIFICATION_ID, mBuilder.build());

        Intent i = new Intent("com.virtusee.receiver.INBOX").putExtra("actionid", 10);
        ctx.sendBroadcast(i);

        /*
        Untuk android Oreo ke atas perlu menambahkan notification channel
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create or update. */
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

            mBuilder.setChannelId(CHANNEL_ID);

            if (nManager != null) {
                nManager.createNotificationChannel(channel);
            }
        }

        Notification notification = mBuilder.build();
        if (nManager != null) {
            nManager.notify(NOTIFICATION_ID, notification);
        }
    }
}

