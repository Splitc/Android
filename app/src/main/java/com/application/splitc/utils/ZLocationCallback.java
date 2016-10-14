package com.application.splitc.utils;

import android.location.Location;

/**
 * Created by apoorvarora on 10/10/16.
 */
public interface ZLocationCallback {
    public void onCoordinatesIdentified(Location loc);
    public void onLocationIdentified();
    public void onLocationNotIdentified();
    public void onDifferentCityIdentified();
    public void locationNotEnabled();
    public void onLocationTimedOut();
    public void onNetworkError();
}
