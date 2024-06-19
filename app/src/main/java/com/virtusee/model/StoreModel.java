package com.virtusee.model;

public class StoreModel {
    public final String _id;
    public final String code;
    public final String name;
    public final int total;
    public final String day;
    public final String week;
    public final String longitude;
    public final String latitude;
    public final String attr;
    public final String[] tag;
    public final String place_code;

    public StoreModel(String _id, String code, String name, int total,String day, String week, String longitude, String latitude, String attr, String[] tag, String place_code) {
        this._id = _id;
        this.code = code;
        this.name = name;
        this.total = total;
        this.day = day;
        this.week = week;
        this.longitude = longitude;
        this.latitude = latitude;
        this.attr = attr;
        this.tag = tag;
        this.place_code = place_code;
    }
}
	