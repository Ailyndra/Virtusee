package com.virtusee.view;

import android.database.Cursor;
import android.view.View;

import com.virtusee.listener.MyRecyclerListener;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by winata on 5/15/17.
 */

public class ViewWrapper<V extends View > extends RecyclerView.ViewHolder implements View.OnClickListener {

    private V view;
    private Cursor cursor;
    private MyRecyclerListener listener;

    public ViewWrapper(V itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        view = itemView;
    }

    public V getView() {
        return view;
    }

    public void bindData(MyRecyclerListener lst, Cursor c) {
        listener = lst;
        this.cursor = c;
    }

    @Override
    public void onClick(View view) {
        int position = getAdapterPosition();
        if(position>=0 && cursor!=null) {
            cursor.moveToPosition(position);
            if(listener!=null) listener.onRecyclerItemClicked(cursor);
        }
    }
}

