package com.adbutler.android.admob.sdk;

/**
 * An AdButler AdMob SDK ad listener to listen for ad events.
 */
public abstract class AdButlerAdListener {
    /**
     * Called when an ad is successfully fetched.
     */
    public void onAdFetchSucceeded() {
        // Default is to do nothing.
    }

    /**
     * Called when an ad fetch fails.
     * @param code The reason the fetch failed.
     */
    public void onAdFetchFailed(AdButlerErrorCode code) {
        // Default is to do nothing.
    }

    /**
     * Called when an ad goes full screen.
     */
    public void onAdFullScreen() {
        // Default is to do nothing.
    }

    /**
     * Called when an ad is closed.
     */
    public void onAdClosed() {
        // Default is to do nothing.
    }
}
