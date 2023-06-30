package com.sa4108.draftca;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<String> {
    // Error image resource ID
    private static final int ERROR_IMAGE_RES = R.drawable.ic_launcher_foreground;  // replace with error image resource

    private LruCache<String, Bitmap> cache = CacheManager.getInstance().getCache();
    private ArrayList<String> imageList;

    CustomListAdapter(Context context,ArrayList<String> imageList) {
        super(context, R.layout.grid_item,imageList);
        this.imageList = imageList;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
        }
        ImageView imageView = convertView.findViewById(R.id.gridImageItem);
        imageView.setImageResource(ERROR_IMAGE_RES);

        String imageUrl = imageList.get(position);
        Bitmap bitmap = cache.get(imageUrl);
        if (bitmap != null) {
            // Display the cached Bitmap in the ImageView
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(ERROR_IMAGE_RES);
        }

        return convertView;
    }
}
