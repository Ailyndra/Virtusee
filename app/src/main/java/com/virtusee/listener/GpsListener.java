package com.virtusee.listener;

import android.location.Location;

public interface GpsListener {
    public void onGpsSet(Location location);
    public void onGpsMock();
    public void onGpsOff();
    public void onGpsSearch();
}
