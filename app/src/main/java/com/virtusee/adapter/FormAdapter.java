package com.virtusee.adapter;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.virtusee.view.FormItemView;
import com.virtusee.view.FormItemView_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class FormAdapter extends CursorAdapter {
    
    @RootContext
    Context context;


    public FormAdapter(Context context) {
		  super(context, null, 0);
    }
	
    @AfterInject
    void initAdapter() {

    }

    public void init(Cursor c) {
        this.changeCursor(c);
    }


	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		Log.e("checkview", "viewbinded");

		FormItemView formItemView;
		formItemView = (FormItemView)view;
		formItemView.bind(cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup view) {
		// TODO Auto-generated method stub
		Log.e("checkview", "viewcreated");

		FormItemView formItemView;
		formItemView = FormItemView_.build(context);

		return formItemView;	
	}
}
