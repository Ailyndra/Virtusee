package com.virtusee.view;

import android.content.Context;
import android.widget.LinearLayout;

public class FormGroupView extends LinearLayout {
	protected boolean inputable = true;
	protected boolean image = false;
	protected boolean audio = false;
	protected String idForm;
    protected String idQuestion;
    protected int type;
    protected int group;
    protected boolean isRequired = false;
    protected boolean isVisible = true;
    protected String reqMsg = "This field is required!";
    protected Context context;

	public FormGroupView(Context context) {
		super(context);
		this.context = context;
	}

	public boolean isInputable(){
		return this.inputable;
	}

	public boolean isImage(){
		return this.image;
	}

	public boolean isAudio() {
		return this.audio;
	}
	
	public void setQuestionId(String idForm, String idQuestion, int type, int group){
		this.idForm = idForm;
		this.idQuestion = idQuestion;
        this.type = type;
        this.group = group;
	}
	
	public String getIdForm(){
		return this.idForm;
	}
	
	public String getIdQuestion(){
		return this.idQuestion;
	}
	
	public String getVal(){
		return null;
	}
	
	public int getType(){
		return this.type;
	}

    public void setRequired(boolean req){
        this.isRequired = req;
    }
    public void setError(){
        return;
    }

    public boolean validate(){
        if(!this.isRequired) return true;
        boolean v = (this.isVisible && getVal().isEmpty()) ? false : true;
        if(!v) this.setError();
        return v;
    }

    public void setInVisible() {
        this.isVisible = false;
    }

    public void setVisible() {
        this.isVisible = true;
    }


}
