package com.smoothlocationfetcher.smoothlocationfinder;

import android.content.Context;
import android.location.Location;

/**
 * Created by pradeep on 25/12/15.
 */
public interface LocationConnection {

    public void init(Context context);

    public void start(OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate);

    public void stop();

    public Location getLastLocation();

}
