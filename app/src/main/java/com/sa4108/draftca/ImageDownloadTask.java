package com.sa4108.draftca;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageDownloadTask implements Runnable{
    private final String newUrl;
    private final MainActivity mainActivity;
    private final LruCache<String, Bitmap> cache = CacheManager.getInstance().getCache();
    public ImageDownloadTask(String newUrl, MainActivity mainActivity) {
        this.newUrl = newUrl;
        this.mainActivity = mainActivity;
    }
    //Main Logic for downloading images
    @Override
    public void run() {
        try {
            int counter = 0;
            //Step 1: Load the web page content
            URL url = new URL(this.newUrl);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.28) Gecko/20120306 Firefox/3.6.28");
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if(responseCode!=200){
                throw new Exception("Non-200 HTTP Response received: " + responseCode);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder htmlContent = new StringBuilder();
            while((line = reader.readLine()) != null) {
                if(Thread.currentThread().isInterrupted()) {
                    httpConnection.disconnect();
                    reader.close();
                    return;  // Thread has been interrupted, stop the task.
                }
                htmlContent.append(line);
            }
            reader.close();
            //Step 2: Extract the imageURLS
            //searches for <img> tags with src attributes containing image file extensions like .jpeg, .png, .jpg, .gif
            Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"](https?://[^'\"]+(?:\\.jpeg|\\.png|\\.jpg|\\.gif|\\.JPEG|\\.PNG|\\.JPG|\\.GIF))['\"][^>]*>");
            Matcher matcher = pattern.matcher(htmlContent.toString());
            //    private final ArrayList<String> imageList = new ArrayList<>();
            int maxNumberOfImages = 20;
            while(matcher.find() && mainActivity.imageList.size() < maxNumberOfImages) {
                if (Thread.currentThread().isInterrupted()) {
                    return;  // Thread has been interrupted, stop the task.
                }
                String imageUrlString = matcher.group(1);
                mainActivity.imageList.add(imageUrlString);
            }
            //Step3: Download images
            mainActivity.progressBar.setMax(mainActivity.imageList.size());
            for(String imageUrlString : mainActivity.imageList){
                if (Thread.currentThread().isInterrupted()) {
                    return;  // Thread has been interrupted, stop the task.
                }
                URL imageUrl = new URL(imageUrlString);
                HttpURLConnection httpImgURLConnection = (HttpURLConnection) imageUrl.openConnection();
                httpImgURLConnection.setDoInput(true);
                httpImgURLConnection.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.28) Gecko/20120306 Firefox/3.6.28");
                httpImgURLConnection.connect();
                InputStream input = httpImgURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                if(bitmap != null) {
                    // Resize the bitmap if it was loaded
                    bitmap = resizeBitmap(bitmap);
                    cache.put(imageUrlString, bitmap);
                }
                input.close();
                httpImgURLConnection.disconnect();
                counter++;
                String message = String.format(Locale.UK,"Downloaded %d of %d images", counter,mainActivity.imageList.size());
                mainActivity.updateGridView(message);
            }
            // Download complete, remove task from futuresMap
            mainActivity.futuresMap.remove(url.toString());
        } catch(IOException eio) {
            eio.printStackTrace();
            // Handle exception
            mainActivity.updateGridView("No Images available. Please try another URL");
        } catch(Exception e) {
            e.printStackTrace();
            // Handle exception
        }
    }
    //Resize bitmap while maintaining aspect ratio. To reduce memory usage if bitmap is especially large
    private Bitmap resizeBitmap(Bitmap source) {
        int desiredSize = (int) (100 * mainActivity.getResources().getDisplayMetrics().density);
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
