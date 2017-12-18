/*
 * Copyright (C) 2014 Sparklit Networks Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ads.mediation.sample.mediationsample;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.adbutler.android.admob.sdk.AdButlerCustomEventBanner;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sparklit.adbutler.AdButler;

import java.util.Calendar;
import java.util.Random;

/**
 * A simple {@link android.app.Activity} that displays adds using the sample adapter and sample
 * custom event.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Ads/AdButler", "In onCreate in MainActivity");

        // Required, initialize AdButler SDK first as early as possible in Main Activity.
        AdButler.initialize(this);

        // Example data.
        Calendar userBirthday = getUserBirthday();
        int userGender = getUserGender();
        Location userLocation = getUserLocation();

        // Optional extras
        Bundle AdButlerExtras = new Bundle();
        AdButlerExtras.putInt("age", 123);
        AdButlerExtras.putInt("yearOfBirth", 1234);
        AdButlerExtras.putInt("coppa", 1);


        // Sample custom event banner.
        AdView mCustomEventAdView = (AdView) findViewById(R.id.customevent_adview);
        AdRequest mCustomEventRequest = new AdRequest.Builder()
                //.addTestDevice("A62F5EE59078F0657513309CE9D874BF")
                .setBirthday(userBirthday.getTime())
                .setGender(userGender)
                .setLocation(userLocation)
                .addCustomEventExtrasBundle(AdButlerCustomEventBanner.class, AdButlerExtras)
                .build();

        mCustomEventAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d("Ads/AdButler", "Fired listener event onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.d("Ads/AdButler", "Fired listener event onAdFailedToLoad with code "+ errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.d("Ads/AdButler", "Fired listener event onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.d("Ads/AdButler", "Fired listener event onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                Log.d("Ads/AdButler", "Fired listener event onAdClosed");
            }
        });

        mCustomEventAdView.loadAd(mCustomEventRequest);

        Log.d("Ads/AdButler", "After mCustomEventAdView.loadAd() was called");
    }

    // dummy birthday
    public Calendar getUserBirthday() {
        Calendar birthdayCalendar = Calendar.getInstance();
        birthdayCalendar.set(Calendar.YEAR, 1980);
        birthdayCalendar.set(Calendar.MONTH, 1);
        birthdayCalendar.set(Calendar.DAY_OF_MONTH, 1);
        birthdayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        birthdayCalendar.set(Calendar.MINUTE, 0);
        birthdayCalendar.set(Calendar.SECOND, 0);
        birthdayCalendar.set(Calendar.MILLISECOND, 0);
        return birthdayCalendar;
    }

    // dummy gender
    public int getUserGender() {
        Random rand = new Random();
        int i = rand.nextInt(3);
        int[] genders = {AdRequest.GENDER_UNKNOWN, AdRequest.GENDER_MALE, AdRequest.GENDER_FEMALE};
        return genders[i];
    }

    public Location getUserLocation() {
        Location loc = new Location("Dummy");
        loc.setLatitude(37.4220);
        loc.setLongitude(122.0841);
        return loc;
    }
}
