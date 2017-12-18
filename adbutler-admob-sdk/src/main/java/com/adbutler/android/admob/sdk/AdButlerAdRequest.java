package com.adbutler.android.admob.sdk;

import android.location.Location;

import java.util.Date;
import java.util.Set;

/**
 * An AdButler AdMob SDK ad request used to load an ad.
 */
public class AdButlerAdRequest {

    // Mediation data
    private Boolean isTestMode;
    private Date birthday;
    private int gender;
    private Location location;
    private int age = 0;
    private int yearOfBirth = 0;
    private int coppa = 0;

    /**
     * Creates a new {@link AdButlerAdRequest}.
     */
    public AdButlerAdRequest() {
    }

    /* SETTERS */

    /**
     * Sets keywords for targeting purposes.
     *
     * @param keywords A set of keywords.
     */
    public void setKeywords(Set<String> keywords) {
        // Normally we'd save the keywords. But since this is a sample network, we'll do nothing.
    }

    /**
     * Designates a request for test mode.
     *
     * @param useTesting {@code true} to enable test mode.
     */
    public void setTestMode(boolean useTesting) {
        this.isTestMode = useTesting;
    }

    /**
     * Sets the mediation location data.
     *
     * @param location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public int getCoppa() {
        return coppa;
    }

    public void setCoppa(int coppa) {
        this.coppa = coppa;
    }
}
