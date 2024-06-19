package com.virtusee.view;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.virtusee.core.R;
import com.virtusee.listener.FormTagListener;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;


@EViewGroup(R.layout.form_tag_item)
public class FormTagItemView extends RelativeLayout {

    @ViewById
    Button formTagTitle;

    public FormTagItemView(Context context) {
        super(context);
    }

    public void bind(Cursor form, final FormTagListener formTagListener, final int pos, final SparseBooleanArray selectedItems) {
        final String tg = form.getString(0);
        formTagTitle.setText(tg);
        boolean isSelect = (selectedItems==null) ? false : selectedItems.get(pos, false);


        if(isSelect){
            pressed();
        } else {
            unpressed();
        }


        formTagTitle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                if (selectedItems!=null && selectedItems.get(pos, false)) {
                    selectedItems.delete(pos);
                    unpressed();
                }
                else {
                    selectedItems.put(pos, true);
                    pressed();
                }
                formTagListener.tagClick(formTagTitle,tg);
            }
        });
    }

    private void pressed(){
        formTagTitle.setSelected(true);
        formTagTitle.setTag(1);
    }

    private void unpressed(){
        formTagTitle.setSelected(false);
        formTagTitle.setTag(0);
    }

}
