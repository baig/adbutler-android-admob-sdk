package com.adbutler.android.admob.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.sparklit.adbutler.AdButler;
import com.sparklit.adbutler.Placement;
import com.sparklit.adbutler.PlacementRequestConfig;
import com.sparklit.adbutler.PlacementResponse;
import com.sparklit.adbutler.PlacementResponseListener;

import java.util.Calendar;

/**
 * An ad view for the AdButler AdMob SDK.
 */
public class AdButlerAdView extends WebView {

    public Placement placement;

    private Integer accountID;
    private Integer zoneID;
    private Integer zoneWidth;
    private Integer zoneHeight;

    private AdButlerAdSize mAdSize;
    private AdButlerAdListener mListener;

    /**
     * Create a new {@link AdButlerAdView}.
     * @param context An Android {@link Context}.
     */
    public AdButlerAdView(Context context) {
        super(context);
    }

    /**
     *
     * @param accountID The account ID.
     */
    public void setAccount(Integer accountID) {
        this.accountID = accountID;
    }

    /**
     *
     * @param zoneID The zone ID to serve.
     */
    public void setZone(Integer zoneID) {
        this.zoneID = zoneID;
    }

    /**
     *
     * @param zoneWidth The width of this zone.
     */
    public void setZoneWidth(Integer zoneWidth) {
        this.zoneWidth = zoneWidth;
    }

    /**
     *
     * @param zoneHeight The height of this zone.
     */
    public void setZoneHeight(Integer zoneHeight) {
        this.zoneHeight = zoneHeight;
    }

    /**
     * Sets the size of the banner.
     * @param size The banner size.
     */
    public void setSize(AdButlerAdSize size) {
        this.mAdSize = size;
    }

    /**
     * Sets a {@link AdButlerAdListener} to listen for ad events.
     * @param listener The ad listener.
     */
    public void setAdListener(AdButlerAdListener listener) {
        this.mListener = listener;
    }

    /**
     * Get the actual ad markup.
     * @param body The ad body.
     * @return String
     */
    public String getAdMarkup(String body) {
        return "<!DOCTYPE HTML><html><head><style>html,body{padding:0;margin:0;}iframe{border:0;overflow:none;}</style></head><body>"
                + body + "</body></html>";
    }

    /**
     * Fetch an ad from AdButler.
     * @param request The ad request with targeting information.
     */
    public void fetchAd(AdButlerAdRequest request) {
        final AdButlerAdView adView = this;

        Log.d("Ads/AdButler", "In AdButlerAdView.fetchAd()");
        if (mListener == null) {
            return;
        }

        if (this.accountID == 0 || this.zoneID == 0 || mAdSize == null) {
            mListener.onAdFetchFailed(AdButlerErrorCode.BAD_REQUEST);
            return;
        }

        // TODO move this inside the success state of loading placements from adbutler
        mListener.onAdFetchSucceeded();

        final PlacementRequestConfig config = new PlacementRequestConfig.Builder(this.accountID, this.zoneID, this.zoneWidth, this.zoneHeight)
                .build();

        AdButler AdButlerSDK = new AdButler();
        AdButlerSDK.requestPlacement(config, new PlacementResponseListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void success(PlacementResponse response) {
                for (Placement placement : response.getPlacements()) {
                    Log.d("Ads/AdButler", "BannerID: " + placement.getBannerId());
                }

                Placement placement = null;
                if (response.getPlacements().size() > 0) {
                    placement = response.getPlacements().get(0);
                }

                if (placement == null) {
                    mListener.onAdFetchFailed(AdButlerErrorCode.NO_INVENTORY);

                } else {
                    adView.placement = placement;
                    final String placementRedirectURL = placement.getRedirectUrl();

                    /*
                     * Basic flow:
                     *  - Configure WebView Settings
                     *  - Disable scrolling
                     *  - Configure touch listener
                     *  - Calculate the layout width and height
                     *  - Load markup into WebView
                     *  - Finally, report fetch succeeded!
                     */

                    // WebView settings
                    WebSettings settings = adView.getSettings();
                    settings.setJavaScriptEnabled(true);
                    settings.setDomStorageEnabled(true);

                    // Disable scrolling
                    adView.setScrollContainer(false);
                    adView.setVerticalScrollBarEnabled(false);
                    adView.setHorizontalScrollBarEnabled(false);

                    // Set up the OnTouchListener
                    adView.setOnTouchListener(new View.OnTouchListener() {
                        private static final int MAX_CLICK_DURATION = 200;
                        private long clickStartTime;

                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            switch(motionEvent.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    clickStartTime = Calendar.getInstance().getTimeInMillis();
                                    break;

                                case MotionEvent.ACTION_UP:
                                    long clickDuration = Calendar.getInstance().getTimeInMillis() - clickStartTime;
                                    if(clickDuration < MAX_CLICK_DURATION) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(placementRedirectURL));
                                        adView.getContext().startActivity(intent);
                                    }
                                    break;

                                case MotionEvent.ACTION_MOVE:
                                    break;
                            }

                            return true;
                        }
                    });

                    // Calculate the layout width and height
                    DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
                    Integer actualRenderWidth = Math.round(placement.getWidth() * displayMetrics.density);
                    Integer actualRenderHeight = Math.round(placement.getHeight() * displayMetrics.density);
                    adView.setLayoutParams(new FrameLayout.LayoutParams(actualRenderWidth, actualRenderHeight));

                    // Load the ad markup into the view
                    String markup = "";
                    if (placement.getBody().length() > 0) {
                        markup = getAdMarkup(placement.getBody());
                    } else {
                        markup = getAdMarkup("<img src=\"" + placement.getImageUrl() + "\">");
                    }
                    adView.loadData(markup, "text/html; charset=utf-8", "UTF-8");

                    Log.d("Ads/AdButler", "Loading ad markup into view.");

                    // Fetch successful, record an impression.
                    placement.recordImpression();
                }
            }

            @Override
            public void error(Throwable throwable) {
                Log.d("Ads/AdButler", "Zone request error occurred.");
                mListener.onAdFetchFailed(AdButlerErrorCode.NETWORK_ERROR);
            }
        });
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * Destroy the banner.
     */
    public void destroy() {
        mListener = null;
    }
}
