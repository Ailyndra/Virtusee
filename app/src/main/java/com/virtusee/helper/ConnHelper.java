package com.virtusee.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class ConnHelper {
	public static final int CONN_UNDEFINED = 0;
	public static final int CONN_WIFI = 1;
	public static final int CONN_LTE = 2;
	public static final int CONN_HSDPA = 3;
	public static final int CONN_GPRS = 4;


    public static boolean hasConnection(Context context){
		ConnectivityManager cm =
			        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return (netInfo != null && netInfo.isConnected()) ? true:false;
	}
	
	public static int getType(Context context){
		ConnectivityManager cm =
		        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo.getType();
	}


}
