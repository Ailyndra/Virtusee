package com.virtusee.helper;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface PrefHelper {
	 //String imgUri();
	String lastDownload();
	String lastUpload();
	String userid();
	String username();
	String password();
	String domain();
	String lastLogin();
	String syncTime();
	String appId();
	String fullname();
	String company();
	String lastSyncFirebase();
	String proximity();
	String lastLocationForm();
	String printerAlamat();
	String printerContact();
	String printerLogo();

	@DefaultInt(0)
	int radius();

	@DefaultInt(5)
	int reverse_proximity();

	@DefaultLong(0)
	long resendImage();

    @DefaultBoolean(false)
    boolean isresend();

	@DefaultInt(10)
	int ver();

	@DefaultInt(15)
	int total_checkout();
}
