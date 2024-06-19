package com.virtusee.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

import com.virtusee.core.R;
import com.virtusee.helper.UtilHelper;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import androidx.core.content.ContextCompat;


@EViewGroup(R.layout.form_text)
public class FormTextView extends FormGroupView {
    private Context ctx;

    @ViewById
    protected TextView formTextItem;
    
	public FormTextView(Context context) {
		super(context);
		//super.isInputable = false;
        ctx = context;
		inputable = false;
	}

	public void bind(String title) {
		formTextItem.setText(title);
	}

	public void setId(int id){
		formTextItem.setId(id);
	}

    public void  setHeader(){
	    formTextItem.setAllCaps(true);
        formTextItem.setTypeface(formTextItem.getTypeface(), Typeface.BOLD);
        formTextItem.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        formTextItem.setTextColor(ContextCompat.getColor(ctx,R.color.White));
        formTextItem.setBackgroundColor(ContextCompat.getColor(ctx,R.color.vsblue));

        int p16 = UtilHelper.dptopx(ctx,16);
        int p10 = UtilHelper.dptopx(ctx,10);

        formTextItem.setPadding(p16,p10,p16,p10);

    }

    public void setVisible(){
        formTextItem.setVisibility(VISIBLE);
    }

    public void setInVisible(){
        formTextItem.setVisibility(GONE);
    }

    @Override
    public boolean validate(){
        return true;
    }

}
