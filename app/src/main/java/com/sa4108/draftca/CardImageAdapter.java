package com.sa4108.draftca;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class CardImageAdapter extends ArrayAdapter {
    private final LruCache<String, Bitmap> cache = CacheManager.getInstance().getImageCache();
    private ArrayList<ImageItem> gameImages;
    private boolean[] revealed;
    public CardImageAdapter(Context context) {
        super(context, R.layout.grid_item);
        gameImages = generateImages();
        revealed = new boolean[gameImages.size()];
    }
    @Override
    public int getCount() {
        return gameImages.size();
    }
    public void revealImage(int position) {
        revealed[position] = true;
        notifyDataSetChanged();
    }

    public void unrevealImage(int position) {
        revealed[position] = false;
        notifyDataSetChanged();
    }

    public ImageItem getImageItem(int position) {
        return gameImages.get(position);
    }
    public boolean isImageRevealed(int position) {
        return revealed[position];
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
        }
        ImageView imageView = convertView.findViewById(R.id.gridImageItem);
        // Get the ImageItem object from gameImages based on position
        ImageItem imageItem = gameImages.get(position);
        // Set the image bitmap
        Bitmap image = imageItem.getBitmap();
        // Set the tag number
        int tagNumber = imageItem.getTagNumber();
        imageView.setTag(tagNumber);

        if (revealed[position]) {
            imageView.setImageBitmap(image);
        } else {
            // Set a common card back as the image
            imageView.setImageResource(R.drawable.card_back);
        }

        return convertView;
    }

    private ArrayList<ImageItem> generateImages(){
        ArrayList<ImageItem> imageItemList = new ArrayList<>();
        Map<String, Bitmap> cacheSnapshot = cache.snapshot();
        int tagNumber=1;
        for (Map.Entry<String, Bitmap> entry : cacheSnapshot.entrySet()) {
            Bitmap bitmap = entry.getValue();
            // Add each image from cache two times
            imageItemList.add(new ImageItem(bitmap, tagNumber));
            imageItemList.add(new ImageItem(bitmap, tagNumber));
            tagNumber++;
        }
        // Shuffle the list
        Collections.shuffle(imageItemList);
        return imageItemList;
    }
}