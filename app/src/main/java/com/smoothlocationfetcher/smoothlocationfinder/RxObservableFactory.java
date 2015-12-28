package com.smoothlocationfetcher.smoothlocationfinder;

import android.location.Location;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by pradeep on 25/12/15.
 */
public class RxObservableFactory {

    private static final String TAG = RxObservableFactory.class.getSimpleName();

    public static Observable<Location> fetchLocation(final LocationController locationController) {

        if (locationController.getTimeOut() > 0) {
            return fetchLocationWithTimeout(locationController);
        }

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


    public static final Observable<Location> fetchLocationWithTimeout(final LocationController locationController) {
        return RxObservableFactory.fetchLocation(locationController)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(locationController.getTimeOut(), TimeUnit.SECONDS)
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends Location>>() {
                    @Override
                    public Observable<? extends Location> call(Throwable throwable) {
                        return fetchLastLocation(locationController);
                    }
                });
    }

    private static Observable<? extends Location> fetchLastLocation(final LocationController locationController) {
        return Observable.create(new Observable.OnSubscribe<Location>() {
            @Override
            public void call(Subscriber<? super Location> subscriber) {
                subscriber.onNext(locationController.getLastLocation());

            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}