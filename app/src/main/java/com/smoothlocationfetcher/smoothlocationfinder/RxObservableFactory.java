package com.smoothlocationfetcher.smoothlocationfinder;

import android.location.Location;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;


/**
 * Created by pradeep on 25/12/15.
 */
public class RxObservableFactory {

    private static final String TAG = RxObservableFactory.class.getSimpleName();

    public static Observable<Location> fetchLocation(final LocationController locationController) {
        return Observable.create(new Observable.OnSubscribe<Location>() {
            @Override
            public void call(final Subscriber<? super Location> subscriber) {
                locationController.start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        Log.d(TAG, "Location updated: " + location.toString());
                        subscriber.onNext(location);
                    }
                });
            }
        });
    }
}
