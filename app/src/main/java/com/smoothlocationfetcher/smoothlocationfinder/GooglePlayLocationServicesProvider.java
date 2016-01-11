package com.smoothlocationfetcher.smoothlocationfinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import rx.subjects.PublishSubject;

/**
 * Created by pradeep on 25/12/15.
 */

public class GooglePlayLocationServicesProvider implements LocationConnection, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    private static final String TAG = GooglePlayLocationServicesProvider.class.getSimpleName();
    public static final int REQUEST_START_LOCATION_FIX = 10001;
    public static final int REQUEST_CHECK_SETTINGS = 20001;
    private static final String GMS_ID = "GMS";

    private GoogleApiClient client;
    private OnLocationUpdatedListener listener;
    private boolean shouldStart = false;
    private boolean stopped = false;
    private LocationStore locationStore;
    private LocationRequest locationRequest;
    private Context context;
    private final GooglePlayServicesListener googlePlayServicesListener;
    private boolean checkLocationSettings;
    private boolean fulfilledCheckLocationSettings;

    private static PublishSubject<LocationServicesStatus> locationServicesStatusPublishSubject = PublishSubject.create();
    private static PublishSubject<Boolean> locationDialogActionPublishSubject = PublishSubject.create();

    public GooglePlayLocationServicesProvider() {
        this(null);
    }

    public GooglePlayLocationServicesProvider(GooglePlayServicesListener playServicesListener) {
        googlePlayServicesListener = playServicesListener;
        checkLocationSettings = false;
        fulfilledCheckLocationSettings = false;
    }

    @Override
    public void init(Context context) {
        this.context = context;

        locationStore = new LocationStore(context);

        if (!shouldStart) {
            this.client = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            client.connect();
        } else {
            Log.d(TAG,"already started");
        }
    }

    private LocationRequest createRequest(LocationParams params, boolean singleUpdate) {
        LocationRequest request = LocationRequest.create()
                .setFastestInterval(params.getInterval())
                .setInterval(params.getInterval())
                .setSmallestDisplacement(params.getDistance());

        switch (params.getAccuracy()) {
            case HIGH:
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                break;
            case MEDIUM:
                request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            case LOW:
                request.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                break;
            case LOWEST:
                request.setPriority(LocationRequest.PRIORITY_NO_POWER);
                break;
        }

        if (singleUpdate) {
            request.setNumUpdates(1);
        }

        return request;
    }

    @Override
    public void start(OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
        this.listener = listener;
        if (listener == null) {
            Log.d(TAG,"Listener is null, you sure about this?");
        }
        locationRequest = createRequest(params, singleUpdate);

        if (client.isConnected()) {
            startUpdating(locationRequest);
        } else if (stopped) {
            shouldStart = true;
            client.connect();
            stopped = false;
        } else {
            shouldStart = true;
            Log.d(TAG,"still not connected - scheduled start when connection is ok");
        }
    }

    private void startUpdating(LocationRequest request) {
        // TODO wait until the connection is done and retry
        if (checkLocationSettings && !fulfilledCheckLocationSettings) {
            Log.d(TAG,"startUpdating wont be executed for now, as we have to test the location settings before");
            checkLocationSettings();
            return;
        }
        if (client.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this).setResultCallback(this);
        } else {
            Log.d(TAG,"startUpdating executed without the GoogleApiClient being connected!!");
        }
    }

    private void checkLocationSettings() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        LocationServices.SettingsApi.checkLocationSettings(client, request).setResultCallback(settingsResultCallback);
    }

    @Override
    public void stop() {
        Log.d(TAG,"stop");
        if (client.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
            client.disconnect();
        }
        fulfilledCheckLocationSettings = false;
        shouldStart = false;
        stopped = true;
    }

    @Override
    public Location getLastLocation() {
        if (client != null && client.isConnected()) {
            return LocationServices.FusedLocationApi.getLastLocation(client);
        }

        if (locationStore != null) {
            Location location = locationStore.get(GMS_ID);
            if (location != null) {
                return location;
            }
        }

        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG,"onConnected");
        if (shouldStart) {
            startUpdating(locationRequest);
        }

        if (googlePlayServicesListener != null) {
            googlePlayServicesListener.onConnected(bundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"onConnectionSuspended " + i);
        if (googlePlayServicesListener != null) {
            googlePlayServicesListener.onConnectionSuspended(i);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectionFailed " + connectionResult.toString());
        if (googlePlayServicesListener != null) {
            googlePlayServicesListener.onConnectionFailed(connectionResult);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"onLocationChanged" + location.toString());

        if (listener != null) {
            listener.onLocationUpdated(location);
        }

        if (locationStore != null) {
            Log.d(TAG,"Stored in SharedPreferences");
            locationStore.put(GMS_ID, location);
        }
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.d(TAG,"Locations update request successful");

        } else if (status.hasResolution() && context instanceof Activity) {
            Log.d(TAG,
                    "Unable to register, but we can solve this - will startActivityForResult. You should hook into the Activity onActivityResult and call this provider onActivityResult method for continuing this call flow.");
            try {
                status.startResolutionForResult((Activity) context, REQUEST_START_LOCATION_FIX);
            } catch (IntentSender.SendIntentException e) {
                Log.d(TAG,"problem with startResolutionForResult" + e.toString());
            }
        } else {
            // No recovery. Weep softly or inform the user.
            Log.d(TAG,"Registering failed: " + status.getStatusMessage());
        }
    }

    /**
     * @return TRUE if active, FALSE if the settings wont be checked before launching the location updates request
     */
    public boolean isCheckingLocationSettings() {
        return checkLocationSettings;
    }

    /**
     * Sets whether or not we should request (before starting updates) the availability of the
     * location settings and act upon it.
     *
     * @param allowingLocationSettings TRUE to show the dialog if needed, FALSE otherwise (default)
     */
    public void setCheckLocationSettings(boolean allowingLocationSettings) {
        this.checkLocationSettings = allowingLocationSettings;
    }

    /**
     * This method should be called in the onActivityResult of the calling activity whenever we are
     * trying to implement the Check Location Settings fix dialog.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.d(TAG,"User agreed to make required location settings changes.");
                    fulfilledCheckLocationSettings = true;
                    startUpdating(locationRequest);
                    break;
                case Activity.RESULT_CANCELED:
                    Log.d(TAG,"User chose not to make required location settings changes.");
                    stop();
                    break;
            }
        } else if (requestCode == REQUEST_START_LOCATION_FIX) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.d(TAG,"User fixed the problem.");
                    startUpdating(locationRequest);
                    break;
                case Activity.RESULT_CANCELED:
                    Log.d(TAG,"User chose not to fix the problem.");
                    stop();
                    break;
            }
        }
    }

    private ResultCallback<LocationSettingsResult> settingsResultCallback = new ResultCallback<LocationSettingsResult>() {
        @Override
        public void onResult(LocationSettingsResult locationSettingsResult) {
            final Status status = locationSettingsResult.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    Log.d(TAG,"All location settings are satisfied.");
                    locationServicesStatusPublishSubject.onNext(LocationServicesStatus.ENABLED);
                    fulfilledCheckLocationSettings = true;
                    startUpdating(locationRequest);
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.d(TAG,"Location settings are not satisfied. Show the user a dialog to" +
                            "upgrade location settings. You should hook into the Activity onActivityResult and call this provider onActivityResult method for continuing this call flow. ");
                    locationServicesStatusPublishSubject.onNext(LocationServicesStatus.DISABLED);

                    if (context instanceof Activity) {
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult((Activity) context, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.d(TAG,"PendingIntent unable to execute request.");
                        }

                    } else {
                        Log.d(TAG,"Provided context is not the context of an activity, therefore we cant launch the resolution activity.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.d(TAG,"Location settings are inadequate, and cannot be fixed here. Dialog " +
                            "not created.");
                    locationServicesStatusPublishSubject.onNext(LocationServicesStatus.HELPLESS);
                    stop();
                    break;
            }
        }
    };


}