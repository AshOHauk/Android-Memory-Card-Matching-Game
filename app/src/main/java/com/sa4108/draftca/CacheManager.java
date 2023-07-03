package com.sa4108.draftca;

import android.graphics.Bitmap;
import android.util.LruCache;

//Responsible for creating and managing a cache of Bitmap objects.
//Uses the singleton design pattern, ensuring that only one instance of CacheManager can exist.
public class CacheManager {
    private static CacheManager instance;
    private final LruCache<String, Bitmap> imageCache;

    private CacheManager() {
        // Private constructor to prevent instantiation from outside the class
        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = maxMemory / 8;
        imageCache = new LruCache<>(cacheSize);
    }
    public static synchronized CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }
    public LruCache<String, Bitmap> getImageCache() {
        return imageCache;
    }
}
