package com.virtusee.helper;

import android.content.Context;
import android.provider.Settings;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Date;
import java.util.UUID;

@EBean
public class AuthHelper {
    private static final String DELIM = "//";
    private static final String APPIDDELIM = "####";

	@Pref
    PrefHelper_ myPrefs;

    public String getLastSyncFirebase(){
        if(myPrefs.lastSyncFirebase().exists()) return myPrefs.lastSyncFirebase().get();
        return "";
    }

    public void clearLastSyncFirebase(){
        myPrefs.lastSyncFirebase().remove();
    }

    public String getDomain(){
		if(myPrefs.domain().exists()) return myPrefs.domain().get(); 
		return "";
	}

	public String getUserid(){
		if(myPrefs.userid().exists()) return myPrefs.userid().get(); 
		return "";
	}

	public String getUsername(){
		if(myPrefs.username().exists()) return myPrefs.username().get(); 
		return "";
	}
	
	public String getPassword(){
		if(myPrefs.password().exists()) return myPrefs.password().get(); 
		return "";
	}

    public String getFullname(){
        if(myPrefs.fullname().exists()) return myPrefs.fullname().get();
        return "";
    }

    public boolean getResend(){
        if(myPrefs.isresend().exists()) return myPrefs.isresend().get();
        else return false;
    }


    public void setResend(){
        myPrefs.isresend().put(true);
    }

    public void setResendImage(long w) {
        myPrefs.resendImage().put(w);
    }

    public long getResendImage(){
        if(myPrefs.resendImage().exists()) return myPrefs.resendImage().get();
        else return 0;
    }


    public String getCompany(){
        if(myPrefs.company().exists()) return myPrefs.company().get();
        return "";
    }

	public String getFormattedUsername(){
		return getAppId() + APPIDDELIM + getDomain()+DELIM+getUsername();
	}
	
	public void setCredentials(String domain,String userid, String username, String password, String fullname, String company){
		Date d = new Date();
		String now = Long.toString(d.getTime()/1000);

		myPrefs.domain().put(domain);
    	myPrefs.userid().put(userid);
    	myPrefs.username().put(username);
    	myPrefs.password().put(password);
    	myPrefs.lastLogin().put(now);
        myPrefs.fullname().put(fullname);
        myPrefs.company().put(company);
	}
	
	public void clearCredentials(){
/*
		myPrefs.domain().remove();
    	myPrefs.userid().remove();
        myPrefs.username().remove();
        myPrefs.fullname().remove();
        myPrefs.company().remove();
    	myPrefs.password().remove();
        myPrefs.lastLogin().remove();
        myPrefs.lastDownload().remove();
        myPrefs.lastUpload().remove();
        myPrefs.syncTime().remove();
        myPrefs.syncTime().remove();
        */

        myPrefs.clear();

	}
	
	public boolean isLogged(){
		String lastLogin = null;
		Date d = new Date();
		long now = d.getTime()/1000;
		
		if(!myPrefs.userid().exists()) return false;
		
		if(myPrefs.lastLogin().exists()) lastLogin = myPrefs.lastLogin().get();
		
		//return (lastLogin != null && Long.parseLong(lastLogin)>(now-(60*60*24))) ? true:false;
		return (lastLogin != null) ? true:false;
	}


    public String genAppId(Context context){
        String androidid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if(androidid==null) androidid = "";
        String appid =  androidid + "-" + UUID.randomUUID().toString();
        myPrefs.appId().put(appid);
        return appid;
    }

    public String getAppId(){
        if(myPrefs.appId().exists()) return myPrefs.appId().get();
        return "";
    }

    public void setLastSyncFirebase(String lastSync){
        myPrefs.lastSyncFirebase().put(lastSync);

    }

}
