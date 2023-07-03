package com.sa4108.draftca;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<String> {
    private static final int PLACEHOLDER_IMAGE_RES = R.drawable.ic_launcher_foreground;
    private final LruCache<String, Bitmap> cache = CacheManager.getInstance().getImageCache();
    private final ArrayList<String> imageList;

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
        imageView.setImageResource(PLACEHOLDER_IMAGE_RES);

        String imageUrl = imageList.get(position);
        Bitmap bitmap = cache.get(imageUrl);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        return convertView;
    }
}
