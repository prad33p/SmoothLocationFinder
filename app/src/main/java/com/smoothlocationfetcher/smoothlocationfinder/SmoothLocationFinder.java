package com.smoothlocationfetcher.smoothlocationfinder;

import android.content.Context;

/**
 * Created by pradeep on 25/12/15.
 */
public class SmoothLocationFinder {
    private Context mContext;

    public SmoothLocationFinder(Context context) {
        mContext = context;
    }

    public static SmoothLocationFinder with(Context context) {
        return new Builder(context).build();
    }

    public LocationController fetchLocation(GooglePlayLocationServicesProvider locationServicesProvider) {
        return new LocationController(mContext, locationServicesProvider);
    }


    /*
    * Builder to initialize SmoothLocationFetcher.
    * */

    public static class Builder {
        private Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public SmoothLocationFinder build() {
            return new SmoothLocationFinder(context);
        }
    }

}
