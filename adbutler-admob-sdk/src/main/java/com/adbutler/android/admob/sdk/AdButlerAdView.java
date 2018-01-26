package com.adbutler.android.admob.sdk;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.net.http.SslError;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.sparklit.adbutler.AdButler;
import com.sparklit.adbutler.AdButlerAppInfo;
import com.sparklit.adbutler.AdButlerDeviceInfo;
import com.sparklit.adbutler.AdButlerNetworkInfo;
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

    private AdButlerAdSize mAdSize;
    private AdButlerAdListener mListener;

    // AdButler data
    private Integer accountID;
    private Integer zoneID;
    private Integer zoneWidth;
    private Integer zoneHeight;

    private FrameLayout.LayoutParams calculatedLayout;

    /**
     * Create a new {@link AdButlerAdView}.
     *
     * @param context An Android {@link Context}.
     */
    public AdButlerAdView(Context context) {
        super(context);
    }

    /**
     * @param accountID The account ID.
     */
    public void setAccount(Integer accountID) {
        this.accountID = accountID;
    }

    /**
     * @param zoneID The zone ID to serve.
     */
    public void setZone(Integer zoneID) {
        this.zoneID = zoneID;
    }

    /**
     * @param zoneWidth The width of this zone.
     */
    public void setZoneWidth(Integer zoneWidth) {
        this.zoneWidth = zoneWidth;
    }

    /**
     * @param zoneHeight The height of this zone.
     */
    public void setZoneHeight(Integer zoneHeight) {
        this.zoneHeight = zoneHeight;
    }

    /**
     * Sets the size of the banner.
     *
     * @param size The banner size.
     */
    public void setSize(AdButlerAdSize size) {
        this.mAdSize = size;
    }

    /**
     * Sets a {@link AdButlerAdListener} to listen for ad events.
     *
     * @param listener The ad listener.
     */
    public void setAdListener(AdButlerAdListener listener) {
        this.mListener = listener;
    }

    /**
     * Get the actual ad markup.
     *
     * @param body The ad body.
     * @return String
     */
    public String getAdMarkup(String body) {
        return "<!DOCTYPE HTML><html><head><link rel=\"icon\" href=\"data:;base64,iVBORw0KGgo=\"><style>html,body{padding:0;margin:0;background:transparent;}iframe{border:0;overflow:none;}a{outline:0;-webkit-tap-highlight-color:transparent;}</style></head><body>"
                + body + "</body></html>";
    }

    /**
     * Fetch an ad from AdButler.
     *
     * @param request The ad request with targeting information.
     */
    public void fetchAd(AdButlerAdRequest request) {
        final AdButlerAdView adView = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("Ads/AdButler", "Registering debugging events on the WebView component.");
            this.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Log.d("Ads/AdButler", "onPageStarted: " + url);
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.d("Ads/AdButler", "onPageFinished: " + url);
                    super.onPageFinished(view, url);
                }

                @Override
                public void onLoadResource(WebView view, String url) {
                    Log.d("Ads/AdButler", "Loading URL: " + url);
                    super.onLoadResource(view, url);
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Log.d("Ads/AdButler", "onReceivedError: " + failingUrl);
                    Log.d("Ads/AdButler", "onReceivedError Error: " + errorCode + ", " + description);
                    super.onReceivedError(view, errorCode, description, failingUrl);
                }

                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    Log.d("Ads/AdButler", "onReceivedError: " + request.getUrl());
                    Log.d("Ads/AdButler", "onReceivedError Error: " + error.getErrorCode() + ", " + error.getDescription());
                    super.onReceivedError(view, request, error);
                }

                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                    Log.d("Ads/AdButler", "onReceivedHttpError: " + request.getUrl());
                    Log.d("Ads/AdButler", "onReceivedHttpError Status: " + errorResponse.getStatusCode());
                    super.onReceivedHttpError(view, request, errorResponse);
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    Log.d("Ads/AdButler", "onReceivedSslError: " + error.getUrl());
                    Log.d("Ads/AdButler", "onReceivedSslError Status: " + error.getPrimaryError());
                    super.onReceivedSslError(view, handler, error);
                }
            });
        }

        AdButler AdButlerSDK = AdButler.getInstance();
        AdButlerSDK.setApiHostname("adbutler-fermion.com");
        AdButlerSDK.setApiAppVersion("adserve-p");

        Log.d("Ads/AdButler", "In AdButlerAdView.fetchAd()");
        if (mListener == null) {
            Log.e("Ads/AdButler", "Ad listener was not set, do NOT proceed. Terminating AdButler ad request.");
            return;
        }

        if (this.accountID == 0 || this.zoneID == 0 || mAdSize == null) {
            mListener.onAdFetchFailed(AdButlerErrorCode.BAD_REQUEST);
            return;
        }

        // Gather required info now that we know we need it.

        AdButlerAppInfo appInfo = new AdButlerAppInfo();
        appInfo.initialize(getContext());

        AdButlerDeviceInfo deviceInfo = new AdButlerDeviceInfo();
        deviceInfo.initialize();

        AdButlerNetworkInfo networkInfo = new AdButlerNetworkInfo();
        networkInfo.initialize(getContext());

        // Calculate the layout width and height
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        Integer actualRenderWidth = Math.round(mAdSize.getWidth() * displayMetrics.density);
        Integer actualRenderHeight = Math.round(mAdSize.getHeight() * displayMetrics.density);
        Log.d("Ads/AdButler", "Layout: actualRenderWidth=" + actualRenderWidth + ", actualRenderHeight=" + actualRenderHeight);
        this.calculatedLayout = new FrameLayout.LayoutParams(actualRenderWidth, actualRenderHeight);

        // WebView settings
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");

        // Disable scrolling
        this.setScrollContainer(false);
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalScrollBarEnabled(false);


        //
        // PASS TO ADBUTLER
        //

        Calendar nowInstance = Calendar.getInstance();
        int currentYear = nowInstance.get(Calendar.YEAR);

        Location loc;
        Calendar birthdayCalendar = null;
        int age = 0;
        int yearOfBirth = 0;

        if (null != request.getBirthday()) {
            birthdayCalendar = Calendar.getInstance();
            birthdayCalendar.setTime(request.getBirthday());
        }

        //
        loc = request.getLocation();

        //
        if (request.getAge() > 0) {
            age = request.getAge();
        }
        if (request.getYearOfBirth() > 0) {
            yearOfBirth = request.getYearOfBirth();
        }

        // try to fill age in through secondary methods
        if (age <= 0) {
            if (yearOfBirth > 0) {
                age = currentYear - yearOfBirth;
            } else if (null != birthdayCalendar) {
                age = currentYear - birthdayCalendar.get(Calendar.YEAR);
            }
        }
        // try to fill in year of birth through secondary methods
        if (yearOfBirth <= 0) {
            if (age > 0) {
                yearOfBirth = currentYear - age;
            } else if (null != birthdayCalendar) {
                yearOfBirth = birthdayCalendar.get(Calendar.YEAR);
            }
        }


        // Begin request build.
        PlacementRequestConfig.Builder requestBuilder = new PlacementRequestConfig.Builder(this.accountID, this.zoneID, this.zoneWidth, this.zoneHeight);

        // Advertising ID & DNT
        if (null != AdButler.AdvertisingInfo.advertisingId) {
            requestBuilder
                    .setAdvertisingId(AdButler.AdvertisingInfo.advertisingId)
                    .setDoNotTrack(AdButler.AdvertisingInfo.limitAdTrackingEnabled ? 1 : 0);
        }

        // Location
        if (loc != null) {
            requestBuilder.setLatitude(loc.getLatitude());
            requestBuilder.setLongitude(loc.getLongitude());
        }

        // Age & year of birth
        if (age > 0) {
            requestBuilder.setAge(age);
        }
        switch (request.getGender()) {
            case AdRequest.GENDER_MALE:
                requestBuilder.setGender("male");
                break;
            case AdRequest.GENDER_FEMALE:
                requestBuilder.setGender("female");
                break;
            default:
            case AdRequest.GENDER_UNKNOWN:
                requestBuilder.setGender("unknown");
                break;
        }
        if (yearOfBirth > 0) {
            requestBuilder.setYearOfBirth(yearOfBirth);
        }

        // App
        requestBuilder.setAppName(appInfo.appName);
        requestBuilder.setAppPackageName(appInfo.packageName);

        // Device
        requestBuilder.setLanguage(deviceInfo.language);
        requestBuilder.setOsName(deviceInfo.osName);
        requestBuilder.setOsVersion(deviceInfo.osVersion);
        requestBuilder.setDeviceType(deviceInfo.isTablet ? "tablet" : "phone");
        requestBuilder.setDeviceModel(deviceInfo.model);
        requestBuilder.setDeviceManufacturer(deviceInfo.manufacturer);
        requestBuilder.setScreenWidth(deviceInfo.screenWidth);
        requestBuilder.setScreenHeight(deviceInfo.screenHeight);
        requestBuilder.setScreenPixelDensity(deviceInfo.screenPixelDensity);

        // Network
        requestBuilder.setNetworkClass(networkInfo.networkClass);
        requestBuilder.setCarrierCountryIso(networkInfo.carrierCountryIso);
        requestBuilder.setCarrier(networkInfo.carrierName);
        requestBuilder.setCarrierCode(networkInfo.carrierCode);

        // Compliance
        requestBuilder.setCoppa(request.getCoppa());

        // Finalize request config build.
        final PlacementRequestConfig config = requestBuilder.build();


        Log.d("Ads/AdButler", "Requesting ad from AdButler...");

        AdButlerSDK.requestPlacement(config, new PlacementResponseListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void success(PlacementResponse response) {
                for (Placement placement : response.getPlacements()) {
                    Log.d("Ads/AdButler", "BannerID: " + placement.getBannerId());
                }

                if (null == adView.mListener) {
                    Log.e("Ads/AdButler", "Ad listener was null despite the earlier check.");
                    return;
                }

                Placement placement = null;
                if (response.getPlacements().size() > 0) {
                    placement = response.getPlacements().get(0);
                }


                if (null == placement) {
                    adView.mListener.onAdFetchFailed(AdButlerErrorCode.NO_INVENTORY);

                } else {
                    // Register the selected placement.
                    adView.placement = placement;

                    final String placementRedirectURL = placement.getRedirectUrl();

                    // Set up the OnTouchListener
                    adView.setOnTouchListener(new View.OnTouchListener() {
//                        private static final int MAX_CLICK_DURATION = 200;
//                        private long clickStartTime;

                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            boolean preventTouch = false;

                            switch (motionEvent.getAction()) {
//                                case MotionEvent.ACTION_DOWN:
//                                    clickStartTime = Calendar.getInstance().getTimeInMillis();
//                                    preventTouch = true;
//                                    break;
//
//                                case MotionEvent.ACTION_UP:
//                                    long clickDuration = Calendar.getInstance().getTimeInMillis() - clickStartTime;
//                                    if (clickDuration < MAX_CLICK_DURATION) {
//                                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                                        intent.setData(Uri.parse(placementRedirectURL));
//                                        adView.getContext().startActivity(intent);
//                                        preventTouch = false;
//                                    }
//                                    break;

                                case MotionEvent.ACTION_MOVE:
                                    preventTouch = true;
                                    break;
                            }

                            return preventTouch;
                        }
                    });

                    // Load the ad markup into the view
                    String markup = "";
                    if (placement.getBody().length() > 0) {
                        markup = getAdMarkup(placement.getBody());
                    } else {
                        String markupBody = "";
                        if (placementRedirectURL.length() > 0) {
                            markupBody += "<a href=\"" + placementRedirectURL + "\" target=\"_blank\">";
                        }
                        markupBody += "<img src=\"" + placement.getImageUrl() + "\">";
                        if (placementRedirectURL.length() > 0) {
                            markupBody += "</a>";
                        }
                        markup = getAdMarkup(markupBody);
                    }
                    //adView.loadData(markup, "text/html; charset=utf-8", "UTF-8");
                    //adView.loadDataWithBaseURL("http://servedbyadbutler.com/placeholder.html", markup, "text/html", "US-ASCII", null);
                    adView.loadDataWithBaseURL("http://servedbyadbutler.com/placeholder.html", markup, "text/html; charset=utf-8", "UTF-8", null);

                    Log.d("Ads/AdButler", "Loading ad markup into view.");

                    // Fetch successful, record an impression.
                    placement.recordImpression();

                    // Register successful ad fetch.
                    adView.mListener.onAdFetchSucceeded();
                }
            }

            @Override
            public void error(Throwable throwable) {
                Log.d("Ads/AdButler", "Zone request error occurred.");
                adView.mListener.onAdFetchFailed(AdButlerErrorCode.NETWORK_ERROR);
            }
        });
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Before final draw, set the calculated layout to correctly indicate the dimensions of the ad.
        if (null != this.calculatedLayout) {
            this.setLayoutParams(this.calculatedLayout);
        }
    }

    /**
     * Destroy the banner.
     */
    public void destroy() {
        mListener = null;
    }
}
