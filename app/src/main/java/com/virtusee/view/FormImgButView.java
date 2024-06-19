package com.virtusee.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.virtusee.core.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import androidx.core.content.ContextCompat;


@EViewGroup(R.layout.form_imgbut)
public class FormImgButView extends FormGroupView {
    @ViewById
    protected ImageView formImgItem;

    private Context ctx;
    
	public FormImgButView(Context context) {
		super(context);
		ctx = context;
		image = true;
	}

	public void onClick(OnClickListener formImgItem_OnClickListener){
		formImgItem.setOnClickListener(formImgItem_OnClickListener);
	}
	
	public void setBackground(Bitmap background){
		formImgItem.setImageBitmap(background);
	}
	
	public void setVisible(boolean vis){
		if(vis){
			formImgItem.setVisibility(VISIBLE);
		} else {
			formImgItem.setVisibility(INVISIBLE);			
		}
	}
	
	@Override
	public void setQuestionId(String idForm, String idQuestion,int type, int group){
		this.idForm = idForm;
		this.idQuestion = idQuestion;
		this.type = type;
		this.group = group;
		formImgItem.setTag(R.id.TAG_QUESTION_ID, idQuestion);
	}
	
	
	public ImageView getImage(){
		return formImgItem;
	}
	
	@Override
	public String getVal(){
		if(formImgItem.getTag(R.id.TAG_PATH)!=null)  return formImgItem.getTag(R.id.TAG_PATH).toString();
		else return "";
	}

	public void setId(int id){
		formImgItem.setId(id);
	}

    @Override
    public void setVisible(){
        super.setVisible();
        formImgItem.setVisibility(VISIBLE);
    }

    @Override
    public void setInVisible(){
        super.setInVisible();
        formImgItem.setVisibility(GONE);
    }

    @Override
    public void setError(){
        formImgItem.setColorFilter(ContextCompat.getColor(ctx, R.color.Red));
    }

}
