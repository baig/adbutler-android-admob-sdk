package com.adbutler.android.admob.sdk;

import java.util.Set;

/**
 * An AdButler AdMob SDK ad request used to load an ad.
 */
public class AdButlerAdRequest {

    /**
     * Creates a new {@link AdButlerAdRequest}.
     */
    public AdButlerAdRequest() {
    }

    /**
     * Sets keywords for targeting purposes.
     * @param keywords A set of keywords.
     */
    public void setKeywords(Set<String> keywords) {
        // Normally we'd save the keywords. But since this is a sample network, we'll do nothing.
    }

    /**
     * Designates a request for test mode.
     * @param useTesting {@code true} to enable test mode.
     */
    public void setTestMode(boolean useTesting) {
        // Normally we'd save this flag. But since this is a sample network, we'll do nothing.
    }

    public void setShouldAddAwesomeSauce(boolean shouldAddAwesomeSauce) {
        // Normally we'd save this flag but since this is a sample network, we'll do nothing.
    }

    public void setIncome(int income) {
        // Normally we'd save this value but since this is a sample network, we'll do nothing.
    }
}
