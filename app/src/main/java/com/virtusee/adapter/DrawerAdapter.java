package com.virtusee.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.virtusee.core.R;
import com.virtusee.model.DrawerModel;

public class DrawerAdapter extends ArrayAdapter<DrawerModel> {
    private int selectedIndex = -1;
    private int selectedColor = Color.parseColor("#DBDBDB");

    public DrawerAdapter(Context context, DrawerModel[] data) {
        super(context, 0, data);
    }

    public void setSelectedIndex(int ind) {
        selectedIndex = ind;
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_item, parent, false);
        }

        DrawerModel drawerModel = getItem(position);
        RelativeLayout ly = (RelativeLayout) convertView.findViewById(R.id.drawerItemLayout);
        TextView name = (TextView) convertView.findViewById(R.id.drawerText);
        ImageView icon = (ImageView) convertView.findViewById(R.id.drawerIcon);

        name.setText(drawerModel.name);
        icon.setImageResource(drawerModel.icon);

        if(selectedIndex!= -1 && position == selectedIndex) {
            ly.setBackgroundColor(selectedColor);
        }
        else {
            ly.setBackgroundColor(Color.parseColor("#FCFFFF"));
        }
        return convertView;

    }

}
