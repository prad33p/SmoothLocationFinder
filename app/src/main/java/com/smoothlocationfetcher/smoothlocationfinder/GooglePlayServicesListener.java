package com.smoothlocationfetcher.smoothlocationfinder;

/**
 * Created by pradeep on 25/12/15.
 */

public interface GooglePlayServicesListener {
    void onConnected(android.os.Bundle bundle);

    void onConnectionSuspended(int i);

    void onConnectionFailed(com.google.android.gms.common.ConnectionResult connectionResult);

}

