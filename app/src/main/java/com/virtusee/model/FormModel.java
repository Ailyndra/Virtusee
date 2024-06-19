package com.virtusee.model;

public class FormModel {
    public final String _id;
    public final String title;
    public final String desc;
    public final String content;
    public final int urut;
    public final int sticky;
    public final String[] tag;
    public final String[] form_tag;
    public final int mandatory;

    public FormModel(String _id, String title, String desc, String content, int urut, int sticky, String[] tag, String[] form_tag, int mandatory) {
        this._id = _id;
        this.title = title;
        this.desc = desc;
        this.content = content;
        this.urut = urut;
        this.sticky = sticky;
        this.tag = tag;
        this.form_tag = form_tag;
        this.mandatory = mandatory;
    }
}
	
