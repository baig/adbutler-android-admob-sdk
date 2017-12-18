package com.sparklit.adbutler;

import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

public class AdButlerDeviceInfo {

    public String language;
    public String manufacturer;
    public String model;
    public String osName;
    //public String osVariant;
    public String osVersion;
    public int screenWidth;
    public int screenHeight;
    public float screenPixelDensity;
    public Boolean isPhone;
    public Boolean isTablet;

    public void initialize() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();

        language = Locale.getDefault().toString();
        manufacturer = Build.MANUFACTURER;
        model = Build.MODEL;
        osName = "Android";
        //osVariant = Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
        osVersion = Build.VERSION.RELEASE;

        screenWidth = Math.round(displayMetrics.widthPixels / displayMetrics.density);
        screenHeight = Math.round(displayMetrics.heightPixels / displayMetrics.density);
        screenPixelDensity = displayMetrics.density;

        float yInches= displayMetrics.heightPixels/displayMetrics.ydpi;
        float xInches= displayMetrics.widthPixels/displayMetrics.xdpi;
        double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches);
        // A little fuzzy logic, will improve this further.
        isTablet = diagonalInches >= 6.5;
        isPhone = !isTablet;
    }
}
