package com.virtusee.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;


@EViewGroup(R.layout.activity_item)
public class ActivityItemView extends RelativeLayout {
    @Bean
    DateHelper dateHelper;

    @ViewById
    ImageView activityIcon;

    @ViewById
    CardView activityCard;

    @ViewById
    TextView activityItemGroup;

    @ViewById
    TextView activityAt;

    @ViewById
    TextView activityImgSync;

    @ViewById
    LinearLayout activityImgWrap;

    @ViewById
    TextView activityTitle;

    @ViewById
    TextView activityPlace;

    @ViewById
    TextView activityForm;

    @ViewById
    TextView activityWhen;

    @ViewById
    TextView activitySync;

    private Context ctx;

    public ActivityItemView(Context context) {
        super(context);
        ctx = context;
    }

	public void bind(Cursor history) {

        int id = history.getInt(history.getColumnIndex(AnswerTable.COLUMN_ID));
        String form = history.getString(history.getColumnIndex(AnswerTable.COLUMN_ID_FORM));

        if(id<0) {
            setItemGroup(history);
        } else if(form.equals("ABSENSI")){
//            setAbsen(history);
        } else {
            setItem(history);
        }

	}

	private void setAbsen(Cursor history){
        String jam,sync,title,place;
        int icon = R.drawable.ic_assignment;
        int color = R.color.CadetBlue;

        jam = history.getString(history.getColumnIndex("jam"));
        sync = history.getString(history.getColumnIndex(AnswerTable.COLUMN_LAST_SYNC));
        String idstore = history.getString(history.getColumnIndex(AnswerTable.COLUMN_ID_STORE));
        String status = history.getString(history.getColumnIndex(AnswerTable.COLUMN_CONTENT));

        if(idstore.equals("wday")){
            title = " ";
            switch (status){
                case "1":
                    place = "Workday Started";
                    icon = R.drawable.ic_alarm_on;
                    color = R.color.DarkBlue;
                    break;
                case "2":
                    place = "Workday Ended";
                    icon = R.drawable.ic_alarm_off;
                    color = R.color.DarkGray;
                    break;
                case "3":
                    place = "Workday Paused";
                    icon = R.drawable.ic_pause_circle;
                    color = R.color.GreenYellow;
                    break;
                default:
                    place = "";
                    break;
            }
        } else {
            switch (status) {
                case "1":
                    title = "Checked In at";
                    color = R.color.Yellow;
                    break;
                case "2":
                    title = "Checked Out from ";
                    color = R.color.Red;
                    break;
                default:
                    title = "";
                    break;
            }
            place = history.getString(history.getColumnIndex(StoreTable.COLUMN_STORE_NAME));
            icon = R.drawable.ic_place;
        }


        activityItemGroup.setVisibility(View.GONE);
        activityCard.setVisibility(View.VISIBLE);

        activityTitle.setText(title);
        activityPlace.setText("");
        activityForm.setText(place);
        activityAt.setVisibility(View.GONE);

        setViewSync(sync);
        setViewJam(jam);
        setViewIcon(icon,color);
    }

	private void setItem(Cursor history){
        String jam;
        String sync;
        String title = "You saved";
        int imgtot,imgsync;
        int icon = R.drawable.ic_assignment;

        jam = history.getString(history.getColumnIndex("jam"));
        sync = history.getString(history.getColumnIndex(AnswerTable.COLUMN_LAST_SYNC));
        imgtot = history.getInt(history.getColumnIndex("imgtot"));
        imgsync = history.getInt(history.getColumnIndex("imgsync"));

        activityItemGroup.setVisibility(View.GONE);
        activityCard.setVisibility(View.VISIBLE);

        activityPlace.setText(history.getString(history.getColumnIndex(StoreTable.COLUMN_STORE_NAME)));
        activityForm.setText(history.getString(history.getColumnIndex(FormTable.COLUMN_FORM_TITLE)));
        activityTitle.setText(title);
        activityAt.setVisibility(View.VISIBLE);

        setViewJam(jam);
        setViewSync(sync);
        setViewImg(imgtot,imgsync);
//        setViewIcon(icon,R.color.CadetBlue);
    }

    private void setViewImg(int imgtot,int imgsync){
        if(imgtot>0) {
            activityImgWrap.setVisibility(View.VISIBLE);
            activityImgSync.setText(String.valueOf(imgtot));
            if(imgsync>0) activityImgSync.setTextColor(ContextCompat.getColor(ctx,R.color.LimeGreen));
            else activityImgSync.setTextColor(Color.RED);
        } else {
            activityImgWrap.setVisibility(View.GONE);
        }

    }

    private void setItemGroup(Cursor history) {
        String tgl = history.getString(history.getColumnIndex("tgl"));

        activityItemGroup.setVisibility(View.VISIBLE);
        activityCard.setVisibility(View.GONE);

        SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date MyDate = null;

        try {
            MyDate = newDateFormat.parse(tgl);
            newDateFormat.applyPattern("EEEE, d MMM yyyy");
            String MyDate2 = newDateFormat.format(MyDate);
            activityItemGroup.setText(MyDate2);

        } catch (ParseException e) {
            activityItemGroup.setText(tgl);
            e.printStackTrace();
        }

    }

    private void setViewJam(String jam) {
        SimpleDateFormat newDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date MyDate = null;

        try {
            MyDate = newDateFormat.parse(jam);
            newDateFormat.applyPattern("h:mm a");
            String MyDate2 = newDateFormat.format(MyDate);
            activityWhen.setText(MyDate2);

        } catch (ParseException e) {
            activityWhen.setText(jam);
            e.printStackTrace();
        }
    }

    private void setViewSync(String sync){
        if((sync!=null) && !sync.equals("0") && !sync.equals("")) {
            activitySync.setText("Synced");
            activitySync.setTextColor(ContextCompat.getColor(ctx,R.color.LimeGreen));
        } else {
            activitySync.setText("Not Synced");
            activitySync.setTextColor(Color.RED);
        }
    }

    private void setViewIcon(int icon,int tint){
        try {
            activityIcon.setImageResource(icon);
            activityIcon.setColorFilter(ContextCompat.getColor(ctx,tint));
        } catch (Exception e){}

    }
	
}