package com.virtusee.model;

public class AnswerModel {

        public final String id;
	    public final String idForm;
	    public final String idStore;
	    public final String idUsr;
	    public final String answerContent;
	    public final String whenupdate;
	    public final String longitude;
	    public final String latitude;
	    public final String gpsTime;
        public final String gpsAccuracy;
        public final String entertime;
        public final String lastsync;
        public final String appVersion;
        public final String deviceName;

    	public AnswerModel(String id,String idForm, String idStore, String idUsr, String answerContent, String whenupdate,String longitude,String latitude,String gpsTime,String gpsAccuracy, String entertime, String lastsync, String appVersion, String deviceName) {
            this.id = id;
            this.idForm = idForm;
    		this.idStore = idStore;
    		this.idUsr = idUsr;
    		this.answerContent = answerContent;
    		this.whenupdate = whenupdate;
    		this.longitude = longitude;
    		this.latitude = latitude;
    		this.gpsTime = gpsTime;
    		this.gpsAccuracy = gpsAccuracy;
            this.entertime = entertime;
            this.lastsync = lastsync;
            this.appVersion = appVersion;
            this.deviceName = deviceName;
    	}
}
	
