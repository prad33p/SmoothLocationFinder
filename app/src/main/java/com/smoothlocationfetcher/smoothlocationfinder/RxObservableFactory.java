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

    public static Observable<Location> getLocation(final LocationController locationController) {

        if (locationController.getTimeOut() > 0) {
            return getLocationWithTimeout(locationController);
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

    /*Observable to fetch current location of user but with a timeout after which
     his last known location is returned if present*/

    public static Observable<Location> getLocationWithTimeout(final LocationController locationController) {
        return RxObservableFactory.getLocation(locationController)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(locationController.getTimeOut(), TimeUnit.SECONDS)
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends Location>>() {
                    @Override
                    public Observable<? extends Location> call(Throwable throwable) {
                        return getLastLocation(locationController);
                    }
                });
    }


    /***********
     * Observable to fetch last location of user.
     ***************************/

    public static Observable<Location> getLastLocation(final LocationController locationController) {
        return Observable.create(new Observable.OnSubscribe<Location>() {
            @Override
            public void call(Subscriber<? super Location> subscriber) {
                subscriber.onNext(locationController.getLastLocation());

            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /************ Observable to get location of user after some interval regularly ***************************/

    public static Observable<Location> getLocationAtInterval(final LocationController locationController, final int interval) {
        locationController.oneFix();

        return Observable.create(new Observable.OnSubscribe<Location>() {

            @Override
            public void call(final Subscriber<? super Location> subscriber) {
                Observable.interval(interval,TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(new Subscriber<Long>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG,"Complete");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG,"Error occured in getting location at interval.");
                            }

                            @Override
                            public void onNext(Long aLong) {

                                getLocation(locationController)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(Schedulers.io())
                                        .subscribe(new Subscriber<Location>() {
                                            @Override
                                            public void onCompleted() {

                                            }

                                            @Override
                                            public void onError(Throwable e) {

                                            }

                                            @Override
                                            public void onNext(Location location) {
                                                subscriber.onNext(location);
                                            }
                                        });

                            }
                        });
            }
        });

    }
}