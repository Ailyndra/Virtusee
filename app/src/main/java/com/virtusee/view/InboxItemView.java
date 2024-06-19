package com.virtusee.view;

import android.content.Context;
import android.database.Cursor;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.virtusee.core.R;
import com.virtusee.db.InboxTable;
import com.virtusee.helper.DateHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@EViewGroup(R.layout.inbox_item)
public class InboxItemView extends RelativeLayout {
    @Bean
    DateHelper dateHelper;

    @ViewById
    TextView inboxContent;

    @ViewById
    TextView inboxFilename;

    @ViewById
    ImageView inboxFiletype;

    @ViewById
    TextView inboxWhen;

    @ViewById
    LinearLayout inboxFname;

    @ViewById
    LinearLayout inboxMsg;

    private Context ctx;

    public InboxItemView(Context context) {
        super(context);
        ctx = context;
    }

	public void bind(Cursor cursor) {
        setItem(cursor);
	}


    private void setItem(Cursor cursor){

        String whenupdate = cursor.getString(cursor.getColumnIndex(InboxTable.COLUMN_WHEN));
        String url = cursor.getString(cursor.getColumnIndex(InboxTable.COLUMN_URL));
        String content = cursor.getString(cursor.getColumnIndex(InboxTable.COLUMN_CONTENT));

        if(url.equals("")) setMsg(content);
        else setFile(content);

        setViewJam(whenupdate);
    }

    private void setFile(String content){
        int icon = R.drawable.ic_file;
        if(content.endsWith(".pdf")){
            icon = R.drawable.ic_pdf;
        } else if(content.endsWith(".xls")){
            icon = R.drawable.ic_xls;
        } else if(content.endsWith(".xlsx")){
            icon = R.drawable.ic_xls;
        } else if(content.endsWith(".doc")){
            icon = R.drawable.ic_doc;
        } else if(content.endsWith(".docx")){
            icon = R.drawable.ic_doc;
        } else if(content.endsWith(".jpg")){
            icon = R.drawable.ic_jpg;
        } else if(content.endsWith(".jpeg")){
            icon = R.drawable.ic_jpg;
        } else if(content.endsWith(".png")){
            icon = R.drawable.ic_png;
        }
        inboxFilename.setText(content);

        try {
            inboxFiletype.setImageResource(icon);
        } catch (Exception e){}

        inboxFname.setVisibility(VISIBLE);
        inboxMsg.setVisibility(GONE);
    }

    private void setMsg(String content){
        /*
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            inboxContent.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(InboxTable.COLUMN_CONTENT)),Html.FROM_HTML_MODE_LEGACY));
        } else {
            inboxContent.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(InboxTable.COLUMN_CONTENT))));
        }
        inboxContent.setMovementMethod(LinkMovementMethod.getInstance());
*/

        inboxContent.setText(content);
        inboxMsg.setVisibility(VISIBLE);
        inboxFname.setVisibility(GONE);
    }

    private void setViewJam(String whenupdate) {

        SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date MyDate = null;
        Long time = new Date().getTime();
        Date nowdate = new Date(time - time % (24 * 60 * 60 * 1000));


        try {
            MyDate = newDateFormat.parse(whenupdate);
            if(nowdate.compareTo(MyDate)>0){
                newDateFormat.applyPattern("MMM d,yyyy");
            } else {
                newDateFormat.applyPattern("h:mm a");
            }
            String MyDate2 = newDateFormat.format(MyDate);
            inboxWhen.setText(MyDate2);

        } catch (ParseException e) {
            inboxWhen.setText(whenupdate);
            e.printStackTrace();
        }
    }

	
}