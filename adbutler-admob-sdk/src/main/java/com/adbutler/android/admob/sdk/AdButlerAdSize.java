package com.adbutler.android.admob.sdk;

/**
 * The size of an ad request.
 */
public class AdButlerAdSize {
    private int mWidth;
    private int mHeight;

    public AdButlerAdSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
