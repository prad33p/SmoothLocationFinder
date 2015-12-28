package com.smoothlocationfetcher.smoothlocationfinder;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;

/**
 * Created by pradeep on 25/12/15.
 */

/*
 *LocationController class controls all the settings used while getting location such as getting lastLocation, timeOut,
 *continuous location updates or just one time location and showLocation popup .
 */

public class LocationController {

    private LocationParams params;
    private LocationConnection locationConnection;
    private boolean oneFix;
    private boolean enableLocationSettings;
    private int timeOut;

    public LocationController(Context context, LocationConnection locationConnection) {
        params = LocationParams.BEST_EFFORT;
        oneFix = false;
        timeOut = 0;
        this.locationConnection = locationConnection;

        if (locationConnection == null) {
            throw new RuntimeException("A provider must be initialized");
        } else {
            locationConnection.init(context);
        }
    }

    public LocationController config(LocationParams params) {
        this.params = params;
        return this;
    }

    public LocationController oneFix() {
        this.oneFix = true;
        return this;
    }

    public LocationController continuous() {
        this.oneFix = false;
        return this;
    }

    public LocationController enableLocationSettings() {
        this.enableLocationSettings = true;
        return this;
    }

    public LocationController timeOut(int timeOut) {
        if (timeOut < 0) {
            this.timeOut = 0;
        } else {
            this.timeOut = timeOut;
        }
        return this;
    }

    public int getTimeOut() {
        return this.timeOut;
    }

    @Nullable
    public Location getLastLocation() {
        return locationConnection.getLastLocation();
    }

    public LocationController get() {
        return this;
    }

    public void start(OnLocationUpdatedListener listener) {
        locationConnection.start(listener, params, oneFix);
    }

    public void stop() {
        locationConnection.stop();
    }
}
