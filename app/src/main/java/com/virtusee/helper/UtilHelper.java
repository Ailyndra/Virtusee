package com.virtusee.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;

import com.virtusee.core.R;
import com.virtusee.listener.DialogListener;

import androidx.appcompat.app.AlertDialog;

/**
 * Created by winata on 5/26/17.
 */

public class UtilHelper {
    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    public static int getTabsHeight(Context context) {
        return (int) context.getResources().getDimension(R.dimen.tabsHeight);
    }


    public static void Alert(Context context,DialogListener dialogListener, String title, String content, int icon){
        Alert(context,dialogListener, title, content, icon,0);
    }

    public static void Alert(Context context,DialogListener dialogListener, String title, String content, int icon,int type) {
        final DialogListener lst = dialogListener;
        final int dtype = type;
        AlertDialog.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        try{
            builder.setTitle(title)
                    .setMessage(content)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            lst.OnDialogOkay(dtype);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            lst.OnDialogCancel(dtype);
                        }
                    })
                    .setIcon(icon)
                    .show();
        } catch (Exception e){}

    }


    public static int dptopx(Context context, int dp){
        float scale = context.getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (dp*scale + 0.5f);
        return dpAsPixels;
    }
}
