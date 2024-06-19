package com.virtusee.recycleradapter;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.ViewGroup;

import com.virtusee.listener.FormTagListener;
import com.virtusee.listener.MyRecyclerListener;
import com.virtusee.view.FormTagItemView;
import com.virtusee.view.FormTagItemView_;
import com.virtusee.view.ViewWrapper;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class FormTagRecyclerAdapter extends RecyclerViewAdapterBase<FormTagItemView> {

    @RootContext
    Context context;

    private MyRecyclerListener listener;
    private FormTagListener formTagListener;
    SparseBooleanArray selectedItems = new SparseBooleanArray();


    public void setTagClickedListener(FormTagListener formTagListener){
        this.formTagListener = formTagListener;
    }

    public void setOnRecyclerItemClickedListener(MyRecyclerListener listener){
        this.listener = listener;
    }

    @Override
    protected FormTagItemView onCreateItemView(ViewGroup parent, int viewType) {
        return FormTagItemView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<FormTagItemView> holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }

        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        onBindViewHolder(holder,mCursor,position);
    }

    public void onBindViewHolder(ViewWrapper<FormTagItemView> holder, final Cursor cursor, int position) {
        FormTagItemView view = holder.getView();
        holder.bindData(this.listener,cursor);
        view.bind(cursor,this.formTagListener,position,selectedItems);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<FormTagItemView> holder, final Cursor cursor) {
    }

}