package com.sparklit.adbutler;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public class AdButlerAppInfo {

    public String packageName;
    public String appName;

    public void initialize(Context context) {
        appName = getApplicationName(context);
        packageName = context.getPackageName();
    }

    private String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
}
