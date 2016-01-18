package com.smoothlocationfetcher.smoothlocationfinder;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.smoothlocationfetcher.smoothlocationfetcher.R;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SmoothLocationFinderActivity extends AppCompatActivity {
    private static final String TAG = SmoothLocationFinderActivity.class.getSimpleName();
    private GooglePlayLocationServicesProvider googlePlayLocationServicesProvider;
    private LocationController locationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smooth_location_fetcher);

        final TextView latitude = (TextView) findViewById(R.id.latitude);
        final TextView longitude = (TextView) findViewById(R.id.longitude);

        googlePlayLocationServicesProvider = new GooglePlayLocationServicesProvider();

        Log.d(TAG, googlePlayLocationServicesProvider.toString());

        locationController = SmoothLocationFinder.with(this).fetchLocation(googlePlayLocationServicesProvider)
                .continuous()
//                .enableLocationSettings()
                .config(LocationParams.NAVIGATION);

        /*RxObservableFactory.getLocation(locationController)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Location>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.toString());

                    }

                    @Override
                    public void onNext(Location location) {
                        latitude.setText(Double.toString(location.getLatitude()));
                        longitude.setText(Double.toString(location.getLongitude()));
                        Log.d(TAG, "Location updated.");
                    }
                });*/


        //Pass the interval in milliseconds.
        RxObservableFactory.getLocationAfterInterval(locationController, 20000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Location>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Error in getting location after interval" + e.toString());

                    }

                    @Override
                    public void onNext(Location location) {
                        latitude.setText(Double.toString(location.getLatitude()));
                        longitude.setText(Double.toString(location.getLongitude()));
                        Log.d(TAG, "Location updated." + location.toString());
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationController.getGooglePlayLocationServicesProvider().onActivityResult(requestCode, resultCode, data);
    }
}
