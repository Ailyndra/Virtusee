package com.virtusee.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.virtusee.core.R;
import com.virtusee.model.DrawerModel;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;


@EViewGroup(R.layout.drawer_item)
public class DrawerItemView extends RelativeLayout {

    @ViewById
    ImageView drawerIcon;

    @ViewById
    TextView drawerText;

    public DrawerItemView(Context context) {
        super(context,null);
    }

    public static DrawerItemView inflate(ViewGroup parent) {
        DrawerItemView itemView = (DrawerItemView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.drawer_item, parent, false);
        return itemView;
    }

	public void bind(DrawerModel drawerModel) {
        drawerIcon.setImageResource(drawerModel.icon);
        drawerText.setText(drawerModel.name);
    }
	
}