package com.virtusee.view;

import android.content.Context;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.virtusee.core.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.form_audio)
public class FormAudioView extends FormGroupView {
    @ViewById
    protected ImageView formMic;

    @ViewById
    protected ImageView formPlay;

    private Context ctx;

    public FormAudioView(Context context) {
        super(context);
        ctx = context;
        audio = true;
    }

    @Override
    public void setQuestionId(String idForm, String idQuestion,int type, int group){
        this.idForm = idForm;
        this.idQuestion = idQuestion;
        this.type = type;
        this.group = group;
        formMic.setTag(R.id.TAG_QUESTION_ID, idQuestion);
    }

    @Override
    public String getVal(){
        if(formMic.getTag(R.id.TAG_PATH)!=null)  return formMic.getTag(R.id.TAG_PATH).toString();
        else return "";
    }

    public void setId(int id){
        formMic.setId(id);
    }

    public void onMicClick(OnClickListener formMic_OnClickListener){
        formMic.setOnClickListener(formMic_OnClickListener);
    }

    public void onPlayClick(OnClickListener formPlay_OnClickListener){
        formPlay.setOnClickListener(formPlay_OnClickListener);
    }

    public void setVisible(boolean vis){
        if(vis){
            formPlay.setVisibility(VISIBLE);
        } else {
            formPlay.setVisibility(INVISIBLE);
        }
    }
}