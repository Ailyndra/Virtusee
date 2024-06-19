package com.virtusee.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import com.virtusee.core.R;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import androidx.core.app.NotificationCompat;



@EBean
public class NotifHelper {
	@RootContext
	Context context;
	
	public static final boolean PROGRESS_SET = true;
	public static final boolean PROGRESS_UNSET = false;

	public static final boolean CANCEL_SET = true;
	public static final boolean CANCEL_UNSET = false;
	
	private static final int NOTIFID = 101;
	private static final String CHANNEL_ID = "INBOX_CHANNEL";
	private static final String CHANNEL_NAME = "INBOX";
	private static final String CHANNEL_DESCRIPTION = "Inbox Notification";
	private NotificationCompat.Builder mBuilder;
	private NotificationManager notificationManager;

	//New version
	private Notification.Builder builder;

	@AfterInject
	void initNotif() {
		final Intent emptyIntent = new Intent();
		PendingIntent pendingIntent = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			pendingIntent = PendingIntent.getActivity(context, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		} else {
			pendingIntent = PendingIntent.getActivity(context, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

//		mBuilder =
//			    new NotificationCompat.Builder(context)
//				.setSmallIcon(R.drawable.notif)
//			    .setContentTitle("Virtusee")
//			    .setContentIntent(pendingIntent)
//			    .setAutoCancel(false);

		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// Check if the Android version is Oreo (API level 26) or higher
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
			channel.setDescription(CHANNEL_DESCRIPTION);
			channel.enableVibration(true);
			notificationManager.createNotificationChannel(channel);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			builder = new Notification.Builder(context, CHANNEL_ID);
		} else {
			builder = new Notification.Builder(context);
		}

		builder.setContentTitle("Virtusee")
				.setSmallIcon(R.drawable.notif)
				.setContentIntent(pendingIntent)
				.setAutoCancel(false);
	}
	
	public void show(String msg,boolean progress,boolean cancel){
//		mBuilder.setContentText(msg);
//		mBuilder.setProgress(0,0,progress);
//		if(cancel) mBuilder.setAutoCancel(true);

		builder.setContentText(msg);
		if(cancel) builder.setAutoCancel(true);

		notificationManager.notify(NOTIFID, builder.build());
	}

}
