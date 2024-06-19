package com.virtusee.view;

import android.content.Context;
import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.virtusee.core.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;


@EViewGroup(R.layout.form_combo)
public class FormComboView extends FormGroupView {
	
    @ViewById
    protected Spinner formComboItem;
    
	public FormComboView(Context context) {
		super(context);
	}

	public void bind(ArrayAdapter adapter){
		formComboItem.setAdapter(adapter);
	}
	
	@Override
	public String getVal(){
		Object s;
		s = formComboItem.getSelectedItem();
		return (s!=null) ? s.toString() : "";
	}

	public void setVal(String title) {
		if(title==null) return;
		for (int i = 0; i < formComboItem.getCount(); i++) {
			String s = (String) formComboItem.getItemAtPosition(i);
			if (s.equalsIgnoreCase(title)) {
				formComboItem.setSelection(i);
				break;
			}
		}

	}
	
	public void setId(int id){
		formComboItem.setId(id);
	}
	
    public void setReadonly(){
        formComboItem.setClickable(false);
        formComboItem.setEnabled(false);
    }

    @Override
    public void setError(){
        TextView errorText = (TextView)formComboItem.getSelectedView();
        errorText.setError("");
        errorText.setTextColor(Color.RED);//just to highlight that this is an error
        errorText.setText(reqMsg);//changes the selected item text to this
    }

    @Override
    public boolean validate(){
        return true;
    }


    @Override
    public void setVisible(){
        super.setVisible();
        formComboItem.setVisibility(VISIBLE);
    }

    @Override
    public void setInVisible(){
        super.setInVisible();
        formComboItem.setVisibility(GONE);
    }
}
