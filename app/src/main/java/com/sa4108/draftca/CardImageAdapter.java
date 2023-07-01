package com.sa4108.draftca;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class CardImageAdapter extends BaseAdapter {
    private Context context;

    public CardImageAdapter(Context c) {
        context = c;
    }

    public int getCount() {
        return 12; // 12 cards
    }

    public Object getItem(int position) {
        // TODO: Return image at the specific position
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(context);
        } else {
            imageView = (ImageView) convertView;
        }

        // Set image resource
        //TODO: imageView.setImageResource(...);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new GridView.LayoutParams(85, 85)); // Set the size of the imageview
        return imageView;
    }
}