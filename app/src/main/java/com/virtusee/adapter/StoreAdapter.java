package com.virtusee.adapter;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.virtusee.listener.StoreListener;
import com.virtusee.view.StoreItemView;
import com.virtusee.view.StoreItemView_;

public class StoreAdapter extends CursorAdapter {

    private Context context;
    private StoreListener storeListener;

    public StoreAdapter(Context context,StoreListener lst) {
        super(context, null, 0);
        storeListener = lst;
    }

    public void init(Cursor c) {
        this.changeCursor(c);
    }


	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub

		StoreItemView storeItemView;
		storeItemView = (StoreItemView)view;
		storeItemView.bind(cursor,storeListener);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup view) {
		// TODO Auto-generated method stub
		Log.e("checkview", "viewcreated");

		StoreItemView storeItemView;
		storeItemView = StoreItemView_.build(context);

		return storeItemView;	
	}
	
}
