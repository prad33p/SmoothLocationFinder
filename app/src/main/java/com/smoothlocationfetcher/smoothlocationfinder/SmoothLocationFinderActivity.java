package com.smoothlocationfetcher.smoothlocationfinder;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.smoothlocationfetcher.smoothlocationfetcher.R;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SmoothLocationFinderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smooth_location_fetcher);

        final TextView latitude = (TextView) findViewById(R.id.latitude);
        final TextView longitude = (TextView) findViewById(R.id.longitude);

        RxObservableFactory.fetchLocation(SmoothLocationFinder.with(this).fetchLocation().continuous().config(LocationParams.NAVIGATION))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Location>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Location location) {
                        latitude.setText(Double.toString(location.getLatitude()));
                        longitude.setText(Double.toString(location.getLongitude()));
                    }
                });
    }
}