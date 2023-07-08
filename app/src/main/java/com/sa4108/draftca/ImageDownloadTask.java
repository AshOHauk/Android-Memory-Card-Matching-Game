package com.sa4108.draftca;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageDownloadTask implements Runnable {
    private final String newUrl;
    private final ArrayList<String> imageList = new ArrayList<>();
    private final ImageDownloadCallback callback;
    private final Context context;
    private final LruCache<String, Bitmap> imageCache = CacheManager.getInstance().getImageCache();

    public ImageDownloadTask(String newUrl, @NonNull ImageDownloadCallback callback, Context context) {
        this.newUrl = newUrl;
        this.callback = callback;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            // Load the web page content
            URL url = new URL(this.newUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.28) Gecko/20120306 Firefox/3.6.28");
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Non-200 HTTP Response received: " + responseCode);
            }

            InputStream inputStream = connection.getInputStream();
            String htmlContent = readInputStream(inputStream);
            inputStream.close();

            // Extract the imageURLS
            String patternString = "<img[^>]+src\\s*=\\s*['\"](https?://[^'\"]+(?:\\.jpeg|\\.png|\\.jpg|\\.gif|\\.JPEG|\\.PNG|\\.JPG|\\.GIF))['\"][^>]*>";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(htmlContent);
            int maxNumberOfImages = 20;
            while (matcher.find() && imageList.size() < maxNumberOfImages) {
                String imageUrlString = matcher.group(1);
                imageList.add(imageUrlString);
            }

            callback.onUrlRetrievalComplete(imageList);

            // Download images
            if (imageList.size() == 0) {
                callback.onImageDownloadError(this.newUrl, "URL has no images. Please try a different URL");
                return;
            }

            int numThreads = Math.min(imageList.size(), Runtime.getRuntime().availableProcessors());
            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

            for (String imageUrlString : imageList) {
                executorService.execute(() -> {
                    try {
                        URL imageUrl = new URL(imageUrlString);
                        HttpURLConnection imgConnection = (HttpURLConnection) imageUrl.openConnection();
                        imgConnection.setRequestProperty("User-Agent",
                                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.28) Gecko/20120306 Firefox/3.6.28");
                        InputStream imgInputStream = imgConnection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(imgInputStream);
                        imgInputStream.close();
                        imgConnection.disconnect();

                        if (bitmap != null) {
                            // Resize the bitmap if it was loaded
                            bitmap = resizeBitmap(bitmap);
                            imageCache.put(imageUrlString, bitmap);
                        }

                        callback.onEachImageDownloadComplete(imageCache.size(), imageList.size());

                        if (imageCache.size() == imageList.size()) {
                            callback.onAllImageDownloadComplete(url.toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        callback.onImageDownloadError(this.newUrl, "Error downloading image: " + e.getMessage());
                    }
                });
            }

            executorService.shutdown();
        } catch (IOException eio) {
            eio.printStackTrace();
            callback.onImageDownloadError(this.newUrl, "Please try a different URL");
        } catch (Exception e) {
            e.printStackTrace();
            callback.onImageDownloadError(this.newUrl, "Error: " + e.getMessage());
        }
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private Bitmap resizeBitmap(Bitmap source) {
        int desiredSize = (int) (100 * context.getResources().getDisplayMetrics().density);
        int width = source.getWidth();
        int height = source.getHeight();

        float bitmapRatio = (float) width / (float) height;
        int scaledWidth, scaledHeight;

        if (bitmapRatio > 1) {
            scaledWidth = desiredSize;
            scaledHeight = (int) (scaledWidth / bitmapRatio);
        } else {
            scaledHeight = desiredSize;
            scaledWidth = (int) (scaledHeight * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true);
    }
}
