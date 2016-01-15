package com.smoothlocationfetcher.smoothlocationfinder;

import android.location.Location;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


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


    /*
     * Observable to fetch last location of user.
     */

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

    public static Observable<Location> getLocationAfterInterval(final LocationController locationController, final int interval) {
        final PublishSubject<Location> pollingSubject = PublishSubject.create();

        locationController.oneFix();
        locationController.timeOut(0);

        Observable<Location> locationObservable = Observable.create(new Observable.OnSubscribe<Location>() {
            @Override
            public void call(final Subscriber<? super Location> subscriber) {
                pollingSubject.subscribe(new Subscriber<Location>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Error while polling" + e.toString());
                    }

                    @Override
                    public void onNext(Location location) {
                        subscriber.onNext(location);
                        pollingLocation(locationController, pollingSubject, interval);
                    }
                });
            }
        });

        pollingLocation(locationController, pollingSubject, 0);

        return locationObservable;
    }


    private static void pollingLocation(LocationController locationController, final PublishSubject<Location> pollingSubject, int interval) {

        getLocation(locationController)
                .delay(interval, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Location>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Error in Polling : " + e.toString());
                    }

                    @Override
                    public void onNext(Location location) {
                        pollingSubject.onNext(location);
                    }
                });
    }

}