package com.smoothlocationfetcher.smoothlocationfinder;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;

/**
 * Created by pradeep on 25/12/15.
 */
public class LocationController {

    private LocationParams params;
    private GooglePlayServicesLocationProvider locationProvider;
    private boolean oneFix;

    public LocationController(Context context, GooglePlayServicesLocationProvider locationProvider) {
        params = LocationParams.BEST_EFFORT;
        oneFix = false;
        this.locationProvider = locationProvider;

        if (locationProvider == null) {
            throw new RuntimeException("A provider must be initialized");
        } else {
            locationProvider.init(context);
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

    @Nullable
    public Location getLastLocation() {
        return locationProvider.getLastLocation();
    }

    public LocationController get() {
        return this;
    }

    public void start(OnLocationUpdatedListener listener) {
        locationProvider.start(listener, params, oneFix);
    }

    public void stop() {
        locationProvider.stop();
    }
}
