package com.virtusee.model;

public class AuthModel {
    public final String _id;
    public final String loginname;
    public final String fullname;
    public final String company;
    public final String token;

    public AuthModel(String _id, String loginname, String fullname, String company, String token) {
        this._id = _id;
        this.loginname = loginname;
        this.fullname = fullname;
        this.company = company;
        this.token = token;
    }
}
	
