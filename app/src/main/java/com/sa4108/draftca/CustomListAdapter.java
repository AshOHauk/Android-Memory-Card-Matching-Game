package com.sa4108.draftca;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<String> {
    //image cache
    public final LruCache<String, Bitmap> imageCache;
    // Error image resource ID
    private static final int ERROR_IMAGE_RES = R.drawable.ic_launcher_foreground;  // replace with error image resource

    private MainActivity mainActivity;

    CustomListAdapter(Context context, ArrayList<String> items,MainActivity mainActivity) {
        super(context, R.layout.grid_item, items);
        this.mainActivity = mainActivity;
        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = maxMemory / 8;
        imageCache = new LruCache<>(cacheSize);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.gridImageItem);

        // Default Image
        imageView.setImageResource(ERROR_IMAGE_RES);

        mainActivity.executorService.execute(() -> {
            //Get the URL of the image from the dataset
            String imageUrl = getItem(position);
            //Get the bitmap from the cache using URL as the key
            Bitmap bitmap = imageCache.get(imageUrl);
            //if the bitmap is not in the cache
            if (bitmap == null) {
                //fetch the bitmap from the internet
                bitmap = getBitmapFromURL(imageUrl);
                //if the fetched bitmap is not null, cache the bitmap for future use, using the url as key
                if (bitmap != null)
                    imageCache.put(imageUrl, bitmap);
            }

            //Attach the bitmap to the imageView
            Bitmap finalBitmap = bitmap;
            imageView.post(() -> imageView.setImageBitmap(finalBitmap));
        });

        return convertView;
    }

    private Bitmap getBitmapFromURL(String imageUrl) {
        // Return a bitmap from the imageUrl using HttpURLConnection
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.28) Gecko/20120306 Firefox/3.6.28");
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            connection.disconnect();
            if(bitmap != null) {
                // Resize the bitmap if it was loaded
                bitmap = resizeBitmap(bitmap);
            }
            mainActivity.updateProgressText();
            try{
                Thread.sleep(100); //TODO remove after testing
            }catch(Exception e_sleep){}
            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    public Bitmap resizeBitmap(Bitmap source) {
        int desiredSize = (int) (100 * getContext().getResources().getDisplayMetrics().density);
        int width = source.getWidth();
        int height = source.getHeight();

        float bitmapRatio = (float) width / (float) height;
        int scaledWidth, scaledHeight;

        // Checking if the image is landscape or portrait
        if (bitmapRatio > 1) {
            scaledWidth = desiredSize;
            scaledHeight = (int) (scaledWidth / bitmapRatio);
        } else {
            scaledHeight = desiredSize;
            scaledWidth = (int) (scaledHeight * bitmapRatio);
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true);

        if (source != scaledBitmap) {
            // Ensure we're using the scaled bitmap and that the original bitmap is ready for garbage collection
            source.recycle();
        }

        return scaledBitmap;
    }

}
