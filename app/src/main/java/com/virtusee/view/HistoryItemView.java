package com.virtusee.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.virtusee.core.R;
import com.virtusee.db.AnswerTable;
import com.virtusee.db.FormTable;
import com.virtusee.db.StoreTable;
import com.virtusee.helper.DateHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;


@EViewGroup(R.layout.history_item)
public class HistoryItemView extends RelativeLayout {
    @Bean
    DateHelper dateHelper;


    @ViewById
    TextView historyPlace;

    @ViewById
    TextView historyForm;

    @ViewById
    TextView historyWhen;

    @ViewById
    TextView historySync;

    public HistoryItemView(Context context) {
        super(context);
    }

	public void bind(Cursor history) {
        String tgl;
        String sync;

		historyPlace.setText(history.getString(history.getColumnIndex(StoreTable.COLUMN_STORE_NAME)));
        historyForm.setText(history.getString(history.getColumnIndex(FormTable.COLUMN_FORM_TITLE)));
        tgl = history.getString(history.getColumnIndex(AnswerTable.COLUMN_WHEN));
        sync = history.getString(history.getColumnIndex(AnswerTable.COLUMN_LAST_SYNC));

        String skr = dateHelper.getCurrentDate();

        String[] waktu = tgl.split(" ");
        String[] tglsplit = waktu[0].split("-");
        String[] jamsplit = waktu[1].split(":");

        if(skr.equals(waktu[0])) {
            historyWhen.setText(jamsplit[0] + ":" + jamsplit[1]);
        } else {
            historyWhen.setText(tglsplit[2] + "/" + tglsplit[1]);
        }


        if((sync!=null) && !sync.equals("0") && !sync.equals("")) {
            historySync.setText("Synced");
            historySync.setTextColor(Color.GREEN);
        } else {
            historySync.setText("Not Synced");
            historySync.setTextColor(Color.RED);
        }
	}
	
}