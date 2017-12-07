package com.adbutler.android.admob.sdk;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

/**
 * A {@link AdButlerAdListener} that forwards events to AdMob's
 * {@link CustomEventBannerListener}.
 */
public class AdButlerCustomBannerEventForwarder extends AdButlerAdListener {
    private CustomEventBannerListener mBannerListener;
    private AdButlerAdView mAdView;

    /**
     * Creates a new {@code AdButlerBannerEventForwarder}.
     * @param listener An AdMob Mediation {@link CustomEventBannerListener} that should receive
     *                 forwarded events.
     * @param adView   A {@link AdButlerAdView}.
     */
    public AdButlerCustomBannerEventForwarder(
            CustomEventBannerListener listener, AdButlerAdView adView) {
        this.mBannerListener = listener;
        this.mAdView = adView;
    }

    @Override
    public void onAdFetchSucceeded() {
        mBannerListener.onAdLoaded(mAdView);

        // Fetch successful, record an impression.
        if (mAdView.placement != null) {
            mAdView.placement.recordImpression();
        }
    }

    @Override
    public void onAdFetchFailed(AdButlerErrorCode errorCode) {
        switch (errorCode) {
            case UNKNOWN:
                mBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                break;
            case BAD_REQUEST:
                mBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
                break;
            case NETWORK_ERROR:
                mBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
                break;
            case NO_INVENTORY:
                mBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                break;
        }
    }

    @Override
    public void onAdFullScreen() {
        mBannerListener.onAdClicked();
        mBannerListener.onAdOpened();
        // Only call onAdLeftApplication if your ad network actually exits the developer's app.
        mBannerListener.onAdLeftApplication();
    }

    @Override
    public void onAdClosed() {
        mBannerListener.onAdClosed();
    }
}
