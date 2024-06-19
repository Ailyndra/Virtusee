package com.virtusee.view;

import android.content.Context;
import android.widget.EditText;

import com.virtusee.core.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;


@EViewGroup(R.layout.form_multiple_choice)
public class FormMultipleChoiceView extends FormGroupView {

    @ViewById
    protected EditText formMultipleChoiceItem;

	public FormMultipleChoiceView(Context context) {
		super(context);
	}

	public void onClick(OnClickListener formMultipleChoiceItem_OnClickListener){
		formMultipleChoiceItem.setOnClickListener(formMultipleChoiceItem_OnClickListener);
	}
	
	public void setId(int id){
		formMultipleChoiceItem.setId(id);
	}

    public int idForm() {
        return formMultipleChoiceItem.getId();
    }

	public void setVal(String title, int group) {
        if(title==null) return;

		formMultipleChoiceItem.setTag(R.id.TAG_MC_ID, title);
		formMultipleChoiceItem.setText(title.replace("~@~",","));
        formMultipleChoiceItem.setTag(R.id.TAG_MC_GROUP, group);
	}

	@Override
	public String getVal(){
		if(formMultipleChoiceItem.getTag(R.id.TAG_MC_ID)!=null)  return formMultipleChoiceItem.getTag(R.id.TAG_MC_ID).toString();
		else return "";
	}

    @Override
    public void setError(){
        formMultipleChoiceItem.setError(reqMsg);
    }

    @Override
    public void setVisible(){
        super.setVisible();
        formMultipleChoiceItem.setVisibility(VISIBLE);
    }

    @Override
    public void setInVisible(){
        super.setInVisible();
        formMultipleChoiceItem.setVisibility(GONE);
    }

    public void setIcon(){
    }

    public void setReadonly(){
        formMultipleChoiceItem.setCompoundDrawables(null,null,null,null);
    }

}
