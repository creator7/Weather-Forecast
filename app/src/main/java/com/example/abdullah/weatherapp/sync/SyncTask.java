package com.example.abdullah.weatherapp.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.abdullah.weatherapp.MainActivity;
import com.example.abdullah.weatherapp.data.Preferences;
import com.example.abdullah.weatherapp.data.WeatherContract;
import com.example.abdullah.weatherapp.utilities.NetworkUtils;
import com.example.abdullah.weatherapp.utilities.NotificationUtils;
import com.example.abdullah.weatherapp.utilities.OpenWeatherJsonUtils;

import org.json.JSONException;

import java.net.URL;

/**
 * Created by Abdullah on 12/14/2016.
 */

public class SyncTask {

    synchronized public static void syncWeather(final Context context) {

        URL weatherRequestUrl = NetworkUtils.getUrl(context);

        StringRequest request = new StringRequest(Request.Method.GET, weatherRequestUrl.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    ContentValues[] weatherValues = OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context, response);

                    if (weatherValues != null && weatherValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                        ContentResolver contentResolver = context.getContentResolver();

//              COMPLETED (4) If we have valid results, delete the old data and insert the new
                /* Delete old weather data because we don't need to keep multiple days' data */
                        contentResolver.delete(
                                WeatherContract.WeatherEntry.CONTENT_URI,
                                null,
                                null);

                /* Insert our new weather data into Sunshine's ContentProvider */
                        contentResolver.bulkInsert(
                                WeatherContract.WeatherEntry.CONTENT_URI,
                                weatherValues);

                        boolean notificationEnabled = Preferences.areNotificationsEnabled(context);
                        long timeSinceLastNotification = Preferences.getEllapsedTimeSinceLastNotification(context);
                        boolean oneDayPassedSinceLastNotification = false;

                        if(timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS){
                            oneDayPassedSinceLastNotification = true;
                        }

                        if(notificationEnabled && oneDayPassedSinceLastNotification){
                            NotificationUtils.notifyUserOfNewWeather(context);
                        }

                        MainActivity.swipeRefreshLayout.setRefreshing(false);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context,"Error retrieving data(No Network)",Toast.LENGTH_SHORT).show();
                MainActivity.swipeRefreshLayout.setRefreshing(false);
            }
        });

        NetworkUtils.getInstance(context).add(request);
    }
}
