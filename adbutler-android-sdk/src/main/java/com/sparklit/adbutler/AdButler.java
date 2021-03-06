package com.sparklit.adbutler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Makes requests against the AdButler API.
 */
public class AdButler {

    private String apiHostname = "servedbyadbutler.com";
    private String apiAppVersion = "adserve";

    private APIService service;

    private static AdButler instance;

    public static AdButler getInstance() {
        if (null == instance) {
            instance = new AdButler();
        }
        return instance;
    }

    public static void initialize(Context context) {
        AdButler AdButlerSDK = AdButler.getInstance();
        AdButlerSDK.loadAdvertisingInfoViaTask(context);
    }

    public void setApiHostname(String apiHostname) {
        this.apiHostname = apiHostname;
    }

    public void setApiAppVersion(String apiAppVersion) {
        this.apiAppVersion = apiAppVersion;
    }

    /**
     * Requests a pixel.
     *
     * @param url the URL string for this pixel.
     */
    public void requestPixel(String url) {
        Call<ResponseBody> call = getAPIService().requestPixel(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println(response.isSuccessful());
                // :)
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                // :)
            }
        });
    }

    /**
     * Requests a single placement.
     *
     * @param config   the configuration used for requesting one placement.
     * @param listener the results, when ready, will be given to this given listener.
     */
    public void requestPlacement(PlacementRequestConfig config, final PlacementResponseListener listener) {
        Call<PlacementResponse> call = getAPIService().requestPlacement(buildConfigParam(config));
        call.enqueue(new Callback<PlacementResponse>() {
            @Override
            public void onResponse(Call<PlacementResponse> call, Response<PlacementResponse> response) {
                listener.success(response.body());
            }

            @Override
            public void onFailure(Call<PlacementResponse> call, Throwable t) {
                listener.error(t);
            }
        });
    }

    /**
     * Requests multiple placements.
     *
     * @param configs  the configurations, each used for one placement respectively.
     * @param listener the results, when ready, will be given to this given listener.
     */
    public void requestPlacements(List<PlacementRequestConfig> configs, final PlacementResponseListener listener) {
        final List<Call<PlacementResponse>> calls = new ArrayList<>();
        for (PlacementRequestConfig config : configs) {
            calls.add(getAPIService().requestPlacement(buildConfigParam(config)));
        }
        final List<PlacementResponse> responses = new ArrayList<>();
        final List<Throwable> throwables = new ArrayList<>();
        for (Call<PlacementResponse> call : calls) {
            call.enqueue(new Callback<PlacementResponse>() {
                @Override
                public void onResponse(Call<PlacementResponse> call, Response<PlacementResponse> response) {
                    responses.add(response.body());
                    checkResults(listener, calls, responses, throwables);
                }

                @Override
                public void onFailure(Call<PlacementResponse> call, Throwable t) {
                    throwables.add(t);
                    checkResults(listener, calls, responses, throwables);
                }
            });
        }
    }

    private void checkResults(final PlacementResponseListener listener,
                                    List<Call<PlacementResponse>> calls,
                                    List<PlacementResponse> responses,
                                    List<Throwable> throwables) {
        if (responses.size() + throwables.size() != calls.size()) {
            return;
        }

        if (!throwables.isEmpty()) {
            listener.error(throwables.get(0));
            return;
        }

        List<Placement> placements = new ArrayList<>();
        for (PlacementResponse response : responses) {
            if (response.getStatus().equals("SUCCESS")) {
                placements.addAll(response.getPlacements());
            }
        }
        String status = placements.isEmpty() ? "NO_ADS" : "SUCCESS";
        PlacementResponse placementResponse = new PlacementResponse();
        placementResponse.setStatus(status);
        placementResponse.setPlacements(placements);

        listener.success(placementResponse);
    }


    private APIService getAPIService() {
        if (service == null) {
            String baseUrl = "https://" + this.apiHostname + "/" + this.apiAppVersion + "/";
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson));
            service = builder.build().create(APIService.class);
        }

        return service;
    }

    private String buildConfigParam(PlacementRequestConfig config) {
        String urlString = String.format(";ID=%d;size=%dx%d;setID=%d",
                config.getAccountId(),
                config.getWidth(),
                config.getHeight(),
                config.getZoneId());

        urlString += ";type=json";

        if (config.getKeywords() != null && config.getKeywords().size() > 0) {
            String keywordsQuery = null;
            for (String keyword : config.getKeywords()) {
                if (keywordsQuery == null) {
                    keywordsQuery = ";kw=" + encodeParam(keyword);
                } else {
                    keywordsQuery += "," + encodeParam(keyword);
                }
            }
            urlString += keywordsQuery;
        }

        // User Agent
        if (config.getUserAgent() != null && config.getUserAgent().length() > 0) {
            urlString += ";ua=" + encodeParam(config.getUserAgent());
        }

        // IP
        if (config.getIp() != null) {
            urlString += ";ip=" + encodeParam(config.getIp());
        }

        // Advertising ID
        if (config.getAdvertisingId() != null) {
            urlString += ";aduid=" + encodeParam(config.getAdvertisingId());
            urlString += ";dnt=" + config.getDoNotTrack();
        }

        // Location
        if (config.getLatitude() != null && config.getLongitude() != null) {
            if (config.getLatitude() > 0) {
                urlString += ";lat=" + encodeParam(config.getLatitude().toString());
            }
            if (config.getLongitude() > 0) {
                urlString += ";long=" + encodeParam(config.getLongitude().toString());
            }
        }

        // User
        if (config.getAge() > 0) {
            urlString += ";age="+config.getAge();
        }
        if (config.getYearOfBirth() > 0) {
            urlString += ";yob="+config.getYearOfBirth();
        }
        if (config.getGender() != null) {
            urlString += ";gender="+encodeParam(config.getGender());
        }

        // Device
        if (config.getDeviceType() != null) {
            urlString += ";dvtype=" + encodeParam(config.getDeviceType());
        }
        if (config.getDeviceManufacturer() != null) {
            urlString += ";dvmake=" + encodeParam(config.getDeviceManufacturer());
        }
        if (config.getDeviceModel() != null) {
            urlString += ";dvmodel=" + encodeParam(config.getDeviceModel());
        }
        if (config.getOsName() != null) {
            urlString += ";os=" + encodeParam(config.getOsName());
        }
        if (config.getOsVersion() != null) {
            urlString += ";osv=" + encodeParam(config.getOsVersion());
        }
        if (config.getLanguage() != null) {
            urlString += ";lang=" + encodeParam(config.getLanguage());
        }

        // Screen
        if (config.getScreenWidth() > 0) {
            urlString += ";sw=" + config.getScreenWidth();
        }
        if (config.getScreenWidth() > 0) {
            urlString += ";sh=" + config.getScreenHeight();
        }
        if (config.getScreenPixelDensity() > 0) {
            urlString += ";spr=" + config.getScreenPixelDensity();
        }

        // Network
        if (config.getNetworkClass() != null) {
            urlString += ";network=" + encodeParam(config.getNetworkClass());
        }
        if (config.getCarrier() != null) {
            urlString += ";carrier="+ encodeParam(config.getCarrier());
        }
        if (config.getCarrierCode() != null) {
            urlString += ";carriercode="+ encodeParam(config.getCarrierCode());
        }
        if (config.getCarrierCountryIso() != null) {
            urlString += ";carriercountry="+ encodeParam(config.getCarrierCountryIso());
        }

        // App
        if (null != config.getAppName()) {
            urlString += ";appname=" + encodeParam(config.getAppName());
        }
        if (null != config.getAppPackageName()) {
            urlString += ";appcode=" + encodeParam(config.getAppPackageName());
        }
        if (null != config.getAppVersion()) {
            urlString += ";appversion=" + encodeParam(config.getAppVersion());
        }

        // Flags
        urlString += ";coppa="+config.getCoppa();

        if (config.getClick() != null) {
            urlString += ";click=" + encodeParam(config.getClick());
        }

        Log.d("Ads/AdButler", "QUERY: " + urlString);

        return urlString;
    }

    private String encodeParam(String param) {
        return URLEncoder.encode(param).replaceAll("\\+", "%20");
    }











    public static AdvertisingInfo AdvertisingInfo;

    public static class AdvertisingInfo {
        public final String advertisingId;
        public final boolean limitAdTrackingEnabled;

        public AdvertisingInfo(String advertisingId, boolean limitAdTrackingEnabled) {
            this.advertisingId = advertisingId;
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }
    }

    private void loadAdvertisingInfoViaTask(Context context) {
        AdvertisingInfo = new AdvertisingInfo(null, false);

        AdvertisingInfoLoadTask task = new AdvertisingInfoLoadTask(context);
        task.execute();
    }

    private void setAdvertisingInfo(String advertisingId, Boolean limitAdTracking) {
        AdvertisingInfo = new AdvertisingInfo(advertisingId, limitAdTracking);
    }

    private static class AdvertisingInfoLoadTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<Context> weakRefContext;

        public AdvertisingInfoLoadTask(Context context) {
            weakRefContext = new WeakReference<Context>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AdvertisingIdClient.Info advertisingIdInfo = null;
            AdButler AdButlerSDK = AdButler.getInstance();
            try {
                Context context = weakRefContext.get();
                advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (null != advertisingIdInfo) {
                AdButlerSDK.setAdvertisingInfo(advertisingIdInfo.getId(), advertisingIdInfo.isLimitAdTrackingEnabled());
            } else {
                Log.d("Ads/AdButler", "Unable to retrieve the AdvertisingIdClient.Info data.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void voidValue) {
            // do nothing right now
        }
    }
}
