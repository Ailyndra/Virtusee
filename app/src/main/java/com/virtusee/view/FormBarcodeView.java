package com.virtusee.view;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;

import com.virtusee.core.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.form_barcode)
public class FormBarcodeView extends FormGroupView {

    @ViewById
    protected EditText formEditBarcode;

    @ViewById
    protected ImageView formImgBarcode;

    public FormBarcodeView(Context context) {
        super(context);
    }

    public void onClick(OnClickListener formImgItem_OnClickListener){
        formImgBarcode.setOnClickListener(formImgItem_OnClickListener);
    }

    public void setMultiLine(){
        formEditBarcode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        formEditBarcode.setMinLines(3);
        formEditBarcode.setMaxLines(15);
        formEditBarcode.setGravity(Gravity.LEFT | Gravity.TOP);
    }

    @Override
    public String getVal(){
        return formEditBarcode.getText().toString();
    }

    public void setVal(String title) {
        if(title!=null) formEditBarcode.setText(title);
    }

    public void setId(int id){
        formEditBarcode.setId(id);
    }

    public void setReadonly(){
        formEditBarcode.setCompoundDrawables(null,null,null,null);
        formEditBarcode.setFocusable(false);
        formImgBarcode.setVisibility(GONE);
    }

    @Override
    public void setVisible(){
        super.setVisible();
        formEditBarcode.setVisibility(VISIBLE);
    }

    @Override
    public void setInVisible(){
        super.setInVisible();
        formEditBarcode.setVisibility(GONE);
    }

    @Override
    public void setError(){
        formEditBarcode.setError(reqMsg);
    }
}
