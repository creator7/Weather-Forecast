package com.example.abdullah.weatherapp.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.abdullah.weatherapp.data.Preferences;
import com.example.abdullah.weatherapp.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;


public final class OpenWeatherJsonUtils {

    /* Weather information. Each day's forecast info is an element of the "list" array */
    private static final String OWM_LIST = "list";
    private static final String OWM_MAIN = "main";

    private static final String OWM_PRESSURE = "pressure";
    private static final String OWM_HUMIDITY = "humidity";
    private static final String OWM_WIND = "wind";
    private static final String OWM_WINDSPEED = "speed";
    private static final String OWM_WIND_DIRECTION = "deg";

    /* Max temperature for the day */
    private static final String OWM_MAX = "temp_max";
    private static final String OWM_MIN = "temp_min";

    private static final String OWM_WEATHER = "weather";
    private static final String OWM_WEATHER_ID = "id";

    private static final String OWM_MESSAGE_CODE = "cod";

    public static ContentValues[] getWeatherContentValuesFromJson(Context context, String forecastJsonStr)
            throws JSONException {

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        /* Is there an error? */
        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray jsonWeatherArray = forecastJson.getJSONArray(OWM_LIST);

        ContentValues[] weatherContentValues = new ContentValues[jsonWeatherArray.length()];

        long normalizedUtcStartDay = MyDateUtils.getNormalizedUtcDateForToday();

        for (int i = 0; i < jsonWeatherArray.length(); i++) {

            long dateTimeMillis;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            int weatherId;

            /* Get the JSON object representing the day */
            JSONObject dayForecast = jsonWeatherArray.getJSONObject(i);
            JSONObject mainData = dayForecast.getJSONObject(OWM_MAIN);
            JSONObject windObject = dayForecast.getJSONObject(OWM_WIND);

            /*
             * We ignore all the datetime values embedded in the JSON and assume that
             * the values are returned in-order by day (which is not guaranteed to be correct).
             */
            dateTimeMillis = normalizedUtcStartDay + MyDateUtils.DAY_IN_MILLIS * i;

            pressure = mainData.getDouble(OWM_PRESSURE);
            humidity = mainData.getInt(OWM_HUMIDITY);
            windSpeed = windObject.getDouble(OWM_WINDSPEED);
            windDirection = windObject.getDouble(OWM_WIND_DIRECTION);

            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            high = mainData.getDouble(OWM_MAX);
            low = mainData.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTimeMillis);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            weatherContentValues[i] = weatherValues;
        }

        return weatherContentValues;
    }
}
