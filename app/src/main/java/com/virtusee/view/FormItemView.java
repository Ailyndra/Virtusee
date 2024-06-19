package com.virtusee.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.virtusee.core.R;
import com.virtusee.db.FormTable;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import androidx.core.content.ContextCompat;


@EViewGroup(R.layout.form_item)
public class FormItemView extends RelativeLayout {

    @ViewById
    TextView formTitle;

    @ViewById
    ImageView formIcon;
    private Context ctx;

    public FormItemView(Context context) {
        super(context);
        ctx = context;
	}

	public void bind(Cursor form) {
        int req = form.getInt(form.getColumnIndex(FormTable.COLUMN_FORM_MANDATORY));
        String jawab = form.getString(form.getColumnIndex("jawab"));
        formTitle.setText(form.getString(form.getColumnIndex(FormTable.COLUMN_FORM_TITLE)));

        if (jawab!=null && !jawab.equals("")) {
            formIcon.setImageResource(R.drawable.ic_check_circle);
            formIcon.getDrawable().setColorFilter(ContextCompat.getColor(formIcon.getContext(), R.color.LimeGreen), PorterDuff.Mode.SRC_ATOP);
            formIcon.setVisibility(VISIBLE);
        } else if (req == 1) {
            formIcon.setImageResource(R.drawable.ic_error_locked);
            formIcon.getDrawable().setColorFilter(ContextCompat.getColor(formIcon.getContext(), R.color.Red), PorterDuff.Mode.SRC_ATOP);
            formIcon.setVisibility(VISIBLE);
        } else {
            formIcon.setVisibility(INVISIBLE);
        }


        /*
        if (jawab==null || jawab.equals("")){
            formTitle.setTypeface(formTitle.getTypeface(), Typeface.BOLD);
            formTitle.setTextColor(Color.parseColor("#777777"));
        } else {
            formTitle.setTypeface(formTitle.getTypeface(), Typeface.BOLD_ITALIC);
            formTitle.setTextColor(Color.parseColor("#00A388"));
        }
        */
	}

}
