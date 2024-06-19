package com.virtusee.helper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

@EBean
public class SyncHelper {

	@Pref
    PrefHelper_ myPrefs;


	public void setCredentials(String domain,String userid, String username, String password){
		Date d = new Date();
		String now = Long.toString(d.getTime()/1000);

		myPrefs.domain().put(domain);
    	myPrefs.userid().put(userid);
    	myPrefs.username().put(username);
    	myPrefs.password().put(password);
    	myPrefs.lastLogin().put(now);

	}
	
	public String getLastDownload(String userId){
		if(!myPrefs.syncTime().exists()) return null; 
		
		Gson gson = new Gson();
		String content = myPrefs.syncTime().get(); 
		Type syncMap = new TypeToken<Map<String, String>>(){}.getType();
		Map<String,String> syncData = gson.fromJson(content, syncMap);

		if(syncData==null) return null;
		//if(syncData.get(userId))
		
		return null;
	}
}
