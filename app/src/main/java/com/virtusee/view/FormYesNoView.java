package com.virtusee.view;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.virtusee.core.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;


@EViewGroup(R.layout.form_yesno)
public class FormYesNoView extends FormGroupView {
    @ViewById
    protected RadioGroup formYesNoGroup;

    @ViewById
    protected RadioButton formYes;

    @ViewById
    protected RadioButton formNo;

    @ViewById
    protected ImageView imgError;

	public FormYesNoView(Context context) {
		super(context);
	}

    public void onClick(RadioGroup.OnCheckedChangeListener formYesNo_OnClickListener){
        formYesNoGroup.setOnCheckedChangeListener(formYesNo_OnClickListener);
    }

	@Override
	public String getVal() {
        boolean y = formYes.isChecked();
        boolean n = formNo.isChecked();
        return (y) ? "Yes" : (n) ? "No" : "";
	}

    public void setVal(String title) {
        if(title==null) return;
        if(title.equals("")) return;

        if(title.equals("Yes")) formYes.setChecked(true);
        else formNo.setChecked(true);
    }


    @Override
    public void setVisible(){
        super.setVisible();
        formYesNoGroup.setVisibility(VISIBLE);
    }

    @Override
    public void setInVisible(){
        super.setInVisible();
        formYesNoGroup.setVisibility(GONE);
    }

    @Override
    public void setError(){
        imgError.setVisibility(VISIBLE);

    }

    public void unSetError(){
        imgError.setVisibility(INVISIBLE);
    }

    public void setReadonly(){
        formYesNoGroup.setEnabled(false);
        formYes.setEnabled(false);
        formNo.setEnabled(false);
    }
}
