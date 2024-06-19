package com.virtusee.recycleradapter;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.ViewGroup;

import com.virtusee.listener.MyRecyclerListener;
import com.virtusee.listener.StoreTagListener;
import com.virtusee.view.StoreTagItemView;
import com.virtusee.view.StoreTagItemView_;
import com.virtusee.view.ViewWrapper;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class StoreTagRecyclerAdapter extends RecyclerViewAdapterBase<StoreTagItemView> {

    @RootContext
    Context context;

    SparseBooleanArray selectedItems = new SparseBooleanArray();

    private MyRecyclerListener listener;
    private StoreTagListener storeTagListener;


    public void setTagClickedListener(StoreTagListener storeTagListener){
        this.storeTagListener = storeTagListener;
    }

    public void setOnRecyclerItemClickedListener(MyRecyclerListener listener){
        this.listener = listener;
    }

    @Override
    protected StoreTagItemView onCreateItemView(ViewGroup parent, int viewType) {
        return StoreTagItemView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<StoreTagItemView> holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }

        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        onBindViewHolder(holder,mCursor,position);
    }

    public void onBindViewHolder(ViewWrapper<StoreTagItemView> holder, final Cursor cursor, int position) {
        StoreTagItemView view = holder.getView();
        holder.bindData(this.listener,cursor);
        view.bind(cursor,this.storeTagListener,position,selectedItems);

    }


    @Override
    public void onBindViewHolder(ViewWrapper<StoreTagItemView> holder, final Cursor cursor) {
//        StoreTagItemView view = holder.getView();
//        holder.bindData(this.listener,cursor);
//        view.bind(cursor,this.storeTagListener);
    }

}