package com.virtusee.view;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.virtusee.core.R;
import com.virtusee.listener.StoreTagListener;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;


@EViewGroup(R.layout.store_tag_item)
public class StoreTagItemView extends RelativeLayout {
    Context ctx;

    @ViewById
    Button storeTagTitle;

    public StoreTagItemView(Context context) {
        super(context);
        ctx = context;
    }

    public void bind(Cursor store, final StoreTagListener storeTagListener, final int pos, final SparseBooleanArray selectedItems) {
        final String tg = store.getString(0);
        storeTagTitle.setText(tg);
        boolean isSelect = (selectedItems==null) ? false : selectedItems.get(pos, false);


        if(isSelect){
            pressed();
        } else {
            unpressed();
        }

        storeTagTitle.setOnClickListener(new OnClickListener() {

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
                storeTagListener.tagClick(storeTagTitle,tg);
            }
        });
    }

    private void pressed(){
        storeTagTitle.setSelected(true);
        storeTagTitle.setTag(1);
    }

    private void unpressed(){
        storeTagTitle.setSelected(false);
        storeTagTitle.setTag(0);
    }

}
