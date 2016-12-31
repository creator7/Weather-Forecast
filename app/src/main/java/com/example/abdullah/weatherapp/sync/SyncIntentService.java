package com.example.abdullah.weatherapp.sync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Abdullah on 12/14/2016.
 */

public class SyncIntentService extends IntentService{

    public SyncIntentService() {
        super("SyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("Sync", "weather synced");
        SyncTask.syncWeather(this);
    }
}
