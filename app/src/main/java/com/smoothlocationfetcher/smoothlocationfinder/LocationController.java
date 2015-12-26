package com.smoothlocationfetcher.smoothlocationfinder;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;

/**
 * Created by pradeep on 25/12/15.
 */
public class LocationController {

    private LocationParams params;
    private LocationConnection locationConnection;
    private boolean oneFix;

    public LocationController(Context context, LocationConnection locationConnection) {
        params = LocationParams.BEST_EFFORT;
        oneFix = false;
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
