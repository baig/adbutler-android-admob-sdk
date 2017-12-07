package com.adbutler.android.admob.sdk;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A custom event for the the AdButler SDK. Custom events allow publishers to write their own
 * mediation adapter.
 *
 * Since the custom event is not directly referenced by the Google Mobile Ads SDK and is instead
 * instantiated with reflection, it's possible that ProGuard might remove it. Use the {@link Keep}}
 * annotation to make sure that the adapter is not removed when minifying the project.
 */
@Keep
public class AdButlerCustomEventBanner implements CustomEventBanner {
    protected static final String TAG = AdButlerCustomEventBanner.class.getSimpleName();

    /**
     * The {@link AdButlerAdView} representing a banner ad.
     */
    private AdButlerAdView mAdButlerAdView;

    /**
     * The event is being destroyed. Perform any necessary cleanup here.
     */
    @Override
    public void onDestroy() {
        if (mAdButlerAdView != null) {
            mAdButlerAdView.destroy();
        }
    }

    /**
     * The app is being paused. This call will only be forwarded to the adapter if the developer
     * notifies mediation that the app is being paused.
     */
    @Override
    public void onPause() {
        // The sample ad network doesn't have an onPause method, so it does nothing.
    }

    /**
     * The app is being resumed. This call will only be forwarded to the adapter if the developer
     * notifies mediation that the app is being resumed.
     */
    @Override
    public void onResume() {
        // The sample ad network doesn't have an onResume method, so it does nothing.
    }

    @Override
    public void requestBannerAd(Context context,
                                CustomEventBannerListener listener,
                                String serverParameter,
                                AdSize size,
                                MediationAdRequest mediationAdRequest,
                                Bundle customEventExtras) {
        /*
         * In this method, you should:
         *
         * 1. Create your banner view.
         * 2. Set your ad network's listener.
         * 3. Make an ad request.
         *
         * When setting your ad network's listener, don't forget to send the following callbacks:
         *
         * listener.onAdLoaded(this);
         * listener.onAdFailedToLoad(this, AdRequest.ERROR_CODE_*);
         * listener.onAdClicked(this);
         * listener.onAdOpened(this);
         * listener.onAdLeftApplication(this);
         * listener.onAdClosed(this);
         */
        Log.d("Ads/AdButler", "received serverParameter=" + serverParameter);

        Integer accountId = 0;
        Integer zoneId = 0;
        Integer zoneWidth = 0;
        Integer zoneHeight = 0;

        try {
            JSONObject serverParametersJsonObject = new JSONObject(serverParameter);
            accountId = serverParametersJsonObject.getInt("account");
            zoneId = serverParametersJsonObject.getInt("zone");
            zoneWidth = serverParametersJsonObject.getInt("width");
            zoneHeight = serverParametersJsonObject.getInt("height");
        } catch (JSONException e) {
            Log.w("Ads/AdButler", e.getMessage());
        }

        mAdButlerAdView = new AdButlerAdView(context);

        mAdButlerAdView.setAccount(accountId);
        mAdButlerAdView.setZone(zoneId);
        mAdButlerAdView.setZoneWidth(zoneWidth);
        mAdButlerAdView.setZoneHeight(zoneHeight);

        // Internally, smart banners use constants to represent their ad size, which means a call to
        // AdSize.getHeight could return a negative value. You can accommodate this by using
        // AdSize.getHeightInPixels and AdSize.getWidthInPixels instead, and then adjusting to match
        // the device's display metrics.
        int widthInPixels = size.getWidthInPixels(context);
        int heightInPixels = size.getHeightInPixels(context);
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int widthInDp = Math.round(widthInPixels / displayMetrics.density);
        int heightInDp = Math.round(heightInPixels / displayMetrics.density);

        mAdButlerAdView.setSize(new AdButlerAdSize(widthInDp, heightInDp));

        // Implement a AdButlerAdListener and forward callbacks to mediation. The callback forwarding
        // is handled by AdButlerCustomBannerEventForwarder.
        mAdButlerAdView.setAdListener(new AdButlerCustomBannerEventForwarder(listener, mAdButlerAdView));

        // Make an ad request.
        mAdButlerAdView.fetchAd(createSampleRequest(mediationAdRequest));
    }

    /**
     * Helper method to create a {@link AdButlerAdRequest}.
     *
     * @param mediationAdRequest The mediation request with targeting information.
     * @return The created {@link AdButlerAdRequest}.
     */
    private AdButlerAdRequest createSampleRequest(MediationAdRequest mediationAdRequest) {
        AdButlerAdRequest request = new AdButlerAdRequest();
        request.setTestMode(mediationAdRequest.isTesting());
        request.setKeywords(mediationAdRequest.getKeywords());
        return request;
    }
}
