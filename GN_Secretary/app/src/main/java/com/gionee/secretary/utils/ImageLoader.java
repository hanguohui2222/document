package com.gionee.secretary.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class ImageLoader {

    private static final String TAG = "ImageLoader";
    private static ImageLoader imageLoader = null;
    private LruCache<String, Bitmap> mMemoryCache;

    private ImageLoader() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    /**
     * @return a new instance of ImageLoader
     */
    public static ImageLoader build() {
        if (imageLoader == null) {
            synchronized (ImageLoader.class) {
                if (imageLoader == null) {
                    imageLoader = new ImageLoader();
                }
            }
        }
        return imageLoader;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void recycle() {
        mMemoryCache.evictAll();
    }
}
