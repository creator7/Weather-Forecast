package com.example.abdullah.weatherapp.sync;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;

/**
 * Created by Abdullah on 12/19/2016.
 */

public class FirebaseJobService extends com.firebase.jobdispatcher.JobService {

    Thread mFetchWeatherData;
    @Override
    public boolean onStartJob(final com.firebase.jobdispatcher.JobParameters job) {
        mFetchWeatherData = new Thread(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                SyncTask.syncWeather(context);
                jobFinished(job, false);

            }
        });

        mFetchWeatherData.start();
        return true;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        if(mFetchWeatherData != null){
            mFetchWeatherData.interrupt();
            mFetchWeatherData = null;
        }
        return true;
    }
}
