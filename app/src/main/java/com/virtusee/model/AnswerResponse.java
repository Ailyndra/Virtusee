package com.virtusee.model;

public class AnswerResponse {

    public final boolean err;
    public final String errmsg;
    public final String[] idAnswer;

    public AnswerResponse(boolean err, String errmsg, String[] idAnswer) {
        this.err= err;
        this.errmsg = errmsg;
        this.idAnswer = idAnswer;
    }
}
	
