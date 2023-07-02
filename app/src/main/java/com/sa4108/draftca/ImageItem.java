package com.sa4108.draftca;

import android.graphics.Bitmap;

public class ImageItem {
    private Bitmap bitmap;
    private int tagNumber;

    public ImageItem(Bitmap bitmap, int tagNumber) {
        this.bitmap = bitmap;
        this.tagNumber = tagNumber;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }
    public int getTagNumber() {
        return tagNumber;
    }
}

