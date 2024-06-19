package com.virtusee.recycleradapter;

import android.content.Context;
import android.database.Cursor;
import android.view.ViewGroup;

import com.virtusee.listener.MyRecyclerListener;
import com.virtusee.view.InboxItemView;
import com.virtusee.view.InboxItemView_;
import com.virtusee.view.ViewWrapper;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class InboxRecyclerAdapter extends RecyclerViewAdapterBase<InboxItemView> {

    @RootContext
    Context context;

    private MyRecyclerListener listener;


    public void setOnRecyclerItemClickedListener(MyRecyclerListener listener){
        this.listener = listener;
    }

    @Override
    protected InboxItemView onCreateItemView(ViewGroup parent, int viewType) {
        return InboxItemView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<InboxItemView> holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }

        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        onBindViewHolder(holder,mCursor);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<InboxItemView> holder, final Cursor cursor) {
        InboxItemView view = holder.getView();
        holder.bindData(this.listener,cursor);
        view.bind(cursor);
    }

}