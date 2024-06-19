package com.virtusee.recycleradapter;

import android.content.Context;
import android.database.Cursor;
import android.view.ViewGroup;

import com.virtusee.listener.MyRecyclerListener;
import com.virtusee.view.ActivityItemView;
import com.virtusee.view.ActivityItemView_;
import com.virtusee.view.ViewWrapper;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class ActivityRecyclerAdapter extends RecyclerViewAdapterBase<ActivityItemView> {

    @RootContext
    Context context;

    private MyRecyclerListener listener;


    public void setOnRecyclerItemClickedListener(MyRecyclerListener listener){
        this.listener = listener;
    }

    @Override
    protected ActivityItemView onCreateItemView(ViewGroup parent, int viewType) {
        return ActivityItemView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<ActivityItemView> holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }

        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        onBindViewHolder(holder,mCursor);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<ActivityItemView> holder, final Cursor cursor) {
        if(cursor==null) return;

        ActivityItemView view = holder.getView();
        holder.bindData(this.listener,cursor);
        view.bind(cursor);
    }

}