package com.virtusee.listener;

import android.database.Cursor;

/**
 * Created by winata on 5/15/17.
 */

public interface MyRecyclerListener{
    void onRecyclerItemClicked(Cursor cursor);
    void onRecyclerLongClicked(Cursor cursor);
}