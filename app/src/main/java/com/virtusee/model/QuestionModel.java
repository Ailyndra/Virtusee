package com.virtusee.model;

public class QuestionModel {
    public String id;
    public String question;
    public final int type;
    public final String selval;
    public final boolean required;
    public final int group;
    public final String master_type;
    public final String prev;
    public final String photo_type;

    public QuestionModel(String id, String question, int type, String selval, boolean required,int group, String master_type, String prev, String photo_type) {
        this.id = id;
        this.question = question;
        this.type = type;
        this.selval = selval;
        this.required = required;
        this.group = group;
        this.master_type = master_type;
        this.prev = prev;
        this.photo_type = photo_type;
    }

    @Override
    public String toString() {
        return "QuestionModel{" +
                "id='" + id + '\'' +
                ", question='" + question + '\'' +
                ", type=" + type +
                ", selval='" + selval + '\'' +
                ", required=" + required +
                ", group=" + group +
                ", master_type='" + master_type + '\'' +
                ", prev='" + prev + '\'' +
                ", photo_type='" + photo_type + '\'' +
                '}';
    }
}
	
