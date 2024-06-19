package com.virtusee.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.EditText;

import com.virtusee.core.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.DateFormat;
import java.util.GregorianCalendar;


@EViewGroup(R.layout.form_date)
public class FormDateView extends FormGroupView {
    @ViewById
    protected EditText formDateItem;
    
	public FormDateView(Context context) {
		super(context);
	}

	public void onClick(OnClickListener formDateItem_OnClickListener){
		formDateItem.setOnClickListener(formDateItem_OnClickListener);
	}
	
	public void setId(int id){
		formDateItem.setId(id);
	}

	public void setVal(String title) {
		if(title==null || title.equals("")) return;

		String[] cs = title.split("-");
		
		int dd = Integer.valueOf(cs[0]);
		int mm = Integer.valueOf(cs[1]);
		int yy = Integer.valueOf(cs[2]);
		
		formDateItem.setTag(R.id.TAG_DATE_ID, title);
		formDateItem.setTag(R.id.TAG_DATE_DD, dd);
		formDateItem.setTag(R.id.TAG_DATE_MM, mm);
		formDateItem.setTag(R.id.TAG_DATE_YY, yy);
		
        GregorianCalendar dateConverted = new GregorianCalendar(yy, mm-1, dd);
        DateFormat fmt = DateFormat.getDateInstance();
        formDateItem.setText(fmt.format(dateConverted.getTime()));
	}
	
	@Override
	public String getVal(){
		if(formDateItem.getTag(R.id.TAG_DATE_ID)!=null)  return formDateItem.getTag(R.id.TAG_DATE_ID).toString();
		else return "";
	}

    @Override
    public void setError(){
        formDateItem.setError(reqMsg);
    }

    @Override
    public void setVisible(){
        super.setVisible();
        formDateItem.setVisibility(VISIBLE);
    }

    @Override
    public void setInVisible(){
        super.setInVisible();
        formDateItem.setVisibility(GONE);
    }

    public void setIcon(){
    }

    public void setReadonly(){
        Drawable l = formDateItem.getCompoundDrawables()[0];
        formDateItem.setCompoundDrawables(l,null,null,null);
    }

}
