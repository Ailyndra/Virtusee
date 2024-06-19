package com.virtusee.model;

public class AnswerContentModel {

     public final String idQuestion;
     public final String answer;
     public final int type;
     public final String idMaster;
     public final String typeMaster;
//    public final boolean required;

  //  public AnswerContentModel(String idQuestion, String answer, int type, boolean required) {
  public AnswerContentModel(String idQuestion, String answer, int type, String idMaster, String typeMaster) {
        this.idQuestion = idQuestion;
        this.answer = answer;
        this.type = type;
        this.idMaster = idMaster;
        this.typeMaster = typeMaster;

        //this.required = (!required) ? false : required;
    }

}
	
