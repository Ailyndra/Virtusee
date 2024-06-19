package com.virtusee.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.virtusee.view.HistoryItemView;
import com.virtusee.view.HistoryItemView_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class HistoryAdapter extends CursorAdapter {

    @RootContext
    Context context;

    public HistoryAdapter(Context context) {
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
		HistoryItemView historyItemView;
        historyItemView = (HistoryItemView) view;
        historyItemView.bind(cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup view) {
        HistoryItemView historyItemView;
        historyItemView = HistoryItemView_.build(context);

		return historyItemView;
	}

}
