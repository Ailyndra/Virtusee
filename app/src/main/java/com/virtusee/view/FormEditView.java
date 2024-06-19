package com.virtusee.view;

import android.content.Context;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;

import com.virtusee.core.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;


@EViewGroup(R.layout.form_edit)
public class FormEditView extends FormGroupView {

    @ViewById
    protected EditText formEditItem;


	public FormEditView(Context context) {
		super(context);
	}

	public void setSingleLine(){
		formEditItem.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		formEditItem.setLines(1);
		formEditItem.setSingleLine();
	}

	public void setMultiLine(){
		formEditItem.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		formEditItem.setMinLines(3);
		formEditItem.setMaxLines(15);
		formEditItem.setGravity(Gravity.LEFT | Gravity.TOP);	
	}

	public void setNumeric(){
		formEditItem.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
	}
	
	@Override
	public String getVal(){
		return formEditItem.getText().toString();
		
	}
	  	
	public void setVal(String title) {
		if(title!=null) formEditItem.setText(title);
	}

	public void setId(int id){
		formEditItem.setId(id);
	}

	public int idForm() {
		return formEditItem.getId();
	}

    public void setReadonly(){
		formEditItem.setCompoundDrawables(null,null,null,null);
		formEditItem.setFocusable(false);
    }

    @Override
    public void setVisible(){
        super.setVisible();
        formEditItem.setVisibility(VISIBLE);
    }

    @Override
    public void setInVisible(){
        super.setInVisible();
        formEditItem.setVisibility(GONE);
    }
    @Override
    public void setError(){
        formEditItem.setError(reqMsg);
    }

	public void addTextChangedListener(TextWatcher watcher) {
		formEditItem.addTextChangedListener(watcher);
	}

	public EditText getEditText() {
		return formEditItem;
	}
}
