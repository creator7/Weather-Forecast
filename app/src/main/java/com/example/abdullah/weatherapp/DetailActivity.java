package com.example.abdullah.weatherapp;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.abdullah.weatherapp.data.Preferences;
import com.example.abdullah.weatherapp.data.WeatherContract;
import com.example.abdullah.weatherapp.databinding.ActivityDetailBinding;
import com.example.abdullah.weatherapp.utilities.MyDateUtils;
import com.example.abdullah.weatherapp.utilities.WeatherUtils;

import static android.R.attr.description;

/**
 * Created by Abdullah on 12/10/2016.
 */

public class DetailActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>{

    private static final String SHARE_HASHTAG = " #WeatherForecast By Abdullah Tariq";

    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_HUMIDITY = 3;
    public static final int INDEX_WEATHER_PRESSURE = 4;
    public static final int INDEX_WEATHER_WIND_SPEED = 5;
    public static final int INDEX_WEATHER_DEGREES = 6;
    public static final int INDEX_WEATHER_CONDITION_ID = 7;

    private static final int ID_DETAIL_LOADER = 353;

    private String mForecastSummary;

    /* The URI that is used to access the chosen day's weather details */
    private Uri mUri;

    private ActivityDetailBinding mDetailBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDetailBinding = DataBindingUtil.setContentView(this,R.layout.activity_detail);


        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        /* This connects our Activity into the loader lifecycle. */
        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null,this);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private Intent createShareIntent(){
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary + SHARE_HASHTAG).getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.detail,menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareIntent());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int selectedItem = item.getItemId();
        if(selectedItem == android.R.id.home){
            onBackPressed();
            return true;
        }
        if (selectedItem == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case ID_DETAIL_LOADER:
                return new CursorLoader(this,
                        mUri,
                        WEATHER_DETAIL_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        String tempUnit;
        if(Preferences.isMetric(this))
            tempUnit = this.getResources().getString(R.string.temperature_celsius);
        else
            tempUnit = this.getResources().getString(R.string.temperature_farenheit);
        mDetailBinding.primaryInfo.temperatureUnit.setText(tempUnit);

        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = MyDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);

        mDetailBinding.primaryInfo.date.setText(dateText);

        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        int weatherImageId = WeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);

        mDetailBinding.primaryInfo.weatherIcon.setImageResource(weatherImageId);
        /* Use the weatherId to obtain the proper description */
        String description = WeatherUtils.getStringForWeatherCondition(this, weatherId);
        String descriptionA11y = getString(R.string.a11y_forecast, description);

        /* Set the text */
        mDetailBinding.primaryInfo.weatherDescription.setText(description);
        mDetailBinding.primaryInfo.weatherDescription.setContentDescription(descriptionA11y);

        mDetailBinding.primaryInfo.weatherIcon.setContentDescription(descriptionA11y);

        double highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String highString = WeatherUtils.formatTemperature(this, highInCelsius);
        String highA11y = getString(R.string.a11y_high_temp, highString);

        /* Set the text */
        mDetailBinding.primaryInfo.highTemperature.setText(highString);
        mDetailBinding.primaryInfo.highTemperature.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String lowString = WeatherUtils.formatTemperature(this, lowInCelsius);
        String lowA11y = getString(R.string.a11y_low_temp, lowString);


        /* Set the text */
        mDetailBinding.primaryInfo.lowTemperature.setText(lowString);
        mDetailBinding.primaryInfo.lowTemperature.setContentDescription(lowA11y);

        /************
         * Humidity *
         ************/
        /* Read humidity from the cursor */
        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);
        String humidityA11y = getString(R.string.a11y_humidity, humidityString);

        mDetailBinding.extraDetails.humidity.setText(humidityString);
        mDetailBinding.extraDetails.humidity.setContentDescription(humidityA11y);

//      COMPLETED (19) Set the content description of the humidity label to the humidity a11y String
        mDetailBinding.extraDetails.humidityLabel.setContentDescription(humidityA11y);

                /* Read wind speed (in MPH) and direction (in compass degrees) from the cursor  */
        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = WeatherUtils.getFormattedWind(this, windSpeed, windDirection);

        String windA11y = getString(R.string.a11y_wind, windString);

//      COMPLETED (13) Use mDetailBinding to display the wind and set the content description
        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.wind.setText(windString);
        mDetailBinding.extraDetails.wind.setContentDescription(windA11y);

//      COMPLETED (22) Set the content description of the wind label to the wind a11y String
        mDetailBinding.extraDetails.windLabel.setContentDescription(windA11y);

        /************
         * Pressure *
         ************/
        /* Read pressure from the cursor */
        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);

        /*
         * Format the pressure text using string resources. The reason we directly access
         * resources using getString rather than using a method from SunshineWeatherUtils as
         * we have for other data displayed in this Activity is because there is no
         * additional logic that needs to be considered in order to properly display the
         * pressure.
         */
        String pressureString = getString(R.string.format_pressure, pressure);

        String pressureA11y = getString(R.string.a11y_pressure, pressureString);

        mDetailBinding.extraDetails.pressure.setText(pressureString);
        mDetailBinding.extraDetails.pressure.setContentDescription(pressureA11y);

        mDetailBinding.extraDetails.pressureLabel.setContentDescription(pressureA11y);



        /* Store the forecast summary String in our forecast summary field to share later */
        mForecastSummary = String.format("%s - %s - %s/%s",
                dateText, description, highString, lowString);

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

}
