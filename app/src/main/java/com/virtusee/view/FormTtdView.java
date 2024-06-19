package com.virtusee.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.virtusee.core.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import androidx.core.content.ContextCompat;


@EViewGroup(R.layout.form_ttd)
public class FormTtdView extends FormGroupView {
    @ViewById
    protected ImageView formTtdItem;

    private Context ctx;

	public FormTtdView(Context context) {
		super(context);
		ctx = context;
		image = true;
	}

	public void onClick(OnClickListener formTtdItem_OnClickListener){
		formTtdItem.setOnClickListener(formTtdItem_OnClickListener);
	}
	
	public void setBackground(Bitmap background){
		formTtdItem.setImageBitmap(background);
	}
	
	public void setVisible(boolean vis){
		if(vis){
			formTtdItem.setVisibility(VISIBLE);
		} else {
			formTtdItem.setVisibility(INVISIBLE);			
		}
	}
	
	@Override
	public void setQuestionId(String idForm, String idQuestion,int type, int group){
		this.idForm = idForm;
		this.idQuestion = idQuestion;
		this.type = type;
		this.group = group;
		formTtdItem.setTag(R.id.TAG_QUESTION_ID, idQuestion);
	}
	
	
	public ImageView getImage(){
		return formTtdItem;
	}
	
	@Override
	public String getVal(){
		if(formTtdItem.getTag(R.id.TAG_PATH)!=null)  return formTtdItem.getTag(R.id.TAG_PATH).toString();
		else return "";
	}

	public void setId(int id){
		formTtdItem.setId(id);
	}

    @Override
    public void setVisible(){
        super.setVisible();
        formTtdItem.setVisibility(VISIBLE);
    }

    @Override
    public void setInVisible(){
        super.setInVisible();
        formTtdItem.setVisibility(GONE);
    }

    @Override
    public void setError(){
        formTtdItem.setColorFilter(ContextCompat.getColor(ctx, R.color.Red));
    }

}
