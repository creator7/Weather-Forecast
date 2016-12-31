package com.example.abdullah.weatherapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.abdullah.weatherapp.data.WeatherContract;
import com.example.abdullah.weatherapp.sync.SyncUtil;
import com.example.abdullah.weatherapp.data.Preferences;
import com.example.abdullah.weatherapp.utilities.GPSTracker;
import com.example.abdullah.weatherapp.utilities.NotificationUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.zip.Inflater;


public class MainActivity extends AppCompatActivity implements ViewAdapter.AdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener{

    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;

    private static final int LOADER_ID = 44;

    private ViewAdapter adapter;
    private RecyclerView recyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private ProgressBar loadingIndicator;
    public static SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0f);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        GPSTracker gpsTracker = new GPSTracker(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new ViewAdapter(this, this);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this);

        showLoading();

        Log.v("Main onCreate", "called");
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        if(!Preferences.isLocationLatLonAvailable(this))
            Preferences.setDefaultLocation(this,gpsTracker);

        SyncUtil.initialized(this);

    }

    private void openLocationInMap() {

        double[] coords = Preferences.getLocationCoordinates(this);
        String posLat = Double.toString(coords[0]);
        String posLong = Double.toString(coords[1]);
        Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d("Map Loading Error", "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int selectedItem = item.getItemId();

        if(selectedItem == R.id.action_map){
            openLocationInMap();
            return true;
        }
        if (selectedItem == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        if(selectedItem == R.id.action_aboutus){
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("About us");

            alertDialog.setMessage("Abdullah Tariq\n" +
                    "\u00a9Copyrights Reserved \n" +
                    "abdullabintariq@gmail.com");

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(long date) {
        Intent weatherDetailIntent = new Intent(MainActivity.this, DetailActivity.class);
        Uri uriForDateClicked = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date);
        weatherDetailIntent.setData(uriForDateClicked);
        startActivity(weatherDetailIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case LOADER_ID:
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

                String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                return new CursorLoader(this,forecastQueryUri,MAIN_FORECAST_PROJECTION,selection,null,sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if(mPosition == RecyclerView.NO_POSITION)
            mPosition = 0;
        recyclerView.smoothScrollToPosition(mPosition);
        if(data.getCount() != 0 )
            showWeatherDataView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void showLoading() {
        recyclerView.setVisibility(View.INVISIBLE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showWeatherDataView() {
        loadingIndicator.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        SyncUtil.startImmediateSync(getApplicationContext());
    }
}
