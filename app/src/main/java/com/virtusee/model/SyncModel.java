package com.virtusee.model;

public class SyncModel {

	public final String userId;
	public final String lastdownload;
	public final String lastupload;
	    
	public SyncModel(String userId, String lastdownload, String lastupload) {
        this.userId = userId;
        this.lastdownload = lastdownload;
        this.lastupload = lastupload;
    }

}
	
