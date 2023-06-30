package com.sa4108.draftca;

import android.graphics.Bitmap;
import android.util.LruCache;

public class CacheManager {
    private static CacheManager instance;
    private LruCache<String, Bitmap> cache;

    private CacheManager() {
        // Private constructor to prevent instantiation from outside the class
        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = maxMemory / 8;
        cache = new LruCache<>(cacheSize);
    }

    public static synchronized CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }

    public LruCache<String, Bitmap> getCache() {
        return cache;
    }
}
