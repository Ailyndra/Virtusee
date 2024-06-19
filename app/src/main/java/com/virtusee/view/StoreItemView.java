package com.virtusee.view;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.virtusee.contentprovider.AnswerContentProvider;
import com.virtusee.contentprovider.FormContentProvider;
import com.virtusee.core.R;
import com.virtusee.db.AnswerTable;
import com.virtusee.db.FormTable;
import com.virtusee.db.StoreTable;
import com.virtusee.helper.PrefHelper_;
import com.virtusee.listener.StoreListener;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


@EViewGroup(R.layout.store_item)
public class StoreItemView extends RelativeLayout {
    private StoreListener storeListener;
    private Context context;

    @ViewById
    TextView storeTitle;

    @ViewById
    TextView storeDesc;

    @ViewById
    ImageView storeFlag;

    @Pref
    PrefHelper_ myPrefs;

    public StoreItemView(Context context) {
        super(context);
        this.context = context;
    }

	public void bind(Cursor store, final StoreListener storeListener) {
        String store_id = store.getString(store.getColumnIndexOrThrow(StoreTable.COLUMN_STORE_ID));

    //    final String content;
        int locked = store.getInt(store.getColumnIndexOrThrow(StoreTable.COLUMN_LOCKED));

        String whenupdate = store.getString(store.getColumnIndexOrThrow(StoreTable.COLUMN_LOCKED_WHEN));
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date whendate = sdf.parse(whenupdate);
            Date today = new Date();
            Date todayDate = sdf.parse(sdf.format(today));
            if(todayDate.after(whendate))  locked = 0;
        } catch (ParseException e) {
        } catch (NullPointerException e) {
        }

        storeTitle.setText(store.getString(store.getColumnIndexOrThrow(StoreTable.COLUMN_STORE_NAME)));
		storeDesc.setText(store.getString(store.getColumnIndexOrThrow(StoreTable.COLUMN_STORE_CODE)));

		if(locked==1) storeFlag.setVisibility(VISIBLE);
		else  storeFlag.setVisibility(GONE);

        Uri dtUri = Uri.parse(FormContentProvider.CONTENT_URI + "/t/" + Uri.encode(store_id));
        Cursor cursor = context.getContentResolver().query(dtUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            ArrayList<Boolean> jawabs = new ArrayList<>();
            do {
                jawabs.add(cursor.getString(cursor.getColumnIndexOrThrow("jawab")) != null);
            } while (cursor.moveToNext());

            if (!jawabs.contains(false)) {
                storeFlag.setImageResource(R.drawable.ic_check_circle);
                storeFlag.getDrawable().setColorFilter(ContextCompat.getColor(storeFlag.getContext(), R.color.LimeGreen), PorterDuff.Mode.SRC_ATOP);
                storeFlag.setVisibility(VISIBLE);
            }
        }
        cursor.close();


		/*
        storeReadmore.setVisibility(GONE);

        content = store.getString(store.getColumnIndex(StoreTable.COLUMN_ATTR));
        if(content==null || content.equals("")){
        } else {
            storeReadmore.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    storeListener.readMoreClick(content);
                }
            });
        }
        */
	}
}